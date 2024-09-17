package com.lyneon.cytoidinfoquerier.ui.compose.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.CytoidColors
import com.lyneon.cytoidinfoquerier.data.constant.SearchLevelCategory
import com.lyneon.cytoidinfoquerier.data.enums.AvatarSize
import com.lyneon.cytoidinfoquerier.data.enums.ImageSize
import com.lyneon.cytoidinfoquerier.data.model.graphql.LevelLeaderboard
import com.lyneon.cytoidinfoquerier.data.model.shared.Level
import com.lyneon.cytoidinfoquerier.data.model.webapi.LevelComment
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.lyneon.cytoidinfoquerier.ui.compose.component.LevelBackgroundImage
import com.lyneon.cytoidinfoquerier.ui.compose.component.UserAvatar
import com.lyneon.cytoidinfoquerier.ui.viewmodel.LevelDetailUIState
import com.lyneon.cytoidinfoquerier.ui.viewmodel.LevelDetailViewModel
import com.lyneon.cytoidinfoquerier.ui.viewmodel.SharedViewModel
import com.lyneon.cytoidinfoquerier.util.DateParser
import com.lyneon.cytoidinfoquerier.util.DateParser.formatToTimeString
import com.lyneon.cytoidinfoquerier.util.extension.saveIntoClipboard
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import com.patrykandpatrick.vico.compose.common.shape.toComposeShape
import com.patrykandpatrick.vico.core.common.shape.Shape
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelDetailScreen(
    navController: NavController,
    viewModel: LevelDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLeaderboard by viewModel.currentLeaderboard.collectAsState()
    val levelCommentList by viewModel.levelCommentList.collectAsState()
    val sharedViewModel = viewModel<SharedViewModel>(LocalContext.current as MainActivity)
    val level = sharedViewModel.sharedLevelForLevelDetailScreen
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LaunchedEffect(level) {
        level?.let { viewModel.updateLevelCommentList(it.uid) }
        level?.charts?.first()
            ?.let { viewModel.setSelectedLevelLeaderboardDifficultyType(it.difficultyType) }
    }
    LaunchedEffect(uiState.selectedLevelLeaderboardDifficultyType) {
        level?.uid?.let {
            uiState.selectedLevelLeaderboardDifficultyType?.let { difficultyType ->
                viewModel.updateCurrentLeaderboard(
                    it,
                    difficultyType,
                    0,
                    10
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                title = { Text(text = "关卡详情") },
                colors = TopAppBarDefaults.topAppBarColors(scrolledContainerColor = MaterialTheme.colorScheme.surface),
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .fillMaxSize()
        ) {
            if (level == null) {
                Card {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Error, contentDescription = null)
                        Text(text = "Error: Level is null")
                    }
                }
            } else {
                if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    LandscapeLevelDetailScreen(
                        level,
                        scrollBehavior.nestedScrollConnection,
                        levelCommentList,
                        currentLeaderboard,
                        viewModel,
                        uiState
                    )
                } else {
                    PortraitLevelDetailScreen(
                        level,
                        scrollBehavior.nestedScrollConnection,
                        levelCommentList,
                        currentLeaderboard,
                        viewModel,
                        uiState
                    )
                }
            }
        }
    }
}

@Composable
private fun LandscapeLevelDetailScreen(
    level: Level,
    nestedScrollConnection: NestedScrollConnection,
    commentList: List<LevelComment>,
    leaderboard: LevelLeaderboard?,
    viewModel: LevelDetailViewModel,
    uiState: LevelDetailUIState
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(6f)
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LevelHeaderCard(level)
            LevelDetailsCard(level)
            LevelMetadataCard(level)
            LevelCommentListCard(commentList)
            BottomNavigationPaddingSpacer()
        }
        Column(
            modifier = Modifier
                .weight(4f)
                .verticalScroll(rememberScrollState())
        ) {
            LevelLeaderboardCard(level, leaderboard, viewModel, uiState)
            BottomNavigationPaddingSpacer()
        }
    }
}

@Composable
private fun PortraitLevelDetailScreen(
    level: Level,
    nestedScrollConnection: NestedScrollConnection,
    commentList: List<LevelComment>,
    leaderboard: LevelLeaderboard?,
    viewModel: LevelDetailViewModel,
    uiState: LevelDetailUIState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LevelHeaderCard(level)
            LevelDetailsCard(level)
            LevelMetadataCard(level)
            LevelCommentListCard(commentList)
            LevelLeaderboardCard(level, leaderboard, viewModel, uiState)
        }
        BottomNavigationPaddingSpacer()
    }
}

@Composable
private fun LevelHeaderCard(level: Level) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card {
                LevelBackgroundImage(
                    modifier = Modifier.fillMaxWidth(),
                    levelID = level.uid,
                    backgroundImageSize = ImageSize.Original,
                    remoteUrl = level.coverRemoteURL
                )
            }
            Text(text = level.title, style = MaterialTheme.typography.headlineLarge)
            level.artist?.let { Text(text = it) }
            LevelChartsDifficultiesFlowRow(level.charts)
            Text(
                "创建于${
                    DateParser.parseISO8601Date(level.creationDate)
                        .formatToTimeString()
                }"
            )
            Text(
                "最后更新于${
                    DateParser.parseISO8601Date(level.modificationDate)
                        .formatToTimeString()
                }"
            )
            Text("下载次数：${level.downloads}")
            Text("游玩次数：${level.plays}")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LevelLeaderboardCard(
    level: Level,
    levelLeaderboard: LevelLeaderboard?,
    viewModel: LevelDetailViewModel,
    uiState: LevelDetailUIState
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Row {
                Text(
                    text = "排行榜",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(onClick = {
                    val start = uiState.leaderboardStart.toIntOrNull()
                    val end = uiState.leaderboardEnd.toIntOrNull()
                    if (
                        start != null && end != null &&
                        start > 0 && end > 0 &&
                        listOf(
                            "easy",
                            "hard",
                            "extreme"
                        ).contains(uiState.selectedLevelLeaderboardDifficultyType) && end >= start
                    ) {
                        viewModel.updateCurrentLeaderboard(
                            level.uid,
                            uiState.selectedLevelLeaderboardDifficultyType!!,
                            start - 1,
                            end - start + 1
                        )
                    }
                }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                }
            }
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                level.charts.forEach { chart ->
                    FilterChip(
                        selected = chart.difficultyType == (uiState.selectedLevelLeaderboardDifficultyType
                            ?: ""),
                        onClick = { viewModel.setSelectedLevelLeaderboardDifficultyType(chart.difficultyType) },
                        label = {
                            Text(text = chart.difficultyName
                                ?: chart.difficultyType.replaceFirstChar {
                                    if (it.isLowerCase()) it.uppercase() else it.toString()
                                }
                            )
                        },
                        leadingIcon = {
                            AnimatedVisibility(chart.difficultyType == uiState.selectedLevelLeaderboardDifficultyType) {
                                Icon(imageVector = Icons.Default.Done, contentDescription = null)
                            }
                        }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(
                    value = uiState.leaderboardStart,
                    onValueChange = {
                        if (it.any { char -> !char.isDigit() }) return@OutlinedTextField
                        else viewModel.setLeaderboardStart(it)
                    },
                    suffix = { Text("名") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text(text = "至")
                OutlinedTextField(
                    value = uiState.leaderboardEnd,
                    onValueChange = {
                        if (it.any { char -> !char.isDigit() }) return@OutlinedTextField
                        else viewModel.setLeaderboardEnd(it)
                    },
                    suffix = { Text("名") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                levelLeaderboard?.data?.chart?.leaderboard?.let { leaderboard ->
                    leaderboard.forEach { leaderboardRecord ->
                        leaderboardRecord.owner?.let { owner ->
                            val textMeasurer = rememberTextMeasurer()

                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(
                                    modifier = Modifier.size(48.dp),
                                    userUid = owner.uid ?: owner.id,
                                    avatarSize = AvatarSize.Large,
                                    remoteAvatarUrl = owner.avatar.large ?: ""
                                )
                                Text(
                                    text = owner.let { it.uid ?: it.id },
                                    modifier = Modifier.width(
                                        textMeasurer.measure(
                                            "UserName",
                                            style = LocalTextStyle.current
                                        ).size.width.dp
                                    )
                                )
                                Text(
                                    text = leaderboardRecord.score.toString(),
                                    modifier = Modifier.width(
                                        textMeasurer.measure(
                                            "999999",
                                            style = LocalTextStyle.current
                                        ).size.width.dp
                                    )
                                )
                                Text(
                                    text = (leaderboardRecord.accuracy*100).toString() + "%",
                                    modifier = Modifier.width(
                                        textMeasurer.measure(
                                            "100.0%",
                                            style = LocalTextStyle.current
                                        ).size.width.dp
                                    )
                                )
                                Text(
                                    text = leaderboardRecord.details.maxCombo.toString() + "x",
                                    modifier = Modifier.width(
                                        textMeasurer.measure(
                                            "1000x",
                                            style = LocalTextStyle.current
                                        ).size.width.dp
                                    )
                                )
                                Row(
                                    modifier = Modifier.width(
                                        textMeasurer.measure(
                                            "1000/0000",
                                            style = LocalTextStyle.current
                                        ).size.width.dp
                                    ),
                                ) {
                                    Text(
                                        text = leaderboardRecord.details.perfect.toString(),
                                        color = CytoidColors.perfectColor
                                    )
                                    Text(text = "/")
                                    Text(
                                        text = leaderboardRecord.details.great.toString(),
                                        color = CytoidColors.greatColor
                                    )
                                    Text(text = "/")
                                    Text(
                                        text = leaderboardRecord.details.good.toString(),
                                        color = CytoidColors.goodColor
                                    )
                                    Text(text = "/")
                                    Text(
                                        text = leaderboardRecord.details.bad.toString(),
                                        color = CytoidColors.badColor
                                    )
                                    Text(text = "/")
                                    Text(
                                        text = leaderboardRecord.details.miss.toString(),
                                        color = CytoidColors.missColor
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    leaderboardRecord.mods.forEach { mod ->
                                        Image(
                                            painter = painterResource(
                                                id = when (mod) {
                                                    "HideNotes" -> R.drawable.mod_hide_notes
                                                    "HideScanline" -> R.drawable.mod_hide_scanline
                                                    "Slow" -> R.drawable.mod_slow
                                                    "Fast" -> R.drawable.mod_fast
                                                    "Hard" -> R.drawable.mod_hyper
                                                    "ExHard" -> R.drawable.mod_another
                                                    "AP" -> R.drawable.mod_ap
                                                    "FC" -> R.drawable.mod_fc
                                                    "FlipAll" -> R.drawable.mod_flip_all
                                                    "FlipX" -> R.drawable.mod_flip_x
                                                    "FlipY" -> R.drawable.mod_flip_y
                                                    else -> throw Exception("Unknown condition branch enter action")
                                                }
                                            ),
                                            contentDescription = mod,
                                            modifier = Modifier.height(32.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = DateParser.parseISO8601Date(leaderboardRecord.date)
                                        .formatToTimeString(),
                                    modifier = Modifier.width(
                                        textMeasurer.measure(
                                            "19700101",
                                            style = LocalTextStyle.current
                                        ).size.width.dp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationPaddingSpacer(
    padding: Dp = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
) {
    Spacer(modifier = Modifier.height(padding))
}

@Composable
private fun LevelChartsDifficultiesFlowRow(charts: List<Level.Chart>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        charts.forEach { chart ->
            item {
                Text(
                    text = " ${
                        chart.difficultyName
                            ?: chart.difficultyType.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            }
                    } ${chart.difficultyLevel} ",
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                when (chart.difficultyType) {
                                    "easy" -> CytoidColors.easyColor
                                    "extreme" -> CytoidColors.extremeColor
                                    else -> CytoidColors.hardColor
                                }
                            ), Shape.Pill.toComposeShape()
                        )
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LevelDetailsCard(level: Level) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            level.owner?.let {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "上传者")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            userUid = level.owner.uid ?: level.owner.id,
                            avatarSize = AvatarSize.Small,
                            remoteAvatarUrl = level.owner.avatar.small ?: ""
                        )
                        Text(
                            text = level.owner.uid ?: level.owner.id
                        )
                    }
                }
            }
            level.description?.let { Text(text = it) }
            if (level.tags.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "标签")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        level.tags.forEach { tag ->
                            AssistChip(
                                label = { Text(text = tag) },
                                onClick = {
                                    tag.saveIntoClipboard()
                                    "已复制到剪贴板".showToast()
                                }
                            )
                        }
                    }
                }
            }
            if (level.category.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "类别")
                    FlowRow {
                        level.category.forEach { category ->
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
        }
    }
}

@Composable
private fun LevelMetadataCard(level: Level) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        SelectionContainer {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(text = "曲师")
                    Text(
                        text = level.artist ?: "null",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Column {
                    Text(text = "曲绘")
                    Text(
                        text = level.illustrator ?: "null",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Column {
                    Text(text = "谱师")
                    Text(
                        text = level.charter ?: "null",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                level.storyboarder?.let {
                    Column {
                        Text(text = "故事板制作")
                        Text(text = it, style = MaterialTheme.typography.headlineMedium)
                    }
                }
                Column {
                    Text(text = "关卡ID")
                    Text(text = level.uid, style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
    }
}

@Composable
private fun LevelCommentListCard(
    commentList: List<LevelComment>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        commentList.forEach { comment ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                UserAvatar(
                    modifier = Modifier.size(48.dp),
                    userUid = comment.owner.uid ?: comment.owner.id,
                    avatarSize = AvatarSize.Large,
                    remoteAvatarUrl = comment.owner.avatar.large ?: ""
                )
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = comment.owner.uid ?: comment.owner.id,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(text = comment.content)
                        Text(
                            text = "评论于${
                                DateParser.parseISO8601Date(comment.date).formatToTimeString()
                            }"
                        )
                    }
                }
            }
        }
    }
}