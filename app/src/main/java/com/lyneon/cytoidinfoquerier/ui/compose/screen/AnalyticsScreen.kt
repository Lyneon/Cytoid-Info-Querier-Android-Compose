package com.lyneon.cytoidinfoquerier.ui.compose.screen

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.RecordQueryOrder
import com.lyneon.cytoidinfoquerier.data.constant.RecordQuerySort
import com.lyneon.cytoidinfoquerier.data.constant.RecordQuerySort.Companion.displayName
import com.lyneon.cytoidinfoquerier.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.data.model.graphql.BestRecords
import com.lyneon.cytoidinfoquerier.data.model.graphql.RecentRecords
import com.lyneon.cytoidinfoquerier.data.model.webapi.ProfileDetails
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.lyneon.cytoidinfoquerier.ui.compose.component.ErrorMessageCard
import com.lyneon.cytoidinfoquerier.ui.compose.component.RecordCard
import com.lyneon.cytoidinfoquerier.ui.compose.component.UserDetailsHeader
import com.lyneon.cytoidinfoquerier.ui.viewmodel.AnalyticsPreset
import com.lyneon.cytoidinfoquerier.ui.viewmodel.AnalyticsUIState
import com.lyneon.cytoidinfoquerier.ui.viewmodel.AnalyticsViewModel
import com.lyneon.cytoidinfoquerier.util.AppSettingsMMKVKeys
import com.lyneon.cytoidinfoquerier.util.CytoidIdAutoFillUtils
import com.lyneon.cytoidinfoquerier.util.MMKVId
import com.lyneon.cytoidinfoquerier.util.extension.isValidCytoidID
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = viewModel(),
    navController: NavController,
    navBackStackEntry: NavBackStackEntry,
    withInitials: Boolean = false,
    withShortcutPreset: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val bestRecords by viewModel.bestRecords.collectAsState()
    val recentRecords by viewModel.recentRecords.collectAsState()
    val profileDetails by viewModel.profileDetails.collectAsState()
    var playbackState by rememberSaveable { mutableIntStateOf(ExoPlayer.STATE_IDLE) }
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    val exoPlayer by remember {
        mutableStateOf(ExoPlayer.Builder(BaseApplication.context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(newPlaybackState: Int) {
                    super.onPlaybackStateChanged(newPlaybackState)
                    playbackState = newPlaybackState
                }

                override fun onIsPlayingChanged(newIsPlaying: Boolean) {
                    super.onIsPlayingChanged(newIsPlaying)
                    isPlaying = newIsPlaying
                }
            })
        })
    }
    var initialLoaded by rememberSaveable { mutableStateOf(false) }
    var shortcutPresetLoaded by rememberSaveable { mutableStateOf(false) }
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (withInitials && !initialLoaded) {
            launch {
                val initialCytoidID = navBackStackEntry.arguments?.getString("initialCytoidID")
                val initialCacheType = navBackStackEntry.arguments?.getString("initialCacheType")
                val initialCacheTime =
                    navBackStackEntry.arguments?.getString("initialCacheTime")?.toLong()
                if (initialCytoidID != null && initialCacheType != null && initialCacheTime != null) {
                    viewModel.setCytoidID(initialCytoidID)
                    viewModel.updateProfileDetailsWithInnerScope()
                    if (initialCacheType == "BestRecords") {
                        viewModel.setQueryType(AnalyticsUIState.QueryType.BestRecords)
                        viewModel.loadSpecificCacheBestRecords(initialCacheTime)
                    } else {
                        viewModel.setQueryType(AnalyticsUIState.QueryType.RecentRecords)
                        viewModel.loadSpecificCacheRecentRecords(initialCacheTime)
                    }
                }
                initialLoaded = true
            }
        }
        if (withShortcutPreset && !shortcutPresetLoaded) {
            launch {
                val shortcutPreset = navBackStackEntry.arguments?.getString("shortcutPreset")
                val appUserID =
                    MMKV.mmkvWithID(MMKVId.AppSettings.id)
                        .decodeString(AppSettingsMMKVKeys.APP_USER_CYTOID_ID.name)
                when (shortcutPreset) {
                    AnalyticsPreset.B30.name -> {
                        viewModel.run {
                            setQueryType(AnalyticsUIState.QueryType.BestRecords)
                            setQueryCount("30")
                            setQueryOrder(RecordQueryOrder.DESC)
                        }
                    }

                    AnalyticsPreset.R10.name -> {
                        viewModel.run {
                            setQueryType(AnalyticsUIState.QueryType.RecentRecords)
                            setQueryCount("10")
                            setQuerySort(RecordQuerySort.RecentRating)
                            setQueryOrder(RecordQueryOrder.DESC)
                        }
                    }
                }
                if (appUserID != null) {
                    viewModel.setCytoidID(appUserID)
                }
                awaitFrame()
                if (uiState.canQuery()) {
                    viewModel.enqueueQuery()
                }
                shortcutPresetLoaded = true
            }
        }
    }

    LaunchedEffect(exoPlayer) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(newPlaybackState: Int) {
                playbackState = newPlaybackState
            }
        })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.analytics)) },
                actions = {
                    if (uiState.foldTextFiled) {
                        IconButton(onClick = { viewModel.setFoldTextFiled(false) }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "展开输入框"
                            )
                        }
                    }
                    IconButton(onClick = {
                        viewModel.setExpandAnalyticsOptionsDropdownMenu(true)
                    }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                        DropdownMenu(
                            expanded = uiState.expandAnalyticsOptionsDropdownMenu,
                            onDismissRequest = {
                                viewModel.setExpandAnalyticsOptionsDropdownMenu(false)
                            }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.history)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    navController.navigate(MainActivity.Screen.AnalyticsHistory.route) {
                                        launchSingleTop = true
                                        popUpTo(MainActivity.Screen.AnalyticsHistory.route) {
                                            inclusive = true
                                        }
                                    }
                                    viewModel.setExpandAnalyticsOptionsDropdownMenu(false)
                                }
                            )
                        }
                    }
                },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        floatingActionButton = {
            AnimatedVisibility(visible = playbackState != ExoPlayer.STATE_IDLE && playbackState != ExoPlayer.STATE_ENDED) {
                FloatingActionButton(onClick = { exoPlayer.stop() }) {
                    if (playbackState == ExoPlayer.STATE_BUFFERING) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(8.dp)
                                .scale(0.8f)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "停止播放"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 12.dp)
        ) {
            AnalyticsInputField(uiState, viewModel)
            ResultDisplayList(
                uiState,
                bestRecords,
                recentRecords,
                profileDetails,
                exoPlayer,
                playbackState,
                topAppBarScrollBehavior
            )
        }

        if (uiState.showBottomSheet.value) {
            BottomSheet(
                sheetState = sheetState,
                onDismissRequest = {
                    scope.launch {
                        sheetState.hide()
                        uiState.showBottomSheet.value = false
                    }
                },
                analyticsUIState = uiState,
                onUpdatePresetsList = {
                    scope.launch {
                        viewModel.extraPresets.value = LocalDataSource.getAllAnalyticsPresets()
                    }
                }
            )
        }
    }
}

@Composable
private fun AnalyticsInputField(
    uiState: AnalyticsUIState,
    viewModel: AnalyticsViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    AnimatedVisibility(
        modifier = Modifier.fillMaxWidth(),
        visible = !uiState.foldTextFiled,
        enter = expandIn(expandFrom = Alignment.TopCenter),
        exit = shrinkOut(shrinkTowards = Alignment.TopCenter)
    ) {
        Column {
            TextField(
                value = uiState.cytoidID,
                onValueChange = {
                    if (it.isValidCytoidID(checkLengthMin = false)) {
                        viewModel.setCytoidID(it)
                        viewModel.clearBestRecords()
                        viewModel.clearRecentRecords()
                        viewModel.clearProfileDetails()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = !uiState.cytoidID.isValidCytoidID() && uiState.cytoidID.isNotEmpty(),
                label = { Text(text = stringResource(id = R.string.cytoid_id)) },
                trailingIcon = {
                    Row(
                        modifier = Modifier.padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.setFoldTextFiled(true) }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = stringResource(R.string.fold_input_field)
                            )
                        }
                        IconButton(onClick = { viewModel.setExpandQueryOptionsDropdownMenu(true) }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.query_settings)
                            )
                            QuerySettingsDropDownMenu(uiState = uiState, viewModel = viewModel)
                        }
                        if (uiState.isQuerying) {
                            CircularProgressIndicator(modifier = Modifier.scale(0.8f))
                        } else {
                            TextButton(onClick = {
                                viewModel.setErrorMessage("")
                                if (!uiState.cytoidID.isValidCytoidID()) {
                                    viewModel.setErrorMessage(context.getString(R.string.invalid_cytoid_id))
                                } else if (uiState.queryCount.isEmpty()) {
                                    viewModel.setErrorMessage(context.getString(R.string.empty_query_count))
                                } else {
                                    scope.launch {  // 此处不进行线程转换，在viewmodel层中再转换到IO线程
                                        viewModel.setIsQuerying(true)
                                        viewModel.enqueueQuery()
                                        CytoidIdAutoFillUtils.saveCytoidId(uiState.cytoidID)
                                        uiState.autoFillList.value =
                                            CytoidIdAutoFillUtils.getSavedCytoidIds()
                                    }
                                }
                            }) {
                                Text(text = stringResource(id = R.string.query))
                            }
                        }
                    }
                }
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.autoFillList.value.filter { it.startsWith(uiState.cytoidID) }.sorted()
                    .forEach {
                        item {
                            InputChip(
                                selected = false,
                                label = { Text(text = it) },
                                onClick = {
                                    viewModel.setCytoidID(it)
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.remove),
                                        modifier = Modifier.clickable {
                                            CytoidIdAutoFillUtils.forgetCytoidId(it)
                                            uiState.autoFillList.value =
                                                CytoidIdAutoFillUtils.getSavedCytoidIds()
                                        }
                                    )
                                },
                                modifier = Modifier.wrapContentWidth()
                            )
                        }
                    }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuerySettingsDropDownMenu(uiState: AnalyticsUIState, viewModel: AnalyticsViewModel) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var extraPresets = viewModel.extraPresets
    var isRemovingPreset by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch {
            extraPresets.value = LocalDataSource.getAllAnalyticsPresets()
        }
    }

    DropdownMenu(
        modifier = Modifier
            .padding(MenuDefaults.DropdownMenuItemContentPadding)
            .widthIn(max = 480.dp),
        expanded = uiState.expandQueryOptionsDropdownMenu,
        onDismissRequest = { viewModel.setExpandQueryOptionsDropdownMenu(false) }
    ) {
        Text(
            text = stringResource(id = R.string.query_type),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        Row {
            FilterChip(
                selected = uiState.queryType == AnalyticsUIState.QueryType.BestRecords,
                onClick = { viewModel.setQueryType(AnalyticsUIState.QueryType.BestRecords) },
                label = { Text(text = AnalyticsUIState.QueryType.BestRecords.displayName) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = uiState.queryType == AnalyticsUIState.QueryType.RecentRecords,
                onClick = { viewModel.setQueryType(AnalyticsUIState.QueryType.RecentRecords) },
                label = { Text(text = AnalyticsUIState.QueryType.RecentRecords.displayName) }
            )
        }
        TextField(
            value = uiState.queryCount,
            onValueChange = {
                if (it.isDigitsOnly() && it.length <= 3) viewModel.setQueryCount(it)
            },
            isError = uiState.queryCount.isEmpty() or !uiState.queryCount.isDigitsOnly(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            label = { Text(text = stringResource(id = R.string.query_count)) }
        )
        AnimatedVisibility(visible = uiState.queryType == AnalyticsUIState.QueryType.RecentRecords) {
            Column {
                Text(
                    text = stringResource(R.string.sort_strategy),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 3
                ) {
                    AssistChip(
                        onClick = { viewModel.setQueryOrder(if (uiState.queryOrder == RecordQueryOrder.ASC) RecordQueryOrder.DESC else RecordQueryOrder.ASC) },
                        label = {
                            Text(
                                text = if (uiState.queryOrder == RecordQueryOrder.ASC) stringResource(
                                    R.string.asc
                                ) else stringResource(R.string.desc)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (uiState.queryOrder == RecordQueryOrder.ASC) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null
                            )
                        }
                    )
                    RecordQuerySort.entries.forEach { sort ->
                        FilterChip(
                            selected = uiState.querySort == sort,
                            onClick = { viewModel.setQuerySort(sort) },
                            label = { Text(text = sort.displayName) }
                        )
                    }
                }
            }
        }
        Text(
            text = stringResource(R.string.load_preset),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 3
        ) {
            Button(onClick = {
                viewModel.run {
                    setQueryType(AnalyticsUIState.QueryType.BestRecords)
                    setQueryCount("30")
                    setQueryOrder(RecordQueryOrder.DESC)
                    setQuerySort(RecordQuerySort.Rating)
                    setIgnoreLocalCacheData(false)
                    setKeep2DecimalPlaces(true)
                    setImageGenerationColumns("6")
                }
            }) {
                Text(text = stringResource(id = R.string.b30))
            }
            Button(onClick = {
                viewModel.run {
                    setQueryType(AnalyticsUIState.QueryType.RecentRecords)
                    setQueryCount("10")
                    setQuerySort(RecordQuerySort.RecentRating)
                    setQueryOrder(RecordQueryOrder.DESC)
                    setIgnoreLocalCacheData(false)
                    setKeep2DecimalPlaces(true)
                    setImageGenerationColumns("5")
                }
            }) {
                Text(text = stringResource(id = R.string.r10))
            }
            extraPresets.value.forEach { preset ->
                Button(
                    onClick = {
                        if (isRemovingPreset) {
                            scope.launch {
                                LocalDataSource.deleteAnalyticsPreset(preset.name)
                                extraPresets.value = LocalDataSource.getAllAnalyticsPresets()
                            }
                        } else {
                            viewModel.run {
                                setQueryType(AnalyticsUIState.QueryType.valueOf(preset.queryType))
                                setQueryCount(preset.queryCount.toString())
                                setQuerySort(RecordQuerySort.valueOf(preset.querySort))
                                setQueryOrder(RecordQueryOrder.valueOf(preset.queryOrder))
                                setIgnoreLocalCacheData(preset.ignoreLocalCacheData)
                                setKeep2DecimalPlaces(preset.keep2DecimalPlaces)
                                setImageGenerationColumns(preset.imageGenerationColumns.toString())
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isRemovingPreset) MaterialTheme.colorScheme.error else Color.Unspecified)
                ) {
                    Text(text = preset.name)
                }
            }
            OutlinedIconButton(onClick = {
                uiState.showBottomSheet.value = true
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
            OutlinedIconButton(onClick = {
                isRemovingPreset = !isRemovingPreset
            }) {
                Icon(
                    imageVector = if (isRemovingPreset) Icons.Default.Done else Icons.Default.Remove,
                    contentDescription = null
                )
            }
        }
        Text(
            text = stringResource(id = R.string.query_options),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
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
                        checked = uiState.ignoreLocalCacheData,
                        onCheckedChange = { viewModel.setIgnoreLocalCacheData(it) })
                }
            },
            onClick = { viewModel.setIgnoreLocalCacheData(!uiState.ignoreLocalCacheData) }
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
                        checked = uiState.keep2DecimalPlaces,
                        onCheckedChange = { viewModel.setKeep2DecimalPlaces(it) })
                }
            },
            onClick = { viewModel.setKeep2DecimalPlaces(!uiState.keep2DecimalPlaces) }
        )
        TextField(
            value = uiState.imageGenerationColumns,
            onValueChange = {
                if (it.isDigitsOnly() && it.length <= 3) viewModel.setImageGenerationColumns(it)
            },
            isError = uiState.imageGenerationColumns.isEmpty() or !uiState.imageGenerationColumns.isDigitsOnly(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            label = { Text(text = stringResource(id = R.string.columns_count)) },
            trailingIcon = {
                TextButton(
                    onClick = {
                        if (uiState.imageGenerationColumns.isEmpty()) {
                            context.getString(R.string.empty_columnsCount).showToast()
                            return@TextButton
                        }
                        context.getString(R.string.saving).showToast()
                        scope.launch {
                            viewModel.saveRecordsAsPicture()
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp),
                    enabled = !uiState.isGenerating
                ) {
                    Text(text = stringResource(id = R.string.save_as_picture))
                }
            }
        )
        AnimatedVisibility(visible = uiState.isGenerating) {
            LinearProgressIndicator(
                progress = {
                    uiState.generatingProgress.toFloat() / (uiState.queryCount.toIntOrNull()
                        ?: Int.MAX_VALUE)
                },
                modifier = Modifier.fillMaxWidth()
            )
            Text(text = "${uiState.generatingProgress}/${uiState.queryCount}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultDisplayList(
    uiState: AnalyticsUIState,
    bestRecords: BestRecords?,
    recentRecords: RecentRecords?,
    profileDetails: ProfileDetails?,
    exoPlayer: ExoPlayer,
    playbackState: Int,
    topAppBarScrollBehavior: TopAppBarScrollBehavior
) {
    val orientation = LocalConfiguration.current.orientation

    if (uiState.errorMessage.isNotEmpty()) {
        ErrorMessageCard(errorMessage = uiState.errorMessage)
    } else {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(
                MMKV.mmkvWithID(MMKVId.AppSettings.id).decodeInt(
                    if (orientation == Configuration.ORIENTATION_PORTRAIT) AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT.name
                    else AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_LANDSCAPE.name, 1
                )
            ),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            ),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
        ) {
            profileDetails?.let {
                item(span = StaggeredGridItemSpan.FullLine) {
                    UserDetailsHeader(
                        profileDetails = profileDetails,
                        keep2DecimalPlaces = uiState.keep2DecimalPlaces
                    )
                }
            }
            if (uiState.queryType == AnalyticsUIState.QueryType.BestRecords) {
                bestRecords?.data?.profile?.let { profile ->
                    var remainRecordsToShowCount =
                        uiState.queryCount.run { if (this.isNotEmpty() && this.isDigitsOnly()) this.toInt() else 0 }
                    for (i in 0 until profile.bestRecords.size) {
                        if (remainRecordsToShowCount <= 0) break
                        val record = profile.bestRecords[i]
                        item {
                            RecordCard(
                                cytoidId = uiState.cytoidID,
                                record = record,
                                recordIndex = i + 1,
                                keep2DecimalPlaces = uiState.keep2DecimalPlaces,
                                exoPlayer = exoPlayer,
                                playbackState = playbackState
                            )
                        }
                        remainRecordsToShowCount--
                    }
                }
            } else {
                recentRecords?.data?.profile?.let { profile ->
                    var remainRecordsToShowCount =
                        uiState.queryCount.run { if (this.isNotEmpty() && this.isDigitsOnly()) this.toInt() else 0 }
                    for (i in 0 until profile.recentRecords.size) {
                        if (remainRecordsToShowCount <= 0) break
                        val record = profile.recentRecords[i]
                        item {
                            RecordCard(
                                cytoidId = uiState.cytoidID,
                                record = record,
                                recordIndex = i + 1,
                                keep2DecimalPlaces = uiState.keep2DecimalPlaces,
                                exoPlayer = exoPlayer,
                                playbackState = playbackState
                            )
                        }
                        remainRecordsToShowCount--
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    analyticsUIState: AnalyticsUIState,
    onUpdatePresetsList: () -> Unit
) {
    var presetName by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    ModalBottomSheet(sheetState = sheetState, onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.save_preset),
                style = MaterialTheme.typography.titleLarge
            )
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = presetName,
                    onValueChange = {
                        presetName = it
                    },
                    label = {
                        Text(text = stringResource(R.string.preset_name))
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(onClick = {
                    if (presetName.isEmpty()) {
                        context.getString(R.string.preset_name_empty).showToast()
                        return@Button
                    }
                    val preset = com.lyneon.cytoidinfoquerier.data.model.local.AnalyticsPreset(
                        name = presetName,
                        queryType = analyticsUIState.queryType.name,
                        queryCount = analyticsUIState.queryCount.toInt(),
                        querySort = analyticsUIState.querySort.name,
                        queryOrder = analyticsUIState.queryOrder.name,
                        ignoreLocalCacheData = analyticsUIState.ignoreLocalCacheData,
                        keep2DecimalPlaces = analyticsUIState.keep2DecimalPlaces,
                        imageGenerationColumns = analyticsUIState.imageGenerationColumns.toInt()
                    )
                    scope.launch {
                        LocalDataSource.saveAnalyticsPreset(preset)
                        context.getString(R.string.saved).showToast()
                        onDismissRequest()
                        onUpdatePresetsList()
                    }
                }) { Text(text = stringResource(R.string.save)) }
            }
            Text(
                text = stringResource(R.string.preset_info),
                style = MaterialTheme.typography.titleSmall
            )
            Text(text = "查询类型 - ${analyticsUIState.queryType.name}")
            Text(text = "查询数量 - ${analyticsUIState.queryCount}")
            Text(text = "排序方式 - ${analyticsUIState.querySort.name}")
            Text(text = "排序顺序 - ${analyticsUIState.queryOrder.name}")
            Text(text = "忽略缓存数据 - ${analyticsUIState.ignoreLocalCacheData}")
            Text(text = "保留两位小数 - ${analyticsUIState.keep2DecimalPlaces}")
            Text(text = "图片生成列数 - ${analyticsUIState.imageGenerationColumns}")
        }
    }
}
