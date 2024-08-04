package com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.component

import android.content.ContentValues
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import coil.compose.AsyncImage
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.CytoidDeepLink
import com.lyneon.cytoidinfoquerier.data.constant.CytoidColors
import com.lyneon.cytoidinfoquerier.data.constant.CytoidScoreRange
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.type.UserRecord
import com.lyneon.cytoidinfoquerier.util.DateParser
import com.lyneon.cytoidinfoquerier.util.DateParser.formatToTimeString
import com.lyneon.cytoidinfoquerier.util.extension.getImageRequestBuilderForCytoid
import com.lyneon.cytoidinfoquerier.util.extension.saveIntoClipboard
import com.lyneon.cytoidinfoquerier.util.extension.saveIntoMediaStore
import com.lyneon.cytoidinfoquerier.util.extension.setPrecision
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import com.lyneon.cytoidinfoquerier.util.extension.toBitmap
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.net.URL
import java.util.Locale
import kotlin.concurrent.thread

@OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeApi::class)
@Composable
fun RecordCard(
    cytoidId: String,
    record: UserRecord,
    recordIndex: Int?,
    keep2DecimalPlaces: Boolean,
    exoPlayer: ExoPlayer?,
    playbackState: Int?
) {
    val captureController = rememberCaptureController()
    var showActionsDialog by remember { mutableStateOf(false) }
    var showCopyDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        showActionsDialog = true
                    }
                )
            }
            .capturable(captureController)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            record.chart?.level?.let { level ->
                Box {
                    RecordCardBackgroundImage(level = level)
                    Box(
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        if (exoPlayer != null && playbackState != null) {
                            RecordCardMusicPreviewButton(
                                exoPlayer = exoPlayer,
                                playbackState = playbackState,
                                musicPreviewUrl = record.chart.level.bundle?.musicPreview.toString()
                            )
                        }
                    }
                }
            }
            RecordCardDetails(
                cytoidId = cytoidId,
                record = record,
                recordIndex = recordIndex,
                keep2DecimalPlaces = keep2DecimalPlaces
            )
        }
    }

    if (showActionsDialog) {
        AlertDialog(
            onDismissRequest = { showActionsDialog = false },
            confirmButton = { },
            title = { Text(record.chart?.level?.title ?: "LevelTitle") },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState())
                ) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.view_in_cytoid)) },
                        modifier = Modifier.clickable {
                            if (record.chart?.level?.uid == null) {
                                "谱面信息缺失，无法查看！".showToast()
                            } else {
                                if (BaseApplication.cytoidIsInstalled) {
                                    BaseApplication.context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(CytoidDeepLink.getCytoidLevelDeepLink(record.chart.level.uid))
                                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                } else BaseApplication.context
                                    .getString(R.string.cytoid_is_not_installed).showToast()
                            }
                        }
                    )
                    ListItem(
                        headlineContent = { Text(text = stringResource(id = R.string.view_in_cytoid_io)) },
                        modifier = Modifier.clickable {
                            if (record.chart?.level?.uid == null) {
                                "谱面信息缺失，无法查看！".showToast()
                            } else {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://cytoid.io/levels/${record.chart.level.uid}")
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                BaseApplication.context.startActivity(intent)
                            }
                        }
                    )
                    ListItem(
                        headlineContent = { Text(BaseApplication.context.getString(R.string.copy_content)) },
                        modifier = Modifier.clickable { showCopyDialog = true }
                    )
                    ListItem(
                        headlineContent = { Text(BaseApplication.context.getString(R.string.save_illustration)) },
                        modifier = Modifier.clickable {
                            if (record.chart?.level?.uid == null) {
                                "谱面信息缺失，无法保存！".showToast()
                            } else {
                                BaseApplication.context
                                    .getString(R.string.saving_illustration)
                                    .showToast()
                                thread {
                                    kotlin.runCatching {
                                        URL(record.chart.level.bundle?.backgroundImage?.original)
                                            .toBitmap()
                                            .saveIntoMediaStore(
                                                BaseApplication.context.contentResolver,
                                                ContentValues()
                                            )
                                    }.onSuccess {
                                        BaseApplication.context
                                            .getString(R.string.saved_into_gallery)
                                            .showToast()
                                    }.onFailure {
                                        BaseApplication.context.getString(R.string.fail)
                                            .showToast()
                                    }
                                }
                            }
                        }
                    )
                    ListItem(
                        headlineContent = { Text(BaseApplication.context.getString(R.string.save_as_picture)) },
                        modifier = Modifier.clickable {
                            BaseApplication.context
                                .getString(R.string.saving)
                                .showToast()
                            coroutineScope.launch(Dispatchers.IO) {
                                val bitmap = captureController.captureAsync().await()
                                bitmap.asAndroidBitmap().saveIntoMediaStore()
                                BaseApplication.context.getString(R.string.saved).showToast()
                            }
                        }
                    )
                }
            }
        )
    }
    if (showCopyDialog) {
        AlertDialog(
            onDismissRequest = { showCopyDialog = false },
            confirmButton = { },
            title = { Text(stringResource(R.string.choose_copy_content)) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState())
                ) {
                    record.chart?.let { chart ->
                        chart.level?.let { level ->
                            val copyContents = listOf(
                                level.title,
                                level.uid,
                                "${
                                    chart.difficultyName ?: chart.difficultyType.replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                                    }
                                } ${chart.difficultyLevel}",
                                record.score.toString(),
                                "Mods:${record.mods}",
                                "${
                                    (record.accuracy * 100).run {
                                        if (keep2DecimalPlaces) this.setPrecision(
                                            2
                                        ) else this
                                    }
                                }% accuracy | ${record.details.maxCombo} max combo",
                                "${(record.rating).run { if (keep2DecimalPlaces) this.setPrecision(2) else this }}",
                                "Perfect ${record.details.perfect} Great ${record.details.great} Good ${record.details.good} Bad ${record.details.bad} Miss ${record.details.miss}",
                                "$cytoidId | ${
                                    DateParser.parseISO8601Date(record.date).formatToTimeString()
                                }",
                                record.toString()
                            )

                            copyContents.forEach { copyContent ->
                                ListItem(
                                    headlineContent = { Text(copyContent) },
                                    modifier = Modifier.clickable { copyContent.saveIntoClipboard() }
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun RecordCardBackgroundImage(level: UserRecord.RecordChart.RecordLevel) {
    val scope = rememberCoroutineScope()

    val currentLevelLocalBackgroundImageFile = LocalDataSource.getBackgroundImageBitmapFile(
        level.uid,
        LocalDataSource.BackgroundImageSize.ORIGINAL
    )
    if (currentLevelLocalBackgroundImageFile.isFile) {
        val bitmap = FileInputStream(currentLevelLocalBackgroundImageFile).use {
            BitmapFactory.decodeStream(it)
        }
        Card {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = level.title,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        Card {
            Box {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                AsyncImage(
                    model = getImageRequestBuilderForCytoid(level.bundle?.backgroundImage?.original.toString())
                        .build(),
                    modifier = Modifier.fillMaxWidth(),
                    contentDescription = level.title,
                    onSuccess = { successState ->
                        try {
                            scope.launch(Dispatchers.IO) {
                                LocalDataSource.saveBackgroundImageBitmap(
                                    level.uid,
                                    LocalDataSource.BackgroundImageSize.ORIGINAL,
                                    successState.result.drawable.toBitmap()
                                )
                            }
                        } catch (e: Exception) {
                            e.message?.showToast()
                        }
                    },
                    contentScale = ContentScale.FillWidth,
                )
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun RecordCardMusicPreviewButton(
    exoPlayer: ExoPlayer,
    playbackState: Int,
    musicPreviewUrl: String
) {
    AnimatedVisibility(visible = playbackState == ExoPlayer.STATE_IDLE || playbackState == ExoPlayer.STATE_ENDED) {
        IconButton(
            onClick = {
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
private fun RecordCardDetails(
    cytoidId: String,
    record: UserRecord,
    recordIndex: Int?,
    keep2DecimalPlaces: Boolean
) {
    Column {
        recordIndex?.let {
            Text(text = "#${it}.")
        }
        LevelInfoText(record.chart?.level)
        Spacer(modifier = Modifier.height(8.dp))
        record.chart?.let {
            DifficultyPillText(
                it.difficultyName,
                it.difficultyLevel,
                it.difficultyType
            )
        }
        ScoreText(record.score)
        ModsLazyRow(record.mods)
        AccuracyAndMaxComboText(record.accuracy, record.details.maxCombo, keep2DecimalPlaces)
        RatingPillText(record.rating, keep2DecimalPlaces)
        JudgementDetailsFlowRow(record.details)
        PlayerAndDateText(cytoidId, record.date)
    }
}

@Composable
private fun LevelInfoText(level: UserRecord.RecordChart.RecordLevel?) {
    Text(
        text = level?.title ?: "LevelTitle",
        fontWeight = FontWeight.SemiBold,
        fontSize = LocalTextStyle.current.fontSize * 2,
        lineHeight = LocalTextStyle.current.lineHeight * 1.5
    )
    Text(text = level?.uid ?: "LevelUid")
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

@Composable
private fun ScoreText(score: Int) {
    Text(
        text = score.toString(),
        fontSize = LocalTextStyle.current.fontSize.times(3),
        style = LocalTextStyle.current.copy(
            brush = when (score) {
                CytoidScoreRange.max -> Brush.linearGradient(CytoidColors.maxColor)
                in CytoidScoreRange.sss -> Brush.linearGradient(CytoidColors.sssColor)
                else -> null
            }
        )
    )
}

@Composable
private fun AccuracyAndMaxComboText(accuracy: Float, maxCombo: Int, keep2DecimalPlaces: Boolean) {
    Text(
        text = "${
            (accuracy * 100).run {
                if (keep2DecimalPlaces) this.setPrecision(
                    2
                ) else this
            }
        }% accuracy | $maxCombo max combo"
    )
}

@Composable
private fun ModsLazyRow(mods: List<String>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (mod in mods) {
            item {
                var modTextIsVisible by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { modTextIsVisible = !modTextIsVisible }
                            )
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                        modifier = Modifier
                            .height(32.dp)
                    )
                    AnimatedVisibility(visible = modTextIsVisible) {
                        Text(text = mod)
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingPillText(rating: Float, keep2DecimalPlaces: Boolean) {
    Text(
        modifier = Modifier.background(
            Color.hsl(230f, 0.15f, 0.5f),
            RoundedCornerShape(CornerSize(100))
        ),
        text = " Rating ${
            rating.run {
                if (keep2DecimalPlaces) this.setPrecision(2) else this
            }
        } ",
        color = Color.White
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JudgementDetailsFlowRow(judgementDetails: UserRecord.RecordDetails) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = "Perfect")
        Text(
            text = judgementDetails.perfect.toString(),
            color = CytoidColors.perfectColor
        )
        Text(text = "Great")
        Text(
            text = judgementDetails.great.toString(),
            color = CytoidColors.greatColor
        )
        Text(text = "Good")
        Text(
            text = judgementDetails.good.toString(),
            color = CytoidColors.goodColor
        )
        Text(text = "Bad")
        Text(
            text = judgementDetails.bad.toString(),
            color = CytoidColors.badColor
        )
        Text(text = "Miss")
        Text(
            text = judgementDetails.miss.toString(),
            color = CytoidColors.missColor
        )
    }

}

@Composable
private fun PlayerAndDateText(cytoidId: String, date: String) {
    Text(
        text = "$cytoidId | ${
            DateParser.parseISO8601Date(date)
                .formatToTimeString()
        }"
    )
}