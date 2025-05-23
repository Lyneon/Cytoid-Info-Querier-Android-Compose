package com.lyneon.cytoidinfoquerier.ui.activity

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.RemoteException
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.lyneon.cytoidinfoquerier.BaseActivity
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.IFileService
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.CytoidConstant
import com.lyneon.cytoidinfoquerier.service.FileService
import com.lyneon.cytoidinfoquerier.ui.compose.component.ErrorMessageCard
import com.lyneon.cytoidinfoquerier.ui.theme.CytoidInfoQuerierComposeTheme
import com.lyneon.cytoidinfoquerier.util.extension.contentUriToPath
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

class ImportLevelActivity : BaseActivity() {
    var fileService: IFileService? = null
    private val requestShizukuPermissionCode = 1
    private var fileServiceAvailable by mutableStateOf(false)
    private var shizukuAvailable = false
    private val serviceArgs = Shizuku.UserServiceArgs(
        ComponentName(
            BaseApplication.context.packageName,
            FileService::class.java.name
        )
    ).processNameSuffix("CIQ_FileService").daemon(false)
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
            Log.d("ImportLevelActivity", name.toString())
            if (service != null && service.pingBinder()) {
                fileService = IFileService.Stub.asInterface(service) as IFileService
                fileServiceAvailable = true
                Log.d("ImportLevelActivity", "FileService connected")
            } else {
                Log.d("ImportLevelActivity", "Invalid binder")
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            fileService = null
            fileServiceAvailable = false
            Log.d("ImportLevelActivity", "FileService disconnected")
            Handler(Looper.getMainLooper()).postDelayed({
                bindFileService()
            }, 1000)
        }
    }
    private val shizukuBinderReceivedListener =
        Shizuku.OnBinderReceivedListener { shizukuAvailable = true }
    private val shizukuBinderDeadListener =
        Shizuku.OnBinderDeadListener { shizukuAvailable = false }
    private val permissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == requestShizukuPermissionCode) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    getString(R.string.shizuku_needed).showToast()
                } else {
                    bindFileService()
                }
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        Shizuku.addBinderReceivedListenerSticky(shizukuBinderReceivedListener)
        Shizuku.addBinderDeadListener(shizukuBinderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)
        bindFileService()

        val intent = intent

        setContent {
            CytoidInfoQuerierComposeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(text = stringResource(R.string.import_to_cytoid))
                            }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(horizontal = 12.dp)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val data = intent.data
                        Log.d("ImportLevelActivity", data.toString())
                        data?.let {
                            Log.d("ImportLevelActivity", data.toString())
                            var sourceFilePath by remember {
                                mutableStateOf(
                                    data.contentUriToPath() ?: ""
                                )
                            }
                            val targetDirectoryPath =
                                "/storage/emulated/0/Android/data/me.tigerhix.cytoid/files/Cytoid/"
                            val scope = rememberCoroutineScope()

                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val shizukuIntent =
                                        packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")

                                    Text(
                                        text = sourceFilePath.substringAfterLast("/"),
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Text(text = stringResource(R.string.original_uri, data))
                                    OutlinedTextField(
                                        value = sourceFilePath,
                                        onValueChange = { sourceFilePath = it },
                                        label = { Text(text = stringResource(R.string.absolute_path)) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = stringResource(
                                            R.string.shizuku_file_service_status,
                                            if (!fileServiceAvailable) stringResource(R.string.not) else ""
                                        )
                                    )
                                    androidx.compose.animation.AnimatedVisibility(!fileServiceAvailable) {
                                        Button(onClick = { bindFileService() }) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = stringResource(R.string.retry_bind_shizuku),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = stringResource(R.string.retry_bind_shizuku))
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            startActivity(shizukuIntent)
                                        },
                                        enabled = shizukuIntent != null
                                    ) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_shizuku),
                                            contentDescription = stringResource(R.string.shizuku_settings),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = stringResource(R.string.shizuku_settings))
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                scope.launch(Dispatchers.IO) {
                                                    try {
                                                        val result = fileService?.copyFileTo(
                                                            sourceFilePath,
                                                            targetDirectoryPath
                                                        ) == true
                                                        if (result) {
                                                            getString(R.string.import_finished).showToast()
                                                        } else {
                                                            getString(R.string.import_failed).showToast()
                                                        }
                                                    } catch (e: RemoteException) {
                                                        Log.e("ImportLevelActivity", e.message, e)
                                                        (getString(R.string.import_failed) + e.message).showToast()
                                                    }
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            enabled = fileServiceAvailable && sourceFilePath.isNotEmpty()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoveToInbox,
                                                contentDescription = stringResource(R.string.start_import)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = stringResource(R.string.start_import))
                                        }
                                        if (BaseApplication.cytoidIsInstalled) {
                                            Button(
                                                onClick = {
                                                    startActivity(
                                                        packageManager.getLaunchIntentForPackage(
                                                            CytoidConstant.gamePackageName
                                                        )
                                                    )
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.Launch,
                                                    contentDescription = stringResource(R.string.launch_cytoid)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = stringResource(R.string.launch_cytoid))
                                            }
                                        }
                                    }
                                }
                            }
                        } ?: run {
                            ErrorMessageCard(stringResource(R.string.no_level_data_received))
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeBinderReceivedListener(shizukuBinderReceivedListener)
        Shizuku.removeBinderDeadListener(shizukuBinderDeadListener)
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
    }

    private fun bindFileService() {
        if (shizukuAvailable) {
            if (Shizuku.isPreV11()) {
                getString(R.string.warn_shizuku_v11).showToast()
                return
            }
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(requestShizukuPermissionCode)
            } else {
                Shizuku.bindUserService(serviceArgs, serviceConnection)
            }
        }
    }

    private fun unbindFileService() {
        Shizuku.unbindUserService(serviceArgs, serviceConnection, true)
    }
}