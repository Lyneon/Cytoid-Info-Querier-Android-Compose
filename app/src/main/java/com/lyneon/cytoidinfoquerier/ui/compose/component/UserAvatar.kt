package com.lyneon.cytoidinfoquerier.ui.compose.component

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.data.enums.AvatarSize
import com.lyneon.cytoidinfoquerier.data.model.webapi.ProfileDetails
import com.lyneon.cytoidinfoquerier.util.extension.getImageRequestBuilderForCytoid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileInputStream

@Composable
fun UserAvatar(
    modifier: Modifier = Modifier,
    userUid: String,
    avatarSize: AvatarSize = AvatarSize.LARGE,
    remoteAvatarUrl: String
) {
    Box(
        modifier = modifier
    ) {
        val localAvatarFile =
            LocalDataSource.getAvatarBitmapFile(
                userUid,
                avatarSize
            )
        if (localAvatarFile.exists() && localAvatarFile.isFile) {
            val bitmap = FileInputStream(localAvatarFile).use {
                BitmapFactory.decodeStream(it)
            }
            Image(
                modifier = Modifier
                    .heightIn(max = 96.dp)
                    .clip(CircleShape)
                    .clickable {
                        BaseApplication.context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://cytoid.io/profile/${userUid}")
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
                contentAlignment = Alignment.Center,
                modifier = Modifier.heightIn(max = 96.dp)
            ) {
                CircularProgressIndicator()
                AsyncImage(
                    modifier = Modifier
                        .heightIn(max = 96.dp)
                        .clip(CircleShape)
                        .clickable {
                            BaseApplication.context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://cytoid.io/profile/${userUid}")
                                )
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .addCategory(Intent.CATEGORY_BROWSABLE)
                            )
                        },
                    model = getImageRequestBuilderForCytoid(remoteAvatarUrl)
                        .build(),
                    contentDescription = null,
                    onSuccess = { successState ->
                        CoroutineScope(Dispatchers.IO).launch {
                            LocalDataSource.saveAvatarBitmap(
                                cytoidID = userUid,
                                bitmap = successState.result.drawable.toBitmap(),
                                size = avatarSize
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun UserAvatar(profileDetails: ProfileDetails) {
    UserAvatar(
        userUid = profileDetails.user.uid,
        remoteAvatarUrl = profileDetails.user.avatar.large
    )
}