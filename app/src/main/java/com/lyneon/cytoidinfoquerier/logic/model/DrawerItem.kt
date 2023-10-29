package com.lyneon.cytoidinfoquerier.logic.model

import androidx.compose.ui.graphics.vector.ImageVector

data class DrawerItem(
    val icon: ImageVector,
    val label: String,
    val navDestinationRoute: String
)
