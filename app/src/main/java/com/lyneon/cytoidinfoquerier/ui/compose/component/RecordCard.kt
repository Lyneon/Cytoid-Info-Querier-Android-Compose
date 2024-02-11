package com.lyneon.cytoidinfoquerier.ui.compose.component

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.logic.DateParser
import com.lyneon.cytoidinfoquerier.logic.DateParser.formatToTimeString
import com.lyneon.cytoidinfoquerier.model.CytoidDeepLink
import com.lyneon.cytoidinfoquerier.model.graphql.UserRecord
import com.lyneon.cytoidinfoquerier.tool.extension.saveIntoClipboard
import com.lyneon.cytoidinfoquerier.tool.extension.saveIntoMediaStore
import com.lyneon.cytoidinfoquerier.tool.extension.setPrecision
import com.lyneon.cytoidinfoquerier.tool.extension.showDialog
import com.lyneon.cytoidinfoquerier.tool.extension.showToast
import com.lyneon.cytoidinfoquerier.tool.extension.toBitmap
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.microsoft.appcenter.crashes.Crashes
import dev.shreyaspatil.capturable.Capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.util.Locale
import kotlin.concurrent.thread

@Composable
fun RecordCard(record: UserRecord, recordIndex: Int? = null, keep2DecimalPlaces: Boolean = true) {
    val context = LocalContext.current as MainActivity
    val externalCacheStorageDir = context.externalCacheDir
    val captureController = rememberCaptureController()
    Capturable(
        controller = captureController,
        onCaptured = { imageBitmap: ImageBitmap?, throwable: Throwable? ->
            imageBitmap?.asAndroidBitmap()?.let {
                it.saveIntoMediaStore()
                context.getString(R.string.saved).showToast()
            }
            throwable?.let { throw it }
        }) {
        Card(
            Modifier
                .padding(6.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            AlertDialog
                                .Builder(context)
                                .setTitle("${recordIndex}. ${record.chart.level.title}")
                                .setItems(
                                    arrayOf(
                                        context.getString(R.string.view_in_cytoid),
                                        context.getString(R.string.copy_content),
                                        context.getString(R.string.save_illustration),
                                        context.getString(R.string.save_as_picture)
                                    )
                                ) { _, i: Int ->
                                    when (i) {
                                        0 -> if (BaseApplication.cytoidIsInstalled) {
                                            context.startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(
                                                        CytoidDeepLink.getCytoidLevelDeepLink(
                                                            record.chart.level.uid
                                                        )
                                                    )
                                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            )
                                        } else context
                                            .getString(R.string.cytoid_is_not_installed)
                                            .showToast()

                                        1 -> {
                                            AlertDialog
                                                .Builder(context)
                                                .setTitle(context.resources.getString(R.string.choose_copy_content))
                                                .setItems(
                                                    arrayOf(
                                                        record.chart.level.title,
                                                        record.chart.level.uid,
                                                        "${
                                                            record.chart.name
                                                                ?: record.chart.type.replaceFirstChar {
                                                                    if (it.isLowerCase()) it.titlecase(
                                                                        Locale.getDefault()
                                                                    ) else it.toString()
                                                                }
                                                        } ${record.chart.difficulty}",
                                                        record.score.toString(),
                                                        "Mods:${record.mods}",
                                                        "${
                                                            (record.accuracy * 100).run {
                                                                if (keep2DecimalPlaces) this.setPrecision(
                                                                    2
                                                                ) else this
                                                            }
                                                        }% accuracy  ${record.details.maxCombo} max combo",
                                                        "Rating ${
                                                            record.rating.run {
                                                                if (keep2DecimalPlaces) this.setPrecision(
                                                                    2
                                                                ) else this
                                                            }
                                                        }",
                                                        "Perfect ${record.details.perfect} Great ${record.details.great} Good ${record.details.good} Bad ${record.details.bad} Miss ${record.details.miss}",
                                                        DateParser
                                                            .parseISO8601Date(record.date)
                                                            .formatToTimeString(),
                                                        context.getString(R.string.all_contents),
                                                        "（仅调试）UserRecord对象"
                                                    )
                                                ) { _, j: Int ->
                                                    when (j) {
                                                        0 -> record.chart.level.title.saveIntoClipboard()
                                                        1 -> record.chart.level.uid.saveIntoClipboard()
                                                        2 -> "${
                                                            record.chart.name
                                                                ?: record.chart.type.replaceFirstChar {
                                                                    if (it.isLowerCase()) it.titlecase(
                                                                        Locale.getDefault()
                                                                    ) else it.toString()
                                                                }
                                                        } ${record.chart.difficulty}".saveIntoClipboard()

                                                        3 -> record.score
                                                            .toString()
                                                            .saveIntoClipboard()

                                                        4 -> "Mods:${record.mods}".saveIntoClipboard()

                                                        5 -> "${
                                                            (record.accuracy * 100).run {
                                                                if (keep2DecimalPlaces) this.setPrecision(
                                                                    2
                                                                ) else this
                                                            }
                                                        }% accuracy  ${record.details.maxCombo} max combo".saveIntoClipboard()

                                                        6 -> "Rating ${
                                                            record.rating.run {
                                                                if (keep2DecimalPlaces) this.setPrecision(
                                                                    2
                                                                ) else this
                                                            }
                                                        }".saveIntoClipboard()

                                                        7 -> "Perfect ${record.details.perfect} Great ${record.details.great} Good ${record.details.good} Bad ${record.details.bad} Miss ${record.details.miss}".saveIntoClipboard()
                                                        8 -> DateParser
                                                            .parseISO8601Date(record.date)
                                                            .formatToTimeString()
                                                            .saveIntoClipboard()

                                                        9 -> record
                                                            .detailsString()
                                                            .saveIntoClipboard()

                                                        10 -> record
                                                            .toString()
                                                            .saveIntoClipboard()
                                                    }

                                                }
                                                .create()
                                                .show()
                                        }

                                        2 -> {
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
                                                                ) {
                                                                    this.setPositiveButton(
                                                                        context.getString(
                                                                            R.string.confirm
                                                                        )
                                                                    ) { dialogInterface, _ ->
                                                                        dialogInterface.dismiss()
                                                                    }
                                                                }
                                                        }
                                                    }
                                            }
                                        }

                                        3 -> {
                                            context
                                                .getString(R.string.saving)
                                                .showToast()
                                            captureController.capture()
                                        }
                                    }
                                }
                                .create()
                                .show()
                        }
                    )
                }
        ) {
            Column(
                Modifier.padding(6.dp)
            ) {
                if (File(
                        externalCacheStorageDir,
                        "backgroundImage_${record.chart.level.uid}"
                    ).exists() && File(
                        externalCacheStorageDir,
                        "backgroundImage_${record.chart.level.uid}"
                    ).isFile
                ) {
                    val input = FileInputStream(
                        File(
                            externalCacheStorageDir,
                            "backgroundImage_${record.chart.level.uid}"
                        )
                    )
                    val bitmap = BitmapFactory.decodeStream(input)
                    Card {
                        Image(
                            painter = BitmapPainter(bitmap.asImageBitmap()),
                            contentDescription = record.chart.level.title,
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
                            var backgroundImageIsError by remember { mutableStateOf(false) }
                            Column {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(record.chart.level.bundle.backgroundImage.thumbnail)
                                        .crossfade(true)
                                        .setHeader(
                                            "User-Agent",
                                            "CytoidClient/2.1.1"
                                        )
                                        .crossfade(true)
                                        .error(R.drawable.sayakacry)
                                        .build(),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentDescription = record.chart.level.title,
                                    onSuccess = {
                                        val imageFile = File(
                                            externalCacheStorageDir,
                                            "backgroundImage_${record.chart.level.uid}"
                                        )
                                        try {
                                            imageFile.createNewFile()
                                            val output =
                                                FileOutputStream(imageFile)
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
                                        fontSize = LocalTextStyle.current.fontSize.times(2)
                                    )
                                }
                            }
                        }
                    }
                }
                recordIndex?.let {
                    Text(text = "#${it}.")
                }
                Text(
                    text = record.chart.level.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = LocalTextStyle.current.fontSize * 2,
                    lineHeight = LocalTextStyle.current.lineHeight * 1.5
                )
                Text(text = record.chart.level.uid)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = " ${
                        record.chart.name
                            ?: record.chart.type.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            }
                    } ${record.chart.difficulty} ",
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                when (record.chart.type) {
                                    "easy" -> listOf(
                                        Color(0xff4ca2cd),
                                        Color(0xff67b26f)
                                    )

                                    "hard" -> listOf(
                                        Color(0xff4568dc),
                                        Color(0xffb06abc)
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
                                        "HideNotes" -> R.drawable.hide_notes
                                        "HideScanline" -> R.drawable.hide_scanline
                                        "Slow" -> R.drawable.slow
                                        "Fast" -> R.drawable.fast
                                        "Hard" -> R.drawable.hyper
                                        "ExHard" -> R.drawable.another
                                        "AP" -> R.drawable.ap
                                        "FC" -> R.drawable.fc
                                        "FlipAll" -> R.drawable.flip_all
                                        "FlipX" -> R.drawable.flip_x
                                        "FlipY" -> R.drawable.flip_y
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
                Row {
                    Text(text = "Perfect")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = record.details.perfect.toString(),
                        color = Color(0xff60a5fa)
                    )
                }
                Row {
                    Text(text = "Great")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = record.details.great.toString(),
                        color = Color(0xfffacc15)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Good")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = record.details.good.toString(),
                        color = Color(0xff4ade80)
                    )
                }
                Row {
                    Text(text = "Bad")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = record.details.bad.toString(),
                        color = Color(0xfff87171)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Miss")
                    Spacer(modifier = Modifier.width(6.dp))
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
    }
}