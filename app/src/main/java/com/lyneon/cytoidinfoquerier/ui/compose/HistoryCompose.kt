package com.lyneon.cytoidinfoquerier.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.MainActivityScreens
import com.lyneon.cytoidinfoquerier.data.model.ui.AnalyticsScreenDataModel
import com.lyneon.cytoidinfoquerier.data.model.ui.ProfileScreenIntegratedDataModel
import com.lyneon.cytoidinfoquerier.json
import com.lyneon.cytoidinfoquerier.logic.DateParser.timeStampToString
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HistoryCompose(navController: NavController, navBackStackEntry: NavBackStackEntry) {
    val type = navBackStackEntry.arguments?.getString("type")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = stringResource(R.string.history))
                        type?.replaceFirstChar { it.uppercaseChar() }
                            ?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.history)
                        )
                    }
                }
            )
        }
    ) {
        type?.let { type: String ->
            LazyColumn(
                modifier = Modifier
                    .padding(it)
                    .padding(horizontal = 6.dp)
            ) {
                val cacheDirectories =
                    BaseApplication.context.externalCacheDir?.run {
                        File(this.path + "/$type").listFiles()
                    }
                cacheDirectories?.forEach { cytoidIdDirectory: File ->
                    val cacheFiles = cytoidIdDirectory.listFiles()
                    item {
                        var expanded by remember { mutableStateOf(false) }

                        OutlinedCard(
                            onClick = { expanded = !expanded },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = cytoidIdDirectory.name)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (cacheFiles != null) {
                                            Text(text = cacheFiles.size.toString())
                                        }
                                        IconButton(onClick = { expanded = !expanded }) {
                                            Icon(
                                                imageVector = if (!expanded) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.Default.KeyboardArrowDown,
                                                contentDescription = if (expanded) {
                                                    stringResource(R.string.unfold)
                                                } else {
                                                    stringResource(R.string.fold)
                                                }
                                            )
                                        }
                                    }
                                }
                                AnimatedVisibility(visible = expanded) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        cacheFiles?.forEach { cacheFile: File? ->
                                            cacheFile?.let { file ->
                                                HistoryItemCard(
                                                    historyItemFile = file,
                                                    type = type,
                                                    navController = navController,
                                                    cytoidID = cytoidIdDirectory.name
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
    }
}

@Composable
fun HistoryItemCard(
    historyItemFile: File,
    type: String,
    navController: NavController,
    cytoidID: String
) {
    val jsonObject = historyItemFile.readText().run {
        if (type == "analytics") json.decodeFromString<AnalyticsScreenDataModel>(this)
        else json.decodeFromString<ProfileScreenIntegratedDataModel>(this)
    }

    Card(
        onClick = {
            if (type == "analytics") navController.navigate(MainActivityScreens.Analytics.name + "/${cytoidID}/${historyItemFile.name}") {
                launchSingleTop = true
                this.popUpTo(MainActivityScreens.Analytics.name) {
                    inclusive = true
                }
            } else navController.navigate(MainActivityScreens.Profile.name + "/${cytoidID}/${historyItemFile.name}") {
                launchSingleTop = true
                this.popUpTo(MainActivityScreens.Profile.name) {
                    inclusive = true
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = historyItemFile.name.toLong().timeStampToString())
            if (type == "analytics") {
                Text(
                    text = (jsonObject as AnalyticsScreenDataModel).analytics.data.profile?.let { profile ->
                        "${profile.bestRecords.size} bestRecords | ${profile.recentRecords.size} recentRecords"
                    } ?: ""
                )
            }
        }
    }
}