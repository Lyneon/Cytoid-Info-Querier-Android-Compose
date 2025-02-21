package com.lyneon.cytoidinfoquerier.ui.activity

import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.CytoidConstant
import com.lyneon.cytoidinfoquerier.ui.compose.component.ErrorMessageCard
import com.lyneon.cytoidinfoquerier.ui.theme.CytoidInfoQuerierComposeTheme
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

class ImportLevelActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

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
                        data?.let {
                            var sourcePath by remember {
                                mutableStateOf(data.toString().run {
                                    this.substring(this.lastIndexOf("/storage/emulated/0/"))
                                })
                            }
                            val targetPath =
                                "/storage/emulated/0/Android/data/me.tigerhix.cytoid/files/Cytoid/"
                            var commandOutput by remember { mutableStateOf("") }
                            val scope = rememberCoroutineScope()

                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    val shizukuIntent =
                                        packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")

                                    Text(
                                        text = sourcePath.substringAfterLast("/"),
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Text(text = "Uri: $data")
                                    TextField(
                                        value = sourcePath,
                                        onValueChange = { sourcePath = it },
                                        label = { Text(text = "绝对路径") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
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
                                                val shizuku = Shizuku.getBinder()
                                                if (shizuku == null || !Shizuku.pingBinder()) {
                                                    "未连接到Shizuku！".showToast()
                                                    return@Button
                                                }
                                                if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                                                    Shizuku.requestPermission(1)
                                                    return@Button
                                                }
                                                if (Shizuku.getVersion() < 14) {
                                                    val process = Shizuku.newProcess(
                                                        arrayOf("sh"),
                                                        null,
                                                        null
                                                    )
                                                    process.outputStream.use {
                                                        it.write(("cp -v $sourcePath $targetPath\nexit\n").toByteArray())
                                                        it.flush()
                                                    }
                                                    scope.launch(Dispatchers.IO) {
                                                        val exitCode = process.waitFor()
                                                        process.inputStream.use {
                                                            it.reader().use { reader ->
                                                                commandOutput =
                                                                    "${reader.readText()}\n进程已结束，退出码为$exitCode"
                                                            }
                                                        }
                                                    }
                                                    "已执行导入操作！请自行进入游戏查看导入结果。".showToast()
                                                } else {
                                                    // Shizuku 14 is still not released
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
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
                                    OutlinedTextField(
                                        value = commandOutput,
                                        onValueChange = {},
                                        label = { Text(text = "命令输出") },
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
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
}