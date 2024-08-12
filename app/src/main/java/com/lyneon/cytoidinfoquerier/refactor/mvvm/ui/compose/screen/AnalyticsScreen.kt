package com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.screen

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.MMKVKeys
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.BestRecords
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.RecentRecords
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.webapi.ProfileDetails
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.activity.MainActivity
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.component.ErrorMessageCard
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.component.RecordCard
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.component.UserDetailsHeader
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.viewmodel.AnalyticsUIState
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.viewmodel.AnalyticsViewModel
import com.lyneon.cytoidinfoquerier.util.extension.isValidCytoidID
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = viewModel(),
    navController: NavController,
    navBackStackEntry: NavBackStackEntry,
    withInitials: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val bestRecords by viewModel.bestRecords.collectAsState()
    val recentRecords by viewModel.recentRecords.collectAsState()
    val profileDetails by viewModel.profileDetails.collectAsState()
    var playbackState by remember { mutableIntStateOf(ExoPlayer.STATE_IDLE) }
    var isPlaying by remember { mutableStateOf(false) }
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

    if (withInitials) {
        val initialCytoidID = navBackStackEntry.arguments?.getString("initialCytoidID")
        val initialCacheType = navBackStackEntry.arguments?.getString("initialCacheType")
        val initialCacheTime = navBackStackEntry.arguments?.getString("initialCacheTime")?.toLong()
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
                }
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
                paddingValues.calculateBottomPadding()
            )
        }
    }
}

@Composable
private fun AnalyticsInputField(uiState: AnalyticsUIState, viewModel: AnalyticsViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    AnimatedVisibility(visible = !uiState.foldTextFiled) {
        TextField(
            value = uiState.cytoidID,
            onValueChange = { if (it.length <= 16) viewModel.setCytoidID(it) },
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
                            contentDescription = "折叠输入框"
                        )
                    }
                    IconButton(onClick = { viewModel.setExpandQueryOptionsDropdownMenu(true) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "查询设置"
                        )
                        QuerySettingsDropDownMenu(uiState = uiState, viewModel = viewModel)
                    }
                    if (uiState.isQuerying) {
                        CircularProgressIndicator(modifier = Modifier.scale(0.8f))
                    } else {
                        TextButton(onClick = {
                            viewModel.setErrorMessage("")
                            if (uiState.cytoidID.isEmpty()) {
                                viewModel.setErrorMessage(context.getString(R.string.empty_cytoid_id))
                            } else {
                                scope.launch {
                                    try {
                                        viewModel.setIsQuerying(true)
                                        viewModel.enqueueQuery()
                                    } catch (e: Exception) {
                                        viewModel.setErrorMessage(e.message.toString())
                                    }
                                }
                            }
                        }) {
                            Text(text = stringResource(id = R.string.query))
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun QuerySettingsDropDownMenu(uiState: AnalyticsUIState, viewModel: AnalyticsViewModel) {
    val scope = rememberCoroutineScope()

    DropdownMenu(
        expanded = uiState.expandQueryOptionsDropdownMenu,
        onDismissRequest = { viewModel.setExpandQueryOptionsDropdownMenu(false) }
    ) {
        Text(
            text = stringResource(id = R.string.query_type),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(MenuDefaults.DropdownMenuItemContentPadding)
        )
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = AnalyticsUIState.QueryType.BestRecords.displayName)
                    RadioButton(
                        selected = uiState.queryType == AnalyticsUIState.QueryType.BestRecords,
                        onClick = { viewModel.setQueryType(AnalyticsUIState.QueryType.BestRecords) }
                    )
                }
            },
            onClick = { viewModel.setQueryType(AnalyticsUIState.QueryType.BestRecords) }
        )
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = AnalyticsUIState.QueryType.RecentRecords.displayName)
                    RadioButton(
                        selected = uiState.queryType == AnalyticsUIState.QueryType.RecentRecords,
                        onClick = { viewModel.setQueryType(AnalyticsUIState.QueryType.RecentRecords) }
                    )
                }
            },
            onClick = { viewModel.setQueryType(AnalyticsUIState.QueryType.RecentRecords) }
        )
        TextField(
            modifier = Modifier.padding(8.dp),
            value = uiState.queryCount,
            onValueChange = {
                if (it.isDigitsOnly() && it.length <= 3) viewModel.setQueryCount(it)
            },
            isError = uiState.queryCount.isEmpty() or !uiState.queryCount.isDigitsOnly(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            label = { Text(text = stringResource(id = R.string.query_count)) }
        )
        Text(
            text = stringResource(id = R.string.query_options),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(MenuDefaults.DropdownMenuItemContentPadding)
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
            modifier = Modifier.padding(8.dp),
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
                            "列数不能为空".showToast()
                            return@TextButton
                        }
                        "正在保存图片".showToast()
                        scope.launch {
                            viewModel.saveRecordsAsPicture()
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(text = stringResource(id = R.string.save_as_picture))
                }
            }
        )
    }
}

@Composable
private fun ResultDisplayList(
    uiState: AnalyticsUIState,
    bestRecords: BestRecords?,
    recentRecords: RecentRecords?,
    profileDetails: ProfileDetails?,
    exoPlayer: ExoPlayer,
    playbackState: Int,
    bottomPadding: Dp
) {
    if (uiState.errorMessage.isNotEmpty()) {
        ErrorMessageCard(errorMessage = uiState.errorMessage)
    } else {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(
                MMKV.defaultMMKV().decodeInt(
                    if (BaseApplication.context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) MMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT.name
                    else MMKVKeys.GRID_COLUMNS_COUNT_LANDSCAPE.name, 1
                )
            ),
            contentPadding = PaddingValues(top = 8.dp, bottom = bottomPadding),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            profileDetails?.let {
                item {
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
