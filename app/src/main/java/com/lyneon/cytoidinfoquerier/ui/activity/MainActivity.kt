package com.lyneon.cytoidinfoquerier.ui.activity

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.lyneon.cytoidinfoquerier.BaseActivity
import com.lyneon.cytoidinfoquerier.BaseApplication.Companion.context
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.CytoidConstant
import com.lyneon.cytoidinfoquerier.data.constant.MMKVKeys
import com.lyneon.cytoidinfoquerier.data.constant.MainActivityScreens
import com.lyneon.cytoidinfoquerier.ui.compose.AnalyticsCompose
import com.lyneon.cytoidinfoquerier.ui.compose.GridColumnsSettingCompose
import com.lyneon.cytoidinfoquerier.ui.compose.HomeCompose
import com.lyneon.cytoidinfoquerier.ui.compose.ProfileCompose
import com.lyneon.cytoidinfoquerier.ui.compose.SettingsCompose
import com.lyneon.cytoidinfoquerier.ui.theme.CytoidInfoQuerierComposeTheme
import com.tencent.mmkv.MMKV
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.SentryOptions.BeforeSendCallback
import io.sentry.android.core.SentryAndroid
import io.sentry.android.core.SentryAndroidOptions
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    companion object {
        lateinit var drawerState: DrawerState
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mmkv = MMKV.defaultMMKV()

        if (mmkv.decodeBool(MMKVKeys.ENABLE_SENTRY.name, true)) {
            SentryAndroid.init(this) { options: SentryAndroidOptions ->
                options.setDsn("https://0149a51a6abff3008e5272ea306abf47@o4507079700971520.ingest.de.sentry.io/4507079706804304")
                // Add a callback that will be used before the event is sent to Sentry.
                // With this callback, you can modify the event or, when returning null, also discard the event.
                options.beforeSend =
                    BeforeSendCallback { event: SentryEvent, _ ->
                        event.level?.let { eventLevel ->
                            if (eventLevel < SentryLevel.ERROR) return@BeforeSendCallback null
                            else return@BeforeSendCallback event
                        }
                    }
            }
        }

        setContent {
            CytoidInfoQuerierComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    var currentRoute by remember { mutableStateOf(MainActivityScreens.Home.name) }
                    val scope = rememberCoroutineScope()
                    val selfPackageInfo =
                        context.packageManager.getPackageInfo(context.packageName, 0)
                    val cytoidPackageInfo = try {
                        context.packageManager.getPackageInfo(CytoidConstant.gamePackageName, 0)
                    } catch (e: Exception) {
                        null
                    }

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.app_name),
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Text(
                                            text = "${selfPackageInfo.versionName}${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) "(${selfPackageInfo.longVersionCode})" else ""}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "已安装的Cytoid版本：${
                                                if (cytoidPackageInfo != null) {
                                                    "${cytoidPackageInfo.versionName}${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) "(${cytoidPackageInfo.longVersionCode})" else ""}"
                                                } else {
                                                    "未找到"
                                                }
                                            }",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    HorizontalDivider()
                                    Column(
                                        Modifier.padding(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        NavigationDrawerItem(
                                            label = {
                                                Text(text = stringResource(R.string.home))
                                            },
                                            icon = {
                                                Icon(
                                                    Icons.Filled.Home,
                                                    stringResource(R.string.home)
                                                )
                                            },
                                            selected = currentRoute == MainActivityScreens.Home.name,
                                            onClick = {
                                                navController.navigate(MainActivityScreens.Home.name) {
                                                    popUpTo(MainActivityScreens.Home.name) {
                                                        inclusive = false
                                                    }
                                                    launchSingleTop = true
                                                }
                                                scope.launch {
                                                    drawerState.close()
                                                }
                                                currentRoute =
                                                    navController.currentDestination?.route ?: ""
                                            }
                                        )
                                        NavigationDrawerItem(
                                            label = {
                                                Text(text = stringResource(R.string.analytics))
                                            },
                                            icon = {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.ShowChart,
                                                    stringResource(R.string.analytics)
                                                )
                                            },
                                            selected = currentRoute == MainActivityScreens.Analytics.name,
                                            onClick = {
                                                navController.navigate(MainActivityScreens.Analytics.name) {
                                                    popUpTo(MainActivityScreens.Analytics.name) {
                                                        inclusive = false
                                                    }
                                                    launchSingleTop = true
                                                }
                                                scope.launch {
                                                    drawerState.close()
                                                }
                                                currentRoute =
                                                    navController.currentDestination?.route ?: ""
                                            }
                                        )
                                        NavigationDrawerItem(
                                            label = {
                                                Text(text = stringResource(R.string.profile))
                                            },
                                            icon = {
                                                Icon(
                                                    Icons.Default.AccountCircle,
                                                    stringResource(R.string.profile)
                                                )
                                            },
                                            selected = currentRoute == MainActivityScreens.Profile.name,
                                            onClick = {
                                                navController.navigate(MainActivityScreens.Profile.name) {
                                                    popUpTo(MainActivityScreens.Profile.name) {
                                                        inclusive = false
                                                    }
                                                    launchSingleTop = true
                                                }
                                                scope.launch {
                                                    drawerState.close()
                                                }
                                                currentRoute =
                                                    navController.currentDestination?.route ?: ""
                                            }
                                        )
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(
                                        6.dp,
                                        Alignment.End
                                    ),
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .fillMaxWidth(),
                                ) {
                                    Button(onClick = {
                                        navController.navigate(MainActivityScreens.Settings.name) {
                                            popUpTo(MainActivityScreens.Settings.name) {
                                                inclusive = false
                                            }
                                            launchSingleTop = true
                                        }
                                        scope.launch {
                                            drawerState.close()
                                        }
                                        currentRoute =
                                            navController.currentDestination?.route ?: ""
                                    }) {
                                        Column {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = stringResource(id = R.string.settings),
                                                modifier = Modifier.align(Alignment.CenterHorizontally)
                                            )
                                            Text(text = stringResource(id = R.string.settings))
                                        }
                                    }
                                    Button(onClick = { this@MainActivity.finish() }) {
                                        Column {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                                contentDescription = stringResource(id = R.string.exit),
                                                modifier = Modifier.align(Alignment.CenterHorizontally)
                                            )
                                            Text(text = stringResource(id = R.string.exit))
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        Column {
                            NavHost(
                                navController = navController,
                                startDestination = MainActivityScreens.Home.name
                            ) {
                                composable(MainActivityScreens.Home.name) {
                                    HomeCompose {
                                        scope.launch {
                                            drawerState.open()
                                        }
                                    }
                                }
                                composable(MainActivityScreens.Analytics.name) {
                                    AnalyticsCompose()
                                }
                                composable(MainActivityScreens.Profile.name) {
                                    ProfileCompose()
                                }
                                navigation(
                                    startDestination = MainActivityScreens.Settings.name,
                                    route = MainActivityScreens.SettingsNavigation.name
                                ) {
                                    composable(MainActivityScreens.Settings.name) {
                                        SettingsCompose(onNavigateToGridColumnsSetting = {
                                            navController.navigate(MainActivityScreens.GridColumnsSetting.name)
                                            currentRoute =
                                                navController.currentDestination?.route ?: ""
                                        })
                                    }
                                    composable(MainActivityScreens.GridColumnsSetting.name) {
                                        GridColumnsSettingCompose(onNavigateBack = {
                                            navController.popBackStack()
                                            currentRoute =
                                                navController.currentDestination?.route ?: ""
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}