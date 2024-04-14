package com.lyneon.cytoidinfoquerier.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.StayPrimaryPortrait
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult.ActionPerformed
import androidx.compose.material3.SnackbarResult.Dismissed
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.BaseApplication.Companion.context
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.MMKVKeys
import com.lyneon.cytoidinfoquerier.data.constant.NavRoute
import com.lyneon.cytoidinfoquerier.ui.compose.component.TopBar
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.launch


@Composable
fun SettingsCompose(navController: NavController) {
    val mmkv = MMKV.defaultMMKV()
    val scope = rememberCoroutineScope()
    var enableSentry by remember {
        mutableStateOf(mmkv.decodeBool(MMKVKeys.ENABLE_SENTRY, true))
    }
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = { TopBar(title = stringResource(id = R.string.settings)) },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SettingsItem(onClick = {
                enableSentry = !enableSentry
                mmkv.encode(MMKVKeys.ENABLE_SENTRY, enableSentry)
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(
                        context.getString(R.string.changes_need_restart_to_enable),
                        context.getString(R.string.restart),
                        true,
                        SnackbarDuration.Short
                    )
                    when (result) {
                        ActionPerformed -> BaseApplication.restartApp()
                        Dismissed -> {}
                    }
                }

            }, hideDivider = true) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_sentry),
                            contentDescription = stringResource(id = R.string.enable_sentry)
                        )
                        Text(
                            text = stringResource(id = R.string.enable_sentry),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                    Switch(
                        checked = enableSentry, onCheckedChange = { checked ->
                            enableSentry = checked
                            mmkv.encode(MMKVKeys.ENABLE_SENTRY, checked)
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                when (snackbarHostState.showSnackbar(
                                    context.getString(R.string.changes_need_restart_to_enable),
                                    context.getString(R.string.restart),
                                    true,
                                    SnackbarDuration.Short
                                )) {
                                    ActionPerformed -> BaseApplication.restartApp()
                                    Dismissed -> {}
                                }
                            }
                        }
                    )
                }
            }
            SettingsItem(onClick = {
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    when (snackbarHostState.showSnackbar(
                        context.getString(R.string.delete_confirm),
                        context.getString(R.string.confirm),
                        true,
                        SnackbarDuration.Short
                    )) {
                        Dismissed -> {}
                        ActionPerformed -> {
                            val cacheDir = context.externalCacheDir?.listFiles()
                            cacheDir?.run {
                                for (file in cacheDir) {
                                    file.delete()
                                }
                            }
                        }
                    }
                }
            }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.delete_cache_image)
                    )
                    Text(
                        text = stringResource(id = R.string.delete_cache_image),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
            SettingsItem(onClick = {
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    when (snackbarHostState.showSnackbar(
                        context.getString(R.string.testCrash),
                        context.getString(R.string.confirm),
                        true,
                        SnackbarDuration.Short
                    )) {
                        Dismissed -> {}
                        ActionPerformed -> throw Exception("This is a crash for test!")
                    }
                }
            }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = stringResource(id = R.string.testCrash)
                    )
                    Text(
                        text = stringResource(id = R.string.testCrash),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
            SettingsItem(onClick = {
                navController.navigate(NavRoute.gridColumnsSetting)
            }) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterVertically),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.StayPrimaryPortrait,
                            contentDescription = stringResource(R.string.grid_columns_count)
                        )
                        Text(
                            text = stringResource(id = R.string.grid_columns_count)
                        )
                    }
                    Text(
                        text = "竖屏:${
                            mmkv.decodeInt(MMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT, 1)
                        } 横屏:${
                            mmkv.decodeInt(MMKVKeys.GRID_COLUMNS_COUNT_LANDSCAPE, 1)
                        }",
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    hideDivider: Boolean = false,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        if (!hideDivider) HorizontalDivider()
        Box(modifier = Modifier.clickable { onClick() }) {
            content()
        }
    }
}