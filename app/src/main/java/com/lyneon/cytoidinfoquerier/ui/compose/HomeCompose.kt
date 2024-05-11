package com.lyneon.cytoidinfoquerier.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.ui.compose.component.AlertCard
import com.lyneon.cytoidinfoquerier.ui.compose.component.TopBar
import kotlinx.coroutines.launch

@Composable
fun HomeCompose() {
    Column {
        TopBar(title = stringResource(id = R.string.home))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AlertCard(message = stringResource(id = R.string.debug_declaration))
            AlertCard(message = stringResource(id = R.string.sentry_declare))
            AlertCard(message = stringResource(id = R.string.cytoid_resource_declare))
            val scope = rememberCoroutineScope()
            ExtendedFloatingActionButton(
                text = { Text(text = stringResource(id = R.string.open_drawer)) },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = stringResource(id = R.string.open_drawer)
                    )
                },
                onClick = { scope.launch { BaseApplication.globalDrawerState.open() } }
            )
        }
    }
}