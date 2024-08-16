package com.lyneon.cytoidinfoquerier.ui.compose.screen

import android.content.Intent
import android.os.Process
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.ui.activity.CrashActivity
import com.lyneon.cytoidinfoquerier.util.extension.saveIntoClipboard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashActivityScreen(crashMessage: String) {
    val context = LocalContext.current as CrashActivity

    Column {
        CenterAlignedTopAppBar(
            title = { Text(text = stringResource(id = R.string.title_activity_crash)) },
            navigationIcon = {
                TextButton(onClick = {
                    val intent =
                        context.packageManager.getLaunchIntentForPackage(context.packageName)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        context.startActivity(intent)
                    }
                    Process.killProcess(Process.myPid())
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(id = R.string.restart)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(id = R.string.restart))
                }
            },
            actions = {
                TextButton(onClick = {
                    if (crashMessage.isNotEmpty()) {
                        crashMessage.saveIntoClipboard("errorMessage")
                        Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(text = stringResource(id = R.string.copy))
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "复制"
                    )
                }
            }
        )
        SelectionContainer {
            Text(
                text = crashMessage,
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(6.dp)
            )
        }
    }
}