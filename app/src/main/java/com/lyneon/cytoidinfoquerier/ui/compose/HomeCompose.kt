package com.lyneon.cytoidinfoquerier.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.ui.compose.component.AlertCard
import com.lyneon.cytoidinfoquerier.ui.compose.component.TopBar
import kotlinx.coroutines.launch

@Composable
fun HomeCompose(drawerState: DrawerState) {
    Column {
        TopBar(drawerState = drawerState)
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AlertCard(message = stringResource(id = R.string.debug_declaration))
//            Card {
//                val packageManager = BaseApplication.context.packageManager
//                val cytoidApplicationInfo =
//                    try {
//                        packageManager.getApplicationInfo("me.tigerhix.cytoid", 0)
//                    } catch (e: NameNotFoundException) {
//                        null
//                    }
//                Column(
//                    modifier = Modifier.padding(6.dp)
//                ) {
//                    Text(text = stringResource(id = R.string.current_installed_cytoid))
//                    Row {
//                        if (cytoidApplicationInfo != null) {
//                            Image(
//                                bitmap = cytoidApplicationInfo.loadIcon(packageManager).toBitmap()
//                                    .asImageBitmap(),
//                                contentDescription = cytoidApplicationInfo.loadLabel(packageManager)
//                                    .toString()
//                            )
//                            Column {
//                                Text(
//                                    text = cytoidApplicationInfo.loadLabel(packageManager)
//                                        .toString()
//                                )
//                                cytoidApplicationInfo.name?.let { Text(text = it) }
//                                Text(text = cytoidApplicationInfo.packageName)
//                            }
//                        } else {
//                            Text(text = stringResource(id = R.string.cytoid_not_found))
//                        }
//                    }
//                }
//            }
            val scope = rememberCoroutineScope()
            ExtendedFloatingActionButton(
                text = { Text(text = stringResource(id = R.string.open_drawer)) },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = stringResource(id = R.string.open_drawer)
                    )
                },
                onClick = { scope.launch { drawerState.open() } }
            )
        }
    }
}