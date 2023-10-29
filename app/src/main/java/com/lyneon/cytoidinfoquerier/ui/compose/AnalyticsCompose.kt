package com.lyneon.cytoidinfoquerier.ui.compose

import android.content.res.Configuration
import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.logic.model.GQLQueryResponseData
import com.lyneon.cytoidinfoquerier.logic.model.Profile
import com.lyneon.cytoidinfoquerier.logic.model.ProfileData
import com.lyneon.cytoidinfoquerier.logic.network.NetRequest
import com.lyneon.cytoidinfoquerier.tool.isValidCytoidID
import com.lyneon.cytoidinfoquerier.tool.showToast
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.lyneon.cytoidinfoquerier.ui.compose.component.RecordCard
import com.lyneon.cytoidinfoquerier.ui.compose.component.TopBar
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mmkv.MMKV
import kotlin.concurrent.thread

lateinit var profile: GQLQueryResponseData<ProfileData>

@Composable
fun AnalyticsCompose(drawerState: DrawerState) {
    val context = LocalContext.current as MainActivity
    var cytoidID by remember { mutableStateOf("") }
    var isQueryingFinished by remember { mutableStateOf(false) }
    val mmkv = MMKV.defaultMMKV()
    var queryType by remember { mutableStateOf(QueryType.bestRecords) }
    var ignoreCache by remember { mutableStateOf(false) }
    var queryCount by remember { mutableStateOf("30") }
    var queryCountIsNull by remember { mutableStateOf(false) }
    var querySettingsMenuIsExpanded by remember { mutableStateOf(false) }

    Column {
        TopBar(drawerState = drawerState)
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
                        Row(
                            Modifier.padding(horizontal = 6.dp)
                        ) {
                            IconButton(onClick = { querySettingsMenuIsExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = stringResource(id = R.string.queryType)
                                )
                                DropdownMenu(
                                    expanded = querySettingsMenuIsExpanded,
                                    onDismissRequest = { querySettingsMenuIsExpanded = false }) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(text = stringResource(id = R.string.best_records))
                                                RadioButton(
                                                    selected = queryType == QueryType.bestRecords,
                                                    onClick = {
                                                        queryType = QueryType.bestRecords
                                                    }
                                                )
                                            }
                                        },
                                        onClick = { queryType = QueryType.bestRecords }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(text = stringResource(id = R.string.recent_records))
                                                RadioButton(
                                                    selected = queryType == QueryType.recentRecords,
                                                    onClick = {
                                                        queryType = QueryType.recentRecords
                                                    }
                                                )
                                            }
                                        },
                                        onClick = { queryType = QueryType.recentRecords }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Column(
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                TextField(
                                                    value = queryCount,
                                                    onValueChange = {
                                                        queryCountIsNull = it.isEmpty()
                                                        queryCount = it
                                                    },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    isError = queryCountIsNull,
                                                    label = { Text(text = stringResource(id = R.string.query_count)) }
                                                )
                                                AnimatedVisibility(visible = queryCountIsNull) {
                                                    Text(
                                                        text = stringResource(id = R.string.empty_queryCount),
                                                        color = Color.Red
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {}
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(text = stringResource(id = R.string.ignore_cache))
                                                Checkbox(
                                                    checked = ignoreCache,
                                                    onCheckedChange = {
                                                        ignoreCache = it
                                                    }
                                                )
                                            }
                                        },
                                        onClick = { ignoreCache = !ignoreCache }
                                    )
                                }
                            }
                            TextButton(
                                onClick = {
                                    if (cytoidID.isEmpty()) {
                                        context.resources.getString(R.string.empty_cytoidID)
                                            .showToast()
                                        textFieldIsEmpty = true
                                    } else if (!cytoidID.isValidCytoidID()) {
                                        context.resources.getString(R.string.invalid_cytoidID)
                                            .showToast()
                                        textFieldIsError = true
                                    } else if (queryCountIsNull) {
                                        context.resources.getString(R.string.empty_queryCount)
                                            .showToast()
                                        querySettingsMenuIsExpanded = true
                                    } else {
                                        textFieldIsError = false
                                        isQueryingFinished = false
                                        if (System.currentTimeMillis() - mmkv.decodeLong(
                                                "lastQueryProfileTime_${cytoidID}_${queryType}",
                                                -1
                                            ) <= (6 * 60 * 60 * 1000) && !ignoreCache
                                        ) {
                                            "6小时内有查询记录，使用已缓存的数据".showToast()
                                            profile =
                                                NetRequest.convertGQLResponseJSONStringToObject(
                                                    mmkv.decodeString("profileString_${cytoidID}_${queryType}")
                                                        ?: throw Exception()
                                                )
                                            isQueryingFinished = true
                                        } else {
                                            "开始查询$cytoidID".showToast()
                                            thread {
                                                try {
                                                    val profileString =
                                                        NetRequest.getGQLResponseJSONString(
                                                            if (queryType == QueryType.bestRecords) {
                                                                Profile.getGQLQueryString(
                                                                    cytoidID,
                                                                    bestRecordsLimit = queryCount.toInt(),
                                                                    recentRecordsLimit = queryCount.toInt()
                                                                )
                                                            } else {
                                                                Profile.getGQLQueryString(
                                                                    cytoidID,
                                                                    bestRecordsLimit = queryCount.toInt(),
                                                                    recentRecordsLimit = queryCount.toInt()
                                                                )
                                                            }
                                                        )
                                                    profile =
                                                        NetRequest.convertGQLResponseJSONStringToObject(
                                                            profileString
                                                        )
                                                    isQueryingFinished = true
                                                    mmkv.encode(
                                                        "lastQueryProfileTime_${cytoidID}_${queryType}",
                                                        System.currentTimeMillis()
                                                    )
                                                    mmkv.encode(
                                                        "profileString_${cytoidID}_${queryType}",
                                                        profileString
                                                    )
                                                    Looper.prepare()
                                                    "查询${cytoidID}完成，共查询到${if (queryType == QueryType.bestRecords) profile.data.profile.bestRecords.size else profile.data.profile.recentRecords.size}条数据".showToast()
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
                                Text(text = stringResource(id = R.string.query))
                            }
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
                columns = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT)
                    StaggeredGridCells.Fixed(1)
                else StaggeredGridCells.Adaptive(300.dp),
                contentPadding = PaddingValues(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isQueryingFinished && ::profile.isInitialized) {
                    var remainRecord = if (queryCount.isEmpty()) 0 else queryCount.toInt()
                    for (i in 0 until if (queryType == QueryType.bestRecords) profile.data.profile.bestRecords.size else profile.data.profile.recentRecords.size) {
                        if (remainRecord == 0) break
                        val record =
                            if (queryType == QueryType.bestRecords) profile.data.profile.bestRecords[i]
                            else profile.data.profile.recentRecords[i]
                        item(
                            span = if (if (queryType == QueryType.bestRecords) {
                                    profile.data.profile.bestRecords.size
                                } else {
                                    profile.data.profile.recentRecords.size
                                } == 1
                            ) StaggeredGridItemSpan.FullLine
                            else StaggeredGridItemSpan.SingleLane
                        ) {
                            RecordCard(
                                record = record,
                                recordIndex = i + 1,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        remainRecord--
                    }
                }
            }
        }
    }
}

object QueryType {
    const val bestRecords = "bestRecords"
    const val recentRecords = "recentRecords"
}