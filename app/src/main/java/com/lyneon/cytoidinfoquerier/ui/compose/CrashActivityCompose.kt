package com.lyneon.cytoidinfoquerier.ui.compose

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Process
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.ui.activity.CrashActivity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashActivityCompose(crashMessage: String) {
    val context = LocalContext.current as CrashActivity

    Column {
        CenterAlignedTopAppBar(
            title = { Text(text = stringResource(id = R.string.title_activity_crash)) },
            navigationIcon = {
                IconButton(onClick = {
                    val intent =
                        context.packageManager.getLaunchIntentForPackage(context.packageName)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        context.startActivity(intent)
                    }
                    Process.killProcess(Process.myPid())
                }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_menu_restart),
                        contentDescription = stringResource(id = R.string.restart)
                    )
                }
            },
            actions = {
                IconButton(onClick = {
                    if (crashMessage.isNotEmpty()) {
                        val clipboardManager =
                            context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        clipboardManager.setPrimaryClip(
                            ClipData.newPlainText(
                                "errorMessage",
                                crashMessage
                            )
                        )
                        Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_menu_copy),
                        contentDescription = "复制"
                    )
                }
            }
        )
        Text(
            text = crashMessage,
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(6.dp)
        )
    }
}