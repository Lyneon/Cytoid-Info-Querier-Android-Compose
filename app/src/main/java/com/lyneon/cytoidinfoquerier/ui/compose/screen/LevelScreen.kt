package com.lyneon.cytoidinfoquerier.ui.compose.screen

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.navigation.NavController
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.CytoidColors
import com.lyneon.cytoidinfoquerier.data.constant.SearchLevelOrder
import com.lyneon.cytoidinfoquerier.data.constant.SearchLevelSortingStrategy
import com.lyneon.cytoidinfoquerier.data.enums.AvatarSize
import com.lyneon.cytoidinfoquerier.data.enums.ImageSize
import com.lyneon.cytoidinfoquerier.data.model.shared.Level
import com.lyneon.cytoidinfoquerier.data.model.webapi.SearchLevelsResult
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.lyneon.cytoidinfoquerier.ui.compose.component.ErrorMessageCard
import com.lyneon.cytoidinfoquerier.ui.compose.component.LevelBackgroundImage
import com.lyneon.cytoidinfoquerier.ui.compose.component.UserAvatar
import com.lyneon.cytoidinfoquerier.ui.viewmodel.LevelUIState
import com.lyneon.cytoidinfoquerier.ui.viewmodel.LevelViewModel
import com.lyneon.cytoidinfoquerier.ui.viewmodel.SharedViewModel
import com.lyneon.cytoidinfoquerier.util.AppSettingsMMKVKeys
import com.lyneon.cytoidinfoquerier.util.DateParser
import com.lyneon.cytoidinfoquerier.util.DateParser.formatToTimeString
import com.lyneon.cytoidinfoquerier.util.MMKVId
import com.patrykandpatrick.vico.compose.common.shape.toComposeShape
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun LevelScreen(
    viewModel: LevelViewModel = viewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchResult by viewModel.searchResult.collectAsState()
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
    val sharedViewModel = viewModel<SharedViewModel>(LocalContext.current as MainActivity)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.level)) },
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
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            LevelInputField(uiState, viewModel)
            searchResult?.let {
                ResultDisplayList(
                    uiState,
                    it,
                    exoPlayer,
                    playbackState,
                    sharedViewModel,
                    navController
                )
            }
        }
    }
}

@Composable
private fun LevelInputField(uiState: LevelUIState, viewModel: LevelViewModel) {
    val scope = rememberCoroutineScope()

    AnimatedVisibility(
        modifier = Modifier.fillMaxWidth(),
        visible = !uiState.foldTextFiled,
        enter = expandIn(expandFrom = Alignment.TopCenter),
        exit = shrinkOut(shrinkTowards = Alignment.TopCenter)
    ) {
        TextField(
            value = uiState.searchQuery,
            onValueChange = {
                viewModel.setSearchQuery(it)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(text = "搜索关卡") },
            placeholder = { Text(text = "曲名/曲师/谱师/标签...") },
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
                    IconButton(onClick = { viewModel.setExpandSearchOptionsDropdownMenu(true) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "查询设置"
                        )
                        SearchSettingsDropDownMenu(uiState = uiState, viewModel = viewModel)
                    }
                    if (uiState.isSearching) {
                        CircularProgressIndicator(modifier = Modifier.scale(0.8f))
                    } else {
                        IconButton(onClick = {
                            viewModel.setErrorMessage("")
                            scope.launch {  // 此处不进行线程转换，在viewmodel层中再转换到IO线程
                                viewModel.setIsSearching(true)
                                viewModel.enqueueSearch()
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "搜索")
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchSettingsDropDownMenu(uiState: LevelUIState, viewModel: LevelViewModel) {
    DropdownMenu(
        modifier = Modifier
            .padding(MenuDefaults.DropdownMenuItemContentPadding)
            .animateContentSize(),
        expanded = uiState.expandSearchOptionsDropdownMenu,
        onDismissRequest = { viewModel.setExpandSearchOptionsDropdownMenu(false) }
    ) {
        Column {
            Text(
                text = "排序依据",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                AssistChip(
                    onClick = { viewModel.setQueryOrder(if (uiState.queryOrder == SearchLevelOrder.Ascending) SearchLevelOrder.Descending else SearchLevelOrder.Ascending) },
                    label = { Text(text = if (uiState.queryOrder == SearchLevelOrder.Ascending) "升序" else "降序") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (uiState.queryOrder == SearchLevelOrder.Ascending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null
                        )
                    }
                )
                SearchLevelSortingStrategy.entries.forEach { sort ->
                    FilterChip(
                        selected = uiState.querySortStrategy == sort,
                        onClick = { viewModel.setQuerySortStrategy(sort) },
                        label = { Text(text = sort.displayName) },
                        leadingIcon = {
                            AnimatedVisibility(visible = uiState.querySortStrategy == sort) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ResultDisplayList(
    uiState: LevelUIState,
    searchResult: List<SearchLevelsResult>,
    exoPlayer: ExoPlayer,
    playbackState: Int,
    sharedViewModel: SharedViewModel,
    navController: NavController
) {
    if (uiState.errorMessage.isNotEmpty()) {
        ErrorMessageCard(errorMessage = uiState.errorMessage)
    } else if (searchResult.isEmpty()) {
        ErrorMessageCard(errorMessage = "无结果")
    } else {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(
                MMKV.mmkvWithID(MMKVId.AppSettings.id).decodeInt(
                    if (BaseApplication.context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT.name
                    else AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_LANDSCAPE.name, 1
                )
            ),
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding() + 8.dp
            ),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            searchResult.forEach { searchLevelResult ->
                item(key = searchLevelResult.uid) {
                    LevelCard(
                        searchLevelResult = searchLevelResult,
                        exoPlayer = exoPlayer,
                        playbackState = playbackState,
                        sharedViewModel = sharedViewModel,
                        navController = navController
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun LevelCard(
    searchLevelResult: SearchLevelsResult,
    exoPlayer: ExoPlayer,
    playbackState: Int,
    sharedViewModel: SharedViewModel,
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .clickable {
                sharedViewModel.sharedLevelForLevelDetailScreen = Level(
                    title = searchLevelResult.title,
                    uid = searchLevelResult.uid,
                    description = searchLevelResult.description,
                    artist = searchLevelResult.metadata.artist?.name,
                    charter = searchLevelResult.metadata.charter?.name,
                    illustrator = searchLevelResult.metadata.illustrator?.name,
                    storyboarder = searchLevelResult.metadata.storyboarder?.name,
                    musicURL = searchLevelResult.music,
                    musicPreviewURL = searchLevelResult.musicPreview,
                    charts = searchLevelResult.charts.map {
                        Level.Chart(
                            it.difficultyLevel,
                            it.difficultyType,
                            it.difficultyName,
                            it.notesCount
                        )
                    },
                    coverRemoteURL = searchLevelResult.cover?.original
                )
                navController.navigate(MainActivity.Screen.LevelDetail.route) {
                    launchSingleTop = true
                    popUpTo(MainActivity.Screen.LevelDetail.route)
                }
            }
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Box {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LevelBackgroundImage(
                        modifier = Modifier.fillMaxWidth(),
                        levelID = searchLevelResult.uid,
                        backgroundImageSize = ImageSize.Cover,
                        remoteUrl = searchLevelResult.cover?.cover
                    )
                }
                Row(
                    modifier = Modifier
                        .heightIn(max = 64.dp)
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    searchLevelResult.owner?.let { levelOwner ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .animateContentSize()
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    Shape.Pill.toComposeShape()
                                )
                                .padding(4.dp)
                        ) {
                            UserAvatar(
                                modifier = Modifier.sizeIn(maxHeight = 32.dp, maxWidth = 32.dp),
                                userUid = levelOwner.uid ?: levelOwner.id,
                                avatarSize = AvatarSize.Small,
                                remoteAvatarUrl = levelOwner.avatar.small ?: ""
                            )
                            Text(
                                text = levelOwner.uid ?: levelOwner.id,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    MusicPreviewButton(
                        exoPlayer = exoPlayer,
                        playbackState = playbackState,
                        musicPreviewUrl = searchLevelResult.musicPreview
                            ?: searchLevelResult.music
                    )
                }
            }
            Column {
                TitleText(
                    searchLevelResult.title,
                    searchLevelResult.metadata.artist?.name
                )
                ChartsRow(searchLevelResult.charts)
                Text(
                    "创建于${
                        DateParser.parseISO8601Date(searchLevelResult.creationDate)
                            .formatToTimeString()
                    }"
                )
                Text(
                    "上次修改于${
                        DateParser.parseISO8601Date(searchLevelResult.modificationDate)
                            .formatToTimeString()
                    }"
                )
                Text("下载次数：${searchLevelResult.downloads}")
                Text("游玩次数：${searchLevelResult.plays}")
                Text(searchLevelResult.uid)
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun MusicPreviewButton(
    exoPlayer: ExoPlayer,
    playbackState: Int,
    musicPreviewUrl: String?
) {
    AnimatedVisibility(visible = musicPreviewUrl != null && (playbackState == ExoPlayer.STATE_IDLE || playbackState == ExoPlayer.STATE_ENDED)) {
        IconButton(
            onClick = {
                exoPlayer.apply {
                    setMediaSource(
                        ProgressiveMediaSource.Factory(
                            DefaultHttpDataSource.Factory()
                                .setDefaultRequestProperties(mapOf("User-Agent" to "CytoidClient/2.1.1"))
                        ).createMediaSource(
                            MediaItem.Builder().setUri(Uri.parse(musicPreviewUrl)).build()
                        )
                    )
                    prepare()
                    play()
                }
            },
            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "播放音乐预览"
            )
        }
    }
}

@Composable
private fun TitleText(levelTitle: String, levelArtistName: String?) {
    Column {
        Text(
            text = levelTitle,
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 2
        )
        levelArtistName?.let { Text(text = it, maxLines = 1) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChartsRow(charts: List<SearchLevelsResult.LevelChart>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        charts.sortedBy { it.difficultyLevel }.forEach { chart ->
            DifficultyPillText(chart.difficultyName, chart.difficultyLevel, chart.difficultyType)
        }
    }
}

@Composable
private fun DifficultyPillText(
    difficultyName: String?,
    difficultyLevel: Int,
    difficultyType: String
) {
    Text(
        text = " ${
            difficultyName
                ?: difficultyType.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.getDefault()
                    ) else it.toString()
                }
        } $difficultyLevel ",
        maxLines = 1,
        color = Color.White,
        modifier = Modifier
            .background(
                Brush.linearGradient(
                    when (difficultyType) {
                        "easy" -> CytoidColors.easyColor
                        "extreme" -> CytoidColors.extremeColor
                        else -> CytoidColors.hardColor
                    }
                ), RoundedCornerShape(CornerSize(100))
            )
            .padding(6.dp)
    )
}