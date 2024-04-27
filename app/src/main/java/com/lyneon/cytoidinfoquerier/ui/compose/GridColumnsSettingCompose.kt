package com.lyneon.cytoidinfoquerier.ui.compose

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.MMKVKeys
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.tencent.mmkv.MMKV

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridColumnsSettingCompose(onNavigateBack: () -> Unit) {
    val context = LocalContext.current as MainActivity
    val orientation by remember { mutableIntStateOf(context.resources.configuration.orientation) }
    val mmkv = MMKV.defaultMMKV()
    var columnsCount by remember { mutableIntStateOf(1) }

    columnsCount = mmkv.decodeInt(
        if (orientation == Configuration.ORIENTATION_PORTRAIT)
            MMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT.name
        else MMKVKeys.GRID_COLUMNS_COUNT_LANDSCAPE.name, 1
    )
    if (columnsCount < 1) {
        columnsCount = 1
        mmkv.encode(
            if (orientation == Configuration.ORIENTATION_PORTRAIT)
                MMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT.name
            else MMKVKeys.GRID_COLUMNS_COUNT_LANDSCAPE.name, 1
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            id = if (orientation == Configuration.ORIENTATION_PORTRAIT)
                                R.string.grid_columns_count_portrait
                            else R.string.grid_columns_count_landscape
                        ) + "当前：$columnsCount"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                AnimatedVisibility(visible = columnsCount != 1) {
                    Button(
                        onClick = {
                            if (columnsCount != 1) {
                                columnsCount--
                                mmkv.encode(
                                    if (orientation == Configuration.ORIENTATION_PORTRAIT)
                                        MMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT.name
                                    else MMKVKeys.GRID_COLUMNS_COUNT_LANDSCAPE.name, columnsCount
                                )
                            }
                        },
                        modifier = Modifier.padding(6.dp)
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
                                MMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT.name
                            else MMKVKeys.GRID_COLUMNS_COUNT_LANDSCAPE.name, columnsCount
                        )
                    },
                    modifier = Modifier.padding(6.dp)
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
                                MMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT.name
                            else MMKVKeys.GRID_COLUMNS_COUNT_LANDSCAPE.name, columnsCount
                        )
                    },
                    modifier = Modifier.padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.reset)
                    )
                }
                Button(
                    onClick = {
                        context.requestedOrientation =
                            if (orientation == Configuration.ORIENTATION_PORTRAIT) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    },
                    modifier = Modifier.padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = stringResource(R.string.rotate)
                    )
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnsCount),
                modifier = Modifier.padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (1..100).forEach {
                    item {
                        Card(
                            modifier = Modifier.padding(6.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = it.toString())
                            }
                        }
                    }
                }
            }
        }
    }
}