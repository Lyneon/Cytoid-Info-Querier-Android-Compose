package com.lyneon.cytoidinfoquerier.ui.compose.screen

import android.os.Environment
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.CytoidConstant
import com.lyneon.cytoidinfoquerier.data.constant.OkHttpSingleton
import com.lyneon.cytoidinfoquerier.util.DateParser.formatToTimeString
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import okhttp3.Call
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URLDecoder
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadLevelActivityScreen(downloadUrl: String?) {
    val downloadLevelTitle = URLDecoder.decode(downloadUrl?.substringAfter("title="), "UTF-8")
    val downloadLevelId = downloadUrl?.substringAfter("levelId=")?.substringBefore("&")
    val downloadExpire = downloadUrl?.substringAfter("expire%3D")?.substringBefore("%26")
    val directDownloadUrl =
        URLDecoder.decode(downloadUrl?.substringAfter("direct=")?.substringBefore("&"), "UTF-8")

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
                        Button(
                            enabled = System.currentTimeMillis() < (downloadExpire?.toLong() ?: 0L),
                            onClick = {
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
                                                val file = File(
                                                    Environment.getExternalStoragePublicDirectory(
                                                        Environment.DIRECTORY_DOWNLOADS
                                                    ), "${downloadLevelId}.cytoidlevel"
                                                )

                                                FileOutputStream(file).use { output ->
                                                    inputStream?.copyTo(output)
                                                }

                                                "下载成功：${file.absolutePath}".showToast()
                                            } else {
                                                "下载失败：${response.code}".showToast()
                                            }
                                        }
                                    })
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