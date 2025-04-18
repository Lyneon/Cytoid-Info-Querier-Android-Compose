package com.lyneon.cytoidinfoquerier.ui.compose.screen

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.ui.activity.ImportLevelActivity
import com.lyneon.cytoidinfoquerier.ui.viewmodel.ToolUIState
import com.lyneon.cytoidinfoquerier.ui.viewmodel.ToolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolScreen(
    viewModel: ToolViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.tool)) },
                scrollBehavior = topAppBarScrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
                .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RatingCalculatorCard(uiState = uiState, viewModel = viewModel)
            PingSettingCard(uiState = uiState, viewModel = viewModel)
            ImportLevelCard()
        }
    }
}

@Composable
fun RatingCalculatorCard(uiState: ToolUIState, viewModel: ToolViewModel) {
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Calculate, contentDescription = null)
                Text(
                    text = stringResource(R.string.rating_calculator),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            OutlinedTextField(
                value = uiState.ratingCalculatorAccuracy,
                onValueChange = { viewModel.setRatingCalculatorAccuracy(it) },
                label = { Text(text = stringResource(R.string.accuracy)) },
                modifier = Modifier.fillMaxWidth(),
                suffix = { Text(text = "%") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = uiState.ratingCalculatorLevel,
                onValueChange = { viewModel.setRatingCalculatorLevel(it) },
                label = { Text(text = stringResource(R.string.difficulty_level)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = uiState.ratingCalculatorRating,
                onValueChange = { viewModel.setRatingCalculatorRating(it) },
                label = { Text(text = stringResource(R.string.rating)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.calculateAccuracy() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.calculate_accuracy))
                }
                Button(
                    onClick = { viewModel.calculateRating() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.calculate_rating))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PingSettingCard(uiState: ToolUIState, viewModel: ToolViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Public, contentDescription = null)
                Text(
                    text = stringResource(R.string.connectivity_test),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = {
                    viewModel.testConnectionToCytoidIO()
                    viewModel.setPingResult(BaseApplication.context.getString(R.string.connecting_to_cytoid_io))
                }) {
                    Text(text = stringResource(R.string.cytoid_io))
                }
            }
            if (uiState.pingResult.isNotEmpty()) {
                Text(text = uiState.pingResult, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ImportLevelCard() {
    val activity = LocalActivity.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val intent = Intent(activity!!, ImportLevelActivity::class.java).apply {
                data = it
            }
            activity.startActivity(intent)
        }
    }

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.MoveToInbox, contentDescription = null)
                Text(
                    text = stringResource(R.string.import_level),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(text = stringResource(R.string.shizuku_needed))
            Button(onClick = {
                launcher.launch("*/*")
            }) {
                Text(text = stringResource(R.string.select_file))
            }
        }
    }
}