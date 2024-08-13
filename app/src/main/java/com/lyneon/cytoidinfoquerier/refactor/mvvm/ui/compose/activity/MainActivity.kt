package com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.activity

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lyneon.cytoidinfoquerier.BaseActivity
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.MMKVKeys
import com.lyneon.cytoidinfoquerier.data.constant.MainActivityScreens
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.screen.AnalyticsScreen
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.screen.GridColumnsCountSettingScreen
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.screen.HistoryScreen
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.screen.HomeScreen
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.screen.ProfileScreen
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.screen.SettingsScreen
import com.lyneon.cytoidinfoquerier.ui.theme.CytoidInfoQuerierComposeTheme
import com.tencent.mmkv.MMKV
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid
import io.sentry.android.core.SentryAndroidOptions

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val mmkv = MMKV.defaultMMKV()

        if (mmkv.decodeBool(MMKVKeys.ENABLE_SENTRY.name, true)) {
            SentryAndroid.init(this) { options: SentryAndroidOptions ->
                options.setDsn("https://0149a51a6abff3008e5272ea306abf47@o4507079700971520.ingest.de.sentry.io/4507079706804304")
                // Add a callback that will be used before the event is sent to Sentry.
                // With this callback, you can modify the event or, when returning null, also discard the event.
                options.beforeSend =
                    SentryOptions.BeforeSendCallback { event: SentryEvent, _ ->
                        event.level?.let { eventLevel ->
                            return@BeforeSendCallback if (eventLevel < SentryLevel.ERROR) null else event
                        }
                    }
            }
        }

        setContent {
            val mainActivity = LocalContext.current as MainActivity
            val navHostController = rememberNavController()

            navHostController.addOnDestinationChangedListener { _, destination, _ ->
                if (destination.route != Screen.GridColumnsCountSetting.route) {
                    mainActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }

            CytoidInfoQuerierComposeTheme {
                mainActivity.window.apply {
                    navigationBarColor = Color.Transparent.toArgb()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ) {
                                RailContent(
                                    navHostController = navHostController
                                ) { mainActivity.finish() }
                            }
                            MainContent(navHostController = navHostController)
                        } else
                            ModalNavigationDrawer(
                                drawerContent = { DrawerContent(navHostController) { mainActivity.finish() } },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                MainContent(navHostController = navHostController)
                            }
                    }
                }
            }
        }
    }

    enum class Screen(val route: String) {
        Home("home"),
        Analytics("analytics"),
        Profile("profile"),
        Settings("settings"),
        GridColumnsCountSetting("settings/gridColumnsCount"),
        History("history/{type}"),
        AnalyticsHistory("history/analytics"),
        ProfileHistory("history/profile")
    }
}

@Composable
private fun DrawerContent(navHostController: NavHostController, onExitButtonClick: () -> Unit) {
    var currentScreenRoute by remember { mutableStateOf(MainActivity.Screen.Home.route) }

    navHostController.addOnDestinationChangedListener { _, destination, _ ->
        currentScreenRoute = destination.route.toString()
    }

    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                NavigationDrawerItem(
                    label = { Text(text = stringResource(R.string.home)) },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = null) },
                    selected = currentScreenRoute == MainActivity.Screen.Home.route,
                    onClick = {
                        navHostController.navigate(MainActivity.Screen.Home.route) {
                            launchSingleTop = true
                            popUpTo(MainActivity.Screen.Home.route)
                        }
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = stringResource(R.string.analytics)) },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = null
                        )
                    },
                    selected = currentScreenRoute.startsWith(MainActivity.Screen.Analytics.route),
                    onClick = {
                        navHostController.navigate(MainActivity.Screen.Analytics.route) {
                            launchSingleTop = true
                            popUpTo(MainActivity.Screen.Analytics.route)
                        }
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = stringResource(R.string.profile)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null
                        )
                    },
                    selected = currentScreenRoute.startsWith(MainActivity.Screen.Profile.route),
                    onClick = {
                        navHostController.navigate(MainActivity.Screen.Profile.route) {
                            launchSingleTop = true
                            popUpTo(MainActivity.Screen.Profile.route)
                        }
                    }
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(onClick = {
                    navHostController.navigate(MainActivityScreens.Settings.name) {
                        launchSingleTop = true
                        this.popUpTo(MainActivityScreens.Settings.name)
                    }
                }) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(text = stringResource(id = R.string.settings))
                    }
                }
                Button(onClick = onExitButtonClick) {
                    Column {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(text = stringResource(id = R.string.exit))
                    }
                }
            }
        }
    }
}

@Composable
private fun RailContent(
    navHostController: NavHostController,
    onExitButtonClick: () -> Unit
) {
    var currentScreenRoute by remember { mutableStateOf(MainActivity.Screen.Home.route) }

    navHostController.addOnDestinationChangedListener { _, destination, _ ->
        currentScreenRoute = destination.route.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NavigationRailItem(
                selected = currentScreenRoute == MainActivity.Screen.Home.route,
                onClick = {
                    navHostController.navigate(MainActivity.Screen.Home.route) {
                        launchSingleTop = true
                        popUpTo(MainActivity.Screen.Home.route)
                    }
                },
                icon = { Icon(imageVector = Icons.Default.Home, contentDescription = null) },
                label = { Text(text = stringResource(R.string.home)) }
            )
            NavigationRailItem(
                selected = currentScreenRoute.startsWith(MainActivity.Screen.Analytics.route),
                onClick = {
                    navHostController.navigate(MainActivity.Screen.Analytics.route) {
                        launchSingleTop = true
                        popUpTo(MainActivity.Screen.Analytics.route)
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ShowChart,
                        contentDescription = null
                    )
                },
                label = { Text(text = stringResource(R.string.analytics)) }
            )
            NavigationRailItem(
                selected = currentScreenRoute.startsWith(MainActivity.Screen.Profile.route),
                onClick = {
                    navHostController.navigate(MainActivity.Screen.Profile.route) {
                        launchSingleTop = true
                        popUpTo(MainActivity.Screen.Profile.route)
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null
                    )
                },
                label = { Text(text = stringResource(R.string.profile)) }
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NavigationRailItem(
                selected = currentScreenRoute == MainActivity.Screen.Settings.route,
                onClick = {
                    navHostController.navigate(MainActivityScreens.Settings.name) {
                        launchSingleTop = true
                        this.popUpTo(MainActivityScreens.Settings.name)
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                },
                label = { Text(text = stringResource(R.string.settings)) }
            )
            Button(onClick = onExitButtonClick) {
                Column {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(text = stringResource(id = R.string.exit))
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun MainContent(navHostController: NavHostController) {
    NavHost(
        navController = navHostController,
        startDestination = MainActivity.Screen.Home.route,
        enterTransition = {
            fadeIn() + scaleIn(initialScale = 0.8f)
        },
        exitTransition = {
            fadeOut() + scaleOut(targetScale = 0.8f)
        }
    ) {
        composable(MainActivity.Screen.Home.route) { HomeScreen() }
        composable(MainActivity.Screen.Analytics.route) {
            AnalyticsScreen(
                navController = navHostController,
                navBackStackEntry = it
            )
        }
        composable(MainActivity.Screen.Analytics.route + "/{initialCytoidID}/{initialCacheType}/{initialCacheTime}") {
            AnalyticsScreen(
                navController = navHostController,
                navBackStackEntry = it,
                withInitials = true
            )
        }
        composable(MainActivity.Screen.Profile.route) {
            ProfileScreen(
                navController = navHostController,
                navBackStackEntry = it
            )
        }
        composable(MainActivity.Screen.Profile.route + "/{initialCytoidID}/{initialCacheTime}") {
            ProfileScreen(
                navController = navHostController,
                navBackStackEntry = it,
                withInitials = true
            )
        }
        composable(MainActivity.Screen.Settings.route) { SettingsScreen(navController = navHostController) }
        composable(MainActivity.Screen.GridColumnsCountSetting.route) {
            GridColumnsCountSettingScreen(
                navController = navHostController
            )
        }
        composable(MainActivity.Screen.History.route) {
            HistoryScreen(
                navController = navHostController,
                navBackStackEntry = it
            )
        }
    }
}