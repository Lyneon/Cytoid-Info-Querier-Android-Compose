package com.lyneon.cytoidinfoquerier.ui.compose.screen

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.lyneon.cytoidinfoquerier.util.AppSettingsMMKVKeys
import com.lyneon.cytoidinfoquerier.util.MMKVId
import com.tencent.mmkv.MMKV

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridColumnsCountSettingScreen(
    navController: NavController
) {
    val mainActivity = LocalContext.current as MainActivity
    val mmkv = MMKV.mmkvWithID(MMKVId.AppSettings.id)
    val orientation = BaseApplication.context.resources.configuration.orientation
    var columnsCount by remember {
        mutableIntStateOf(
            mmkv.decodeInt(
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT.name
                else AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_LANDSCAPE.name, 1
            )
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            id = if (orientation == Configuration.ORIENTATION_PORTRAIT)
                                R.string.grid_columns_count_portrait
                            else R.string.grid_columns_count_landscape
                        ) + "${stringResource(R.string.current)}：$columnsCount"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                AnimatedVisibility(visible = columnsCount != 1) {
                    Button(
                        onClick = {
                            if (columnsCount != 1) {
                                columnsCount--
                                mmkv.encode(
                                    if (orientation == Configuration.ORIENTATION_PORTRAIT)
                                        AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT.name
                                    else AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_LANDSCAPE.name,
                                    columnsCount
                                )
                            }
                        },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = stringResource(R.string.decrease)
                        )
                    }
                }
                Button(
                    onClick = {
                        columnsCount++
                        mmkv.encode(
                            if (orientation == Configuration.ORIENTATION_PORTRAIT)
                                AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT.name
                            else AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_LANDSCAPE.name, columnsCount
                        )
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.increase)
                    )
                }
                Button(
                    onClick = {
                        columnsCount = 1
                        mmkv.encode(
                            if (orientation == Configuration.ORIENTATION_PORTRAIT)
                                AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT.name
                            else AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_LANDSCAPE.name, columnsCount
                        )
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.reset)
                    )
                }
                Button(
                    onClick = {
                        mainActivity.requestedOrientation =
                            if (orientation == Configuration.ORIENTATION_PORTRAIT) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = stringResource(R.string.rotate)
                    )
                }
            }
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(columnsCount),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp
            ) {
                (1..100).forEach {
                    item {
                        Card {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .height((128..512).random().dp)
                            ) {
                                Text(
                                    text = it.toString(),
                                    style = MaterialTheme.typography.headlineLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}