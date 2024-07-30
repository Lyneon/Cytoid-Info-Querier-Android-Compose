package com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.component

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.webapi.ProfileDetails
import com.lyneon.cytoidinfoquerier.util.extension.getImageRequestBuilderForCytoid
import com.lyneon.cytoidinfoquerier.util.extension.setPrecision
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import java.io.FileInputStream
import java.io.FileOutputStream

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserDetailsHeader(
    cytoidID: String,
    profileDetails: ProfileDetails,
    keep2DecimalPlaces: Boolean
) {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        UserAvatar(cytoidID = cytoidID, profileDetails = profileDetails)
        Column(
            modifier = Modifier.fillMaxHeight()
        ) {
            Text(text = profileDetails.user.uid, style = MaterialTheme.typography.titleLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Lv. ${profileDetails.exp.currentLevel}",
                    color = Color.Black,
                    modifier = Modifier
                        .background(Color(0xFF9EB3FF), RoundedCornerShape(100))
                        .padding(horizontal = 6.dp)
                )
                Text(
                    text = "Rating ${
                        profileDetails.rating.run {
                            if (keep2DecimalPlaces) setPrecision(2) else this
                        }
                    }",
                    color = Color.Black,
                    modifier = Modifier
                        .background(Color(0xFF6AF5FF), RoundedCornerShape(100))
                        .padding(horizontal = 6.dp)
                )
                profileDetails.tier?.let {
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
private fun UserAvatar(cytoidID: String, profileDetails: ProfileDetails) {
    val localAvatarFile =
        LocalDataSource.getAvatarBitmapFile(cytoidID, LocalDataSource.AvatarSize.LARGE)
    if (localAvatarFile.exists() && localAvatarFile.isFile) {
        val bitmap = FileInputStream(localAvatarFile).use {
            BitmapFactory.decodeStream(it)
        }
        Image(
            modifier = Modifier
                .clip(CircleShape)
                .heightIn(max = 96.dp)
                .clickable {
                    BaseApplication.context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://cytoid.io/profile/${profileDetails.user.uid}")
                        )
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .addCategory(Intent.CATEGORY_BROWSABLE)
                    )
                },
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null
        )
    } else {
        Box(
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
            AsyncImage(
                modifier = Modifier
                    .clip(CircleShape)
                    .heightIn(max = 96.dp)
                    .clickable {
                        BaseApplication.context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://cytoid.io/profile/${profileDetails.user.uid}")
                            )
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .addCategory(Intent.CATEGORY_BROWSABLE)
                        )
                    },
                model = getImageRequestBuilderForCytoid(profileDetails.user.avatar.large)
                    .build(),
                contentDescription = null,
                onSuccess = { successState ->
                    try {
                        localAvatarFile.run {
                            this.createNewFile()
                            FileOutputStream(this)
                        }.use { output ->
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
                }
            )
        }
    }
}