package com.lyneon.cytoidinfoquerier.ui.compose

import android.content.Intent
import android.net.Uri
import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.logic.DateParser
import com.lyneon.cytoidinfoquerier.logic.DateParser.formatToTimeString
import com.lyneon.cytoidinfoquerier.model.graphql.ProfileGraphQL
import com.lyneon.cytoidinfoquerier.model.webapi.Comment
import com.lyneon.cytoidinfoquerier.model.webapi.ProfileWebapi
import com.lyneon.cytoidinfoquerier.tool.extension.getImageRequestBuilderForCytoid
import com.lyneon.cytoidinfoquerier.tool.extension.isValidCytoidID
import com.lyneon.cytoidinfoquerier.tool.extension.setPrecision
import com.lyneon.cytoidinfoquerier.tool.extension.showDialog
import com.lyneon.cytoidinfoquerier.tool.extension.showToast
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.lyneon.cytoidinfoquerier.ui.compose.component.LevelCard
import com.lyneon.cytoidinfoquerier.ui.compose.component.RecordCard
import com.lyneon.cytoidinfoquerier.ui.compose.component.TopBar
import com.microsoft.appcenter.crashes.Crashes
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
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
import com.patrykandpatrick.vico.core.component.shape.DashedShape
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.milliseconds

lateinit var profileGraphQL: ProfileGraphQL
lateinit var profileWebapi: ProfileWebapi
lateinit var comments: ArrayList<Comment>
val chartEntryModelProducer = ChartEntryModelProducer()

@Composable
fun ProfileCompose() {
    val context = LocalContext.current as MainActivity
    var cytoidID by remember { mutableStateOf("") }
    var isQueryingFinished by remember { mutableStateOf(false) }
    var textFieldIsError by remember { mutableStateOf(false) }
    var textFieldIsEmpty by remember { mutableStateOf(false) }

    Column {
        TopBar(title = stringResource(id = R.string.profile))
        Column(
            Modifier.padding(6.dp, 6.dp, 6.dp)
        ) {
            Column {
                TextField(
                    isError = textFieldIsError or textFieldIsEmpty,
                    value = cytoidID,
                    onValueChange = {
                        cytoidID = it
                        textFieldIsError = !it.isValidCytoidID()
                        textFieldIsEmpty = it.isEmpty()
                    },
                    label = { Text(text = stringResource(id = R.string.playerName)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Row(
                            Modifier.padding(horizontal = 6.dp)
                        ) {
                            TextButton(onClick = {
                                if (cytoidID.isEmpty()) {
                                    context.getString(R.string.empty_cytoidID)
                                        .showToast()
                                    textFieldIsEmpty = true
                                } else if (!cytoidID.isValidCytoidID()) {
                                    context.getString(R.string.invalid_cytoidID)
                                        .showToast()
                                    textFieldIsError = true
                                } else {
                                    textFieldIsError = false
                                    isQueryingFinished = false
                                    "开始查询$cytoidID".showToast()
                                    thread {
                                        try {
                                            val job = Job()
                                            CoroutineScope(job).launch {
                                                val profiles =
                                                    awaitAll(
                                                        async { ProfileGraphQL.get(cytoidID) },
                                                        async { ProfileWebapi.get(cytoidID) }
                                                    )
                                                profileGraphQL = profiles[0] as ProfileGraphQL
                                                profileWebapi = profiles[1] as ProfileWebapi
                                                comments =
                                                    async { Comment.get(profileGraphQL.data.profile.user.id) }.await()
                                                isQueryingFinished = true
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            Crashes.trackError(e)
                                            Looper.prepare()
                                            e.stackTraceToString()
                                                .showDialog(context, "查询失败")
                                        }
                                    }
                                }
                            }) {
                                Text(text = stringResource(id = R.string.query))
                            }
                        }
                    },
                    singleLine = true
                )
                AnimatedVisibility(visible = textFieldIsError) {
                    Text(
                        text = stringResource(id = R.string.invalid_cytoidID),
                        color = Color.Red
                    )
                }
                AnimatedVisibility(visible = textFieldIsEmpty) {
                    Text(
                        text = stringResource(id = R.string.empty_cytoidID),
                        color = Color.Red
                    )
                }
            }
            AnimatedVisibility(visible = isQueryingFinished && ::profileGraphQL.isInitialized && ::profileWebapi.isInitialized) {
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HeaderBar(profileWebapi = profileWebapi)
                    BiographyCard(profileGraphQL = profileGraphQL)
                    BadgesCard(profileGraphQL = profileGraphQL)
                    RecentRecordsCard(profileGraphQL = profileGraphQL)
                    DetailsCard(profileWebapi = profileWebapi)
                    CollectionsCard(profileGraphQL = profileGraphQL)
                    LevelsCard(profileGraphQL = profileGraphQL)
                    CommentsColumn(comments = comments)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeaderBar(profileWebapi: ProfileWebapi) {
    Row {
        AsyncImage(
            model = getImageRequestBuilderForCytoid(profileWebapi.user.avatar.large)
                .build(),
            contentDescription = profileWebapi.user.uid,
            modifier = Modifier
                .clip(CircleShape)
                .clickable {
                    BaseApplication.context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://cytoid.io/profile/${profileWebapi.user.uid}")
                        )
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .addCategory(Intent.CATEGORY_BROWSABLE)
                    )
                }
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = profileWebapi.user.uid,
                style = MaterialTheme.typography.titleLarge
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Lv. ${profileWebapi.exp.currentLevel}",
                    color = Color.Black,
                    modifier = Modifier
                        .background(Color(0xFF9EB3FF), RoundedCornerShape(100))
                        .padding(horizontal = 6.dp)
                )
                Text(
                    text = "Rating ${profileWebapi.rating.setPrecision(2)}",
                    color = Color.Black,
                    modifier = Modifier
                        .background(Color(0xFF6AF5FF), RoundedCornerShape(100))
                        .padding(horizontal = 6.dp)
                )
                profileWebapi.tier?.let {
                    val backgroundColorList =
                        it.colorPalette.background.split(",").run {
                            listOf(
                                Color(android.graphics.Color.parseColor(this[0])),
                                Color(android.graphics.Color.parseColor(this[1]))
                            )
                        }
                    Text(
                        text = it.name,
                        color = Color.White,
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    backgroundColorList,
                                    Offset.Infinite,
                                    Offset.Zero
                                )
                            )
                            .padding(horizontal = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BiographyCard(profileGraphQL: ProfileGraphQL) {
    Card(
        Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(6.dp)
        ) {
            Row {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_calendar_person),
                    contentDescription = null
                )
                Text(
                    text = "注册于${
                        DateParser.parseISO8601Date(profileGraphQL.data.profile.user.registrationDate)
                            .formatToTimeString()
                    }，${
                        (System.currentTimeMillis() - DateParser.parseISO8601Date(profileGraphQL.data.profile.user.registrationDate).time)
                            .milliseconds.inWholeDays
                    }天前"
                )
            }
            if (profileGraphQL.data.profile.bio.isNotEmpty()) {
                var folded by remember { mutableStateOf(false) }

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
                            imageVector = if (folded) Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowDown,
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
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = profileGraphQL.data.profile.bio)
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgesCard(profileGraphQL: ProfileGraphQL) {
    Card {
        Column(
            Modifier.padding(6.dp)
        ) {
            var folded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stringResource(R.string.badge)}（共${profileGraphQL.data.profile.badges.size}个）",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                IconButton(onClick = { folded = !folded }) {
                    Icon(
                        imageVector = if (folded) Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowDown,
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
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    profileGraphQL.data.profile.badges.forEach {
                        Text(text = it.title)
                        Text(text = it.description)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentRecordsCard(profileGraphQL: ProfileGraphQL) {
    Card {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(6.dp)
        ) {
            var folded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "最新游玩纪录（共${profileGraphQL.data.profile.recentRecords.size}个）",
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.CenterVertically),
                    style = MaterialTheme.typography.titleLarge,
                )
                IconButton(onClick = { folded = !folded }) {
                    Icon(
                        imageVector = if (folded) Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowDown,
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
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    profileGraphQL.data.profile.recentRecords.forEach {
                        RecordCard(record = it)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailsCard(profileWebapi: ProfileWebapi) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val timeSeries = profileWebapi.timeSeries.run {
        sortBy { it.date.replace("-", "").toInt() }
        this
    }
    chartEntryModelProducer.setEntries(timeSeries.map {
        entryOf(
            timeSeries.indexOf(it),
            when (tabIndex) {
                0 -> it.rating.setPrecision(2).toFloat()
                1 -> it.count
                2 -> (it.accuracy * 100).setPrecision(2).toFloat()
                else -> -1
            }
        )
    })

    Card(
        Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row {
                Column(
                    Modifier.weight(1f)
                ) {
                    Text(text = "总游玩次数")
                    Text(
                        text = profileWebapi.activities.totalRankedPlays.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    Modifier.weight(1f)
                ) {
                    Text(text = "总Note数")
                    Text(
                        text = profileWebapi.activities.clearedNotes.toString(),
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
                        text = profileWebapi.activities.maxCombo.toString(),
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
                            (profileWebapi.activities.averageRankedAccuracy * 100).setPrecision(
                                2
                            )
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
                        text = profileWebapi.activities.totalRankedScore.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    Modifier.weight(1f)
                ) {
                    val duration =
                        (profileWebapi.activities.totalPlayTime * 100).toLong().milliseconds
                    val days = duration.inWholeDays
                    val hours = duration.inWholeHours - duration.inWholeDays * 24
                    val minutes = duration.inWholeMinutes - duration.inWholeHours * 60
                    val seconds = duration.inWholeSeconds - duration.inWholeMinutes * 60
                    val milliseconds = duration.inWholeMilliseconds - duration.inWholeSeconds * 1000

                    Text(text = "总游玩时间")
                    Text(
                        text = (if (days != 0.toLong()) "${days}天" else "") +
                                (if (hours != 0.toLong()) "${hours}时" else "") +
                                (if (minutes != 0.toLong()) "${minutes}分" else "") +
                                (if (seconds != 0.toLong()) "${seconds}秒" else "") +
                                (if (milliseconds != 0.toLong()) "${milliseconds}毫秒" else ""),
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
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "MAX ${profileWebapi.grade.MAX}",
                    color = Color.Black,
                    modifier = Modifier
                        .background(Color(0xFFFFCC00), RoundedCornerShape(100))
                        .padding(horizontal = 6.dp)
                )
                Text(
                    text = "SSS ${profileWebapi.grade.SSS}",
                    color = Color.Black,
                    modifier = Modifier
                        .background(Color(0xFF08CFFF), RoundedCornerShape(100))
                        .padding(horizontal = 6.dp)
                )
                Text(
                    text = "SS ${profileWebapi.grade.SS}",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF3F4561), RoundedCornerShape(100))
                        .padding(horizontal = 6.dp)
                )
                Text(
                    text = "S ${profileWebapi.grade.S}",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF3F4561), RoundedCornerShape(100))
                        .padding(horizontal = 6.dp)
                )
                Text(
                    text = "AA ${profileWebapi.grade.AA}",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF3F4561), RoundedCornerShape(100))
                        .padding(horizontal = 6.dp)
                )
                Text(
                    text = "A ${profileWebapi.grade.A}",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF3F4561), RoundedCornerShape(100))
                        .padding(horizontal = 6.dp)
                )
                Text(
                    text = "B ${profileWebapi.grade.B}",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF3F4561), RoundedCornerShape(100))
                        .padding(horizontal = 6.dp)
                )
                Text(
                    text = "C ${profileWebapi.grade.C}",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF3F4561), RoundedCornerShape(100))
                        .padding(horizontal = 6.dp)
                )
                Text(
                    text = "D ${profileWebapi.grade.D}",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF3F4561), RoundedCornerShape(100))
                        .padding(horizontal = 6.dp)
                )
                Text(
                    text = "F ${profileWebapi.grade.F}",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF3F4561), RoundedCornerShape(100))
                        .padding(horizontal = 6.dp)
                )
            }
            TabRow(
                selectedTabIndex = tabIndex,
                containerColor = Color.Transparent,
            ) {
                Tab(
                    selected = tabIndex == 0,
                    onClick = { tabIndex = 0 }
                ) {
                    Text(
                        text = "Rating",
                        modifier = Modifier.padding(6.dp)
                    )
                }
                Tab(
                    selected = tabIndex == 1,
                    onClick = { tabIndex = 1 }
                ) {
                    Text(
                        text = "游玩次数",
                        modifier = Modifier.padding(6.dp)
                    )
                }
                Tab(
                    selected = tabIndex == 2,
                    onClick = { tabIndex = 2 }
                ) {
                    Text(
                        text = "平均精准度",
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
            ProvideChartStyle(m3ChartStyle()) {
                Chart(
                    chart = if (tabIndex == 1) columnChart() else lineChart(),
                    chartModelProducer = chartEntryModelProducer,
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = { value, _ ->
                            timeSeries[value.toInt()].date.replace("-", "w")
                        },
                        labelRotationDegrees = 90f
                    ),
                    startAxis = rememberStartAxis(
                        valueFormatter = { value, _ ->
                            when (tabIndex) {
                                0 -> value.setPrecision(2)
                                1 -> value.toInt().toString()
                                2 -> "${value.setPrecision(2)}%"
                                else -> "Error"
                            }
                        },
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
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            inner = shapeComponent(
                                shape = Shapes.pillShape,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            innerPaddingAll = 6.dp
                        ),
                        guideline = lineComponent(
                            color = Color(0x80808080),
                            thickness = 2.dp,
                            shape = DashedShape(Shapes.pillShape)
                        )
                    ).apply {
                        indicatorSize = 16.dp
                    }
                )
            }
        }
    }
}

@Composable
private fun CollectionsCard(profileGraphQL: ProfileGraphQL) {
    Card {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            var folded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "合集（共${profileGraphQL.data.profile.user.collectionsCount}个）",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                IconButton(onClick = { folded = !folded }) {
                    Icon(
                        imageVector = if (folded) Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowDown,
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
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    profileGraphQL.data.profile.user.collections.forEach {
                        Card {
                            Box {
                                AsyncImage(
                                    model = getImageRequestBuilderForCytoid(it.cover.thumbnail).build(),
                                    contentDescription = it.title,
                                    modifier = Modifier.fillMaxWidth(),
                                    contentScale = ContentScale.FillWidth
                                )
                                Column(
                                    Modifier
                                        .align(Alignment.BottomStart)
                                        .fillMaxWidth()
                                        .background(Color(0x80000000))
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = it.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = it.slogan,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "${it.levelCount}个关卡",
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .background(
                                            Color(0xFF414558),
                                            Shapes.pillShape.composeShape()
                                        )
                                        .padding(6.dp)
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
private fun LevelsCard(profileGraphQL: ProfileGraphQL) {
    Card {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            var folded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "上传的关卡（共${profileGraphQL.data.profile.user.levelsCount}个）",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                IconButton(onClick = { folded = !folded }) {
                    Icon(
                        imageVector = if (folded) Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowDown,
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
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    profileGraphQL.data.profile.user.levels.forEach {
                        LevelCard(level = it)
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentsColumn(comments: ArrayList<Comment>) {
    Column(
        modifier = Modifier.padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        var folded by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "留言（共${comments.size}个）",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            IconButton(onClick = { folded = !folded }) {
                Icon(
                    imageVector = if (folded) Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowDown,
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
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                comments.forEach {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AsyncImage(
                            model = getImageRequestBuilderForCytoid(it.owner.avatar.medium).build(),
                            contentDescription = it.owner.uid,
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                        )
                        Card(
                            Modifier.weight(9f)
                        ) {
                            Column(
                                Modifier.padding(6.dp)
                            ) {
                                Row {
                                    Text(
                                        text = it.owner.uid,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${
                                            (System.currentTimeMillis() - DateParser.parseISO8601Date(
                                                it.date
                                            ).time)
                                                .milliseconds.inWholeDays
                                        }天前"
                                    )
                                }
                                Text(text = it.content, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}