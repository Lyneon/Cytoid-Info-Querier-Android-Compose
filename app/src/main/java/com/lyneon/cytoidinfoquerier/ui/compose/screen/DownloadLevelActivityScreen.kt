package com.lyneon.cytoidinfoquerier.ui.compose.screen

import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.CytoidConstant
import com.lyneon.cytoidinfoquerier.data.constant.OkHttpSingleton
import com.lyneon.cytoidinfoquerier.ui.activity.DownloadLevelActivity
import com.lyneon.cytoidinfoquerier.ui.viewmodel.DownloadLevelViewModel
import com.lyneon.cytoidinfoquerier.util.DateParser.formatToTimeString
import com.lyneon.cytoidinfoquerier.util.MMKVId
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import com.tencent.mmkv.MMKV
import okhttp3.Call
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.io.InputStream
import java.net.URLDecoder
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadLevelActivityScreen(
    downloadUrl: String?,
    setSaveUriLauncher: ActivityResultLauncher<Intent>,
    viewModel: DownloadLevelViewModel = viewModel()
) {
    val downloadLevelTitle = URLDecoder.decode(downloadUrl?.substringAfter("title="), "UTF-8")
    val downloadLevelId = downloadUrl?.substringAfter("levelId=")?.substringBefore("&")
    val downloadExpire = downloadUrl?.substringAfter("expire%3D")?.substringBefore("%26")
    val directDownloadUrl =
        URLDecoder.decode(downloadUrl?.substringAfter("direct=")?.substringBefore("&"), "UTF-8")
    val mmkv = MMKV.mmkvWithID(MMKVId.AppSettings.id)
    val saveUri = viewModel.saveUriString.collectAsState()
    var saveFileName by remember { mutableStateOf("${downloadLevelId}.cytoidlevel") }
    val context = LocalContext.current as DownloadLevelActivity

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = stringResource(R.string.download_level))
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (directDownloadUrl == null) {
                        Text(text = "下载链接为空")
                    } else {
                        downloadLevelTitle?.let {
                            Text(text = it, style = MaterialTheme.typography.titleLarge)
                        }
                        downloadLevelId?.let { Text(text = it) }
                        HorizontalDivider()
                        Text(text = "原始下载链接：$downloadUrl")
                        Text(text = "直接下载链接：$directDownloadUrl")
                        downloadExpire?.let {
                            Text(
                                text = if (System.currentTimeMillis() < downloadExpire.toLong()) "此下载链接将于 ${
                                    Date(
                                        downloadExpire.toLong()
                                    ).formatToTimeString()
                                } 过期" else "此下载链接已于 ${
                                    Date(
                                        downloadExpire.toLong()
                                    ).formatToTimeString()
                                } 过期"
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = (Uri.parse(saveUri.value).path ?: ""),
                                enabled = false,
                                onValueChange = { },
                                label = { Text(text = "下载至") },
                                trailingIcon = {
                                }
                            )
                            Text("/")
                            OutlinedTextField(
                                value = saveFileName,
                                onValueChange = { saveFileName = it },
                                label = { Text(text = "文件名") }
                            )
                        }
                        Button(
                            onClick = {
                                setSaveUriLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                                    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                                })
                            }
                        ) {
                            Text(text = "更改路径")
                        }
                        Button(
                            enabled = (System.currentTimeMillis() < (downloadExpire?.toLong()
                                ?: 0L)) && (saveUri.value != ""),
                            onClick = {
                                DocumentFile.fromTreeUri(
                                    context,
                                    Uri.parse(saveUri.value)
                                )?.let { downloadDirDocumentFile ->
                                    if (downloadDirDocumentFile.canWrite()) {
                                        val documentFile = downloadDirDocumentFile.createFile(
                                            "application/octet-stream",
                                            saveFileName
                                        )
                                        "开始下载".showToast()
                                        val request = Request.Builder()
                                            .url(directDownloadUrl)
                                            .addHeader("User-Agent", CytoidConstant.clientUA)
                                            .build()
                                        OkHttpSingleton.instance.newCall(request)
                                            .enqueue(object : okhttp3.Callback {
                                                override fun onFailure(call: Call, e: IOException) {
                                                    "下载失败: ${e.message}".showToast()
                                                }

                                                override fun onResponse(
                                                    call: Call,
                                                    response: Response
                                                ) {
                                                    if (response.isSuccessful) {
                                                        val inputStream: InputStream? =
                                                            response.body?.byteStream()

                                                        documentFile?.let {
                                                            context.contentResolver.openOutputStream(
                                                                it.uri
                                                            )
                                                                ?.use { outputStream ->
                                                                    inputStream?.copyTo(outputStream)
                                                                }
                                                        }

                                                        "下载成功：${documentFile?.uri?.path}".showToast()
                                                    } else {
                                                        "下载失败：${response.code}".showToast()
                                                    }
                                                }
                                            })
                                    } else {
                                        "目标路径不可写，请重新选择路径".showToast()
                                    }
                                }
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "下载")
                            }
                        }
                    }
                }
            }
        }
    }
}