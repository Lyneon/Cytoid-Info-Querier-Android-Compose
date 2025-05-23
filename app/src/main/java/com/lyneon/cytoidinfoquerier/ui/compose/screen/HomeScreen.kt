package com.lyneon.cytoidinfoquerier.ui.compose.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.AccessibleForward
import androidx.compose.material.icons.automirrored.filled.Shortcut
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.datasource.RemoteDataSource
import com.lyneon.cytoidinfoquerier.data.model.github.Release
import com.lyneon.cytoidinfoquerier.data.model.webapi.ProfileDetails
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.lyneon.cytoidinfoquerier.ui.compose.component.UserDetailsHeader
import com.lyneon.cytoidinfoquerier.ui.viewmodel.AnalyticsPreset
import com.lyneon.cytoidinfoquerier.util.AppSettingsMMKVKeys
import com.lyneon.cytoidinfoquerier.util.DateParser
import com.lyneon.cytoidinfoquerier.util.DateParser.formatToTimeString
import com.lyneon.cytoidinfoquerier.util.MMKVId
import com.lyneon.cytoidinfoquerier.util.extension.openInBrowser
import com.lyneon.cytoidinfoquerier.util.extension.setPrecision
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import com.tencent.mmkv.MMKV
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, onDrawerButtonClick: () -> Unit) {
    val appUserCytoidID =
        MMKV.mmkvWithID(MMKVId.AppSettings.id)
            .decodeString(AppSettingsMMKVKeys.APP_USER_CYTOID_ID.name)
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.home)) },
                navigationIcon = {
                    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        IconButton(onClick = onDrawerButtonClick) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = stringResource(R.string.open_drawer)
                            )
                        }
                    }
                },
                scrollBehavior = topAppBarScrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
                .nestedScroll(
                    topAppBarScrollBehavior.nestedScrollConnection
                )
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            appUserCytoidID?.let { cytoidID ->
                WelcomeCard(cytoidID)
            }
            ShortcutCard(navController = navController)
            CheckUpdateCard()
            Spacer(
                modifier = Modifier.height(
                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                )
            )
        }
    }
}

@Composable
fun WelcomeCard(cytoidID: String) {
    var profileDetails by remember { mutableStateOf<ProfileDetails?>(null) }

    LaunchedEffect(cytoidID) {
        RemoteDataSource.fetchProfileDetailsResult(cytoidID)
            .onSuccess { details -> profileDetails = details }
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = stringResource(R.string.home_welcome, cytoidID))
            AnimatedVisibility(profileDetails != null) {
                profileDetails?.let { details ->
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        UserDetailsHeader(details, true)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShortcutCard(
    navController: NavController,
) {
    val appUserCytoidID =
        MMKV.mmkvWithID(MMKVId.AppSettings.id)
            .decodeString(AppSettingsMMKVKeys.APP_USER_CYTOID_ID.name)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = appUserCytoidID == null) {
                    navController.navigate(MainActivity.Screen.Settings.route)
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (appUserCytoidID == null) Icons.AutoMirrored.Default.AccessibleForward else Icons.AutoMirrored.Default.Shortcut,
                contentDescription = null
            )
            Column {
                Text(
                    text = stringResource(R.string.shortcut),
                    style = MaterialTheme.typography.labelMedium
                )
                if (appUserCytoidID == null) {
                    Text(text = stringResource(R.string.tip_set_id))
                    Text(
                        text = stringResource(R.string.tip_set_id_description),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = { navController.navigate(MainActivity.Screen.Analytics.route + "/${AnalyticsPreset.B30.name}") }) {
                            Text(text = stringResource(R.string.b30))
                        }
                        Button(onClick = { navController.navigate(MainActivity.Screen.Analytics.route + "/${AnalyticsPreset.R10.name}") }) {
                            Text(text = stringResource(R.string.r10))
                        }
                        Button(onClick = { navController.navigate(MainActivity.Screen.Profile.route + "/shortcut") }) {
                            Text(text = stringResource(R.string.profile))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckUpdateCard() {
    var releases by remember { mutableStateOf(emptyList<Release>()) }
    val scope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    scope.launch(Dispatchers.IO) {
                        isChecking = true
                        val result = RemoteDataSource.fetchReleases()
                        if (result.isSuccess) releases = result.getOrDefault(emptyList())
                        else BaseApplication.context.getString(R.string.check_update_failed)
                            .showToast()
                        isChecking = false
                    }
                }, enabled = !isChecking) {
                    Icon(
                        imageVector = Icons.Default.Update,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.check_update))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(
                        R.string.current_version,
                        BaseApplication.context.packageManager.getPackageInfo(
                            BaseApplication.context.packageName,
                            0
                        ).versionName ?: "null"
                    )
                )
            }
            AnimatedVisibility(visible = releases.isNotEmpty()) {
                releases.first().let {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = it.name, style = MaterialTheme.typography.headlineLarge
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(
                                        R.string.release_on, DateParser.parseISO8601Date(
                                            it.publishDate.replace("Z", ".000Z")
                                        ).formatToTimeString()
                                    )
                                )
                            }
                            it.body?.let { body ->
                                HorizontalDivider()
                                MarkdownText(markdown = body)
                            }
                            if (it.assets.isNotEmpty()) {
                                HorizontalDivider()
                                it.assets.forEach { asset ->
                                    Button(onClick = {
                                        URL(asset.downloadUrl).openInBrowser()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${asset.name}(${
                                                (asset.size / 1024f / 1024f).setPrecision(
                                                    2
                                                )
                                            }MB)"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}