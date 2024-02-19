package com.lyneon.cytoidinfoquerier.ui.compose.component

import android.os.Looper
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.tool.extension.showToast
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.concurrent.thread

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String = BaseApplication.context.getString(R.string.app_name),
    additionalActions: @Composable ((Unit) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    CenterAlignedTopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = {
                scope.launch {
                    BaseApplication.globalDrawerState.open()
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = stringResource(id = R.string.drawerMenu)
                )
            }
        },
        actions = {
            var menuIsExpanded by remember { mutableStateOf(false) }
            additionalActions?.invoke(Unit)
            IconButton(onClick = { menuIsExpanded = !menuIsExpanded }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "选项菜单"
                )
            }
            DropdownMenu(
                expanded = menuIsExpanded,
                onDismissRequest = { menuIsExpanded = false }) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_public_24),
                            contentDescription = stringResource(id = R.string.ping)
                        )
                    },
                    text = { Text(text = stringResource(id = R.string.ping)) },
                    onClick = {
                        menuIsExpanded = false
                        "ping start".showToast()
                        thread {
                            val responseCode = OkHttpClient().newCall(
                                Request.Builder().url("https://cytoid.io/")
                                    .head()
                                    .removeHeader("User-Agent")
                                    .addHeader(
                                        "User-Agent",
                                        "CytoidClient/2.1.1"
                                    )
                                    .build()
                            ).execute().code
                            Looper.prepare()
                            "cytoid.io:$responseCode".showToast()
                        }
                    }
                )
            }
        }
    )
}