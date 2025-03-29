package com.lyneon.cytoidinfoquerier.ui.compose.screen

import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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
import com.lyneon.cytoidinfoquerier.data.constant.SearchLevelCategory
import com.lyneon.cytoidinfoquerier.data.constant.SearchLevelOrder
import com.lyneon.cytoidinfoquerier.data.constant.SearchLevelSortingStrategy
import com.lyneon.cytoidinfoquerier.data.constant.SearchLevelSortingStrategy.Companion.displayName
import com.lyneon.cytoidinfoquerier.data.enums.AvatarSize
import com.lyneon.cytoidinfoquerier.data.enums.ImageSize
import com.lyneon.cytoidinfoquerier.data.model.webapi.SearchLevelsResult
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.lyneon.cytoidinfoquerier.ui.compose.component.DifficultyPillText
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
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun LevelScreen(
    viewModel: LevelViewModel = viewModel(),
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
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
    val sharedViewModel = viewModel<SharedViewModel>(LocalActivity.current as MainActivity)
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.level)) },
                actions = {
                    if (uiState.foldTextFiled) {
                        IconButton(onClick = { viewModel.setFoldTextFiled(false) }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.expand_input_field)
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            viewModel.randomLevel()
                        },
                        enabled = !uiState.isSearching
                    ) {
                        if (uiState.isSearching) {
                            CircularProgressIndicator(modifier = Modifier.scale(0.8f))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = stringResource(R.string.random_level)
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
                            contentDescription = stringResource(R.string.stop_playing)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 12.dp)
        ) {
            LevelInputField(uiState, viewModel)
            searchResult?.let {
                ResultDisplayList(
                    uiState,
                    it,
                    exoPlayer,
                    playbackState,
                    sharedViewModel,
                    navController,
                    sharedTransitionScope,
                    animatedContentScope,
                    topAppBarScrollBehavior
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
            label = { Text(text = stringResource(R.string.search_levels)) },
            placeholder = { Text(text = stringResource(R.string.search_levels_placeholder)) },
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
                    IconButton(onClick = { viewModel.setExpandSearchOptionsDropdownMenu(true) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.query_settings)
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
                                viewModel.setIsSearchByTag(false)
                                viewModel.searchLevels(resetPage = true)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.search)
                            )
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
            .padding(MenuDefaults.DropdownMenuItemContentPadding),
        expanded = uiState.expandSearchOptionsDropdownMenu,
        onDismissRequest = { viewModel.setExpandSearchOptionsDropdownMenu(false) }
    ) {
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
                    onClick = { viewModel.setQueryOrder(if (uiState.queryOrder == SearchLevelOrder.Ascending) SearchLevelOrder.Descending else SearchLevelOrder.Ascending) },
                    label = {
                        Text(
                            text = if (uiState.queryOrder == SearchLevelOrder.Ascending) stringResource(
                                R.string.asc
                            ) else stringResource(R.string.desc)
                        )
                    },
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
                        label = { Text(text = sort.displayName) }
                    )
                }
            }
            Text(
                text = stringResource(R.string.category),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.queryFeatured,
                    onClick = { viewModel.setQueryFeatured(!uiState.queryFeatured) },
                    label = { Text(text = stringResource(R.string.featured)) },
                    leadingIcon = {
                        AnimatedVisibility(visible = uiState.queryFeatured) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = null
                            )
                        }
                    }
                )
                FilterChip(
                    selected = uiState.queryQualified,
                    onClick = { viewModel.setQueryQualified(!uiState.queryQualified) },
                    label = { Text(text = stringResource(R.string.qualified)) },
                    leadingIcon = {
                        AnimatedVisibility(visible = uiState.queryQualified) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        }
        TextField(
            value = uiState.queryTag,
            label = { Text(stringResource(R.string.search_by_tag)) },
            onValueChange = {
                viewModel.setQueryTag(it)
            },
            trailingIcon = {
                TextButton(onClick = {
                    viewModel.setIsSearching(true)
                    viewModel.setIsSearchByTag(true)
                    viewModel.searchLevelsByTag(resetPage = true)
                }) {
                    Text(stringResource(R.string.search))
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ResultDisplayList(
    uiState: LevelUIState,
    searchResult: List<SearchLevelsResult>,
    exoPlayer: ExoPlayer,
    playbackState: Int,
    sharedViewModel: SharedViewModel,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    topAppBarScrollBehavior: TopAppBarScrollBehavior
) {
    if (uiState.errorMessage.isNotEmpty()) {
        ErrorMessageCard(errorMessage = uiState.errorMessage)
    } else if (searchResult.isEmpty()) {
        ErrorMessageCard(errorMessage = stringResource(R.string.no_result))
    } else {
        val columnsCount by remember {
            mutableIntStateOf(
                MMKV.mmkvWithID(MMKVId.AppSettings.id).decodeInt(
                    if (BaseApplication.context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT.name
                    else AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_LANDSCAPE.name, 1
                )
            )
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(columnsCount),
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding() + 8.dp
            ),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
        ) {
            searchResult.forEach { searchLevelResult ->
                item(key = searchLevelResult.id) {
                    LevelCard(
                        searchLevelResult = searchLevelResult,
                        exoPlayer = exoPlayer,
                        playbackState = playbackState,
                        sharedViewModel = sharedViewModel,
                        navController = navController,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedContentScope = animatedContentScope
                    )
                }
            }
            item(span = StaggeredGridItemSpan.FullLine) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        8.dp,
                        Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val viewModel = viewModel<LevelViewModel>()

                    IconButton(onClick = {
                        if (uiState.queryPage > 0) {
                            viewModel.setQueryPage(uiState.queryPage - 1)
                            if (uiState.isSearchByTag) {
                                viewModel.searchLevelsByTag()
                            } else {
                                viewModel.searchLevels()
                            }
                        }
                    }, enabled = uiState.queryPage > 0) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = stringResource(R.string.prev_page)
                        )
                    }
                    Text(
                        text = "${uiState.queryPage + 1}/${uiState.totalPages}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    IconButton(onClick = {
                        if (uiState.queryPage < uiState.totalPages - 1) {
                            viewModel.setQueryPage(uiState.queryPage + 1)
                            if (uiState.isSearchByTag) {
                                viewModel.searchLevelsByTag()
                            } else {
                                viewModel.searchLevels()
                            }
                        }
                    }, enabled = uiState.queryPage < uiState.totalPages - 1) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = stringResource(R.string.next_page)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun LevelCard(
    searchLevelResult: SearchLevelsResult,
    exoPlayer: ExoPlayer,
    playbackState: Int,
    sharedViewModel: SharedViewModel,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    with(sharedTransitionScope) {
        Card(
            modifier = Modifier
                .clickable {
                    sharedViewModel.sharedLevelForLevelDetailScreen =
                        searchLevelResult.toSharedLevel()
                    navController.navigate(MainActivity.Screen.LevelDetail.route) {
                        launchSingleTop = true
                        popUpTo(MainActivity.Screen.LevelDetail.route)
                    }
                }
                .sharedElement(
                    sharedTransitionScope.rememberSharedContentState("${searchLevelResult.id}_card"),
                    animatedContentScope
                )
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Box {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LevelBackgroundImage(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.6f)
                                .sharedBounds(
                                    sharedTransitionScope.rememberSharedContentState("${searchLevelResult.id}_backgroundImage"),
                                    animatedContentScope,
                                    clipInOverlayDuringTransition = sharedTransitionScope.OverlayClip(
                                        CardDefaults.shape
                                    )
                                ),
                            levelID = searchLevelResult.uid,
                            backgroundImageSize = ImageSize.Thumbnail,
                            remoteUrl = searchLevelResult.cover?.thumbnail
                        )
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        modifier = Modifier
                            .heightIn(max = 64.dp)
                            .fillMaxWidth()
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        visible = !sharedTransitionScope.isTransitionActive,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                    ) {
                        Row(
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
                                            CorneredShape.Pill.toComposeShape()
                                        )
                                        .padding(
                                            top = 4.dp,
                                            bottom = 4.dp,
                                            start = 4.dp,
                                            end = 8.dp
                                        )
                                ) {
                                    UserAvatar(
                                        size = 32.dp,
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
                    androidx.compose.animation.AnimatedVisibility(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp),
                        visible = !sharedTransitionScope.isTransitionActive,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        FlowRow(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            searchLevelResult.category.forEach { category ->
                                Text(
                                    text = category.replaceFirstChar { it.uppercaseChar() },
                                    maxLines = 1,
                                    color = Color.White,
                                    modifier = Modifier
                                        .background(
                                            Brush.linearGradient(
                                                when (category) {
                                                    SearchLevelCategory.Featured.value -> CytoidColors.featuredColor
                                                    SearchLevelCategory.Qualified.value -> CytoidColors.qualifiedColor
                                                    else -> CytoidColors.hardColor
                                                }
                                            ), RoundedCornerShape(CornerSize(100))
                                        )
                                        .padding(vertical = 4.dp, horizontal = 8.dp)
                                )
                            }
                        }
                    }
                }
                Column {
                    Text(
                        text = searchLevelResult.title,
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 2,
                        modifier = Modifier.sharedElement(
                            sharedTransitionScope.rememberSharedContentState("${searchLevelResult.id}_title"),
                            animatedContentScope
                        )
                    )
                    searchLevelResult.metadata.artist?.name?.let {
                        Text(
                            text = it,
                            maxLines = 1,
                            modifier = Modifier.sharedElement(
                                sharedTransitionScope.rememberSharedContentState("${searchLevelResult.id}_artist"),
                                animatedContentScope
                            )
                        )
                    }
                    ChartsRow(
                        searchLevelResult.charts,
                        levelID = searchLevelResult.id,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedContentScope
                    )
                    Text(
                        stringResource(
                            R.string.create_at,
                            DateParser.parseISO8601Date(searchLevelResult.creationDate)
                                .formatToTimeString()
                        ),
                        modifier = Modifier.sharedElement(
                            sharedTransitionScope.rememberSharedContentState("${searchLevelResult.id}_creationDate"),
                            animatedContentScope
                        )
                    )
                    Text(
                        stringResource(
                            R.string.update_at,
                            DateParser.parseISO8601Date(searchLevelResult.modificationDate)
                                .formatToTimeString()
                        ),
                        modifier = Modifier.sharedElement(
                            sharedTransitionScope.rememberSharedContentState("${searchLevelResult.id}_modificationDate"),
                            animatedContentScope
                        )
                    )
                    Text(
                        "${stringResource(R.string.downloads_count)}：${searchLevelResult.downloads}",
                        modifier = Modifier.sharedElement(
                            sharedTransitionScope.rememberSharedContentState("${searchLevelResult.id}_downloads"),
                            animatedContentScope
                        )
                    )
                    Text(
                        "${stringResource(R.string.plays_count)}：${searchLevelResult.plays}",
                        modifier = Modifier.sharedElement(
                            sharedTransitionScope.rememberSharedContentState("${searchLevelResult.id}_plays"),
                            animatedContentScope
                        )
                    )
                    Text(
                        searchLevelResult.uid,
                        modifier = Modifier.sharedElement(
                            sharedTransitionScope.rememberSharedContentState("${searchLevelResult.id}_uid"),
                            animatedContentScope
                        )
                    )
                }
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
                            MediaItem.Builder().setUri(musicPreviewUrl!!.toUri()).build()
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
                contentDescription = stringResource(R.string.play_music_preview)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun ChartsRow(
    charts: List<SearchLevelsResult.LevelChart>,
    modifier: Modifier = Modifier,
    levelID: Int,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        charts.sortedBy {
            when (it.difficultyType) {
                "easy" -> 0
                "hard" -> 1
                "extreme" -> 2
                else -> 3
            }
        }.forEach { chart ->
            with(sharedTransitionScope) {
                DifficultyPillText(
                    Modifier.sharedElement(
                        sharedTransitionScope.rememberSharedContentState(
                            "${levelID}_${chart.difficultyType}"
                        ), animatedVisibilityScope
                    ), chart.difficultyName,
                    chart.difficultyLevel,
                    chart.difficultyType
                )
            }
        }
    }
}