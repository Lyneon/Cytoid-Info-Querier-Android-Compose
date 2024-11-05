package com.lyneon.cytoidinfoquerier.ui.compose.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.ui.compose.component.ErrorMessageCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.leaderboard)) }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
                .padding(horizontal = 12.dp)
        ) {
            ErrorMessageCard(errorMessage = stringResource(R.string.todo))
        }
    }
}