package com.lyneon.cytoidinfoquerier.ui.compose.component

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Looper
import androidx.annotation.OptIn
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import coil.compose.AsyncImage
import com.lyneon.cytoidinfoquerier.BaseActivity
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.CytoidDeepLink
import com.lyneon.cytoidinfoquerier.data.model.graphql.UserRecord
import com.lyneon.cytoidinfoquerier.logic.DateParser
import com.lyneon.cytoidinfoquerier.logic.DateParser.formatToTimeString
import com.lyneon.cytoidinfoquerier.util.extension.getImageRequestBuilderForCytoid
import com.lyneon.cytoidinfoquerier.util.extension.saveIntoClipboard
import com.lyneon.cytoidinfoquerier.util.extension.saveIntoMediaStore
import com.lyneon.cytoidinfoquerier.util.extension.setPrecision
import com.lyneon.cytoidinfoquerier.util.extension.showDialog
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import com.lyneon.cytoidinfoquerier.util.extension.toBitmap
import com.microsoft.appcenter.crashes.Crashes
import dev.shreyaspatil.capturable.Capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.util.Locale
import kotlin.concurrent.thread

@kotlin.OptIn(ExperimentalLayoutApi::class)
@OptIn(UnstableApi::class)
@SuppressLint("CheckResult")
@Composable
fun RecordCard(record: UserRecord, recordIndex: Int? = null, keep2DecimalPlaces: Boolean = true) {
    val context = LocalContext.current as BaseActivity
    val externalCacheStorageDir = context.externalCacheDir
    val captureController = rememberCaptureController()
    var recordDialogState by remember { mutableStateOf(false) }
    var copyRecordContentDialogState by remember { mutableStateOf(false) }
    var mediaPlayerState by remember { mutableIntStateOf(Player.STATE_IDLE) }
    val exoPlayer = ExoPlayer.Builder(BaseApplication.context).build().apply {
        addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> mediaPlayerState = Player.STATE_ENDED
                    Player.STATE_BUFFERING -> mediaPlayerState = Player.STATE_BUFFERING
                    Player.STATE_IDLE -> mediaPlayerState = Player.STATE_IDLE
                    Player.STATE_READY -> mediaPlayerState = Player.STATE_READY
                }
            }
        })
    }

    Capturable(
        controller = captureController,
        onCaptured = { imageBitmap: ImageBitmap?, throwable: Throwable? ->
            imageBitmap?.asAndroidBitmap()?.let {
                it.saveIntoMediaStore()
                context.getString(R.string.saved).showToast()
            }
            throwable?.let { throw it }
        }
    ) {
        Card(
            Modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            recordDialogState = true
                        }
                    )
                }
        ) {
            Column(
                Modifier.padding(6.dp)
            ) {
                Box {
                    if (record.chart?.level != null) {
                        val level = record.chart.level
                        if (externalCacheStorageDir != null) {
                            val cacheBackgroundImagesDirectory =
                                File(externalCacheStorageDir.path + "/backgroundImage")
                            if (!cacheBackgroundImagesDirectory.exists()) cacheBackgroundImagesDirectory.mkdirs()
                            val cacheBackgroundImageFile =
                                File(
                                    cacheBackgroundImagesDirectory,
                                    level.uid
                                )
                            if (cacheBackgroundImageFile.isFile) {
                                val input = FileInputStream(cacheBackgroundImageFile)
                                val bitmap = BitmapFactory.decodeStream(input)
                                Card {
                                    Image(
                                        painter = BitmapPainter(bitmap.asImageBitmap()),
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
                                        var backgroundImageIsError by remember {
                                            mutableStateOf(
                                                false
                                            )
                                        }
                                        Column {
                                            AsyncImage(
                                                model = getImageRequestBuilderForCytoid(level.bundle.backgroundImage.thumbnail)
                                                    .build(),
                                                modifier = Modifier.fillMaxWidth(),
                                                contentDescription = level.title,
                                                onSuccess = {
                                                    try {
                                                        cacheBackgroundImageFile.createNewFile()
                                                        val output =
                                                            FileOutputStream(
                                                                cacheBackgroundImageFile
                                                            )
                                                        it.result.drawable.toBitmap()
                                                            .compress(
                                                                Bitmap.CompressFormat.PNG,
                                                                100,
                                                                output
                                                            )
                                                        output.flush()
                                                        output.close()
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                        e.stackTraceToString().showToast()
                                                        Crashes.trackError(e)
                                                    }
                                                },
                                                onError = {
                                                    backgroundImageIsError = true
                                                },
                                                contentScale = ContentScale.FillWidth,
                                            )
                                            AnimatedVisibility(
                                                visible = backgroundImageIsError,
                                                modifier = Modifier.align(Alignment.CenterHorizontally)
                                            ) {
                                                Text(
                                                    text = stringResource(id = R.string.imageError),
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = LocalTextStyle.current.fontSize.times(
                                                        2
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Card {
                            Image(
                                painter = painterResource(id = R.drawable.sayakacry),
                                contentDescription = "LevelTitle",
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    record.chart?.level?.let { level ->
                        IconButton(
                            onClick = {
                                if (mediaPlayerState != Player.STATE_READY) {
                                    val dataSourceFactory =
                                        DefaultHttpDataSource.Factory()
                                            .setDefaultRequestProperties(mapOf("User-Agent" to "CytoidClient/2.1.1"))
                                    val mediaItem = MediaItem.Builder()
                                        .setUri(
                                            Uri.parse(
                                                level.bundle.musicPreview ?: level.bundle.music
                                            )
                                        ).build()
                                    val internetAudioSource =
                                        ProgressiveMediaSource.Factory(dataSourceFactory)
                                            .createMediaSource(mediaItem)
                                    exoPlayer.setMediaSource(internetAudioSource)
                                    exoPlayer.prepare()
                                    exoPlayer.play()
                                } else {
                                    exoPlayer.stop()
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    CircleShape
                                )
                        ) {
                            if (mediaPlayerState == Player.STATE_BUFFERING) CircularProgressIndicator(
                                Modifier.padding(6.dp)
                            )
                            else Icon(
                                imageVector = if (mediaPlayerState == Player.STATE_READY) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "${if (mediaPlayerState == Player.STATE_READY) "停止" else "播放"}音乐预览"
                            )
                        }
                    }
                }
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
                                        "easy" -> listOf(
                                            Color(0xff4ca2cd),
                                            Color(0xff67b26f)
                                        )

                                        "extreme" -> listOf(
                                            Color(0xFF200122),
                                            Color(0xff6f0000)

                                        )

                                        else -> listOf(
                                            Color(0xff4568dc),
                                            Color(0xffb06abc)
                                        )
                                    }
                                ), RoundedCornerShape(CornerSize(100))
                            )
                            .padding(6.dp)
                    )
                }
                Text(
                    text = record.score.toString(),
                    fontSize = LocalTextStyle.current.fontSize.times(3)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (mod in record.mods) {
                        item {
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
                        color = Color(0xff60a5fa)
                    )
                    Text(text = "Great")
                    Text(
                        text = record.details.great.toString(),
                        color = Color(0xfffacc15)
                    )
                    Text(text = "Good")
                    Text(
                        text = record.details.good.toString(),
                        color = Color(0xff4ade80)
                    )
                    Text(text = "Bad")
                    Text(
                        text = record.details.bad.toString(),
                        color = Color(0xfff87171)
                    )
                    Text(text = "Miss")
                    Text(
                        text = record.details.miss.toString(),
                        color = Color(0xff94a3b8)
                    )
                }
                Text(
                    text = DateParser.parseISO8601Date(record.date)
                        .formatToTimeString()
                )
            }
        }
        if (recordDialogState) AlertDialog(
            onDismissRequest = { recordDialogState = false },
            confirmButton = {},
            title = {
                Text(
                    text = record.chart?.level?.title ?: "LevelTitle",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState())
                ) {
                    ListItem(
                        headlineContent = { Text(context.getString(R.string.view_in_cytoid)) },
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
                                } else context
                                    .getString(R.string.cytoid_is_not_installed)
                                    .showToast()
                            }
                        }
                    )
                    ListItem(
                        headlineContent = { Text(text = stringResource(id = R.string.view_in_cytoidIO)) },
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
                        headlineContent = { Text(context.getString(R.string.copy_content)) },
                        modifier = Modifier.clickable {
                            copyRecordContentDialogState = true
                        }
                    )
                    ListItem(
                        headlineContent = { Text(context.getString(R.string.save_illustration)) },
                        modifier = Modifier.clickable {
                            if (record.chart?.level?.uid == null) {
                                "谱面信息缺失，无法保存！".showToast()
                            } else {
                                context
                                    .getString(R.string.saving_illustration)
                                    .showToast()
                                thread {
                                    kotlin
                                        .runCatching {
                                            URL(record.chart.level.bundle.backgroundImage.original)
                                                .toBitmap()
                                                .saveIntoMediaStore(
                                                    context.contentResolver,
                                                    ContentValues()
                                                )
                                        }
                                        .onSuccess {
                                            Looper.prepare()
                                            context
                                                .getString(R.string.saved_into_gallery)
                                                .showToast()
                                        }
                                        .onFailure { e ->
                                            e.printStackTrace()
                                            context.runOnUiThread {
                                                e
                                                    .stackTraceToString()
                                                    .showDialog(
                                                        context,
                                                        context.getString(R.string.fail)
                                                    )
                                            }
                                        }
                                }
                            }
                        }
                    )
                    ListItem(
                        headlineContent = { Text(context.getString(R.string.save_as_picture)) },
                        modifier = Modifier.clickable {
                            context
                                .getString(R.string.saving)
                                .showToast()
                            captureController.capture()

                        }
                    )
                }
            }
        )
        if (copyRecordContentDialogState) AlertDialog(
            onDismissRequest = { copyRecordContentDialogState = false },
            confirmButton = {},
            title = {
                Text(
                    text = context.resources.getString(R.string.choose_copy_content),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState())
                ) {
                    if (record.chart?.level != null) {
                        ListItem(
                            headlineContent = { Text(record.chart.level.title) },
                            modifier = Modifier.clickable {
                                record.chart.level.title.saveIntoClipboard()
                            }
                        )
                        ListItem(
                            headlineContent = { Text(record.chart.level.uid) },
                            modifier = Modifier.clickable {
                                record.chart.level.uid.saveIntoClipboard()
                            }
                        )
                        ListItem(
                            headlineContent = {
                                Text("${
                                    record.chart.name
                                        ?: record.chart.type.replaceFirstChar {
                                            if (it.isLowerCase()) it.titlecase(
                                                Locale.getDefault()
                                            ) else it.toString()
                                        }
                                } ${record.chart.difficulty}")
                            },
                            modifier = Modifier.clickable {
                                "${
                                    record.chart.name
                                        ?: record.chart.type.replaceFirstChar {
                                            if (it.isLowerCase()) it.titlecase(
                                                Locale.getDefault()
                                            ) else it.toString()
                                        }
                                } ${record.chart.difficulty}".saveIntoClipboard()
                            }
                        )
                    }
                    ListItem(
                        headlineContent = { Text(record.score.toString()) },
                        modifier = Modifier.clickable {
                            record.score
                                .toString()
                                .saveIntoClipboard()
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Mods:${record.mods}") },
                        modifier = Modifier.clickable {
                            "Mods:${record.mods}".saveIntoClipboard()
                        }
                    )
                    ListItem(
                        headlineContent = {
                            Text("${
                                (record.accuracy * 100).run {
                                    if (keep2DecimalPlaces) this.setPrecision(
                                        2
                                    ) else this
                                }
                            }% accuracy  ${record.details.maxCombo} max combo")
                        },
                        modifier = Modifier.clickable {
                            "${
                                (record.accuracy * 100).run {
                                    if (keep2DecimalPlaces) this.setPrecision(
                                        2
                                    ) else this
                                }
                            }% accuracy  ${record.details.maxCombo} max combo".saveIntoClipboard()
                        }
                    )
                    ListItem(
                        headlineContent = {
                            Text("Rating ${
                                record.rating.run {
                                    if (keep2DecimalPlaces) this.setPrecision(
                                        2
                                    ) else this
                                }
                            }")
                        },
                        modifier = Modifier.clickable {
                            "Rating ${
                                record.rating.run {
                                    if (keep2DecimalPlaces) this.setPrecision(
                                        2
                                    ) else this
                                }
                            }".saveIntoClipboard()
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Perfect ${record.details.perfect} Great ${record.details.great} Good ${record.details.good} Bad ${record.details.bad} Miss ${record.details.miss}") },
                        modifier = Modifier.clickable {
                            "Perfect ${record.details.perfect} Great ${record.details.great} Good ${record.details.good} Bad ${record.details.bad} Miss ${record.details.miss}".saveIntoClipboard()
                        }
                    )
                    ListItem(
                        headlineContent = {
                            Text(
                                DateParser
                                    .parseISO8601Date(record.date)
                                    .formatToTimeString()
                            )
                        },
                        modifier = Modifier.clickable {
                            DateParser
                                .parseISO8601Date(record.date)
                                .formatToTimeString()
                                .saveIntoClipboard()

                        }
                    )
                    ListItem(
                        headlineContent = { Text(context.getString(R.string.all_contents)) },
                        modifier = Modifier.clickable {
                            record
                                .detailsString()
                                .saveIntoClipboard()
                        }
                    )
                    ListItem(
                        headlineContent = { Text("（仅调试）UserRecord对象") },
                        modifier = Modifier.clickable {
                            record
                                .toString()
                                .saveIntoClipboard()
                        }
                    )
                }
            }
        )
    }
}