package com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.component

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import com.lyneon.cytoidinfoquerier.data.constant.CytoidColors
import com.lyneon.cytoidinfoquerier.data.constant.CytoidScoreRange
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.type.UserRecord
import com.lyneon.cytoidinfoquerier.util.DateParser
import com.lyneon.cytoidinfoquerier.util.DateParser.formatToTimeString
import com.lyneon.cytoidinfoquerier.util.extension.getImageRequestBuilderForCytoid
import com.lyneon.cytoidinfoquerier.util.extension.setPrecision
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale

@Composable
fun RecordCard(
    record: UserRecord,
    recordIndex: Int?,
    keep2DecimalPlaces: Boolean,
    exoPlayer: ExoPlayer?,
    playbackState: Int?
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
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
                record = record,
                recordIndex = recordIndex,
                keep2DecimalPlaces = keep2DecimalPlaces
            )
        }
    }
}

@Composable
private fun RecordCardBackgroundImage(level: UserRecord.RecordChart.RecordLevel) {
    val localBackgroundImagesDir =
        BaseApplication.context.getExternalFilesDir("/background_images")
    val currentLevelLocalBackgroundImageFile = localBackgroundImagesDir?.run {
        if (!this.exists()) this.mkdirs()
        File(this, level.uid)
    }
    if (currentLevelLocalBackgroundImageFile != null && currentLevelLocalBackgroundImageFile.isFile) {
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
                    model = getImageRequestBuilderForCytoid(level.bundle?.backgroundImage?.thumbnail.toString())
                        .build(),
                    modifier = Modifier.fillMaxWidth(),
                    contentDescription = level.title,
                    onSuccess = { successState ->
                        try {
                            currentLevelLocalBackgroundImageFile?.run {
                                this.createNewFile()
                                FileOutputStream(this)
                            }?.use { output ->
                                successState.result.drawable.toBitmap()
                                    .compress(
                                        Bitmap.CompressFormat.PNG,
                                        100,
                                        output
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
                .padding(8.dp),
            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "播放音乐预览"
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecordCardDetails(record: UserRecord, recordIndex: Int?, keep2DecimalPlaces: Boolean) {
    Column {
        recordIndex?.let {
            Text(text = "#${it}.")
        }
        Text(
            text = record.chart?.level?.title ?: "LevelTitle",
            fontWeight = FontWeight.SemiBold,
            fontSize = LocalTextStyle.current.fontSize * 2,
            lineHeight = LocalTextStyle.current.lineHeight * 1.5
        )
        Text(text = record.chart?.level?.uid ?: "LevelUid")
        Spacer(modifier = Modifier.height(6.dp))
        record.chart?.let { chart ->
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
                        ), RoundedCornerShape(CornerSize(100))
                    )
                    .padding(6.dp)
            )
        }
        Text(
            text = record.score.toString(),
            fontSize = LocalTextStyle.current.fontSize.times(3),
            style = LocalTextStyle.current.copy(
                brush = when (record.score) {
                    CytoidScoreRange.max -> Brush.linearGradient(CytoidColors.maxColor)
                    in CytoidScoreRange.sss -> Brush.linearGradient(CytoidColors.sssColor)
                    else -> null
                }
            )
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (mod in record.mods) {
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
        Text(
            text = "${
                (record.accuracy * 100).run {
                    if (keep2DecimalPlaces) this.setPrecision(
                        2
                    ) else this
                }
            }% accuracy  ${record.details.maxCombo} max combo"
        )
        Text(
            modifier = Modifier.background(
                Color.hsl(230f, 0.15f, 0.5f),
                RoundedCornerShape(CornerSize(100))
            ),
            text = " Rating ${
                record.rating.run {
                    if (keep2DecimalPlaces) this.setPrecision(
                        2
                    ) else this
                }
            } ",
            color = Color.White
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "Perfect")
            Text(
                text = record.details.perfect.toString(),
                color = CytoidColors.perfectColor
            )
            Text(text = "Great")
            Text(
                text = record.details.great.toString(),
                color = CytoidColors.greatColor
            )
            Text(text = "Good")
            Text(
                text = record.details.good.toString(),
                color = CytoidColors.goodColor
            )
            Text(text = "Bad")
            Text(
                text = record.details.bad.toString(),
                color = CytoidColors.badColor
            )
            Text(text = "Miss")
            Text(
                text = record.details.miss.toString(),
                color = CytoidColors.missColor
            )
        }
        Text(
            text = DateParser.parseISO8601Date(record.date)
                .formatToTimeString()
        )
    }
}