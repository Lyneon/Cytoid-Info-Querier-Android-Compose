package com.lyneon.cytoidinfoquerier.ui.compose

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Looper
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.checkSelfPermission
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.GraphQL
import com.lyneon.cytoidinfoquerier.data.constant.MMKVKeys
import com.lyneon.cytoidinfoquerier.data.model.graphql.Analytics
import com.lyneon.cytoidinfoquerier.logic.network.NetRequest
import com.lyneon.cytoidinfoquerier.logic.service.ImageGenerateService
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.lyneon.cytoidinfoquerier.ui.compose.component.AlertCard
import com.lyneon.cytoidinfoquerier.ui.compose.component.RecordCard
import com.lyneon.cytoidinfoquerier.ui.compose.component.TopBar
import com.lyneon.cytoidinfoquerier.util.extension.isValidCytoidID
import com.lyneon.cytoidinfoquerier.util.extension.showDialog
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import com.microsoft.appcenter.crashes.Crashes
import com.tencent.mmkv.MMKV
import kotlin.concurrent.thread

lateinit var response: Analytics

@Composable
fun AnalyticsCompose() {
    val context = LocalContext.current as MainActivity
    var cytoidID by remember { mutableStateOf("") }
    var isQueryingFinished by remember { mutableStateOf(false) }
    val mmkv = MMKV.defaultMMKV()
    var queryType by remember { mutableStateOf(QueryType.bestRecords) }
    var ignoreCache by remember { mutableStateOf(false) }
    var keep2DecimalPlace by remember { mutableStateOf(true) }
    var queryCount by remember { mutableStateOf("30") }
    var columnsCount by remember { mutableStateOf("6") }
    var queryCountIsNull by remember { mutableStateOf(false) }
    var columnsCountIsNull by remember { mutableStateOf(false) }
    var querySettingsMenuIsExpanded by remember { mutableStateOf(false) }
    var hideInput by remember { mutableStateOf(false) }

    Column {
        TopBar(
            title = stringResource(id = R.string.analytics),
            additionalActions = {
                if (hideInput) IconButton(onClick = { hideInput = false }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(id = R.string.unfold)
                    )
                }
            }
        )
        Column(modifier = Modifier.padding(6.dp, 6.dp, 6.dp)) {
            AnimatedVisibility(visible = !hideInput) {
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
                                IconButton(onClick = { hideInput = true }) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = stringResource(id = R.string.fold)
                                    )
                                }
                                IconButton(onClick = { querySettingsMenuIsExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = stringResource(id = R.string.querySettings)
                                    )
                                    DropdownMenu(
                                        expanded = querySettingsMenuIsExpanded,
                                        onDismissRequest = {
                                            querySettingsMenuIsExpanded = false
                                        }) {
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
                                                        keyboardOptions = KeyboardOptions(
                                                            keyboardType = KeyboardType.Number
                                                        ),
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
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(text = stringResource(id = R.string.keep_2_decimal_places))
                                                    Checkbox(
                                                        checked = keep2DecimalPlace,
                                                        onCheckedChange = {
                                                            keep2DecimalPlace = it
                                                        }
                                                    )
                                                }
                                            },
                                            onClick = { keep2DecimalPlace = !keep2DecimalPlace }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Column(
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    TextField(
                                                        value = columnsCount,
                                                        onValueChange = {
                                                            columnsCountIsNull = it.isEmpty()
                                                            columnsCount = it
                                                        },
                                                        keyboardOptions = KeyboardOptions(
                                                            keyboardType = KeyboardType.Number
                                                        ),
                                                        singleLine = true,
                                                        isError = columnsCountIsNull,
                                                        label = { Text(text = stringResource(id = R.string.columns_count)) },
                                                        trailingIcon = {
                                                            TextButton(onClick = {
                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkSelfPermission(
                                                                        context,
                                                                        Manifest.permission.POST_NOTIFICATIONS
                                                                    ) != PackageManager.PERMISSION_GRANTED
                                                                ) {
                                                                    context.requestPermissions(
                                                                        arrayOf(
                                                                            Manifest.permission.POST_NOTIFICATIONS
                                                                        ), 1
                                                                    )
                                                                }
                                                                val intent =
                                                                    ImageGenerateService.getStartIntent(
                                                                        context,
                                                                        cytoidID,
                                                                        columnsCount.toInt(),
                                                                        queryType,
                                                                        queryCount.toInt(),
                                                                        keep2DecimalPlace
                                                                    )
                                                                context.startService(intent)
                                                            }) {
                                                                Text(text = stringResource(id = R.string.save_as_picture))
                                                            }
                                                        }
                                                    )
                                                    AnimatedVisibility(visible = columnsCountIsNull) {
                                                        Text(
                                                            text = stringResource(id = R.string.empty_columnsCount),
                                                            color = Color.Red
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {}
                                        )
                                    }
                                }
                                TextButton(
                                    onClick = {
                                        if (cytoidID.isEmpty()) {
                                            context.getString(R.string.empty_cytoidID)
                                                .showToast()
                                            textFieldIsEmpty = true
                                        } else if (!cytoidID.isValidCytoidID()) {
                                            context.getString(R.string.invalid_cytoidID)
                                                .showToast()
                                            textFieldIsError = true
                                        } else if (queryCountIsNull) {
                                            context.getString(R.string.empty_queryCount)
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
                                                response = try {
                                                    var toIndex: Int
                                                    val analytics = Analytics.decodeFromJSONString(
                                                        mmkv.decodeString("profileString_${cytoidID}_${queryType}")
                                                            ?: throw Exception("decode local cache failed!")
                                                    ).apply {
                                                        if (this.data.profile != null) {
                                                            if (queryType == QueryType.bestRecords) {
                                                                toIndex =
                                                                    if (queryCount.toInt() <= this.data.profile.bestRecords.size) queryCount.toInt()
                                                                    else this.data.profile.bestRecords.size
                                                                this.data.profile.bestRecords =
                                                                    ArrayList(
                                                                        this.data.profile.bestRecords.subList(
                                                                            0,
                                                                            toIndex
                                                                        )
                                                                    )
                                                            } else {
                                                                toIndex =
                                                                    if (queryCount.toInt() <= this.data.profile.recentRecords.size) queryCount.toInt()
                                                                    else this.data.profile.recentRecords.size
                                                                this.data.profile.recentRecords =
                                                                    ArrayList(
                                                                        this.data.profile.recentRecords.subList(
                                                                            0,
                                                                            toIndex
                                                                        )
                                                                    )
                                                            }
                                                        } else {
                                                            throw Exception("local cache's data.profile is null!")
                                                        }
                                                    }
                                                    "6小时内有查询记录，使用已缓存的数据，共${toIndex}条数据".showToast()
                                                    analytics
                                                } catch (e: Exception) {
                                                    e.stackTraceToString().showDialog(
                                                        context,
                                                        context.getString(R.string.fail)
                                                    )
                                                    Crashes.trackError(e)
                                                    return@TextButton
                                                }
                                                isQueryingFinished = true
                                            } else {
                                                "开始查询$cytoidID".showToast()
                                                thread {
                                                    try {
                                                        val profileString =
                                                            NetRequest.getGQLResponseJSONString(
                                                                GraphQL.getQueryString(
                                                                    if (queryType == QueryType.bestRecords) {
                                                                        Analytics.getQueryString(
                                                                            cytoidID = cytoidID,
                                                                            bestRecordsLimit = queryCount.toInt(),
                                                                            recentRecordsLimit = queryCount.toInt()
                                                                        )
                                                                    } else {
                                                                        Analytics.getQueryString(
                                                                            cytoidID = cytoidID,
                                                                            bestRecordsLimit = queryCount.toInt(),
                                                                            recentRecordsLimit = queryCount.toInt()
                                                                        )
                                                                    }
                                                                )
                                                            )
                                                        response =
                                                            Analytics.decodeFromJSONString(
                                                                profileString
                                                            )
                                                        if (response.data.profile == null) throw Exception(
                                                            "data.profile is null!"
                                                        )
                                                        else {
                                                            mmkv.encode(
                                                                "lastQueryProfileTime_${cytoidID}_${queryType}",
                                                                System.currentTimeMillis()
                                                            )
                                                            mmkv.encode(
                                                                "profileString_${cytoidID}_${queryType}",
                                                                profileString
                                                            )
                                                            Looper.prepare()
                                                            "查询${cytoidID}完成，共查询到${
                                                                if (queryType == QueryType.bestRecords) response.data.profile!!.bestRecords.size
                                                                else response.data.profile!!.recentRecords.size
                                                            }条数据".showToast()
                                                            isQueryingFinished = true
                                                        }
                                                    } catch (e: Exception) {
                                                        Looper.prepare()
                                                        "查询失败：${e.stackTraceToString()}".showToast(
                                                            Toast.LENGTH_LONG
                                                        )
                                                        e.printStackTrace()
                                                        Crashes.trackError(e)
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
            }
            AnimatedVisibility(visible = isQueryingFinished && ::response.isInitialized) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(
                        mmkv.decodeInt(
                            if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) MMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT
                            else MMKVKeys.GRID_COLUMNS_COUNT_LANDSCAPE, 1
                        )
                    ),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalItemSpacing = 6.dp,
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    if (isQueryingFinished && ::response.isInitialized) {
                        if (response.data.profile != null) {
                            var remainRecord = if (queryCount.isEmpty()) 0 else queryCount.toInt()
                            for (i in 0 until
                                    if (queryType == QueryType.bestRecords) response.data.profile!!.bestRecords.size
                                    else response.data.profile!!.recentRecords.size
                            ) {
                                if (remainRecord == 0) break
                                val record =
                                    if (queryType == QueryType.bestRecords) response.data.profile!!.bestRecords[i]
                                    else response.data.profile!!.recentRecords[i]
                                item(
                                    span = if ((if (queryType == QueryType.bestRecords) response.data.profile!!.bestRecords.size
                                        else response.data.profile!!.recentRecords.size) == 1
                                    ) StaggeredGridItemSpan.FullLine
                                    else StaggeredGridItemSpan.SingleLane
                                ) {
                                    RecordCard(
                                        record = record,
                                        recordIndex = i + 1,
                                        keep2DecimalPlace
                                    )
                                }
                                remainRecord--
                            }
                        } else {
                            item {
                                AlertCard(message = "data.profile is null!")
                            }
                        }
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

fun responseIsInitialized() = ::response.isInitialized