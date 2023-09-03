package com.lyneon.cytoidinfoquerier.ui.compose

import android.content.res.Configuration
import android.os.Looper
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.logic.dao.DataParser
import com.lyneon.cytoidinfoquerier.logic.model.B30Records
import com.lyneon.cytoidinfoquerier.logic.network.NetRequest
import com.lyneon.cytoidinfoquerier.tool.showToast
import com.tencent.bugly.crashreport.CrashReport
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.concurrent.thread

lateinit var b30Record: B30Records

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class
)
@Composable
fun MainActivityCompose() {
    val context = LocalContext.current
    var playerName by remember { mutableStateOf("") }
    var isQueryingFinished by remember { mutableStateOf(false) }

    Column {
        CenterAlignedTopAppBar(
            title = { Text(text = stringResource(id = R.string.app_name)) },
            navigationIcon = {
                IconButton(
                    onClick = { /*TODO*/ }) {
                    Icon(imageVector = Icons.Rounded.AccountCircle, contentDescription = "个人资料")
                }
            },
            actions = {
                var menuIsExpanded by remember { mutableStateOf(false) }
                IconButton(onClick = { menuIsExpanded = !menuIsExpanded }) {
                    Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "选项菜单")
                }
                DropdownMenu(
                    expanded = menuIsExpanded,
                    onDismissRequest = { menuIsExpanded = false }) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.baseline_public_24),
                                contentDescription = stringResource(id = R.string.ping)
                            )
                        },
                        text = { Text(text = stringResource(id = R.string.ping)) },
                        onClick = {
                            "ping start".showToast()
                            thread {
                                val responseCode = OkHttpClient().newCall(
                                    Request.Builder().url("https://cytoid.io/")
                                        .head()
                                        .removeHeader("User-Agent")
                                        .addHeader("User-Agent", "CytoidClient/2.1.1")
                                        .build()
                                ).execute().code
                                Looper.prepare()
                                "cytoid.io:$responseCode".showToast()
                            }
                        })
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.baseline_bug_report_24),
                                contentDescription = stringResource(id = R.string.testCrash)
                            )
                        },
                        text = { Text(text = stringResource(id = R.string.testCrash)) },
                        onClick = { CrashReport.testJavaCrash() })
                }
            }
        )
        Column(modifier = Modifier.padding(6.dp, 6.dp, 6.dp)) {
            Row {
                var textFieldIsError by remember { mutableStateOf(false) }
                TextField(
                    isError = textFieldIsError,
                    value = playerName,
                    onValueChange = {
                        playerName = it
                        textFieldIsError = it.isEmpty()
                    },
                    label = { Text(text = stringResource(id = R.string.playerName)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                if (playerName.isEmpty()) {
                                    "Cytoid ID不能为空".showToast()
                                    textFieldIsError = true
                                } else {
                                    textFieldIsError = false
                                    "开始查询$playerName".showToast()
                                    isQueryingFinished = false
                                    thread {
                                        b30Record = NetRequest.getB30Records(
                                            NetRequest.getB30RecordsString(
                                                playerName,
                                                30
                                            )
                                        )
                                        isQueryingFinished = true
                                    }
                                }
                            }
                        ) {
                            Text(text = stringResource(id = R.string.b30))
                        }
                    },
                    singleLine = true
                )
            }
            LazyVerticalStaggeredGrid(
                columns = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    StaggeredGridCells.Fixed(2)
                } else {
                    StaggeredGridCells.Adaptive(160.dp)
                },
                contentPadding = PaddingValues(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalItemSpacing = 6.dp
            ) {
                if (isQueryingFinished && ::b30Record.isInitialized) {
                    for (i in 0 until b30Record.data.profile.bestRecords.size) {
                        val record = b30Record.data.profile.bestRecords[i]
                        item {
                            Card {
                                Column {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(record.chart.level.bundle.backgroundImage.thumbnail)
                                            .crossfade(true)
                                            .setHeader("User-Agent", "CytoidClient/2.1.1")
                                            .build(),
                                        contentDescription = record.chart.level.title,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = "${i + 1}.${DataParser.parseB30RecordToText(record)}",
                                        Modifier.padding(bottom = 6.dp, start = 6.dp, end = 6.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}