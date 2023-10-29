package com.lyneon.cytoidinfoquerier.ui.compose.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun AlertCard(
    icon: ImageVector? = Icons.Rounded.Info,
    message: String
) {
    Card {
        Box(modifier = Modifier.padding(16.dp)) {
            Row {
                icon?.let {
                    Icon(imageVector = it, contentDescription = message)
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(text = message)
            }
        }
    }
}