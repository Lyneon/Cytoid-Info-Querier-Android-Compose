package com.lyneon.cytoidinfoquerier.ui.compose.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.enums.ImageSize
import com.lyneon.cytoidinfoquerier.data.model.shared.Level
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.lyneon.cytoidinfoquerier.ui.compose.component.LevelBackgroundImage
import com.lyneon.cytoidinfoquerier.ui.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelDetailScreen(
    navController: NavController
) {
    val sharedViewModel = viewModel<SharedViewModel>(LocalContext.current as MainActivity)
    val level = sharedViewModel.sharedLevelForLevelDetailScreen

    Scaffold(
        topBar = {
            TopAppBar(navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }, title = { })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (level == null) {
                Card {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Error, contentDescription = null)
                        Text(text = "Error: Level is null")
                    }
                }
            } else {
                if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE){
                    LandscapeLevelDetailScreen(level)
                }else{
                    PortraitLevelDetailScreen(level)
                }
            }
        }
    }
}

@Composable
private fun LandscapeLevelDetailScreen(level:Level) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .weight(7f)
        ) {
            LevelBackgroundImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
                levelID = level.uid,
                backgroundImageSize = ImageSize.Original,
                remoteUrl = level.coverRemoteURL
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFF000000)
                            )
                        )
                    )
                    .align(Alignment.BottomStart)
            ) {
                Text(
                    text = level.title,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1
                )
                level.artist?.let {
                    Text(text = it, maxLines = 1)
                }
            }
        }
        Column(
            modifier = Modifier.weight(3f)
        ) {

        }
    }
}

@Composable
private fun PortraitLevelDetailScreen(level:Level) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

    }
}