package ch.funkoffner

import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.hardware.usb.*
import android.os.Build
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.widget.*
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.Executors

/** USB prototype only. No radio commands or persisted command queue. */
class MainActivity : Activity() {
    private val executor = Executors.newSingleThreadExecutor()
    private val usb by lazy { getSystemService(USB_SERVICE) as UsbManager }
    private lateinit var pages: ViewFlipper
    private lateinit var connectionLink: Button
    private lateinit var status: TextView
    private lateinit var diagnostics: TextView
    private lateinit var deviceDetails: TextView
    private lateinit var radioStatus: TextView
    private var resumed = false
    private var checks = 0
    @Volatile private var running = false
    @Volatile private var generation = 0
    @Volatile private var connection: UsbDeviceConnection? = null
    private val permissionAction get() = "$packageName.USB_PERMISSION"
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val detached = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    if (detached?.vendorId == 0x303a && detached.productId == 0x4001) disconnect()
                }
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
        window.statusBarColor = Color.rgb(17, 24, 32)
        window.navigationBarColor = Color.rgb(17, 24, 32)
        pages = ViewFlipper(this)
        val home = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
            setBackgroundColor(Color.rgb(17, 24, 32))
        }
        home.addView(TextView(this).apply {
            text = "FunkOffner"; textSize = 32f; setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD); gravity = android.view.Gravity.CENTER
        }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(32) })
        home.addView(Button(this).apply {
            text = "Tor öffnen"; textSize = 30f; isAllCaps = false
            minHeight = dp(144); isEnabled = false
            contentDescription = "Tor öffnen – noch kein eingerichtetes Funkprofil"
            setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_gate, 0, 0)
            compoundDrawablePadding = dp(12)
        }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(24) })
        connectionLink = Button(this).apply {
            text = "Nicht verbunden · Einstellungen ›"; textSize = 20f
            isAllCaps = false; minHeight = dp(64)
            setOnClickListener { pages.displayedChild = 1 }
        }
        home.addView(connectionLink, LinearLayout.LayoutParams(-1, -2))
        pages.addView(ScrollView(this).apply { isFillViewport = true; addView(home) })
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(24))
            setBackgroundColor(Color.rgb(17, 24, 32))
        }
        fun label(value: String, size: Float = 20f) = TextView(this).apply {
            text = value; textSize = size; setTextColor(Color.rgb(235, 240, 244))
            setPadding(0, dp(8), 0, dp(8))
        }
        fun card(title: String): LinearLayout {
            val panel = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(20), dp(12), dp(20), dp(16))
                background = GradientDrawable().apply {
                    setColor(Color.rgb(29, 40, 51)); cornerRadius = dp(16).toFloat()
                }
            }
            layout.addView(panel, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(16) })
            panel.addView(label(title, 18f).apply { setTextColor(Color.rgb(128, 203, 196)) })
            return panel
        }
        layout.addView(Button(this).apply {
            text = "‹ Zurück"; textSize = 20f; isAllCaps = false; minHeight = dp(56)
            setOnClickListener { pages.displayedChild = 0 }
        })
        layout.addView(label("Einstellungen", 32f).apply { setTypeface(null, Typeface.BOLD) })
        val connectionPanel = card("USB-Verbindung")
        status = label("Getrennt", 26f)
        connectionPanel.addView(status)
        deviceDetails = label("Noch keine Geräteantwort", 18f)
        connectionPanel.addView(deviceDetails)
        val radioPanel = card("Funkmodul")
        radioStatus = label("Nicht eingerichtet", 24f)
        radioPanel.addView(radioStatus)
        radioPanel.addView(label("Anlernen und Senden sind noch nicht verfügbar.", 18f))
        layout.addView(Button(this).apply {
            text = "USB erneut prüfen"; textSize = 22f; isAllCaps = false
            minHeight = dp(64)
            setOnClickListener { discover() }
        }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(16) })
        val diagnosticPanel = card("Diagnose")
        diagnostics = label("USB-Prüfung wird gestartet", 18f)
        diagnosticPanel.addView(diagnostics)
        layout.addView(label("Verwaltung im Stand bedienen.", 18f))
        pages.addView(ScrollView(this).apply { isFillViewport = true; addView(layout) })
        pages.displayedChild = if (savedInstanceState?.getBoolean("settings_page") == true) 1 else 0
        setContentView(pages)
        val filter = IntentFilter(permissionAction).apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED); addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
        else registerReceiver(receiver, filter)
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("settings_page", pages.displayedChild == 1)
        super.onSaveInstanceState(outState)
    }
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (pages.displayedChild == 1) pages.displayedChild = 0
        else super.onBackPressed()
    }
    override fun onResume() { super.onResume(); resumed = true; discover() }
    override fun onPause() { resumed = false; disconnect(); super.onPause() }
    override fun onDestroy() { unregisterReceiver(receiver); executor.shutdownNow(); super.onDestroy() }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun show(text: String, token: Int = generation) = runOnUiThread {
        if (generation == token) updateStatus(text)
    }
    private fun updateStatus(text: String) {
        status.text = text
        val summary = when {
            text == "Verbunden" -> "Verbunden"
            text.startsWith("Getrennt") -> "Nicht verbunden"
            text.startsWith("Hardware-") || text.startsWith("Verbindungsfehler") -> "Verbindungsfehler"
            text.contains("verweigert") -> "Freigabe fehlt"
            else -> "Verbindung prüfen"
        }
        connectionLink.text = "$summary · Einstellungen ›"
    }
    private fun disconnect() {
        generation++; running = false; connection?.close(); connection = null
        deviceDetails.text = "Noch keine aktuelle Geräteantwort"
        radioStatus.text = "Nicht eingerichtet"
        show("Getrennt")
    }
    // Espressif development VID/default CDC PID. Production needs an assigned VID/PID.
    private fun discover() {
        if (!resumed) return
        checks++
        val devices = usb.deviceList.values.toList()
        diagnostics.text = "USB-Prüfung $checks\nAndroid ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}\n" +
            "USB-Host gemeldet: ${packageManager.hasSystemFeature("android.hardware.usb.host")}\n" +
            if (devices.isEmpty()) "Android meldet keine USB-Geräte. Anderen USB-Anschluss am Radio prüfen."
            else devices.joinToString("\n") {
                "USB %04X:%04X – Freigabe: %s".format(it.vendorId, it.productId, if (usb.hasPermission(it)) "ja" else "nein")
            }
        if (running) return
        val candidates = devices.filter { it.vendorId == 0x303a && it.productId == 0x4001 }
        if (candidates.size != 1) { show(if (candidates.isEmpty()) "Getrennt – kein passendes USB-Gerät" else "Mehrere Geräte – bitte nur eines anschliessen"); return }
        val device = candidates.single()
        if (usb.hasPermission(device)) connect(device)
        else {
            show("USB-Gerät erkannt – Freigabe am Radio bestätigen")
            usb.requestPermission(device, PendingIntent.getBroadcast(this, 0,
                Intent(permissionAction).setPackage(packageName), PendingIntent.FLAG_IMMUTABLE))
        }
    }
    private fun connect(device: UsbDevice) {
        if (!resumed || running) return
        running = true
        show("USB-Gerät erkannt – Verbindung wird aufgebaut")
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
                    check(!state.getBoolean("transmit_enabled")) { "Unerwartete Sendefreigabe" }
                    check(state.getString("state") == "IDLE" && state.getString("radio") == "NOT_CONFIGURED") {
                        "Nicht unterstützter Firmwarezustand"
                    }
                    val receivedAt = java.text.DateFormat.getTimeInstance(java.text.DateFormat.MEDIUM).format(java.util.Date())
                    runOnUiThread {
                        if (generation == token && resumed) {
                            updateStatus("Verbunden")
                            deviceDetails.text = "Gerät: ${hello.getString("device_id")}\nFirmware: ${hello.getString("firmware")} · Protokoll 1\nLetzte Statusantwort: $receivedAt"
                            radioStatus.text = "Nicht eingerichtet · IDLE"
                        }
                    }
                    Thread.sleep(2000)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    if (generation == token && resumed) {
                        updateStatus("Verbindungsfehler\n${e.message ?: e.javaClass.simpleName}")
                        deviceDetails.text = "Keine aktuelle Statusbestätigung. USB erneut prüfen."
                    }
                }
            } finally {
                local?.close()
                if (generation == token) { connection = null; running = false }
            }
        }
    }
}
