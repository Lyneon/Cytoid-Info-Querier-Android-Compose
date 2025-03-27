package com.lyneon.cytoidinfoquerier.ui.activity

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.IFileService
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.CytoidConstant
import com.lyneon.cytoidinfoquerier.service.FileService
import com.lyneon.cytoidinfoquerier.ui.compose.component.ErrorMessageCard
import com.lyneon.cytoidinfoquerier.ui.theme.CytoidInfoQuerierComposeTheme
import com.lyneon.cytoidinfoquerier.util.extension.contentUriToPath
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import rikka.shizuku.Shizuku

class ImportLevelActivity : ComponentActivity() {
    var fileService: IFileService? = null
    private var fileServiceAvailable by mutableStateOf(false)
    private var shizukuAvailable = false
    private val serviceArgs = Shizuku.UserServiceArgs(
        ComponentName(
            BaseApplication.context.packageName,
            FileService::class.java.name
        )
    ).processNameSuffix("CIQ_FileService")
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
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
        }
    }
    private val shizukuBinderReceivedListener =
        Shizuku.OnBinderReceivedListener { shizukuAvailable = true }
    private val shizukuBinderDeadListener =
        Shizuku.OnBinderDeadListener { shizukuAvailable = false }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        Shizuku.addBinderReceivedListenerSticky(shizukuBinderReceivedListener)
        Shizuku.addBinderDeadListener(shizukuBinderDeadListener)
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
                            var sourceFilePath by remember {
                                mutableStateOf(
                                    data.contentUriToPath() ?: ""
                                )
                            }
                            val targetDirectoryPath =
                                "/storage/emulated/0/Android/data/me.tigerhix.cytoid/files/Cytoid/"

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
                                    OutlinedTextField(
                                        value = sourceFilePath,
                                        onValueChange = { sourceFilePath = it },
                                        label = { Text(text = "绝对路径") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(text = "Shizuku文件服务：${if (!fileServiceAvailable) "不" else ""}可用")
                                    androidx.compose.animation.AnimatedVisibility(!fileServiceAvailable) {
                                        Button(
                                            onClick = {
                                                bindFileService()
                                                Log.d(
                                                    "ImportLevelActivity",
                                                    (fileService == null).toString()
                                                )
                                                Log.d(
                                                    "ILA",
                                                    fileServiceAvailable.toString()
                                                )
                                            },
                                            enabled = shizukuIntent != null
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "重试连接服务",
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = "重试连接服务")
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
                                            contentDescription = "Shizuku设置",
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "Shizuku设置")
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val result = fileService?.copyFileTo(
                                                    sourceFilePath,
                                                    targetDirectoryPath
                                                ) ?: false
                                                if (result) {
                                                    "已完成导入操作".showToast()
                                                } else {
                                                    "导入失败".showToast()
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            enabled = fileServiceAvailable && sourceFilePath.isNotEmpty()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoveToInbox,
                                                contentDescription = "确认导入"
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = "确认导入")
                                        }
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
                                                contentDescription = "启动Cytoid"
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = "启动Cytoid")
                                        }
                                    }
                                }
                            }
                        } ?: run {
                            ErrorMessageCard("未接收到关卡数据！")
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
    }

    private fun bindFileService() {
        if (shizukuAvailable) {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(1)
            } else {
                Shizuku.bindUserService(serviceArgs, serviceConnection)
                Log.d("ImportLevelActivity", "bindUserService")
            }
        }
    }

    private fun unbindFileService() {
        Shizuku.unbindUserService(serviceArgs, serviceConnection, true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    getString(R.string.shizuku_needed).showToast()
                }
                bindFileService()
            }
        }
    }
}