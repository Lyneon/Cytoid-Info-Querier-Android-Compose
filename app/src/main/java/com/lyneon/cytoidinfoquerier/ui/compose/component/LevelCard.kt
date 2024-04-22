package com.lyneon.cytoidinfoquerier.ui.compose.component

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.lyneon.cytoidinfoquerier.data.constant.CytoidColors
import com.lyneon.cytoidinfoquerier.data.model.graphql.ProfileGraphQL
import com.lyneon.cytoidinfoquerier.util.extension.getImageRequestBuilderForCytoid
import com.lyneon.cytoidinfoquerier.util.extension.saveIntoMediaStore
import com.lyneon.cytoidinfoquerier.util.extension.showDialog
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import com.lyneon.cytoidinfoquerier.util.extension.toBitmap
import com.patrykandpatrick.vico.compose.component.shape.composeShape
import com.patrykandpatrick.vico.core.component.shape.Shapes
import java.net.URL
import java.util.Locale
import kotlin.concurrent.thread

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LevelCard(level: ProfileGraphQL.ProfileData.Profile.User.UserLevel) {
    val context = LocalContext.current as BaseActivity
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
    var levelDialogState by remember { mutableStateOf(false) }

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
            AsyncImage(
                model = getImageRequestBuilderForCytoid(level.bundle.backgroundImage.thumbnail).build(),
                contentDescription = level.title,
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
                    text = level.title,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                level.description?.let { description ->
                    Text(
                        text = description,
                        color = Color.White,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = level.metadata.artist.name,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                .padding(6.dp)
                        )
                    }
                }
            }
            IconButton(
                onClick = {
                    if (mediaPlayerState != Player.STATE_READY) {
                        val dataSourceFactory =
                            DefaultHttpDataSource.Factory()
                                .setDefaultRequestProperties(mapOf("User-Agent" to "CytoidClient/2.1.1"))
                        val mediaItem = MediaItem.Builder()
                            .setUri(Uri.parse(level.bundle.musicPreview ?: level.bundle.music))
                            .build()
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
                            kotlin
                                .runCatching {
                                    URL(level.bundle.backgroundImage.original)
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
                )
            }
        }
    )
}