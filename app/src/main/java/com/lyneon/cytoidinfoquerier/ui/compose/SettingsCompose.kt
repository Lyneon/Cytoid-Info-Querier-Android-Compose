package com.lyneon.cytoidinfoquerier.ui.compose

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Process
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult.*
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lyneon.cytoidinfoquerier.BaseApplication.Companion.context
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.ui.compose.component.TopBar
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsCompose() {
    val mmkv = MMKV.defaultMMKV()
    val scope = rememberCoroutineScope()
    var enableAppCenter by remember {
        mutableStateOf(mmkv.decodeBool(SettingsMMKVKeys.enableAppCenter, true))
    }
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = { TopBar(title = stringResource(id = R.string.settings)) },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            enableAppCenter = !enableAppCenter
                            mmkv.encode(SettingsMMKVKeys.enableAppCenter, enableAppCenter)
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                val result = snackbarHostState.showSnackbar(
                                    context.getString(R.string.changes_need_restart_to_enable),
                                    context.getString(R.string.restart),
                                    true,
                                    SnackbarDuration.Short
                                )
                                when (result) {
                                    ActionPerformed -> {
                                        val intent =
                                            context.packageManager.getLaunchIntentForPackage(context.packageName)
                                        if (intent != null) {
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            context.startActivity(intent)
                                        }
                                        Process.killProcess(Process.myPid())
                                    }

                                    Dismissed -> {}
                                }
                            }
                        }
                        .padding(6.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.enable_app_center),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Switch(
                        checked = enableAppCenter, onCheckedChange = { checked ->
                            enableAppCenter = checked
                            mmkv.encode(SettingsMMKVKeys.enableAppCenter, checked)
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                when (snackbarHostState.showSnackbar(
                                    context.getString(R.string.changes_need_restart_to_enable),
                                    context.getString(R.string.restart),
                                    true,
                                    SnackbarDuration.Short
                                )) {
                                    ActionPerformed -> {
                                        val intent =
                                            context.packageManager.getLaunchIntentForPackage(context.packageName)
                                        if (intent != null) {
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            context.startActivity(intent)
                                        }
                                        Process.killProcess(Process.myPid())
                                    }

                                    Dismissed -> {}
                                }
                            }
                        }
                    )
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                when (snackbarHostState.showSnackbar(
                                    context.getString(R.string.delete_confirm),
                                    context.getString(R.string.confirm),
                                    true,
                                    SnackbarDuration.Short
                                )) {
                                    Dismissed -> {}
                                    ActionPerformed -> {
                                        val cacheDir = context.externalCacheDir?.listFiles()
                                        cacheDir?.run {
                                            for (file in cacheDir) {
                                                file.delete()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        .padding(6.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.delete_cache_image),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                when (snackbarHostState.showSnackbar(
                                    context.getString(R.string.testCrash),
                                    context.getString(R.string.confirm),
                                    true,
                                    SnackbarDuration.Short
                                )) {
                                    Dismissed -> {}
                                    ActionPerformed -> {
                                        throw Exception("This is a crash for test!")
                                    }
                                }
                            }
                        }
                        .padding(6.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.testCrash),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }

        }
    }
}

object SettingsMMKVKeys {
    const val enableAppCenter = "enableAppCenter"
}