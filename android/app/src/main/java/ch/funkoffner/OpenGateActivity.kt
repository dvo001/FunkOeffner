package ch.funkoffner

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/** Launcher entry only; opening the app never triggers a radio command. */
class OpenGateActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
        finish()
    }
}
