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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.MMKVKeys
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.BestRecords
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.RecentRecords
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.component.RecordCard
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.viewmodel.AnalyticsUIState
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.viewmodel.AnalyticsViewModel
import com.lyneon.cytoidinfoquerier.util.extension.isValidCytoidID
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val bestRecords by viewModel.bestRecords.collectAsState()
    val recentRecords by viewModel.recentRecords.collectAsState()
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
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(visible = playbackState != ExoPlayer.STATE_IDLE && playbackState != ExoPlayer.STATE_ENDED) {
                FloatingActionButton(onClick = { exoPlayer.stop() }) {
                    if (playbackState == ExoPlayer.STATE_BUFFERING) {
                        CircularProgressIndicator(modifier = Modifier.padding(8.dp))
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
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            AnalyticsInputField(uiState, viewModel)
            ResultDisplayList(
                uiState,
                bestRecords,
                recentRecords,
                exoPlayer,
                playbackState
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
            onValueChange = { viewModel.setCytoidID(it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = !uiState.cytoidID.isValidCytoidID() && uiState.cytoidID.isNotEmpty(),
            label = { Text(text = stringResource(id = R.string.cytoid_id)) },
            trailingIcon = {
                Row(
                    modifier = Modifier.padding(end = 8.dp),
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
                    TextButton(onClick = {
                        if (uiState.cytoidID.isEmpty()) {
                            viewModel.setErrorMessage(context.getString(R.string.empty_cytoid_id))
                        }
                        scope.launch { viewModel.enqueueQuery() }
                    }) {
                        Text(text = stringResource(id = R.string.query))
                    }
                }
            }
        )
    }
}

@Composable
private fun QuerySettingsDropDownMenu(uiState: AnalyticsUIState, viewModel: AnalyticsViewModel) {
    DropdownMenu(
        expanded = uiState.expandQueryOptionsDropdownMenu,
        onDismissRequest = { viewModel.setExpandQueryOptionsDropdownMenu(false) }
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
                        selected = uiState.queryType == AnalyticsUIState.QueryType.BEST_RECORDS,
                        onClick = { viewModel.setQueryType(AnalyticsUIState.QueryType.BEST_RECORDS) }
                    )
                }
            },
            onClick = { viewModel.setQueryType(AnalyticsUIState.QueryType.BEST_RECORDS) }
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
                        selected = uiState.queryType == AnalyticsUIState.QueryType.RECENT_RECORDS,
                        onClick = { viewModel.setQueryType(AnalyticsUIState.QueryType.RECENT_RECORDS) }
                    )
                }
            },
            onClick = { viewModel.setQueryType(AnalyticsUIState.QueryType.RECENT_RECORDS) }
        )
    }
}

@Composable
private fun ResultDisplayList(
    uiState: AnalyticsUIState,
    bestRecords: BestRecords?,
    recentRecords: RecentRecords?,
    exoPlayer: ExoPlayer,
    playbackState: Int
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
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (uiState.queryType == AnalyticsUIState.QueryType.BEST_RECORDS) {
                bestRecords?.data?.profile?.let { profile ->
                    var remainRecordsToShowCount = uiState.queryCount
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
                    var remainRecordsToShowCount = uiState.queryCount
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

@Composable
private fun ErrorMessageCard(errorMessage: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Error, contentDescription = null)
            Text(text = errorMessage)
        }
    }
}