package com.lyneon.cytoidinfoquerier.ui.compose

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Looper
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.logic.dao.DataParser
import com.lyneon.cytoidinfoquerier.logic.model.GQLQueryResponseData
import com.lyneon.cytoidinfoquerier.logic.model.Profile
import com.lyneon.cytoidinfoquerier.logic.model.ProfileData
import com.lyneon.cytoidinfoquerier.logic.model.QueryOrder
import com.lyneon.cytoidinfoquerier.logic.model.RecordQuerySort
import com.lyneon.cytoidinfoquerier.logic.network.NetRequest
import com.lyneon.cytoidinfoquerier.tool.isValidCytoidID
import com.lyneon.cytoidinfoquerier.tool.saveIntoClipboard
import com.lyneon.cytoidinfoquerier.tool.showToast
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mmkv.MMKV
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.concurrent.thread

lateinit var profile: GQLQueryResponseData<ProfileData>

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class
)
@Composable
fun MainActivityCompose() {
    val context = LocalContext.current as MainActivity
    var cytoidID by remember { mutableStateOf("") }
    var isQueryingFinished by remember { mutableStateOf(false) }
    val mmkv = MMKV.defaultMMKV()
    val externalCacheStorageDir = context.externalCacheDir

    Column {
        CenterAlignedTopAppBar(
            title = { Text(text = stringResource(id = R.string.app_name)) },
            navigationIcon = {
                IconButton(
                    onClick = {
                        "还没做完，先贵阳一会".showToast()
                    }) {
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
                    DropdownMenuItem(text = { Text(text = "Test") }, onClick = {
                        "Test start".showToast()
                        thread {
                            try {
                                val profileJSONString = NetRequest.getGQLResponseJSONString(
                                    Profile.getGQLQueryString(
                                        cytoidID,
                                        1,
                                        RecordQuerySort.Date,
                                        QueryOrder.DESC,
                                        1
                                    )
                                )
                                profileJSONString.saveIntoClipboard()
                                val profile =
                                    NetRequest.convertGQLResponseJSONStringToObject<ProfileData>(
                                        profileJSONString
                                    )
                                Log.i("Test", profile.toString())
                            } catch (e: Exception) {
                                e.printStackTrace()
                                e.stackTraceToString().saveIntoClipboard()
                            } finally {
                                Looper.prepare()
                                "Test finished".showToast()
                            }
                        }
                    })
                }
            }
        )
        Column(modifier = Modifier.padding(6.dp, 6.dp, 6.dp)) {
            Column {
                var textFieldIsError by remember { mutableStateOf(false) }
                var textFieldIsEmpty by remember { mutableStateOf(false) }
                TextField(
                    isError = textFieldIsError or textFieldIsEmpty,
                    value = cytoidID,
                    onValueChange = {
                        cytoidID = it
                        textFieldIsError = !it.isValidCytoidID()
                        textFieldIsEmpty = it.isEmpty()
                    },
                    label = { Text(text = stringResource(id = R.string.playerName)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                if (cytoidID.isEmpty()) {
                                    context.resources.getString(R.string.empty_cytoidID).showToast()
                                    textFieldIsEmpty = true
                                } else if (!cytoidID.isValidCytoidID()) {
                                    context.resources.getString(R.string.invalid_cytoidID)
                                        .showToast()
                                    textFieldIsError = true
                                } else {
                                    textFieldIsError = false
                                    isQueryingFinished = false
                                    if (System.currentTimeMillis() - mmkv.decodeLong(
                                            "lastQueryProfileTime_${cytoidID}",
                                            -1
                                        ) <= (6 * 60 * 60 * 1000)
                                    ) {
                                        "6小时内有查询记录，使用已缓存的数据".showToast()
                                        profile = NetRequest.convertGQLResponseJSONStringToObject(
                                            mmkv.decodeString("profileString_${cytoidID}")
                                                ?: throw Exception()
                                        )
                                        isQueryingFinished = true
                                    } else {
                                        "开始查询$cytoidID".showToast()
                                        thread {
                                            try {
                                                val profileString =
                                                    NetRequest.getGQLResponseJSONString(
                                                        Profile.getGQLQueryString(
                                                            cytoidID,
                                                            bestRecordsLimit = 30
                                                        )
                                                    )
                                                profile =
                                                    NetRequest.convertGQLResponseJSONStringToObject(
                                                        profileString
                                                    )
                                                isQueryingFinished = true
                                                mmkv.encode(
                                                    "lastQueryProfileTime_${cytoidID}",
                                                    System.currentTimeMillis()
                                                )
                                                mmkv.encode(
                                                    "profileString_${cytoidID}",
                                                    profileString
                                                )
                                                Looper.prepare()
                                                "查询${cytoidID}完成，共查询到${profile.data.profile.bestRecords.size}条数据".showToast()
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                CrashReport.postCatchedException(
                                                    e.cause,
                                                    Thread.currentThread()
                                                )
                                                Looper.prepare()
                                                "查询失败:${e.stackTraceToString()}".showToast()
                                            }
                                        }
                                    }
                                }
                            }
                        ) {
                            Text(text = stringResource(id = R.string.b30))
                        }
                    },
                    singleLine = true
                )
                AnimatedVisibility(visible = textFieldIsError) {
                    Text(
                        text = stringResource(id = R.string.invalid_cytoidID),
                        color = Color.Red
                    )
                }
                AnimatedVisibility(visible = textFieldIsEmpty) {
                    Text(
                        text = stringResource(id = R.string.empty_cytoidID),
                        color = Color.Red
                    )
                }
            }
            LazyVerticalStaggeredGrid(
                columns = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    StaggeredGridCells.Fixed(2)
                } else {
                    StaggeredGridCells.Adaptive(160.dp)
                },
                contentPadding = PaddingValues(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isQueryingFinished && ::profile.isInitialized) {
                    for (i in 0 until profile.data.profile.bestRecords.size) {
                        val record = profile.data.profile.bestRecords[i]
                        item(span = if (profile.data.profile.bestRecords.size == 1) StaggeredGridItemSpan.FullLine else StaggeredGridItemSpan.SingleLane) {
                            Card {
                                Column {
                                    if (File(
                                            externalCacheStorageDir,
                                            "backgroundImage_${record.chart.level.uid}"
                                        ).exists() && File(
                                            externalCacheStorageDir,
                                            "backgroundImage_${record.chart.level.uid}"
                                        ).isFile
                                    ) {
                                        val input = FileInputStream(
                                            File(
                                                externalCacheStorageDir,
                                                "backgroundImage_${record.chart.level.uid}"
                                            )
                                        )
                                        val bitmap = BitmapFactory.decodeStream(input)
                                        Image(
                                            painter = BitmapPainter(bitmap.asImageBitmap()),
                                            contentDescription = record.chart.level.title,
                                            contentScale = ContentScale.FillWidth,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    } else {
                                        Box {
                                            CircularProgressIndicator(
                                                modifier = Modifier.align(
                                                    Alignment.Center
                                                )
                                            )
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(record.chart.level.bundle.backgroundImage.thumbnail)
                                                    .crossfade(true)
                                                    .setHeader(
                                                        "User-Agent",
                                                        "CytoidClient/2.1.1"
                                                    )
                                                    .build(),
                                                modifier = Modifier.fillMaxWidth(),
                                                contentDescription = record.chart.level.title,
                                                onSuccess = {
                                                    val imageFile = File(
                                                        externalCacheStorageDir,
                                                        "backgroundImage_${record.chart.level.uid}"
                                                    )
                                                    try {
                                                        imageFile.createNewFile()
                                                        val output = FileOutputStream(imageFile)
                                                        it.result.drawable.toBitmap().compress(
                                                            Bitmap.CompressFormat.PNG,
                                                            100,
                                                            output
                                                        )
                                                        output.flush()
                                                        output.close()
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                        e.stackTraceToString().showToast()
                                                    }
                                                },
                                                contentScale = ContentScale.FillWidth
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${i + 1}.${
                                            DataParser.parseProfileUserRecordToText(record)
                                        }",
                                        Modifier
                                            .padding(bottom = 6.dp, start = 6.dp, end = 6.dp)
                                            .fillMaxWidth()
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