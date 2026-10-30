package ch.funkoffner

import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.hardware.usb.*
import android.os.Build
import android.os.Bundle
import android.widget.*
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.Executors

/** USB prototype only. No radio commands or persisted command queue. */
class MainActivity : Activity() {
    private val executor = Executors.newSingleThreadExecutor()
    private val usb by lazy { getSystemService(USB_SERVICE) as UsbManager }
    private lateinit var status: TextView
    @Volatile private var running = false
    @Volatile private var generation = 0
    @Volatile private var connection: UsbDeviceConnection? = null
    private val permissionAction get() = "$packageName.USB_PERMISSION"
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_DETACHED -> disconnect()
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> discover()
                permissionAction -> {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    if (device != null && usb.hasPermission(device)) connect(device)
                    else show("USB-Freigabe verweigert")
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24,24,24,24) }
        status = TextView(this).apply { textSize = 24f; text = "Getrennt" }
        layout.addView(status)
        layout.addView(Button(this).apply { text = "USB verbinden / Status prüfen"; textSize = 22f; setOnClickListener { discover() } })
        layout.addView(TextView(this).apply { text = "USB-Prototyp – Funk noch nicht eingerichtet. Verwaltung im Stand bedienen."; textSize = 18f })
        setContentView(layout)
        val filter = IntentFilter(permissionAction).apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED); addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
        else registerReceiver(receiver, filter)
    }
    override fun onResume() { super.onResume(); discover() }
    override fun onPause() { disconnect(); super.onPause() }
    override fun onDestroy() { unregisterReceiver(receiver); executor.shutdownNow(); super.onDestroy() }
    private fun show(text: String) = runOnUiThread { status.text = text }
    private fun disconnect() {
        generation++; running = false; connection?.close(); connection = null; show("Getrennt")
    }
    // Espressif development VID/default CDC PID. Production needs an assigned VID/PID.
    private fun discover() {
        if (running) return
        val candidates = usb.deviceList.values.filter { it.vendorId == 0x303a && it.productId == 0x4001 }
        if (candidates.size != 1) { show(if (candidates.isEmpty()) "Getrennt – kein passendes USB-Gerät" else "Mehrere Geräte – bitte nur eines anschliessen"); return }
        val device = candidates.single()
        if (usb.hasPermission(device)) connect(device)
        else usb.requestPermission(device, PendingIntent.getBroadcast(this, 0,
            Intent(permissionAction).setPackage(packageName), PendingIntent.FLAG_IMMUTABLE))
    }
    private fun connect(device: UsbDevice) {
        if (running) return
        running = true
        val token = ++generation
        executor.execute {
            var local: UsbDeviceConnection? = null
            try {
                val interfaces = (0 until device.interfaceCount).map { device.getInterface(it) }
                val control = interfaces.single { it.interfaceClass == UsbConstants.USB_CLASS_COMM }
                val data = interfaces.single { it.interfaceClass == UsbConstants.USB_CLASS_CDC_DATA }
                val endpoints = (0 until data.endpointCount).map { data.getEndpoint(it) }.filter { it.type == UsbConstants.USB_ENDPOINT_XFER_BULK }
                val input = endpoints.single { it.direction == UsbConstants.USB_DIR_IN }
                val output = endpoints.single { it.direction == UsbConstants.USB_DIR_OUT }
                val conn = usb.openDevice(device) ?: error("Gerät nicht verfügbar")
                local = conn
                check(conn.claimInterface(control, true) && conn.claimInterface(data, true))
                check(conn.controlTransfer(0x21, 0x20, 0, control.id, byteArrayOf(0x00,0xc2.toByte(),0x01,0,0,0,8),7,1000) == 7)
                check(conn.controlTransfer(0x21, 0x22, 3, control.id, null,0,1000) >= 0)
                if (generation != token) return@execute
                connection = conn
                var session: String? = null
                val pending = StringBuilder()
                fun request(cmd: String): JSONObject {
                    val id = UUID.randomUUID().toString()
                    val bytes = (JSONObject().put("v",1).put("id",id).put("cmd",cmd).toString()+"\n").toByteArray(Charsets.UTF_8)
                    check(conn.bulkTransfer(output, bytes, bytes.size,1000) == bytes.size)
                    val deadline = android.os.SystemClock.elapsedRealtime()+3000
                    val buffer = ByteArray(256)
                    while (generation == token && android.os.SystemClock.elapsedRealtime() < deadline) {
                        val n = conn.bulkTransfer(input,buffer,buffer.size,250)
                        if (n <= 0) continue
                        for (i in 0 until n) {
                            val c = buffer[i].toInt() and 255
                            if (c == 10) {
                                val line = pending.toString(); pending.setLength(0)
                                if (line.isBlank()) continue
                                val response = JSONObject(line)
                                check(response.getInt("v") == 1 && response.getString("id") == id)
                                check(!response.has("error"))
                                val nextSession = response.getString("session")
                                check(nextSession.isNotEmpty() && (session == null || session == nextSession))
                                session = nextSession
                                return response.getJSONObject("result")
                            }
                            check(c in 1..127 && pending.length < 1024)
                            pending.append(c.toChar())
                        }
                    }
                    error("USB-Antwort fehlt; erneut verbinden")
                }
                val hello = request("HELLO")
                check(hello.getInt("protocol") == 1 && hello.getString("device_id").startsWith("s2-"))
                while (generation == token) {
                    val state = request("STATUS")
                    check(!state.getBoolean("transmit_enabled"))
                    show("Verbunden: ${hello.getString("device_id")}\n${state.getString("state")} – Funk nicht eingerichtet")
                    Thread.sleep(2000)
                }
            } catch (e: Exception) {
                if (generation == token) show("Hardware-/Verbindungsfehler – erneut verbinden")
            } finally {
                local?.close()
                if (generation == token) { connection = null; running = false }
            }
        }
    }
}
