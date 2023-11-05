package com.lyneon.cytoidinfoquerier.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.ui.compose.component.TopBar

@Composable
fun ProfileCompose() {
    Column {
        TopBar(title = stringResource(id = R.string.profile))
        Column(
            Modifier.padding(6.dp, 6.dp, 6.dp)
        ) {

        }
    }
}