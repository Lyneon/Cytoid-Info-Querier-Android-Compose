package com.lyneon.cytoidinfoquerier.ui.compose.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.model.graphql.BestRecords
import com.lyneon.cytoidinfoquerier.data.model.graphql.RecentRecords
import com.lyneon.cytoidinfoquerier.data.model.screen.ProfileScreenDataModel
import com.lyneon.cytoidinfoquerier.json
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import com.lyneon.cytoidinfoquerier.ui.viewmodel.HistoryUIState
import com.lyneon.cytoidinfoquerier.ui.viewmodel.HistoryViewModel
import com.lyneon.cytoidinfoquerier.util.DateParser.timeStampToString
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(),
    navController: NavController,
    navBackStackEntry: NavBackStackEntry
) {
    val uiState by viewModel.uiState.collectAsState()

    navBackStackEntry.arguments?.getString("type").let {
        when (it) {
            "analytics" -> viewModel.setHistoryType(HistoryUIState.HistoryType.AnalyticsBestRecords)
            "profile" -> viewModel.setHistoryType(HistoryUIState.HistoryType.Profile)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { TopAppBarTitle(uiState) },
                navigationIcon = { TopAppBarBackNavigationIcon(navController) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding())
        ) {
            uiState.historyType.let { type ->
                val localHistoryTypeDir =
                    BaseApplication.context.getExternalFilesDir(type.directoryName)
                when (type) {
                    HistoryUIState.HistoryType.AnalyticsBestRecords, HistoryUIState.HistoryType.AnalyticsRecentRecords -> {
                        AnalyticsHistoryTypeTabRow(selectedType = type, viewModel = viewModel)
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                top = 16.dp,
                                end = 16.dp,
                                bottom = paddingValues.calculateBottomPadding()
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            localHistoryTypeDir?.listFiles().orEmpty()
                                .forEach { userRecordHistoryDir ->
                                    item {
                                        HistoryUserCard(
                                            historyType = type,
                                            userHistoryDir = userRecordHistoryDir,
                                            navController = navController
                                        )
                                    }
                                }
                        }
                    }

                    HistoryUIState.HistoryType.Profile -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                top = 16.dp,
                                end = 16.dp,
                                bottom = paddingValues.calculateBottomPadding()
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            localHistoryTypeDir?.listFiles().orEmpty()
                                .forEach { userProfileDetailsHistoryDir ->
                                    item {
                                        HistoryUserCard(
                                            historyType = type,
                                            userHistoryDir = userProfileDetailsHistoryDir,
                                            navController = navController
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoryUserCard(
    userHistoryDir: File,
    historyType: HistoryUIState.HistoryType,
    navController: NavController
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val userHistoryFiles = userHistoryDir.listFiles()

    if (!userHistoryFiles.isNullOrEmpty()) {
        OutlinedCard(onClick = { expanded = !expanded }) {
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
                    Text(text = userHistoryDir.name)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = userHistoryFiles.size.toString())
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
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        userHistoryFiles.forEach { userHistoryFile: File? ->
                            userHistoryFile?.let { file ->
                                HistoryItemCard(
                                    historyItemFile = file,
                                    historyType = historyType,
                                    navController = navController,
                                    cytoidID = userHistoryDir.name
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    historyItemFile: File,
    historyType: HistoryUIState.HistoryType,
    navController: NavController,
    cytoidID: String
) {
    val jsonObject = historyItemFile.readText().run {
        when (historyType) {
            HistoryUIState.HistoryType.AnalyticsBestRecords -> json.decodeFromString<BestRecords>(
                this
            )

            HistoryUIState.HistoryType.AnalyticsRecentRecords -> json.decodeFromString<RecentRecords>(
                this
            )

            HistoryUIState.HistoryType.Profile -> json.decodeFromString<ProfileScreenDataModel>(this)
        }
    }

    Card(
        onClick = {
            repeat(2) { navController.navigateUp() }
            when (historyType) {
                HistoryUIState.HistoryType.AnalyticsBestRecords -> {
                    navController.navigate(
                        MainActivity.Screen.Analytics.route + "/${cytoidID}/BestRecords/${
                            historyItemFile.name.removeSuffix(".json")
                        }"
                    )
                }

                HistoryUIState.HistoryType.AnalyticsRecentRecords -> {
                    navController.navigate(
                        MainActivity.Screen.Analytics.route + "/${cytoidID}/RecentRecords/${
                            historyItemFile.name.removeSuffix(".json")
                        }"
                    )
                }

                HistoryUIState.HistoryType.Profile -> {
                    navController.navigate(
                        MainActivity.Screen.Profile.route + "/${cytoidID}/${
                            historyItemFile.name.removeSuffix(".json")
                        }"
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = historyItemFile.name.removeSuffix(".json").toLong().timeStampToString())
            when (historyType) {
                HistoryUIState.HistoryType.AnalyticsBestRecords -> {
                    (jsonObject as BestRecords).data.profile?.let { profile ->
                        Text(text = "${profile.bestRecords.size} Best Records")
                    }
                }

                HistoryUIState.HistoryType.AnalyticsRecentRecords -> {
                    (jsonObject as RecentRecords).data.profile?.let { profile ->
                        Text(text = "${profile.recentRecords.size} Recent Records")
                        jsonObject.queryArguments?.let { queryArguments ->
                            Text(text = "Sort: ${queryArguments.recentRecordsSort.name} | Order: ${queryArguments.recentRecordsOrder.name}")
                        }
                    }
                }

                else -> Unit
            }
        }
    }
}

@Composable
private fun TopAppBarTitle(uiState: HistoryUIState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.history))
        when (uiState.historyType) {
            HistoryUIState.HistoryType.AnalyticsBestRecords -> "Analytics"
            HistoryUIState.HistoryType.AnalyticsRecentRecords -> "Analytics"
            HistoryUIState.HistoryType.Profile -> "Profile"
        }.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun TopAppBarBackNavigationIcon(navController: NavController) {
    IconButton(onClick = { navController.navigateUp() }) {
        Icon(
            imageVector = Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = stringResource(R.string.back)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnalyticsHistoryTypeTabRow(
    selectedType: HistoryUIState.HistoryType,
    viewModel: HistoryViewModel
) {
    PrimaryTabRow(selectedTabIndex = selectedType.ordinal) {
        Tab(
            selected = selectedType.ordinal == HistoryUIState.HistoryType.AnalyticsBestRecords.ordinal,
            onClick = { viewModel.setHistoryType(HistoryUIState.HistoryType.AnalyticsBestRecords) },
            text = { Text(text = HistoryUIState.HistoryType.AnalyticsBestRecords.displayName) }
        )
        Tab(
            selected = selectedType.ordinal == HistoryUIState.HistoryType.AnalyticsRecentRecords.ordinal,
            onClick = { viewModel.setHistoryType(HistoryUIState.HistoryType.AnalyticsRecentRecords) },
            text = { Text(text = HistoryUIState.HistoryType.AnalyticsRecentRecords.displayName) }
        )
    }
}