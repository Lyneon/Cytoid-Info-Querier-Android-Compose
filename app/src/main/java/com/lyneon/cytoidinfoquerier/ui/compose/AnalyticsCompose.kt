package com.lyneon.cytoidinfoquerier.ui.compose

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Looper
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
import androidx.compose.material.icons.filled.History
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
import androidx.compose.runtime.MutableState
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
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.GraphQL
import com.lyneon.cytoidinfoquerier.data.constant.MMKVKeys
import com.lyneon.cytoidinfoquerier.data.constant.MainActivityScreens
import com.lyneon.cytoidinfoquerier.data.model.graphql.Analytics
import com.lyneon.cytoidinfoquerier.data.model.ui.AnalyticsScreenDataModel
import com.lyneon.cytoidinfoquerier.json
import com.lyneon.cytoidinfoquerier.logic.network.NetRequest
import com.lyneon.cytoidinfoquerier.logic.service.ImageGenerateService
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.lyneon.cytoidinfoquerier.ui.compose.component.AlertCard
import com.lyneon.cytoidinfoquerier.ui.compose.component.RecordCard
import com.lyneon.cytoidinfoquerier.ui.compose.component.TopBar
import com.lyneon.cytoidinfoquerier.util.extension.isValidCytoidID
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import com.tencent.mmkv.MMKV
import io.sentry.Sentry
import kotlinx.serialization.encodeToString
import java.io.File
import kotlin.concurrent.thread

lateinit var response: AnalyticsScreenDataModel

@Composable
fun AnalyticsCompose(navController: NavController, navBackStackEntry: NavBackStackEntry?) {
    val initArguments = navBackStackEntry?.arguments
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
    var error by remember { mutableStateOf("") }

    initArguments?.let {
        val initCytoidID = it.getString("initCytoidID")
        val initCacheTime = it.getString("initCacheTime")?.toLong()
        val cacheFile =
            File(context.externalCacheDir, "/analytics/${initCytoidID}/${initCacheTime}")

        response = json.decodeFromString(cacheFile.readText())
        isQueryingFinished = true
    }

    Column {
        TopBar(
            title = stringResource(id = R.string.analytics),
            actionsAlwaysShow = {
                if (hideInput) IconButton(onClick = { hideInput = false }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(id = R.string.unfold)
                    )
                }
            },
            actionsDropDownMenuContent = { menuIsExpanded: MutableState<Boolean> ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.history)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = stringResource(id = R.string.history)
                        )
                    },
                    onClick = {
                        navController.navigate(MainActivityScreens.History.name + "/analytics")
                        menuIsExpanded.value = false
                    }
                )
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
                        label = { Text(text = stringResource(id = R.string.cytoid_id)) },
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
                                        contentDescription = stringResource(id = R.string.query_settings)
                                    )
                                    DropdownMenu(
                                        expanded = querySettingsMenuIsExpanded,
                                        onDismissRequest = {
                                            querySettingsMenuIsExpanded = false
                                        }
                                    ) {
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
                                                            text = stringResource(id = R.string.empty_query_count),
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
                                        error = ""
                                        if (cytoidID.isEmpty()) {
                                            context.getString(R.string.empty_cytoid_id)
                                                .showToast()
                                            textFieldIsEmpty = true
                                        } else if (!cytoidID.isValidCytoidID()) {
                                            context.getString(R.string.invalid_cytoid_id)
                                                .showToast()
                                            textFieldIsError = true
                                        } else if (queryCountIsNull) {
                                            context.getString(R.string.empty_query_count)
                                                .showToast()
                                            querySettingsMenuIsExpanded = true
                                        } else {
//                                            ID格式正确，开始查询
                                            textFieldIsError = false
                                            isQueryingFinished = false
                                            val lastQueryTime =
                                                mmkv.decodeLong(
                                                    "lastQueryAnalyticsTime_${cytoidID}",
                                                    -1
                                                )
                                            val cacheAnalyticsDirectory =
                                                context.externalCacheDir?.run {
                                                    File(this.path + "/analytics/${cytoidID}")
                                                }
                                            val cacheAnalyticsFile = cacheAnalyticsDirectory?.run {
                                                if (!this.exists()) this.mkdirs()
                                                File(this, lastQueryTime.toString())
                                            }
//                                            检查是否存在已缓存的数据
                                            if (lastQueryTime != -1L &&
                                                cacheAnalyticsFile != null &&
                                                cacheAnalyticsFile.exists() &&
                                                System.currentTimeMillis() - lastQueryTime <= (6 * 60 * 60 * 1000) &&
                                                !ignoreCache
                                            ) {
//                                                存在已缓存的数据，从本地读取
                                                response = try {
                                                    var toIndex: Int
                                                    val analyticsString =
                                                        cacheAnalyticsFile.readText()
                                                    val analyticsScreenDataModel =
                                                        json.decodeFromString<AnalyticsScreenDataModel>(
                                                            analyticsString
                                                        ).apply {
                                                            if (this.analytics.data.profile != null) {
                                                                val profile =
                                                                    this.analytics.data.profile
                                                                if (queryType == QueryType.bestRecords) {
                                                                    toIndex =
                                                                        if (queryCount.toInt() <= profile.bestRecords.size) queryCount.toInt()
                                                                        else profile.bestRecords.size
                                                                    profile.bestRecords =
                                                                        ArrayList(
                                                                            profile.bestRecords.subList(
                                                                                0,
                                                                                toIndex
                                                                            )
                                                                        )
                                                                } else {
                                                                    toIndex =
                                                                        if (queryCount.toInt() <= profile.recentRecords.size) queryCount.toInt()
                                                                        else profile.recentRecords.size
                                                                    profile.recentRecords =
                                                                        ArrayList(
                                                                            profile.recentRecords.subList(
                                                                                0,
                                                                                toIndex
                                                                            )
                                                                        )
                                                                }
                                                            } else {
                                                                error =
                                                                    "local cache data.profile is null!"
                                                                return@TextButton
                                                            }
                                                        }
                                                    "6小时内有查询记录，使用已缓存的数据，共${toIndex}条数据".showToast()
                                                    analyticsScreenDataModel
                                                } catch (e: Exception) {
                                                    error = e.stackTraceToString()
                                                    Sentry.captureException(e)
                                                    return@TextButton
                                                }
                                                isQueryingFinished = true
                                            } else {
//                                                不存在已缓存的数据，从服务器获取数据并缓存至本地
                                                "开始查询$cytoidID".showToast()
                                                thread {
                                                    try {
                                                        val analyticsString =
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
                                                            AnalyticsScreenDataModel(
                                                                Analytics.decodeFromJSONString(
                                                                    analyticsString
                                                                )
                                                            )
                                                        if (response.analytics.data.profile == null) {
                                                            error = "data.profile is null!"
                                                            return@thread
                                                        } else {
                                                            Looper.prepare()
                                                            "查询${cytoidID}完成，共查询到${
                                                                if (queryType == QueryType.bestRecords) response.analytics.data.profile!!.bestRecords.size
                                                                else response.analytics.data.profile!!.recentRecords.size
                                                            }条数据".showToast()
                                                            isQueryingFinished = true
//                                                            缓存数据至本地
                                                            val currentTime =
                                                                System.currentTimeMillis()
                                                            mmkv.encode(
                                                                "lastQueryAnalyticsTime_${cytoidID}",
                                                                currentTime
                                                            )
                                                            cacheAnalyticsDirectory?.run {
                                                                if (!exists()) mkdirs()
                                                                File(
                                                                    this,
                                                                    currentTime.toString()
                                                                ).outputStream().bufferedWriter()
                                                                    .use {
                                                                        it.write(
                                                                            json.encodeToString<AnalyticsScreenDataModel>(
                                                                                response
                                                                            )
                                                                        )
                                                                    }
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        error = "查询失败：${e.stackTraceToString()}"
                                                        Sentry.captureException(e)
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
                            text = stringResource(id = R.string.invalid_cytoid_id),
                            color = Color.Red
                        )
                    }
                    AnimatedVisibility(visible = textFieldIsEmpty) {
                        Text(
                            text = stringResource(id = R.string.empty_cytoid_id),
                            color = Color.Red
                        )
                    }
                }
            }
            if (error.isNotEmpty()) {
                AlertCard(message = error, modifier = Modifier.padding(vertical = 6.dp))
            } else {
                AnimatedVisibility(visible = isQueryingFinished && ::response.isInitialized) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(
                            mmkv.decodeInt(
                                if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) MMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT.name
                                else MMKVKeys.GRID_COLUMNS_COUNT_LANDSCAPE.name, 1
                            )
                        ),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalItemSpacing = 6.dp,
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        if (isQueryingFinished && ::response.isInitialized) {
                            if (response.analytics.data.profile != null) {
                                var remainRecord =
                                    if (queryCount.isEmpty()) 0 else queryCount.toInt()
                                for (i in 0 until
                                        if (queryType == QueryType.bestRecords) response.analytics.data.profile!!.bestRecords.size
                                        else response.analytics.data.profile!!.recentRecords.size
                                ) {
                                    if (remainRecord == 0) break
                                    val record =
                                        if (queryType == QueryType.bestRecords) response.analytics.data.profile!!.bestRecords[i]
                                        else response.analytics.data.profile!!.recentRecords[i]
                                    item(
                                        span = if ((if (queryType == QueryType.bestRecords) response.analytics.data.profile!!.bestRecords.size
                                            else response.analytics.data.profile!!.recentRecords.size) == 1
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
}

object QueryType {
    const val bestRecords = "bestRecords"
    const val recentRecords = "recentRecords"
}

fun responseIsInitialized() = ::response.isInitialized