package com.lyneon.cytoidinfoquerier.ui.compose

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.MMKVKeys
import com.lyneon.cytoidinfoquerier.data.constant.MainActivityScreens
import com.lyneon.cytoidinfoquerier.data.model.graphql.Analytics
import com.lyneon.cytoidinfoquerier.data.model.ui.AnalyticsScreenDataModel
import com.lyneon.cytoidinfoquerier.json
import com.lyneon.cytoidinfoquerier.logic.service.ImageGenerateService
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.lyneon.cytoidinfoquerier.ui.compose.component.AlertCard
import com.lyneon.cytoidinfoquerier.ui.compose.component.RecordCard
import com.lyneon.cytoidinfoquerier.ui.compose.component.TopBar
import com.lyneon.cytoidinfoquerier.ui.viewmodel.AnalyticsViewModel
import com.lyneon.cytoidinfoquerier.ui.viewmodel.QueryType
import com.lyneon.cytoidinfoquerier.util.extension.isValidCytoidID
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AnalyticsCompose(
    navController: NavController,
    navBackStackEntry: NavBackStackEntry?,
    viewModel: AnalyticsViewModel = viewModel()
) {
    val initArguments = navBackStackEntry?.arguments
    val context = LocalContext.current as MainActivity
    val viewState = viewModel.state.collectAsState()

    var analytics by viewState.value.analytics
    val cytoidID by viewState.value.cytoidID
    var isQueryFinished by viewState.value.isQueryFinished
    val queryType by viewState.value.queryType
    val ignoreCache by viewState.value.ignoreCache
    val keep2DecimalPlace by viewState.value.keep2DecimalPlace
    val queryCount by viewState.value.queryCount
    val columnsCount by viewState.value.columnsCount
    val querySettingsMenuIsExpanded by viewState.value.querySettingsMenuIsExpanded
    var hideInput by viewState.value.hideInput
    var hasLoadedCache by remember { mutableStateOf(false) }

    if (!hasLoadedCache) initArguments?.let {
        val initCytoidID = it.getString("initCytoidID")
        val initCacheTime = it.getString("initCacheTime")?.toLong()
        val cacheFile =
            File(context.externalCacheDir, "/analytics/${initCytoidID}/${initCacheTime}")

        if (initCytoidID != null) {
            viewModel.updateCytoidID(initCytoidID)
        }
        analytics = json.decodeFromString(cacheFile.readText())
        isQueryFinished = true
        hasLoadedCache = true
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
            InputField(
                cytoidID = cytoidID,
                analytics = analytics?.analytics,
                hideInput = hideInput,
                querySettingsMenuIsExpanded = querySettingsMenuIsExpanded,
                queryType = queryType,
                queryCount = queryCount,
                ignoreCache = ignoreCache,
                keep2DecimalPlace = keep2DecimalPlace,
                columnsCount = columnsCount,
                onCytoidIDChange = { viewModel.updateCytoidID(it) },
                onHideInput = { viewModel.hideInput() },
                onQuerySettingsMenuIsExpandedChange = {
                    viewModel.updateQuerySettingsMenuIsExpanded(it)
                },
                onQueryTypeChange = { viewModel.updateQueryType(it) },
                onQueryCountChange = { viewModel.updateQueryCount(it) },
                onIgnoreCacheChange = { viewModel.updateIgnoreCache(it) },
                onKeep2DecimalPlaceChange = { viewModel.updateKeep2DecimalPlace(it) },
                onColumnsCountChange = { viewModel.updateColumnsCount(it) },
                onQuery = { cytoidID, ignoreCache, queryCount ->
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.updateAnalytics(cytoidID, ignoreCache, queryCount)
                    }
                },
                onUpdateErrorMessage = { viewModel.updateErrorMessage(it) }
            )

            viewState.value.errorMessage.value?.let {
                if (it.isNotEmpty()) {
                    ErrorMessageCard(errorMessage = it)
                }
            } ?: analytics?.let { analytics ->
                queryCount?.let { queryCount ->
                    AnalyticsDisplayList(analytics, queryCount, queryType, keep2DecimalPlace)
                } ?: ErrorMessageCard("queryCount is null")
            }
        }
    }
}

@Composable
private fun InputField(
    cytoidID: String?,
    analytics: Analytics?,
    hideInput: Boolean,
    querySettingsMenuIsExpanded: Boolean,
    queryType: QueryType,
    queryCount: Int?,
    ignoreCache: Boolean,
    keep2DecimalPlace: Boolean,
    columnsCount: Int?,
    onCytoidIDChange: (String) -> Unit,
    onHideInput: () -> Unit,
    onQuerySettingsMenuIsExpandedChange: (Boolean) -> Unit,
    onQueryTypeChange: (QueryType) -> Unit,
    onQueryCountChange: (Int?) -> Unit,
    onIgnoreCacheChange: (Boolean) -> Unit,
    onKeep2DecimalPlaceChange: (Boolean) -> Unit,
    onColumnsCountChange: (Int?) -> Unit,
    onQuery: (String, Boolean, Int) -> Unit,
    onUpdateErrorMessage: (String) -> Unit
) {
    val context = LocalContext.current as MainActivity

    AnimatedVisibility(
        visible = !hideInput,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column {
            TextField(
                isError = !cytoidID.isValidCytoidID(),
                value = cytoidID ?: "",
                onValueChange = {
                    onCytoidIDChange(it)
                },
                label = { Text(text = stringResource(id = R.string.cytoid_id)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Row(
                        Modifier.padding(horizontal = 6.dp)
                    ) {
                        IconButton(onClick = { onHideInput() }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = stringResource(id = R.string.fold)
                            )
                        }
                        IconButton(onClick = { onQuerySettingsMenuIsExpandedChange(true) }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(id = R.string.query_settings)
                            )
                            DropdownMenu(
                                expanded = querySettingsMenuIsExpanded,
                                onDismissRequest = { onQuerySettingsMenuIsExpandedChange(false) }
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
                                                selected = queryType == QueryType.BestRecords,
                                                onClick = { onQueryTypeChange(QueryType.BestRecords) }
                                            )
                                        }
                                    },
                                    onClick = { onQueryTypeChange(QueryType.BestRecords) }
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
                                                selected = queryType == QueryType.RecentRecords,
                                                onClick = { onQueryTypeChange(QueryType.RecentRecords) }
                                            )
                                        }
                                    },
                                    onClick = { onQueryTypeChange(QueryType.RecentRecords) }
                                )
                                DropdownMenuItem(text = {
                                    Text(
                                        text = stringResource(R.string.query_type_declaration),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }, onClick = { })
                                DropdownMenuItem(
                                    text = {
                                        Column(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            TextField(
                                                value = queryCount?.toString() ?: "",
                                                onValueChange = { onQueryCountChange(it.toIntOrNull()) },
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Number
                                                ),
                                                singleLine = true,
                                                isError = queryCount == null,
                                                label = { Text(text = stringResource(id = R.string.query_count)) }
                                            )
                                            AnimatedVisibility(visible = queryCount == null) {
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
                                                onCheckedChange = { onIgnoreCacheChange(!ignoreCache) }
                                            )
                                        }
                                    },
                                    onClick = { onIgnoreCacheChange(!ignoreCache) }
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
                                                onCheckedChange = { onKeep2DecimalPlaceChange(!keep2DecimalPlace) }
                                            )
                                        }
                                    },
                                    onClick = { onKeep2DecimalPlaceChange(!keep2DecimalPlace) }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Column(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            TextField(
                                                value = columnsCount?.toString() ?: "",
                                                onValueChange = { onColumnsCountChange(it.toIntOrNull()) },
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Number
                                                ),
                                                singleLine = true,
                                                isError = columnsCount == null,
                                                label = { Text(text = stringResource(id = R.string.columns_count)) },
                                                trailingIcon = {
                                                    TextButton(onClick = {
                                                        if (!cytoidID.isValidCytoidID() || columnsCount == null || queryCount == null) return@TextButton
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
                                                        if (analytics == null) {
                                                            onUpdateErrorMessage("analytics is null")
                                                        } else {
                                                            val intent =
                                                                ImageGenerateService.getStartIntent(
                                                                    context,
                                                                    analytics,
                                                                    cytoidID,
                                                                    columnsCount.toInt(),
                                                                    queryType.name,
                                                                    queryCount.toInt(),
                                                                    keep2DecimalPlace
                                                                )
                                                            context.startService(intent)
                                                        }
                                                    }) {
                                                        Text(text = stringResource(id = R.string.save_as_picture))
                                                    }
                                                }
                                            )
                                            AnimatedVisibility(visible = columnsCount == null) {
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
                                if (cytoidID.isNullOrEmpty()|| queryCount == null) onUpdateErrorMessage("cytoidID or queryCount is null")
                                else {
                                    onQuery(cytoidID, ignoreCache, queryCount)
                                    "已发起查询任务".showToast()
                                }
                            }
                        ) {
                            Text(text = stringResource(id = R.string.query))
                        }
                    }
                },
                singleLine = true
            )
        }
    }
}

@Composable
private fun ErrorMessageCard(errorMessage: String) {
    AlertCard(message = errorMessage, modifier = Modifier.padding(vertical = 6.dp))
}

@Composable
private fun AnalyticsDisplayList(
    analyticsScreenDataModel: AnalyticsScreenDataModel,
    queryCount: Int,
    queryType: QueryType,
    keep2DecimalPlace: Boolean
) {
    val analyticsData = analyticsScreenDataModel.analytics.data
    val mmkv = MMKV.defaultMMKV()

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(
            mmkv.decodeInt(
                if (BaseApplication.context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) MMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT.name
                else MMKVKeys.GRID_COLUMNS_COUNT_LANDSCAPE.name, 1
            )
        ),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalItemSpacing = 6.dp,
        contentPadding = PaddingValues(vertical = 6.dp)
    ) {
        analyticsData.profile?.let { profile ->
            var remainRecord = queryCount
            for (i in 0 until
                    if (queryType == QueryType.BestRecords) profile.bestRecords.size
                    else profile.recentRecords.size
            ) {
                if (remainRecord == 0) break
                val record =
                    if (queryType == QueryType.BestRecords) profile.bestRecords[i]
                    else profile.recentRecords[i]
                item {
                    RecordCard(
                        record = record,
                        recordIndex = i + 1,
                        keep2DecimalPlace
                    )
                }
                remainRecord--
            }
        }
    }
}
