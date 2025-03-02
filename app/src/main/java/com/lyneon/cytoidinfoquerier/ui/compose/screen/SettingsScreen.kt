package com.lyneon.cytoidinfoquerier.ui.compose.screen

import android.graphics.Bitmap.CompressFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult.ActionPerformed
import androidx.compose.material3.SnackbarResult.Dismissed
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.lyneon.cytoidinfoquerier.util.AppSettings
import com.lyneon.cytoidinfoquerier.util.AppSettingsMMKVKeys
import com.lyneon.cytoidinfoquerier.util.MMKVId
import com.lyneon.cytoidinfoquerier.util.extension.isValidCytoidID
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.launch

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
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppUserIDSettingCard()
            HorizontalDivider()
            Text(
                text = stringResource(R.string.data),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
            DeleteImageCacheCard(snackBarHostState)
            DeleteQueryCacheCard(snackBarHostState)
            PictureCompressSettingCard()
            HorizontalDivider()
            Text(
                text = stringResource(R.string.user_interface),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
            LocaleSettingCard(snackBarHostState)
            GridColumnsCountSettingCard(navController)
            HorizontalDivider()
            Text(
                text = stringResource(R.string.debug),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
            SentrySettingCard(uiState, viewModel, snackBarHostState)
            TestCrashSettingCard(snackBarHostState)
            Spacer(
                modifier = Modifier.height(
                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                )
            )
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
                    label = { Text(text = stringResource(R.string.change_your_cytoid_id)) },
                )
                Button(onClick = {
                    if (userId.isValidCytoidID()) {
                        mmkv.encode(AppSettingsMMKVKeys.APP_USER_CYTOID_ID.name, userId)
                        context.getString(R.string.saved).showToast()
                    } else {
                        if (userId?.isEmpty() == true) {
                            mmkv.removeValueForKey(AppSettingsMMKVKeys.APP_USER_CYTOID_ID.name)
                            context.getString(R.string.app_user_id_cleaned).showToast()
                        } else
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
            MMKV.mmkvWithID(MMKVId.AppSettings.id)
                .encode(AppSettingsMMKVKeys.ENABLE_SENTRY.name, it)
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
        title = stringResource(id = R.string.grid_columns_count), description = "${stringResource(R.string.portrait)}:${
            mmkv.decodeInt(AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT.name, 1)
        } ${stringResource(R.string.landscape)}:${
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
private fun TestCrashSettingCard(snackBarHostState: SnackbarHostState) {
    val scope = rememberCoroutineScope()

    SettingsItemCard(
        title = stringResource(id = R.string.test_crash),
        description = stringResource(R.string.test_carsh_desc),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PictureCompressSettingCard() {
    val mmkv = remember { MMKV.mmkvWithID(MMKVId.AppSettings.id) }
    var showCompressFormatTipsDialog by rememberSaveable { mutableStateOf(false) }
    var compressFormat by remember {
        mutableStateOf(
            mmkv.decodeString(
                AppSettingsMMKVKeys.PICTURE_COMPRESS_FORMAT.name,
                CompressFormat.JPEG.name
            )!!
        )
    }
    var compressQuality by remember {
        mutableIntStateOf(
            mmkv.decodeInt(
                AppSettingsMMKVKeys.PICTURE_COMPRESS_QUALITY.name,
                80
            )
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.picture_compress_format),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        modifier = Modifier.clickable { showCompressFormatTipsDialog = true },
                        imageVector = Icons.AutoMirrored.Default.Help,
                        contentDescription = null
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (format in CompressFormat.entries) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = compressFormat == format.name,
                                onClick = {
                                    compressFormat = format.name
                                    mmkv.encode(
                                        AppSettingsMMKVKeys.PICTURE_COMPRESS_FORMAT.name,
                                        format.name
                                    )
                                }
                            )
                            Text(text = format.name)
                        }
                    }
                }
                Slider(
                    value = compressQuality.toFloat(),
                    onValueChange = {
                        compressQuality = it.toInt()
                        mmkv.encode(
                            AppSettingsMMKVKeys.PICTURE_COMPRESS_QUALITY.name,
                            compressQuality
                        )
                    },
                    valueRange = 0f..100f,
                    steps = 101,
                )
                Text(text = compressQuality.toString())
            }
        }
    }

    if (showCompressFormatTipsDialog) {
        AlertDialog(
            onDismissRequest = { showCompressFormatTipsDialog = false },
            confirmButton = {
                TextButton(onClick = { showCompressFormatTipsDialog = false }) {
                    Text(text = stringResource(R.string.close))
                }
            },
            text = {
                Text(
                    text = stringResource(R.string.picture_compress_format_description),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }
        )
    }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LocaleSettingCard(
    snackBarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = null
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.locale),
                    style = MaterialTheme.typography.titleMedium
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("简体中文（中国）" to "zh", "English(US)" to "en", "符语（贵阳）" to "fjw").forEach {
                        Button(
                            onClick = {
                                AppSettings.locale = it.second
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
                        ) {
                            Text(text = it.first)
                        }
                    }
                }
            }
        }
    }
}