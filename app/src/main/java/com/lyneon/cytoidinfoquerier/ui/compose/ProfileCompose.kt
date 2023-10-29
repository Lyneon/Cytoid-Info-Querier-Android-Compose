package com.lyneon.cytoidinfoquerier.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.ui.compose.component.AlertCard
import com.lyneon.cytoidinfoquerier.ui.compose.component.TopBar

@Composable
fun ProfileCompose(drawerState: DrawerState) {
    Column {
        TopBar(drawerState = drawerState)
        Column(
            Modifier.padding(6.dp)
        ) {
            AlertCard(message = stringResource(id = R.string.todo))
        }
    }
}