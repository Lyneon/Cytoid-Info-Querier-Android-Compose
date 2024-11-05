package com.lyneon.cytoidinfoquerier.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.lyneon.cytoidinfoquerier.BaseActivity
import com.lyneon.cytoidinfoquerier.ui.compose.screen.DownloadLevelActivityScreen
import com.lyneon.cytoidinfoquerier.ui.theme.CytoidInfoQuerierComposeTheme
import com.lyneon.cytoidinfoquerier.ui.viewmodel.DownloadLevelViewModel
import com.lyneon.cytoidinfoquerier.util.AppSettingsMMKVKeys
import com.lyneon.cytoidinfoquerier.util.MMKVId
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.update

class DownloadLevelActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val mmkv = MMKV.mmkvWithID(MMKVId.AppSettings.id)
        val viewModel: DownloadLevelViewModel by viewModels()
        val setSaveUriLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.let { uri ->
                        viewModel.saveUriString.update { uri.toString() }
                        mmkv.encode(
                            AppSettingsMMKVKeys.DOWNLOAD_LEVEL_SAVE_URI_STRING.name,
                            uri.toString()
                        )
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    }
                }
            }

        setContent {
            CytoidInfoQuerierComposeTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DownloadLevelActivityScreen(intent.data.toString(), setSaveUriLauncher)
                }
            }
        }
    }
}