package com.lyneon.cytoidinfoquerier.ui.activity

import android.os.Bundle
import android.os.Process
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.lyneon.cytoidinfoquerier.BaseActivity
import com.lyneon.cytoidinfoquerier.ui.activity.ui.theme.CytoidInfoQuerierComposeTheme
import com.lyneon.cytoidinfoquerier.ui.compose.CrashActivityCompose


class CrashActivity : BaseActivity() {
    companion object {
        const val KEY_EXTRA_CRASH_MESSAGE = "crashMessage"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val crashMessage = intent.getStringExtra(KEY_EXTRA_CRASH_MESSAGE) ?: "No Message"
        setContent {
            CytoidInfoQuerierComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CrashActivityCompose(crashMessage)
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