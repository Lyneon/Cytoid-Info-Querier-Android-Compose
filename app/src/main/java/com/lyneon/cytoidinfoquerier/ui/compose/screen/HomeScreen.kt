package com.lyneon.cytoidinfoquerier.ui.compose.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AccessibleForward
import androidx.compose.material.icons.automirrored.filled.Shortcut
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.lyneon.cytoidinfoquerier.ui.viewmodel.AnalyticsPreset
import com.lyneon.cytoidinfoquerier.util.AppSettingsMMKVKeys
import com.lyneon.cytoidinfoquerier.util.MMKVId
import com.tencent.mmkv.MMKV


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(navController: NavController) {
    val appUserCytoidID =
        MMKV.mmkvWithID(MMKVId.AppSettings.id)
            .decodeString(AppSettingsMMKVKeys.APP_USER_CYTOID_ID.name)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = stringResource(id = R.string.home)) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            appUserCytoidID?.let { WelcomeCard(cytoidID = it) }
            ShortcutCard(navController = navController)
        }
    }
}

@Composable
fun WelcomeCard(cytoidID: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(modifier = Modifier.padding(16.dp), text = "欢迎，${cytoidID}！")
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
