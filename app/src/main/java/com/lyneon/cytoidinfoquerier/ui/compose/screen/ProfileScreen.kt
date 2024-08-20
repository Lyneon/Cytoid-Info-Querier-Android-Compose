package com.lyneon.cytoidinfoquerier.ui.compose.screen

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.lyneon.cytoidinfoquerier.data.constant.CytoidColors
import com.lyneon.cytoidinfoquerier.data.enums.ImageSize
import com.lyneon.cytoidinfoquerier.data.model.graphql.ProfileGraphQL
import com.lyneon.cytoidinfoquerier.data.model.webapi.ProfileComment
import com.lyneon.cytoidinfoquerier.data.model.webapi.ProfileDetails
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.lyneon.cytoidinfoquerier.ui.compose.component.CollectionCoverImage
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
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberEndAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.marker.markerComponent
import com.patrykandpatrick.vico.compose.component.overlayingComponent
import com.patrykandpatrick.vico.compose.component.shape.composeShape
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.extension.indicatorSize
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.component.shape.DashedShape
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.patrykandpatrick.vico.core.extension.appendCompat
import com.patrykandpatrick.vico.core.extension.transformToSpannable
import com.patrykandpatrick.vico.core.marker.MarkerLabelFormatter
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.launch
import java.net.URL
import java.util.Locale
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

    if (withInitials && !initialLoaded) {
        val initialCytoidID = navBackStackEntry.arguments?.getString("initialCytoidID")
        val initialCacheTime = navBackStackEntry.arguments?.getString("initialCacheTime")?.toLong()
        if (initialCytoidID != null && initialCacheTime != null) {
            viewModel.setCytoidID(initialCytoidID)
            viewModel.loadSpecificCacheProfileScreenDataModel(initialCacheTime)
        }
        initialLoaded = true
    }
    if (withShortcut && !shortcutLoaded) {
        val appUserID =
            MMKV.mmkvWithID(MMKVId.AppSettings.id)
                .decodeString(AppSettingsMMKVKeys.APP_USER_CYTOID_ID.name)
        if (appUserID != null) {
            viewModel.setCytoidID(appUserID)
        }
        if (uiState.canQuery()) {
            viewModel.enqueueQuery()
        }
        shortcutLoaded = true
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
                .padding(horizontal = 16.dp)
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
                        playbackState
                    )
                }
            }
        }
    }
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
                if (it.length <= 16) {
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
    playbackState: Int
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
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
                            text = "注册于${
                                DateParser.parseISO8601Date(registrationDate)
                                    .formatToTimeString()
                            }，${
                                (System.currentTimeMillis() - DateParser.parseISO8601Date(
                                    registrationDate
                                ).time)
                                    .milliseconds.inWholeDays
                            }天前"
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
                        IconButton(onClick = { folded = !folded }) {
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
                    AnimatedVisibility(visible = !folded) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            SelectionContainer {
                                Text(text = profile.bio)
                            }
                        }
                    }
                }
            }
        }
    }
}

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
                        text = "${stringResource(R.string.badge)}（共${profile.badges.size}个）",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    IconButton(onClick = { folded = !folded }) {
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
                AnimatedVisibility(visible = !folded) {
                    SelectionContainer {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            profile.badges.forEach {
                                Text(text = it.title)
                                it.description?.let { it1 -> Text(text = it1) }
                            }
                        }
                    }
                }
            }
        }
    }
}
/*
@Composable
private fun RecentRecordsCard(profileGraphQL: ProfileGraphQL, keep2DecimalPlace: Boolean) {
    profileGraphQL.data.profile?.let { profile ->
        Card {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                var folded by rememberSaveable { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "最新游玩纪录（共${profile.recentRecords.size}个）",
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    IconButton(onClick = { folded = !folded }) {
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
                AnimatedVisibility(visible = !folded) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        profile.recentRecords.forEach {
                            RecordCard(record = it, keep2DecimalPlaces = keep2DecimalPlace)
                        }
                    }
                }
            }
        }
    }
}
*/

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DetailsCard(profileDetails: ProfileDetails, keep2DecimalPlace: Boolean) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val timeSeries = profileDetails.timeSeries.apply {
        sortBy { it.date.replace("-", "").toInt() }
    }
    val chartEntryModelProducer by remember { mutableStateOf(ChartEntryModelProducer()) }

    chartEntryModelProducer.setEntries(timeSeries.map {
        entryOf(
            timeSeries.indexOf(it),
            when (tabIndex) {
                0 -> it.rating.run {
                    if (keep2DecimalPlace) setPrecision(2).toFloat() else this
                }

                1 -> it.count
                2 -> (it.accuracy * 100).run {
                    if (keep2DecimalPlace) setPrecision(2).toFloat() else this
                }

                else -> -1
            }
        )
    })

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
                    Text(text = "总游玩次数")
                    Text(
                        text = profileDetails.activities.totalRankedPlays.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    Modifier.weight(1f)
                ) {
                    Text(text = "总Note数")
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
                    Text(text = "最高连击数")
                    Text(
                        text = profileDetails.activities.maxCombo.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    Modifier.weight(1f)
                ) {
                    Text(text = "平均精准度")
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
                    Text(text = "总分数")
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

                    Text(text = "总游玩时间")
                    Text(
                        text = (if (days != 0.toLong()) "${days}天" else "") +
                                (if (hours != 0.toLong()) "${hours}时" else "") +
                                (if (minutes != 0.toLong()) "${minutes}分" else "") +
                                (if (seconds != 0.toLong()) "${seconds}秒" else ""),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = "成绩分布",
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
            PrimaryTabRow(
                selectedTabIndex = tabIndex,
                containerColor = Color.Transparent,
            ) {
                Tab(
                    selected = tabIndex == 0,
                    onClick = { tabIndex = 0 }
                ) {
                    Text(
                        text = "Rating",
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Tab(
                    selected = tabIndex == 1,
                    onClick = { tabIndex = 1 }
                ) {
                    Text(
                        text = "游玩次数",
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Tab(
                    selected = tabIndex == 2,
                    onClick = { tabIndex = 2 }
                ) {
                    Text(
                        text = "平均精准度",
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            ProvideChartStyle(m3ChartStyle()) {
                Chart(
                    chart = if (tabIndex == 1) columnChart(
                        axisValuesOverrider = AxisValuesOverrider.adaptiveYValues(1f)
                    ) else lineChart(
                        axisValuesOverrider = AxisValuesOverrider.adaptiveYValues(1f)
                    ),
                    chartModelProducer = chartEntryModelProducer,
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = { value, _ ->
                            (if (value.toInt() < timeSeries.size)
                                timeSeries[value.toInt()].date.replace("-", "w")
                            else "null").substring(2)
                        },
                        labelRotationDegrees = 90f
                    ),
                    startAxis = rememberStartAxis(
                        valueFormatter = { value, _ ->
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
                        },
                        horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside
                    ),
                    endAxis = rememberEndAxis(
                        valueFormatter = { _, _ -> "" },
                        horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside
                    ),
                    marker = markerComponent(
                        label = textComponent(
                            background = shapeComponent(
                                shape = Shapes.pillShape,
                                color = MaterialTheme.colorScheme.surface
                            ),
                            padding = MutableDimensions(6f, 6f),
                            margins = MutableDimensions(0f, 0f, 0f, 6f)
                        ),
                        indicator = overlayingComponent(
                            outer = shapeComponent(
                                shape = Shapes.pillShape,
                                color = MaterialTheme.colorScheme.surface
                            ),
                            inner = shapeComponent(
                                shape = Shapes.pillShape,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            innerPaddingAll = 8.dp
                        ),
                        guideline = lineComponent(
                            color = Color(0x80808080),
                            thickness = 2.dp,
                            shape = DashedShape(Shapes.pillShape)
                        )
                    ).apply {
                        indicatorSize = 16.dp
                        this.labelFormatter = MarkerLabelFormatter { markedEntries, _ ->
                            markedEntries.transformToSpannable { model ->
                                appendCompat(
                                    profileDetails.timeSeries[model.index].date.replace(
                                        "-",
                                        "年第"
                                    ) + "周；",
                                    ForegroundColorSpan(model.color),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                appendCompat(
                                    when (tabIndex) {
                                        0 -> model.entry.y.run {
                                            if (keep2DecimalPlace) setPrecision(2) else this.toString()
                                        }

                                        1 -> model.entry.y.toInt().toString()
                                        2 -> model.entry.y.run {
                                            if (keep2DecimalPlace) setPrecision(2) else this.toString()
                                        } + "%"

                                        else -> ""
                                    },
                                    ForegroundColorSpan(model.color),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CollectionsCard(profileGraphQL: ProfileGraphQL) {
    profileGraphQL.data.profile?.user?.let { user ->
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
                        text = "上传的合集（共${user.collectionsCount}个）",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    IconButton(onClick = { folded = !folded }) {
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

                AnimatedVisibility(visible = !folded) {
                    // TODO: 更改为可变列数瀑布流列表，并解决嵌套滚动
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        user.collections.forEach {
                            CollectionCard(collection = it)
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun CollectionCard(collection: ProfileGraphQL.ProfileData.Profile.User.CollectionUserListing) {
    Card {
        Box {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                CollectionCoverImage(
                    modifier = Modifier.fillMaxWidth(),
                    collectionID = collection.uid,
                    collectionCoverImageSize = ImageSize.Original,
                    remoteUrl = collection.cover?.original
                )
            }
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
                text = "${collection.levelCount}个关卡",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        Color(0xFF414558),
                        Shapes.pillShape.composeShape()
                    )
                    .padding(8.dp)
            )
        }
    }
}


@Composable
private fun LevelsCard(profileGraphQL: ProfileGraphQL, exoPlayer: ExoPlayer, playbackState: Int) {
    profileGraphQL.data.profile?.user?.let { user ->
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
                        text = "上传的关卡（共${user.levelsCount}个）",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    IconButton(onClick = { folded = !folded }) {
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

                AnimatedVisibility(visible = !folded) {
                    // TODO: 更改为可变列数瀑布流列表，并解决嵌套滚动
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        user.levels.forEach {
                            LevelCard(
                                level = it,
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
    level: ProfileGraphQL.ProfileData.Profile.User.UserLevel,
    exoPlayer: ExoPlayer,
    playbackState: Int
) {
    var levelDialogState by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        Modifier.pointerInput(Unit) {
            detectTapGestures(
                onLongPress = {
                    levelDialogState = true
                }
            )
        }
    ) {
        Box {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                LevelBackgroundImage(
                    modifier = Modifier.fillMaxWidth(),
                    levelID = level.uid,
                    backgroundImageSize = ImageSize.Original,
                    remoteUrl = level.bundle?.backgroundImage?.original
                )
            }
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(8.dp)
                ) {

                    level.charts.forEach { chart ->
                        Text(
                            text = " ${
                                chart.name
                                    ?: chart.type.replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(
                                            Locale.getDefault()
                                        ) else it.toString()
                                    }
                            } ${chart.difficulty} ",
                            color = Color.White,
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        when (chart.type) {
                                            "easy" -> CytoidColors.easyColor
                                            "extreme" -> CytoidColors.extremeColor
                                            else -> CytoidColors.hardColor
                                        }
                                    ), Shapes.pillShape.composeShape()
                                )
                                .padding(8.dp)
                        )
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
                                    Uri.parse(CytoidDeepLink.getCytoidLevelDeepLink(level.uid))
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
                            Uri.parse("https://cytoid.io/levels/${level.uid}")
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
                                    .saveIntoMediaStore(
                                        context.contentResolver,
                                        ContentValues()
                                    )
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
    AnimatedVisibility(visible = playbackState == ExoPlayer.STATE_IDLE || playbackState == ExoPlayer.STATE_ENDED) {
        IconButton(
            onClick = {
                if (musicPreviewUrl == null) {
                    "没有音乐预览！".showToast()
                } else {
                    exoPlayer.apply {
                        setMediaSource(
                            ProgressiveMediaSource.Factory(
                                DefaultHttpDataSource.Factory()
                                    .setDefaultRequestProperties(mapOf("User-Agent" to "CytoidClient/2.1.1"))
                            ).createMediaSource(
                                MediaItem.Builder()
                                    .setUri(Uri.parse(musicPreviewUrl)).build()
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
                contentDescription = "播放音乐预览"
            )
        }
    }
}

@Composable
private fun CommentList(commentList: List<ProfileComment>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var folded by rememberSaveable { mutableStateOf(false) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "留言（共${commentList.size}个）",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            IconButton(onClick = { folded = !folded }) {
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

        AnimatedVisibility(visible = !folded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                commentList.forEach { comment ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserAvatar(
                            modifier = Modifier.size(48.dp),
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
                                        text = "${
                                            (System.currentTimeMillis() - DateParser.parseISO8601Date(
                                                comment.date
                                            ).time)
                                                .milliseconds.inWholeDays
                                        }天前"
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
            }
        }
    }
}