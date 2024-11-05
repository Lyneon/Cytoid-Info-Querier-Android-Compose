package com.lyneon.cytoidinfoquerier.ui.compose.screen

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.ui.viewmodel.ToolUIState
import com.lyneon.cytoidinfoquerier.ui.viewmodel.ToolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolScreen(
    viewModel: ToolViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = stringResource(id = R.string.tool)) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RatingCalculatorCard(uiState = uiState, viewModel = viewModel)
            PingSettingCard(uiState = uiState, viewModel = viewModel)
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
                Text(text = "Rating 计算器", style = MaterialTheme.typography.titleMedium)
            }
            OutlinedTextField(
                value = uiState.ratingCalculatorAccuracy,
                onValueChange = { viewModel.setRatingCalculatorAccuracy(it) },
                label = { Text(text = "Accuracy") },
                modifier = Modifier.fillMaxWidth(),
                suffix = { Text(text = "%") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = uiState.ratingCalculatorLevel,
                onValueChange = { viewModel.setRatingCalculatorLevel(it) },
                label = { Text(text = "难度级别") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = uiState.ratingCalculatorRating,
                onValueChange = { viewModel.setRatingCalculatorRating(it) },
                label = { Text(text = "Rating") },
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
                    Text(text = "计算 Accuracy")
                }
                Button(
                    onClick = { viewModel.calculateRating() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "计算 Rating")
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
                    text = "连通性测试",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = {
                    viewModel.testConnectionToCytoidIO()
                    viewModel.setPingResult("Connecting to cytoid.io...")
                }) {
                    Text(text = "cytoid.io")
                }
            }
            if (uiState.pingResult.isNotEmpty()) {
                Text(text = uiState.pingResult, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}