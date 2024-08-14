package com.lyneon.cytoidinfoquerier.ui.compose.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.data.enums.ImageSize
import com.lyneon.cytoidinfoquerier.util.extension.getImageRequestBuilderForCytoid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CollectionCoverImage(
    modifier: Modifier = Modifier,
    collectionID: String,
    collectionCoverImageSize: ImageSize,
    disableLocalCache: Boolean = false,
    remoteUrl: String?
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val currentCollectionCoverImageFile = LocalDataSource.getCollectionCoverImageFile(
        collectionID, collectionCoverImageSize
    )

    Box(
        modifier = modifier
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        if (currentCollectionCoverImageFile.isFile && currentCollectionCoverImageFile.exists() && !disableLocalCache) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = currentCollectionCoverImageFile,
                contentDescription = null,
                onLoading = { isLoading = true },
                onSuccess = { isLoading = false },
                onError = { isLoading = false },
                contentScale = ContentScale.FillWidth
            )
        } else {
            if (remoteUrl == null) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(id = R.drawable.sayakacry),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth
                )
            } else {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = getImageRequestBuilderForCytoid(remoteUrl).build(),
                    contentDescription = null,
                    onLoading = { isLoading = true },
                    onSuccess = { successState ->
                        isLoading = false
                        scope.launch(Dispatchers.IO) {
                            LocalDataSource.saveCollectionCoverImage(
                                collectionID,
                                collectionCoverImageSize,
                                successState.result.drawable.toBitmap()
                            )
                        }
                    },
                    onError = { isLoading = false },
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    }
}