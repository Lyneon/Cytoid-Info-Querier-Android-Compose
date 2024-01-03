package com.lyneon.cytoidinfoquerier.ui.compose

import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.logic.network.NetRequest
import com.lyneon.cytoidinfoquerier.model.webapi.Profile
import com.lyneon.cytoidinfoquerier.tool.extension.isValidCytoidID
import com.lyneon.cytoidinfoquerier.tool.extension.setPrecision
import com.lyneon.cytoidinfoquerier.tool.extension.showDialog
import com.lyneon.cytoidinfoquerier.tool.extension.showToast
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.lyneon.cytoidinfoquerier.ui.compose.component.TopBar
import com.microsoft.appcenter.crashes.Crashes
import kotlin.concurrent.thread

lateinit var profile: Profile

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
                                        profile = NetRequest.getProfile(cytoidID)
                                        Looper.prepare()
                                        "查询${cytoidID}完成".showToast()
                                        isQueryingFinished = true
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
            AnimatedVisibility(visible = isQueryingFinished && ::profile.isInitialized) {
                Column {
                    Row {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(profile.user.avatar.original)
                                .crossfade(true)
                                .setHeader(
                                    "User-Agent",
                                    "CytoidClient/2.1.1"
                                )
                                .crossfade(true)
                                .error(R.drawable.sayakacry)
                                .build(),
                            contentDescription = profile.user.uid,
                            modifier = Modifier
                                .clip(CircleShape)
                                .width(160.dp)
                        )
                        Column {
                            Text(
                                text = profile.user.uid,
                                fontSize = LocalTextStyle.current.fontSize.times(2)
                            )
                            Row {
                                profile.tier?.run {
                                    val tierBackgroundColors = mutableListOf<Color>()
                                    this.colorPalette.background.split(",").forEach {
                                        if (it.startsWith("#")) tierBackgroundColors.add(
                                            Color(android.graphics.Color.parseColor(it))
                                        )
                                    }
                                    Text(
                                        text = this.name,
                                        modifier = Modifier
                                            .background(
                                                brush = Brush.linearGradient(tierBackgroundColors),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(6.dp)
                                    )
                                }
                                Text(
                                    text = "Level ${profile.exp.currentLevel}",
                                    modifier = Modifier
                                        .background(
                                            color = Color.LightGray,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(6.dp)
                                )
                                Text(
                                    text = "Rating ${profile.rating.setPrecision(2)}",
                                    modifier = Modifier
                                        .background(
                                            color = Color.LightGray,
                                            shape = RoundedCornerShape(6.dp)
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