package com.lyneon.cytoidinfoquerier.ui.compose.component

import android.app.AlertDialog
import android.content.Intent
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gaojc.util.DensityUtil
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.isDebugging
import com.lyneon.cytoidinfoquerier.logic.model.Profile
import com.lyneon.cytoidinfoquerier.logic.model.CytoidDeepLink
import com.lyneon.cytoidinfoquerier.tool.DateParser
import com.lyneon.cytoidinfoquerier.tool.DateParser.formatToBeijingTimeString
import com.lyneon.cytoidinfoquerier.tool.fix
import com.lyneon.cytoidinfoquerier.tool.saveIntoClipboard
import com.lyneon.cytoidinfoquerier.tool.showToast
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale

@Composable
fun RecordCard(record: Profile.UserRecord, recordIndex: Int? = null) {
    val context = LocalContext.current as MainActivity
    val externalCacheStorageDir = context.externalCacheDir
    Card(
        Modifier
            .padding(6.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        AlertDialog
                            .Builder(context)
                            .setItems(
                                arrayOf(
                                    context.resources.getString(R.string.view_in_cytoid),
                                    context.resources.getString(R.string.copy_content)
                                )
                            ) { _, i: Int ->
                                when (i) {
                                    0 -> context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(CytoidDeepLink.getDeepLink(record.chart.level.uid))
                                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )

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
                                                    record.mods.toString(),
                                                    "${(record.accuracy * 100).fix(2)}% accuracy  ${record.details.maxCombo} max combo",
                                                    "Rating ${record.rating.fix(2)}",
                                                    "Perfect ${record.details.perfect} Great ${record.details.great} Good ${record.details.good} Bad ${record.details.bad} Miss ${record.details.miss}",
                                                    DateParser
                                                        .parseISO8601Date(record.date)
                                                        .formatToBeijingTimeString()
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

                                                    4 -> record.mods
                                                        .toString()
                                                        .saveIntoClipboard()

                                                    5 -> "${(record.accuracy * 100).fix(2)}% accuracy  ${record.details.maxCombo} max combo".saveIntoClipboard()
                                                    6 -> "Rating ${record.rating.fix(2)}".saveIntoClipboard()
                                                    7 -> "Perfect ${record.details.perfect} Great ${record.details.great} Good ${record.details.good} Bad ${record.details.bad} Miss ${record.details.miss}".saveIntoClipboard()
                                                    8 -> DateParser
                                                        .parseISO8601Date(record.date)
                                                        .formatToBeijingTimeString()
                                                        .saveIntoClipboard()
                                                }
                                            }
                                            .create()
                                            .show()
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
                fontSize = LocalTextStyle.current.fontSize.times(2)
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
                                "easy" -> {
                                    listOf(
                                        Color(0xff4ca2cd),
                                        Color(0xff67b26f)
                                    )
                                }

                                "hard" -> {
                                    listOf(
                                        Color(0xffb06abc),
                                        Color(0xff4568dc)
                                    )
                                }

                                "extreme" -> {
                                    listOf(
                                        Color(0xff6f0000),
                                        Color(0xFF200122)
                                    )
                                }

                                else -> {
                                    listOf(
                                        Color(0xffb06abc),
                                        Color(0xff4568dc)
                                    )
                                }
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
            Text(text = "${(record.accuracy * 100).fix(2)}% accuracy  ${record.details.maxCombo} max combo")
            Text(
                modifier = Modifier.background(
                    Color.hsl(230f, 0.15f, 0.5f),
                    RoundedCornerShape(CornerSize(100))
                ),
                text = " Rating ${record.rating.fix(2)} ",
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
                    .formatToBeijingTimeString()
            )

            /*
             Debug Components
             */
            if (isDebugging) {
                val renderText =
                    "${(record.accuracy * 100).fix(2)}% accuracy  ${record.details.maxCombo} max combo"
                Text(
                    text = "Text render width:${
                        rememberTextMeasurer().measure(renderText).size.width.toFloat()
                    }px ${
                        DensityUtil.pxToDp(
                            BaseApplication.context,
                            rememberTextMeasurer().measure(renderText).size.width.toFloat()
                        )
                    }dp"
                )
            }
        }
    }
}