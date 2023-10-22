package com.lyneon.cytoidinfoquerier.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lyneon.cytoidinfoquerier.isDebugging
import com.lyneon.cytoidinfoquerier.tool.showToast
import com.lyneon.cytoidinfoquerier.ui.activity.currentNavRoute

@Composable
fun HomeCompose(navController: NavController) {
    Column {
        TopBar(navController = navController, enableBackArrow = false)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(6.dp)
        ) {
            item {
                FunctionCard(
                    title = "BestRecord",
                    description = "顶级高手/一代糊神/水谱战神/贵阳儿歌",
                    navController = navController,
                    navDestinationRoute = NavRoute.bestRecord
                )
            }
            item {
                FunctionCard(
                    title = "Profile",
                    description = "椰叶/dd的打歌水平",
                    navController = navController,
                    navDestinationRoute = NavRoute.profile
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunctionCard(
    title: String,
    description: String,
    navController: NavController,
    navDestinationRoute: String
) {
    Card(onClick = {
        navController.navigate(navDestinationRoute)
        currentNavRoute = navDestinationRoute
        if (isDebugging) navController.currentDestination?.route?.showToast()
    }, modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(6.dp)
        ) {
            Text(
                text = title,
                fontSize = LocalTextStyle.current.fontSize * 2
            )
            Text(text = description)
        }
    }
}