package com.lyneon.cytoidinfoquerier.ui.compose.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult.ActionPerformed
import androidx.compose.material3.SnackbarResult.Dismissed
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.BaseApplication.Companion.context
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.datasource.LocalDataSource
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.lyneon.cytoidinfoquerier.ui.viewmodel.SettingsUIState
import com.lyneon.cytoidinfoquerier.ui.viewmodel.SettingsViewModel
import com.lyneon.cytoidinfoquerier.util.AppSettingsMMKVKeys
import com.lyneon.cytoidinfoquerier.util.MMKVId
import com.lyneon.cytoidinfoquerier.util.extension.isValidCytoidID
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.concurrent.thread

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.settings)) },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(MainActivity.Screen.About.route) {
                            launchSingleTop = true
                            popUpTo(MainActivity.Screen.About.route)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(id = R.string.about)
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackBarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppUserIDSettingCard()
            SentrySettingCard(uiState, viewModel, snackBarHostState)
            DeleteImageCacheCard(snackBarHostState)
            DeleteQueryCacheCard(snackBarHostState)
            GridColumnsCountSettingCard(navController)
            PingSettingCard(snackBarHostState)
            TestCrashSettingCard(snackBarHostState)
        }
    }
}

@Composable
private fun AppUserIDSettingCard() {
    val mmkv = MMKV.mmkvWithID(MMKVId.AppSettings.id)
    var userId by remember {
        mutableStateOf(mmkv.decodeString(AppSettingsMMKVKeys.APP_USER_CYTOID_ID.name, ""))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = userId ?: "",
                    onValueChange = { if (it.isValidCytoidID(checkLengthMin = false)) userId = it },
                    label = { Text(text = "更改您的 Cytoid ID") },
                )
                Button(onClick = {
                    if (userId.isValidCytoidID()) {
                        mmkv.encode(AppSettingsMMKVKeys.APP_USER_CYTOID_ID.name, userId)
                        context.getString(R.string.saved).showToast()
                    } else {
                        context.getString(R.string.invalid_cytoid_id).showToast()
                    }
                }) {
                    Text(text = stringResource(id = R.string.save))
                }
            }
        }
    }
}

@Composable
private fun SentrySettingCard(
    uiState: SettingsUIState,
    viewModel: SettingsViewModel,
    snackBarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()

    SettingsItemSwitchCard(
        title = stringResource(id = R.string.enable_sentry),
        description = stringResource(id = R.string.sentry_description),
        icon = {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_sentry),
                contentDescription = null
            )
        },
        value = uiState.enableSentry,
        onValueChange = {
            MMKV.mmkvWithID(MMKVId.AppSettings.id).encode(AppSettingsMMKVKeys.ENABLE_SENTRY.name, it)
            viewModel.setEnableSentry(it)
            scope.launch {
                snackBarHostState.currentSnackbarData?.dismiss()
                when (snackBarHostState.showSnackbar(
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

@Composable
private fun DeleteImageCacheCard(
    snackBarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()

    SettingsItemCard(
        title = stringResource(id = R.string.delete_cache_image),
        description = stringResource(R.string.delete_cache_image_description),
        icon = {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
        },
        onClick = {
            scope.launch {
                snackBarHostState.currentSnackbarData?.dismiss()
                when (snackBarHostState.showSnackbar(
                    context.getString(R.string.delete_confirm),
                    context.getString(R.string.confirm),
                    true,
                    SnackbarDuration.Short
                )) {
                    Dismissed -> {}
                    ActionPerformed -> {
                        LocalDataSource.clearLocalData(
                            LocalDataSource.LocalDataType.Avatar,
                            LocalDataSource.LocalDataType.BackgroundImage,
                            LocalDataSource.LocalDataType.CollectionCoverImage
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun DeleteQueryCacheCard(
    snackBarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()

    SettingsItemCard(
        title = stringResource(R.string.delete_query_cache),
        description = stringResource(R.string.delete_query_cache_description),
        icon = {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
        },
        onClick = {
            scope.launch {
                snackBarHostState.currentSnackbarData?.dismiss()
                when (snackBarHostState.showSnackbar(
                    "确定要删除所有查询缓存吗？",
                    context.getString(R.string.confirm),
                    true,
                    SnackbarDuration.Short
                )) {
                    Dismissed -> {}
                    ActionPerformed -> {
                        LocalDataSource.clearLocalData(
                            LocalDataSource.LocalDataType.BestRecords,
                            LocalDataSource.LocalDataType.RecentRecords,
                            LocalDataSource.LocalDataType.ProfileCommentList,
                            LocalDataSource.LocalDataType.ProfileDetails,
                            LocalDataSource.LocalDataType.ProfileGraphQL,
                            LocalDataSource.LocalDataType.ProfileScreenDataModel
                        )
                        MMKV.mmkvWithID(MMKVId.LastQueryTimeCache.id).clearAll()
                    }
                }
            }
        }
    )
}

@Composable
private fun GridColumnsCountSettingCard(
    navController: NavController
) {
    val mmkv = MMKV.mmkvWithID(MMKVId.AppSettings.id)

    SettingsItemCard(
        title = stringResource(id = R.string.grid_columns_count), description = "竖屏:${
            mmkv.decodeInt(AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT.name, 1)
        } 横屏:${
            mmkv.decodeInt(AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_LANDSCAPE.name, 1)
        }",
        icon = {
            Icon(imageVector = Icons.Default.GridView, contentDescription = null)
        },
        onClick = {
            navController.navigate(MainActivity.Screen.GridColumnsCountSetting.route)
        }
    )
}

@Composable
private fun PingSettingCard(snackBarHostState: SnackbarHostState) {
    val scope = rememberCoroutineScope()

    SettingsItemCard(
        title = stringResource(id = R.string.ping),
        description = "运行连通性测试",
        icon = {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = null
            )
        },
        onClick = {
            scope.launch {
                snackBarHostState.currentSnackbarData?.dismiss()
                when (snackBarHostState.showSnackbar(
                    "start ping?",
                    context.getString(R.string.confirm),
                    true,
                    SnackbarDuration.Short
                )) {
                    Dismissed -> {}
                    ActionPerformed -> {
                        snackBarHostState.currentSnackbarData?.dismiss()
                        snackBarHostState.showSnackbar("pinging cytoid.io...")
                        thread {
                            try {
                                val response = OkHttpClient().newCall(
                                    Request.Builder().url("https://cytoid.io/")
                                        .head()
                                        .removeHeader("User-Agent")
                                        .addHeader(
                                            "User-Agent",
                                            "CytoidClient/2.1.1"
                                        )
                                        .build()
                                ).execute()
                                scope.launch {
                                    snackBarHostState.currentSnackbarData?.dismiss()
                                    snackBarHostState.showSnackbar(
                                        "ping result:\ncytoid.io:${response.code} ${response.message}",
                                        withDismissAction = true,
                                        duration = SnackbarDuration.Indefinite
                                    )
                                }
                            } catch (e: Exception) {
                                scope.launch {
                                    snackBarHostState.currentSnackbarData?.dismiss()
                                    snackBarHostState.showSnackbar(
                                        "ping failed:\n${e.message}",
                                        withDismissAction = true,
                                        duration = SnackbarDuration.Indefinite
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun TestCrashSettingCard(snackBarHostState: SnackbarHostState) {
    val scope = rememberCoroutineScope()

    SettingsItemCard(
        title = stringResource(id = R.string.test_crash),
        description = "(仅调试)立即崩溃",
        icon = {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = null
            )
        },
        onClick = {
            scope.launch {
                snackBarHostState.currentSnackbarData?.dismiss()
                when (snackBarHostState.showSnackbar(
                    context.getString(R.string.test_crash),
                    context.getString(R.string.confirm),
                    true,
                    SnackbarDuration.Short
                )) {
                    Dismissed -> {}
                    ActionPerformed -> throw Exception("This is a crash for test!")
                }
            }
        }
    )
}

@Composable
private fun SettingsItemCard(
    title: String,
    description: String?,
    icon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                it()
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                if (description != null) {
                    Text(text = description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun SettingsItemSwitchCard(
    title: String,
    description: String? = null,
    icon: (@Composable () -> Unit)? = null,
    value: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onValueChange(!value) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                icon?.let {
                    it()
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    description?.let {
                        Text(text = description, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Switch(
                checked = value,
                onCheckedChange = { onValueChange(!value) },
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}