package com.lyneon.cytoidinfoquerier.ui.compose.screen

import android.content.Intent
import android.text.Layout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.CytoidDeepLink
import com.lyneon.cytoidinfoquerier.data.enums.ImageSize
import com.lyneon.cytoidinfoquerier.data.model.graphql.ProfileGraphQL
import com.lyneon.cytoidinfoquerier.data.model.webapi.ProfileComment
import com.lyneon.cytoidinfoquerier.data.model.webapi.ProfileDetails
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.lyneon.cytoidinfoquerier.ui.compose.component.CollectionCoverImage
import com.lyneon.cytoidinfoquerier.ui.compose.component.DifficultyPillText
import com.lyneon.cytoidinfoquerier.ui.compose.component.ErrorMessageCard
import com.lyneon.cytoidinfoquerier.ui.compose.component.LevelBackgroundImage
import com.lyneon.cytoidinfoquerier.ui.compose.component.UserAvatar
import com.lyneon.cytoidinfoquerier.ui.compose.component.UserDetailsHeader
import com.lyneon.cytoidinfoquerier.ui.viewmodel.ProfileUiState
import com.lyneon.cytoidinfoquerier.ui.viewmodel.ProfileViewModel
import com.lyneon.cytoidinfoquerier.util.AppSettingsMMKVKeys
import com.lyneon.cytoidinfoquerier.util.DateParser
import com.lyneon.cytoidinfoquerier.util.DateParser.formatToTimeString
import com.lyneon.cytoidinfoquerier.util.MMKVId
import com.lyneon.cytoidinfoquerier.util.extension.isValidCytoidID
import com.lyneon.cytoidinfoquerier.util.extension.saveIntoMediaStore
import com.lyneon.cytoidinfoquerier.util.extension.setPrecision
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import com.lyneon.cytoidinfoquerier.util.extension.toBitmap
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.fixed
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shadow
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.compose.common.shape.markerCorneredShape
import com.patrykandpatrick.vico.compose.common.shape.toComposeShape
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerMargins
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.LayeredComponent
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.component.Shadow
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.tencent.mmkv.MMKV
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    navController: NavController,
    navBackStackEntry: NavBackStackEntry,
    withInitials: Boolean = false,
    withShortcut: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val profileScreenDataModel by viewModel.profileScreenDataModel.collectAsState()
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
    var shortcutLoaded by rememberSaveable { mutableStateOf(false) }
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val nestedScrollConnection = topAppBarScrollBehavior.nestedScrollConnection

    LaunchedEffect(Unit) {
        if (withInitials && !initialLoaded) {
            launch {
                val initialCytoidID = navBackStackEntry.arguments?.getString("initialCytoidID")
                val initialCacheTime =
                    navBackStackEntry.arguments?.getString("initialCacheTime")?.toLong()
                if (initialCytoidID != null && initialCacheTime != null) {
                    viewModel.setCytoidID(initialCytoidID)
                    viewModel.loadSpecificCacheProfileScreenDataModel(initialCacheTime)
                    viewModel.setFoldTextFiled(true)
                }
                initialLoaded = true
            }
        }
        if (withShortcut && !shortcutLoaded) {
            launch {
                val appUserID =
                    MMKV.mmkvWithID(MMKVId.AppSettings.id)
                        .decodeString(AppSettingsMMKVKeys.APP_USER_CYTOID_ID.name)
                if (appUserID != null) {
                    viewModel.setCytoidID(appUserID)
                }
                awaitFrame()
                if (uiState.canQuery()) {
                    viewModel.enqueueQuery()
                }
                shortcutLoaded = true
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
            TopBar(uiState, viewModel, navController, topAppBarScrollBehavior)
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
            ProfileInputField(uiState, viewModel)
            if (uiState.errorMessage.isNotEmpty()) {
                ErrorMessageCard(errorMessage = uiState.errorMessage)
            } else if (!uiState.isQuerying) {
                profileScreenDataModel?.let {
                    ResultDisplayColumn(
                        uiState,
                        it.profileDetails,
                        it.profileGraphQL,
                        it.commentList,
                        exoPlayer,
                        playbackState,
                        nestedScrollConnection
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TopBar(
    uiState: ProfileUiState,
    viewModel: ProfileViewModel,
    navController: NavController,
    topAppBarScrollBehavior: TopAppBarScrollBehavior
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = R.string.profile)) },
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
                            navController.navigate(MainActivity.Screen.ProfileHistory.route)
                            viewModel.setExpandAnalyticsOptionsDropdownMenu(false)
                        }
                    )
                }
            }
        },
        scrollBehavior = topAppBarScrollBehavior
    )
}

@Composable
private fun ProfileInputField(uiState: ProfileUiState, viewModel: ProfileViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    AnimatedVisibility(
        modifier = Modifier.fillMaxWidth(),
        visible = !uiState.foldTextFiled,
        enter = expandIn(expandFrom = Alignment.TopCenter),
        exit = shrinkOut(shrinkTowards = Alignment.TopCenter)
    ) {
        TextField(
            value = uiState.cytoidID,
            onValueChange = {
                if (it.isValidCytoidID(checkLengthMin = false)) {
                    viewModel.setCytoidID(it)
                    viewModel.clearProfileScreenDataModel()
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
                            } else {
                                scope.launch {  // 此处不进行线程转换，在viewmodel层中再转换到IO线程
                                    viewModel.setIsQuerying(true)
                                    viewModel.enqueueQuery()
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
private fun QuerySettingsDropDownMenu(uiState: ProfileUiState, viewModel: ProfileViewModel) {
    DropdownMenu(
        expanded = uiState.expandQueryOptionsDropdownMenu,
        onDismissRequest = { viewModel.setExpandQueryOptionsDropdownMenu(false) }
    ) {
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
    }
}

@Composable
private fun ResultDisplayColumn(
    uiState: ProfileUiState,
    profileDetails: ProfileDetails?,
    profileGraphQL: ProfileGraphQL?,
    profileCommentList: List<ProfileComment>?,
    exoPlayer: ExoPlayer,
    playbackState: Int,
    topAppBarNestedScrollConnection: NestedScrollConnection
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .nestedScroll(topAppBarNestedScrollConnection),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(
            top = 8.dp,
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        )
    ) {
        profileDetails?.let {
            item {
                UserDetailsHeader(
                    profileDetails = it,
                    keep2DecimalPlaces = uiState.keep2DecimalPlaces
                )
            }
        }
        profileGraphQL?.let {
            item { BiographyCard(profileGraphQL = it) }
            item { BadgesCard(profileGraphQL = profileGraphQL) }
            /*
            item {
                RecentRecordsCard(
                    profileGraphQL = profileGraphQL,
                    keep2DecimalPlace = uiState.keep2DecimalPlaces
                )
            }
            */
        }
        profileDetails?.let {
            item {
                DetailsCard(
                    profileDetails = it,
                    keep2DecimalPlace = uiState.keep2DecimalPlaces
                )
            }
        }
        profileGraphQL?.let {
            item {
                CollectionsCard(profileGraphQL = it)
            }
            item {
                LevelsCard(
                    profileGraphQL = profileGraphQL,
                    exoPlayer = exoPlayer,
                    playbackState = playbackState
                )
            }
        }
        profileCommentList?.let { item { CommentList(commentList = it) } }
    }
}

@Composable
private fun BiographyCard(profileGraphQL: ProfileGraphQL) {
    profileGraphQL.data.profile?.let { profile ->
        Card(
            Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(8.dp)
            ) {
                profile.user?.registrationDate?.let { registrationDate ->
                    Row {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null
                        )
                        Text(
                            text = stringResource(
                                R.string.registration_date,
                                DateParser.parseISO8601Date(registrationDate)
                                    .formatToTimeString(),
                                (System.currentTimeMillis() - DateParser.parseISO8601Date(
                                    registrationDate
                                ).time).milliseconds.inWholeDays
                            )
                        )
                    }
                }
                if (!profile.bio.isNullOrEmpty()) {
                    var folded by rememberSaveable { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.biography),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        FoldExpandButton(folded) { folded = !folded }
                    }
                    AnimatedVisibility(visible = !folded) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            MarkdownText(markdown = profile.bio, isTextSelectable = true)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BadgesCard(profileGraphQL: ProfileGraphQL) {
    profileGraphQL.data.profile?.let { profile ->
        Card {
            Column(
                Modifier.padding(8.dp)
            ) {
                var folded by rememberSaveable { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${stringResource(R.string.badge)}（${profile.badges.size}）",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    FoldExpandButton(folded) { folded = !folded }
                }
                AnimatedVisibility(visible = !folded) {
                    SelectionContainer {
                        FlowRow(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            profile.badges.forEach {
                                OutlinedCard {
                                    Column(
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Text(
                                            text = it.title,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        it.description?.let { it1 ->
                                            HorizontalDivider()
                                            Text(text = it1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailsCard(profileDetails: ProfileDetails, keep2DecimalPlace: Boolean) {
    Card(
        Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row {
                Column(
                    Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.total_plays))
                    Text(
                        text = profileDetails.activities.totalRankedPlays.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.total_note_count))
                    Text(
                        text = profileDetails.activities.clearedNotes.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Row {
                Column(
                    Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.max_combo))
                    Text(
                        text = profileDetails.activities.maxCombo.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.average_accuracy))
                    Text(
                        text = "${
                            (profileDetails.activities.averageRankedAccuracy * 100).run {
                                if (keep2DecimalPlace) setPrecision(2)
                                else this
                            }
                        }%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Row {
                Column(
                    Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.total_score))
                    Text(
                        text = profileDetails.activities.totalRankedScore.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    Modifier.weight(1f)
                ) {
                    val duration =
                        (profileDetails.activities.totalPlayTime * 1000).toLong().milliseconds
                    val days = duration.inWholeDays
                    val hours = duration.inWholeHours - duration.inWholeDays * 24
                    val minutes = duration.inWholeMinutes - duration.inWholeHours * 60
                    val seconds = duration.inWholeSeconds - duration.inWholeMinutes * 60

                    Text(text = stringResource(R.string.total_play_time))
                    Text(
                        text = (if (days != 0.toLong()) "${days}${stringResource(R.string.day)}" else "") +
                                (if (hours != 0.toLong()) "${hours}${stringResource(R.string.hour)}" else "") +
                                (if (minutes != 0.toLong()) "${minutes}${stringResource(R.string.minute)}" else "") +
                                (if (seconds != 0.toLong()) "${seconds}${stringResource(R.string.second)}" else ""),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = stringResource(R.string.grade_distribution),
                style = MaterialTheme.typography.titleLarge
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "MAX ${profileDetails.grade.MAX}",
                    color = Color.Black,
                    modifier = Modifier
                        .background(Color(0xFFFFCC00), RoundedCornerShape(100))
                        .padding(horizontal = 8.dp)
                )
                Text(
                    text = "SSS ${profileDetails.grade.SSS}",
                    color = Color.Black,
                    modifier = Modifier
                        .background(Color(0xFF08CFFF), RoundedCornerShape(100))
                        .padding(horizontal = 8.dp)
                )
                Text(
                    text = "SS ${profileDetails.grade.SS}",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF3F4561), RoundedCornerShape(100))
                        .padding(horizontal = 8.dp)
                )
                Text(
                    text = "S ${profileDetails.grade.S}",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF3F4561), RoundedCornerShape(100))
                        .padding(horizontal = 8.dp)
                )
                Text(
                    text = "AA ${profileDetails.grade.AA}",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF3F4561), RoundedCornerShape(100))
                        .padding(horizontal = 8.dp)
                )
                Text(
                    text = "A ${profileDetails.grade.A}",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF3F4561), RoundedCornerShape(100))
                        .padding(horizontal = 8.dp)
                )
                Text(
                    text = "B ${profileDetails.grade.B}",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF3F4561), RoundedCornerShape(100))
                        .padding(horizontal = 8.dp)
                )
                Text(
                    text = "C ${profileDetails.grade.C}",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF3F4561), RoundedCornerShape(100))
                        .padding(horizontal = 8.dp)
                )
                Text(
                    text = "D ${profileDetails.grade.D}",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF3F4561), RoundedCornerShape(100))
                        .padding(horizontal = 8.dp)
                )
                Text(
                    text = "F ${profileDetails.grade.F}",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF3F4561), RoundedCornerShape(100))
                        .padding(horizontal = 8.dp)
                )
            }
            DetailsChart(
                dataTimeSeries = profileDetails.timeSeries,
                keep2DecimalPlace = keep2DecimalPlace
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsChart(
    dataTimeSeries: List<ProfileDetails.TimeSeriesItem>,
    keep2DecimalPlace: Boolean = true
) {
    // vico示例项目中的点标志
    class ChartMarker {
        private val LABEL_BACKGROUND_SHADOW_RADIUS_DP = 4f
        private val LABEL_BACKGROUND_SHADOW_DY_DP = 2f
        private val CLIPPING_FREE_SHADOW_RADIUS_MULTIPLIER = 1.4f

        @Composable
        fun rememberMarker(
            labelPosition: DefaultCartesianMarker.LabelPosition = DefaultCartesianMarker.LabelPosition.Top,
            showIndicator: Boolean = true,
            valueFormatter: DefaultCartesianMarker.ValueFormatter = DefaultCartesianMarker.ValueFormatter.default()
        ): CartesianMarker {
            val mLabelBackgroundShape = markerCorneredShape(CorneredShape.Pill)
            val mLabelBackground =
                rememberShapeComponent(
                    fill = fill(MaterialTheme.colorScheme.surfaceBright),
                    shape = mLabelBackgroundShape,
                    shadow = shadow(
                        radius = LABEL_BACKGROUND_SHADOW_RADIUS_DP.dp,
                        y = LABEL_BACKGROUND_SHADOW_DY_DP.dp,
                    ),
                )
            val mLabel =
                rememberTextComponent(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlignment = Layout.Alignment.ALIGN_CENTER,
                    padding = insets(8.dp, 4.dp),
                    background = mLabelBackground,
                    minWidth = TextComponent.MinWidth.fixed(40.dp),
                )
            val mIndicatorFrontComponent =
                rememberShapeComponent(fill(MaterialTheme.colorScheme.surface), CorneredShape.Pill)
            val mIndicatorCenterComponent = rememberShapeComponent(shape = CorneredShape.Pill)
            val mIndicatorRearComponent = rememberShapeComponent(shape = CorneredShape.Pill)
            val mIndicator =
                LayeredComponent(
                    back = mIndicatorRearComponent,
                    front =
                        LayeredComponent(
                            back = mIndicatorCenterComponent,
                            front = mIndicatorFrontComponent,
                            padding = insets(5.dp),
                        ),
                    padding = insets(10.dp),
                )
            val mGuideline = rememberAxisGuidelineComponent()
            return remember(mLabel, labelPosition, mIndicator, showIndicator, mGuideline) {
                object :
                    DefaultCartesianMarker(
                        label = mLabel,
                        labelPosition = labelPosition,
                        indicator =
                            if (showIndicator) {
                                { color ->
                                    LayeredComponent(
                                        back = ShapeComponent(
                                            fill(Color(color).copy(alpha = 0.15f)),
                                            CorneredShape.Pill
                                        ),
                                        front =
                                            LayeredComponent(
                                                back = ShapeComponent(
                                                    fill = fill(Color(color)),
                                                    shape = CorneredShape.Pill,
                                                    shadow = Shadow(radiusDp = 12f, color = color),
                                                ),
                                                front = mIndicatorFrontComponent,
                                                padding = insets(5.dp),
                                            ),
                                        padding = insets(10.dp),
                                    )
                                }
                            } else null,
                        indicatorSizeDp = 36f,
                        guideline = mGuideline,
                        valueFormatter = valueFormatter
                    ) {
                    override fun updateLayerMargins(
                        context: CartesianMeasuringContext,
                        layerMargins: CartesianLayerMargins,
                        layerDimensions: CartesianLayerDimensions,
                        model: CartesianChartModel
                    ) {
                        with(context) {
                            val baseShadowInsetDp =
                                CLIPPING_FREE_SHADOW_RADIUS_MULTIPLIER * LABEL_BACKGROUND_SHADOW_RADIUS_DP
                            var topInset =
                                (baseShadowInsetDp - LABEL_BACKGROUND_SHADOW_DY_DP).pixels
                            var bottomInset =
                                (baseShadowInsetDp + LABEL_BACKGROUND_SHADOW_DY_DP).pixels
                            when (labelPosition) {
                                LabelPosition.Top,
                                LabelPosition.AbovePoint -> topInset += mLabel.getHeight(context) + tickSizeDp.pixels

                                LabelPosition.Bottom -> bottomInset += mLabel.getHeight(context) + tickSizeDp.pixels
                                LabelPosition.AroundPoint -> {}
                                LabelPosition.BelowPoint -> {}
                            }
                            layerMargins.ensureValuesAtLeast(top = topInset, bottom = bottomInset)
                        }
                    }
                }
            }
        }
    }

    // 当前查看的数据集
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    // 数据集映射
    val timeSeries =
        remember { dataTimeSeries.apply { sortedBy { it.date.replace("-", "").toInt() } } }
    val ratingSeries =
        remember { timeSeries.map { it.rating.apply { if (keep2DecimalPlace) setPrecision(2).toDouble() } } }
    val countSeries = remember { timeSeries.map { it.count } }
    val accuracySeries = remember {
        timeSeries.map {
            (it.accuracy * 100).apply {
                if (keep2DecimalPlace) setPrecision(2).toDouble()
            }
        }
    }

    val cartesianChartModelProducer by remember { mutableStateOf(CartesianChartModelProducer()) }
    val context = LocalContext.current

    // 更新展示的数据集
    LaunchedEffect(tabIndex) {
        if (timeSeries.isEmpty()) return@LaunchedEffect
        withContext(Dispatchers.Default) {
            cartesianChartModelProducer.runTransaction {
                when (tabIndex) {
                    0 -> lineSeries { series(ratingSeries) }
                    1 -> columnSeries { series(countSeries) }
                    2 -> lineSeries { series(accuracySeries) }
                }
            }
        }
    }

    Column {
        PrimaryTabRow(
            selectedTabIndex = tabIndex,
            containerColor = Color.Transparent,
        ) {
            Tab(
                selected = tabIndex == 0,
                onClick = { tabIndex = 0 }
            ) {
                Text(
                    text = stringResource(R.string.rating),
                    modifier = Modifier.padding(8.dp)
                )
            }
            Tab(
                selected = tabIndex == 1,
                onClick = { tabIndex = 1 }
            ) {
                Text(
                    text = stringResource(R.string.play_count),
                    modifier = Modifier.padding(8.dp)
                )
            }
            Tab(
                selected = tabIndex == 2,
                onClick = { tabIndex = 2 }
            ) {
                Text(
                    text = stringResource(R.string.average_accuracy),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        ProvideVicoTheme(rememberM3VicoTheme()) {
            val lineProvider =
                LineCartesianLayer.LineProvider.series(vicoTheme.lineCartesianLayerColors.map { color ->
                    LineCartesianLayer.rememberLine(
                        // 折线下方的渐变颜色区域填充效果
                        areaFill = LineCartesianLayer.AreaFill.single(
                            fill(
                                ShaderProvider.verticalGradient(
                                    color.copy(alpha = 0.7f).toArgb(),
                                    color.copy(alpha = 0f).toArgb()
                                )
                            )
                        ),
                        // 折现类型改为曲线
                        pointConnector = LineCartesianLayer.PointConnector.cubic()
                    )
                })
            val columnProvider =
                ColumnCartesianLayer.ColumnProvider.series(vicoTheme.columnCartesianLayerColors.map { color ->
                    rememberLineComponent(
                        // 数据柱颜色改为主题色
                        fill = fill(color),
                        // 数据柱形状改为顶部圆角的矩形
                        shape = CorneredShape.rounded(topLeftPercent = 50, topRightPercent = 50),
                        // 数据柱宽度
                        thickness = 16.dp
                    )
                })
            // 让图表y轴底部从数据集的最小值开始而不是从0开始，避免数据都离0过远而挤在一起
            val rangeProvider = object : CartesianLayerRangeProvider {
                override fun getMinY(
                    minY: Double,
                    maxY: Double,
                    extraStore: ExtraStore
                ): Double {
                    return minY
                }
            }

            CartesianChartHost(
                modelProducer = cartesianChartModelProducer,
                chart = rememberCartesianChart(
                    // 针对不同的数据集，分别展示折线图和柱状图
                    layers = arrayOf(
                        rememberLineCartesianLayer(
                            lineProvider = lineProvider,
                            rangeProvider = rangeProvider
                        ),
                        rememberColumnCartesianLayer(
                            columnProvider = columnProvider,
                            rangeProvider = rangeProvider
                        ),
                        rememberLineCartesianLayer(
                            lineProvider = lineProvider,
                            rangeProvider = rangeProvider
                        )
                    ),
                    // 图表左边框
                    startAxis = VerticalAxis.rememberStart(
                        horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
                        valueFormatter = { _, value, _ ->
                            when (tabIndex) {
                                0 -> value.run {
                                    if (keep2DecimalPlace) setPrecision(2) else this
                                }.toString()

                                1 -> value.toInt().toString()
                                2 -> "${
                                    value.run {
                                        if (keep2DecimalPlace) setPrecision(2) else this
                                    }
                                }%"

                                else -> "Error"
                            }
                        }
                    ),
                    // 图表右边框
                    endAxis = VerticalAxis.rememberEnd(
                        horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
                        itemPlacer = VerticalAxis.ItemPlacer.count({ 0 })
                    ),
                    // 图表底边框
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = { _, value, _ ->
                            if (value.toInt() < timeSeries.size) {
                                timeSeries[value.toInt()].date.replace("-", "w").substring(2)
                            } else "null"
                        },
                        labelRotationDegrees = 90f,
                        label = rememberAxisLabelComponent(minWidth = TextComponent.MinWidth.text("YYYYwWW"))
                    ),
                    // 选中数据点时展示的标志
                    marker = ChartMarker().rememberMarker(
                        valueFormatter = { _, targets ->
                            val xIndex = targets.first().x.toInt()
                            val date =
                                context.getString(
                                    R.string.year_week,
                                    timeSeries[xIndex].year.toString(),
                                    timeSeries[xIndex].week.toString()
                                )
                            return@rememberMarker "${date}：${
                                when (tabIndex) {
                                    0 -> ratingSeries[xIndex].run {
                                        if (keep2DecimalPlace) setPrecision(2) else this
                                    }

                                    1 -> countSeries[xIndex]
                                    2 -> accuracySeries[xIndex].run {
                                        if (keep2DecimalPlace) setPrecision(2) else this
                                    }.toString() + "%"

                                    else -> "Error"
                                }
                            }"
                        }
                    ),
                    // 装饰线，这里展示的是平均值线
                    decorations = listOf(
                        HorizontalLine(
                            y = {
                                when (tabIndex) {
                                    0 -> ratingSeries.average()
                                    1 -> countSeries.average()
                                    2 -> accuracySeries.average()
                                    else -> 0.0
                                }
                            },
                            line = rememberLineComponent(
                                fill = fill(MaterialTheme.colorScheme.primary),
                                thickness = 2.dp
                            ),
                            label = {
                                "${context.getString(R.string.average)}：${
                                    when (tabIndex) {
                                        0 -> ratingSeries.average()
                                        1 -> countSeries.average()
                                        2 -> accuracySeries.average()
                                        else -> 0
                                    }.run { if (keep2DecimalPlace) setPrecision(2) else this }
                                        .run { if (tabIndex == 2) "${this}%" else this }
                                }\n${context.getString(R.string.maximum)}：${
                                    when (tabIndex) {
                                        0 -> ratingSeries.max()
                                        1 -> countSeries.max()
                                        2 -> accuracySeries.max()
                                        else -> 0
                                    }.run {
                                        if (tabIndex == 1) return@run this.toInt()
                                        if (keep2DecimalPlace) setPrecision(2) else this
                                    }.run { if (tabIndex == 2) "${this}%" else this }
                                }\n${context.getString(R.string.minimum)}：${
                                    when (tabIndex) {
                                        0 -> ratingSeries.min()
                                        1 -> countSeries.min()
                                        2 -> accuracySeries.min()
                                        else -> 0
                                    }.run {
                                        if (tabIndex == 1) return@run this.toInt()
                                        if (keep2DecimalPlace) setPrecision(2) else this
                                    }.run { if (tabIndex == 2) "${this}%" else this }
                                }"
                            },
                            horizontalLabelPosition = Position.Horizontal.End,
                            verticalLabelPosition = Position.Vertical.Bottom,
                            labelComponent = TextComponent(
                                margins = Insets(4f),
                                padding = Insets(8f, 8f),
                                background = ShapeComponent(
                                    fill(MaterialTheme.colorScheme.surfaceContainer),
                                    CorneredShape.rounded(8f)
                                ),
                                lineCount = 3,
                                color = vicoTheme.textColor.toArgb()
                            )
                        )
                    )
                ),
                // 滚动状态，初始滚动到最后一个点（时间上最新的数据）
                scrollState = rememberVicoScrollState(initialScroll = Scroll.Absolute.End)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionsCard(profileGraphQL: ProfileGraphQL) {
    profileGraphQL.data.profile?.user?.let { user ->
        if (user.collectionsCount != 0) {
            Card {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var folded by rememberSaveable { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${stringResource(R.string.uploaded_collections)}（${user.collectionsCount}）",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        FoldExpandButton(folded) { folded = !folded }
                    }

                    AnimatedVisibility(visible = !folded) {
                        HorizontalMultiBrowseCarousel(
                            state = rememberCarouselState { user.collectionsCount },
                            preferredItemWidth = min(
                                384.dp,
                                LocalConfiguration.current.screenWidthDp.dp.times(0.8f)
                            ),
                            itemSpacing = 8.dp
                        ) { itemIndex ->
                            CollectionCard(
                                modifier = Modifier.maskClip(MaterialTheme.shapes.medium),
                                collection = user.collections[itemIndex]
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionCard(
    modifier: Modifier = Modifier,
    collection: ProfileGraphQL.ProfileData.Profile.User.CollectionUserListing
) {
    Card(
        modifier = modifier
    ) {
        Box {
            CollectionCoverImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.6f),
                collectionID = collection.uid,
                collectionCoverImageSize = ImageSize.Cover,
                remoteUrl = collection.cover?.cover
            )
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFF000000)
                            )
                        )
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = collection.title,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = collection.slogan,
                    color = Color.White,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = stringResource(R.string.collection_levels_count, collection.levelCount),
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .background(
                        Color(0xFF414558),
                        CorneredShape.Pill.toComposeShape()
                    )
                    .padding(8.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LevelsCard(profileGraphQL: ProfileGraphQL, exoPlayer: ExoPlayer, playbackState: Int) {
    profileGraphQL.data.profile?.user?.let { user ->
        if (user.levelsCount != 0) {
            Card {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var folded by rememberSaveable { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${stringResource(R.string.uploaded_levels)}（${user.levelsCount}）",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        FoldExpandButton(folded) { folded = !folded }
                    }

                    AnimatedVisibility(visible = !folded) {
                        HorizontalMultiBrowseCarousel(
                            state = rememberCarouselState { user.levelsCount },
                            preferredItemWidth = min(
                                384.dp,
                                LocalConfiguration.current.screenWidthDp.dp.times(0.8f)
                            ),
                            itemSpacing = 8.dp
                        ) { itemIndex ->
                            LevelCard(
                                modifier = Modifier.maskClip(MaterialTheme.shapes.medium),
                                level = user.levels[itemIndex],
                                exoPlayer = exoPlayer,
                                playbackState = playbackState
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelCard(
    modifier: Modifier = Modifier,
    level: ProfileGraphQL.ProfileData.Profile.User.UserLevel,
    exoPlayer: ExoPlayer,
    playbackState: Int
) {
    var levelDialogState by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onLongPress = {
                    levelDialogState = true
                }
            )
        }
    ) {
        Box {
            LevelBackgroundImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.6f),
                levelID = level.uid,
                backgroundImageSize = ImageSize.Cover,
                remoteUrl = level.bundle?.backgroundImage?.cover
            )
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFF000000)
                            )
                        )
                    )
            ) {
                Column(
                    Modifier.padding(8.dp)
                ) {
                    Text(
                        text = level.title,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = level.description,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    level.metadata.artist?.name?.let {
                        Text(
                            text = it,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    level.charts.sortedBy { it.difficulty }.forEach { chart ->
                        item {
                            DifficultyPillText(
                                difficultyName = chart.name,
                                difficultyType = chart.type,
                                difficultyLevel = chart.difficulty
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                LevelCardMusicPreviewButton(
                    exoPlayer = exoPlayer,
                    playbackState = playbackState,
                    musicPreviewUrl = level.bundle?.musicPreview ?: level.bundle?.music
                )
            }
        }
    }
    if (levelDialogState) AlertDialog(
        onDismissRequest = { levelDialogState = false },
        confirmButton = {},
        title = {
            Text(text = level.title, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState())
            ) {
                ListItem(
                    headlineContent = { Text(text = stringResource(id = R.string.view_in_cytoid)) },
                    modifier = Modifier.clickable {
                        if (BaseApplication.cytoidIsInstalled) {
                            BaseApplication.context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    CytoidDeepLink.getCytoidLevelDeepLink(level.uid).toUri()
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        } else context
                            .getString(R.string.cytoid_is_not_installed)
                            .showToast()
                    }
                )
                ListItem(
                    headlineContent = { Text(text = stringResource(id = R.string.view_in_cytoid_io)) },
                    modifier = Modifier.clickable {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://cytoid.io/levels/${level.uid}".toUri()
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        BaseApplication.context.startActivity(intent)
                    }
                )
                ListItem(
                    headlineContent = { Text(context.getString(R.string.save_illustration)) },
                    modifier = Modifier.clickable {
                        context
                            .getString(R.string.saving_illustration)
                            .showToast()
                        thread {
                            kotlin.runCatching {
                                URL(level.bundle?.backgroundImage?.original)
                                    .toBitmap()
                                    .saveIntoMediaStore(context.contentResolver)
                            }.onSuccess {
                                context
                                    .getString(R.string.saved_into_gallery)
                                    .showToast()
                            }.onFailure { e ->
                                e.printStackTrace()
                                e.stackTraceToString().showToast()
                            }
                        }
                    }
                )
            }
        }
    )
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun LevelCardMusicPreviewButton(
    exoPlayer: ExoPlayer,
    playbackState: Int,
    musicPreviewUrl: String?
) {
    val context = LocalContext.current

    AnimatedVisibility(visible = playbackState == ExoPlayer.STATE_IDLE || playbackState == ExoPlayer.STATE_ENDED) {
        IconButton(
            onClick = {
                if (musicPreviewUrl == null) {
                    context.getString(R.string.no_music_preview).showToast()
                } else {
                    exoPlayer.apply {
                        setMediaSource(
                            ProgressiveMediaSource.Factory(
                                DefaultHttpDataSource.Factory()
                                    .setDefaultRequestProperties(mapOf("User-Agent" to "CytoidClient/2.1.1"))
                            ).createMediaSource(
                                MediaItem.Builder()
                                    .setUri(musicPreviewUrl.toUri()).build()
                            )
                        )
                        prepare()
                        play()
                    }
                }
            },
            modifier = Modifier
                .padding(4.dp),
            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = stringResource(R.string.play_music_preview)
            )
        }
    }
}

@Composable
private fun CommentList(commentList: List<ProfileComment>) {
    if (commentList.isNotEmpty()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var folded by rememberSaveable { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stringResource(R.string.comment)}（${commentList.size}）",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                FoldExpandButton(folded) { folded = !folded }
            }

            AnimatedVisibility(visible = !folded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    commentList.forEach { comment ->
                        CommentCard(comment)
                    }
                }
            }
        }
    }
}

@Composable
private fun FoldExpandButton(folded: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (folded) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.Default.KeyboardArrowDown,
            contentDescription = if (folded) {
                stringResource(R.string.unfold)
            } else {
                stringResource(R.string.fold)
            }
        )
    }
}

@Composable
private fun CommentCard(comment: ProfileComment) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        UserAvatar(
            size = 48.dp,
            userUid = comment.owner.uid,
            remoteAvatarUrl = comment.owner.avatar.large
        )
        Card {
            Column(
                Modifier.padding(8.dp)
            ) {
                Row {
                    Text(
                        text = comment.owner.uid,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(
                            R.string.days_ago,
                            (System.currentTimeMillis() - DateParser.parseISO8601Date(
                                comment.date
                            ).time).milliseconds.inWholeDays
                        )
                    )
                }
                SelectionContainer {
                    Text(
                        text = comment.content,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}