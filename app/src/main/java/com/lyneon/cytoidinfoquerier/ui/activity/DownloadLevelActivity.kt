package com.lyneon.cytoidinfoquerier.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.lyneon.cytoidinfoquerier.BaseActivity
import com.lyneon.cytoidinfoquerier.ui.compose.screen.DownloadLevelActivityScreen
import com.lyneon.cytoidinfoquerier.ui.theme.CytoidInfoQuerierComposeTheme

class DownloadLevelActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CytoidInfoQuerierComposeTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DownloadLevelActivityScreen(intent.data.toString())
                }
            }
        }
    }
}