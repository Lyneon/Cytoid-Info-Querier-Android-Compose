package com.lyneon.cytoidinfoquerier.ui.activity

import android.os.Bundle
import android.os.Process
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.lyneon.cytoidinfoquerier.BaseActivity
import com.lyneon.cytoidinfoquerier.ui.compose.screen.CrashActivityScreen
import com.lyneon.cytoidinfoquerier.ui.theme.CytoidInfoQuerierComposeTheme


class CrashActivity : BaseActivity() {
    companion object {
        const val KEY_EXTRA_CRASH_MESSAGE = "crashMessage"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val crashMessage = intent.getStringExtra(KEY_EXTRA_CRASH_MESSAGE) ?: "No Message"
        setContent {
            CytoidInfoQuerierComposeTheme {
                (LocalContext.current as CrashActivity).window.apply {
                    navigationBarColor = Color.Transparent.toArgb()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CrashActivityScreen(crashMessage)
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
            Process.killProcess(Process.myPid())
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}