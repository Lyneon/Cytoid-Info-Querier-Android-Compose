package com.lyneon.cytoidinfoquerier.ui.compose.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.ui.activity.MainActivity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String = BaseApplication.context.getString(R.string.app_name),
    actionsAlwaysShow: @Composable (() -> Unit)? = null,
    actionsDropDownMenuContent: @Composable (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    CenterAlignedTopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = {
                scope.launch {
                    MainActivity.drawerState.open()
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = stringResource(id = R.string.drawer_menu)
                )
            }
        },
        actions = {
            var menuIsExpanded by remember { mutableStateOf(false) }
            actionsAlwaysShow?.let { it() }
            actionsDropDownMenuContent?.let {
                IconButton(onClick = { menuIsExpanded = !menuIsExpanded }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "选项菜单"
                    )
                }
                DropdownMenu(
                    expanded = menuIsExpanded,
                    onDismissRequest = { menuIsExpanded = false }
                ) { it() }
            }
        }
    )
}