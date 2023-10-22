package com.lyneon.cytoidinfoquerier.ui.compose

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.logic.model.GQLQueryResponseData
import com.lyneon.cytoidinfoquerier.logic.model.Profile
import com.lyneon.cytoidinfoquerier.logic.model.ProfileData
import com.lyneon.cytoidinfoquerier.logic.network.NetRequest
import com.lyneon.cytoidinfoquerier.tool.DateParser
import com.lyneon.cytoidinfoquerier.tool.DateParser.formatToBeijingTimeString
import com.lyneon.cytoidinfoquerier.tool.fix
import com.lyneon.cytoidinfoquerier.tool.isValidCytoidID
import com.lyneon.cytoidinfoquerier.tool.showToast
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mmkv.MMKV
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale
import kotlin.concurrent.thread

lateinit var profile: GQLQueryResponseData<ProfileData>

@Composable
fun BestRecordCompose(navController: NavController) {
    val context = LocalContext.current as MainActivity
    var cytoidID by remember { mutableStateOf("") }
    var isQueryingFinished by remember { mutableStateOf(false) }
    val mmkv = MMKV.defaultMMKV()

    Column {
        TopBar(navController = navController, true)
        Column(modifier = Modifier.padding(6.dp, 6.dp, 6.dp)) {
            Column {
                var textFieldIsError by remember { mutableStateOf(false) }
                var textFieldIsEmpty by remember { mutableStateOf(false) }
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
                        TextButton(
                            onClick = {
                                if (cytoidID.isEmpty()) {
                                    context.resources.getString(R.string.empty_cytoidID).showToast()
                                    textFieldIsEmpty = true
                                } else if (!cytoidID.isValidCytoidID()) {
                                    context.resources.getString(R.string.invalid_cytoidID)
                                        .showToast()
                                    textFieldIsError = true
                                } else {
                                    textFieldIsError = false
                                    isQueryingFinished = false
                                    if (System.currentTimeMillis() - mmkv.decodeLong(
                                            "lastQueryProfileTime_${cytoidID}",
                                            -1
                                        ) <= (6 * 60 * 60 * 1000)
                                    ) {
                                        "6小时内有查询记录，使用已缓存的数据".showToast()
                                        profile = NetRequest.convertGQLResponseJSONStringToObject(
                                            mmkv.decodeString("profileString_${cytoidID}")
                                                ?: throw Exception()
                                        )
                                        isQueryingFinished = true
                                    } else {
                                        "开始查询$cytoidID".showToast()
                                        thread {
                                            try {
                                                val profileString =
                                                    NetRequest.getGQLResponseJSONString(
                                                        Profile.getGQLQueryString(
                                                            cytoidID,
                                                            bestRecordsLimit = 30
                                                        )
                                                    )
                                                profile =
                                                    NetRequest.convertGQLResponseJSONStringToObject(
                                                        profileString
                                                    )
                                                isQueryingFinished = true
                                                mmkv.encode(
                                                    "lastQueryProfileTime_${cytoidID}",
                                                    System.currentTimeMillis()
                                                )
                                                mmkv.encode(
                                                    "profileString_${cytoidID}",
                                                    profileString
                                                )
                                                Looper.prepare()
                                                "查询${cytoidID}完成，共查询到${profile.data.profile.bestRecords.size}条数据".showToast()
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                CrashReport.postCatchedException(
                                                    e.cause,
                                                    Thread.currentThread()
                                                )
                                                Looper.prepare()
                                                "查询失败:${e.stackTraceToString()}".showToast()
                                            }
                                        }
                                    }
                                }
                            }
                        ) {
                            Text(text = stringResource(id = R.string.b30))
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
            LazyVerticalStaggeredGrid(
                columns = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT)
                    StaggeredGridCells.Fixed(1)
                else StaggeredGridCells.Adaptive(320.dp),
                contentPadding = PaddingValues(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isQueryingFinished && ::profile.isInitialized) {
                    for (i in 0 until profile.data.profile.bestRecords.size) {
                        val record = profile.data.profile.bestRecords[i]
                        item(
                            span = if (profile.data.profile.bestRecords.size == 1) StaggeredGridItemSpan.FullLine
                            else StaggeredGridItemSpan.SingleLane
                        ) {
                            RecordCard(
                                record = record,
                                recordIndex = i + 1
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordCard(record: Profile.UserRecord, recordIndex: Int) {
    val context = LocalContext.current as MainActivity
    val externalCacheStorageDir = context.externalCacheDir
    Card(
        Modifier.padding(6.dp)
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
                    Box{
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
            Text(text = "#${recordIndex}.")
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
        }
    }
}