package com.lyneon.cytoidinfoquerier.ui.activity

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lyneon.cytoidinfoquerier.BaseActivity
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.Secret
import com.lyneon.cytoidinfoquerier.ui.compose.screen.AboutScreen
import com.lyneon.cytoidinfoquerier.ui.compose.screen.AnalyticsScreen
import com.lyneon.cytoidinfoquerier.ui.compose.screen.GridColumnsCountSettingScreen
import com.lyneon.cytoidinfoquerier.ui.compose.screen.HistoryScreen
import com.lyneon.cytoidinfoquerier.ui.compose.screen.HomeScreen
import com.lyneon.cytoidinfoquerier.ui.compose.screen.LeaderboardScreen
import com.lyneon.cytoidinfoquerier.ui.compose.screen.LevelDetailScreen
import com.lyneon.cytoidinfoquerier.ui.compose.screen.LevelScreen
import com.lyneon.cytoidinfoquerier.ui.compose.screen.ProfileScreen
import com.lyneon.cytoidinfoquerier.ui.compose.screen.SettingsScreen
import com.lyneon.cytoidinfoquerier.ui.compose.screen.ToolScreen
import com.lyneon.cytoidinfoquerier.ui.compose.screen.WebViewScreen
import com.lyneon.cytoidinfoquerier.ui.theme.CytoidInfoQuerierComposeTheme
import com.lyneon.cytoidinfoquerier.util.AppSettingsMMKVKeys
import com.lyneon.cytoidinfoquerier.util.MMKVId
import com.tencent.mmkv.MMKV
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid
import io.sentry.android.core.SentryAndroidOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val mmkv = MMKV.mmkvWithID(MMKVId.AppSettings.id)

        if (mmkv.decodeBool(AppSettingsMMKVKeys.ENABLE_SENTRY.name, true)) {
            SentryAndroid.init(this) { options: SentryAndroidOptions ->
                options.setDsn(Secret.SENTRY_DSN)
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
            val mainActivity = LocalActivity.current as MainActivity
            val navHostController = rememberNavController()
            val mainActivityUIState by remember { mutableStateOf(MainActivityUIState()) }

            navHostController.addOnDestinationChangedListener { _, destination, _ ->
                if (destination.route != Screen.GridColumnsCountSetting.route) {
                    mainActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }

            CytoidInfoQuerierComposeTheme {
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
                                )
                            }
                            MainContent(
                                navHostController = navHostController,
                                mainActivityUIState = mainActivityUIState
                            )
                        } else
                            ModalNavigationDrawer(
                                drawerContent = {
                                    DrawerContent(
                                        navHostController,
                                        mainActivityUIState
                                    ) { mainActivity.finish() }
                                },
                                modifier = Modifier.fillMaxSize(),
                                drawerState = mainActivityUIState.drawerState
                            ) {
                                MainContent(
                                    navHostController = navHostController,
                                    mainActivityUIState = mainActivityUIState
                                )
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
        ProfileHistory("history/profile"),
        About("about"),
        Level("level"),
        Tool("tool"),
        LevelDetail("levelDetail"),
        Leaderboard("leaderboard"),
        WebView("webview")
    }
}

@Composable
private fun DrawerContent(
    navHostController: NavHostController,
    uiState: MainActivityUIState,
    onExitButtonClick: () -> Unit
) {
    var currentScreenRoute by rememberSaveable { mutableStateOf(MainActivity.Screen.Home.route) }
    val scope = rememberCoroutineScope()

    navHostController.addOnDestinationChangedListener { _, destination, _ ->
        currentScreenRoute = destination.route.toString()
    }

    BackHandler(enabled = uiState.drawerState.isOpen) {
        scope.launch {
            uiState.drawerState.close()
        }
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
                        scope.launch {
                            uiState.drawerState.close()
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
                        scope.launch {
                            uiState.drawerState.close()
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
                        scope.launch {
                            uiState.drawerState.close()
                        }
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = stringResource(R.string.level)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = null
                        )
                    },
                    selected = currentScreenRoute.startsWith(MainActivity.Screen.Level.route),
                    onClick = {
                        navHostController.navigate(MainActivity.Screen.Level.route) {
                            launchSingleTop = true
                            popUpTo(MainActivity.Screen.Level.route)
                        }
                        scope.launch {
                            uiState.drawerState.close()
                        }
                    }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.leaderboard)) },
                    icon = {
                        Icon(imageVector = Icons.Default.Leaderboard, contentDescription = null)
                    },
                    selected = currentScreenRoute == MainActivity.Screen.Leaderboard.route,
                    onClick = {
                        navHostController.navigate(MainActivity.Screen.Leaderboard.route) {
                            launchSingleTop = true
                            popUpTo(MainActivity.Screen.Leaderboard.route)
                        }
                        scope.launch {
                            uiState.drawerState.close()
                        }
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = stringResource(R.string.tool)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Construction,
                            contentDescription = null
                        )
                    },
                    selected = currentScreenRoute.startsWith(MainActivity.Screen.Tool.route),
                    onClick = {
                        navHostController.navigate(MainActivity.Screen.Tool.route) {
                            launchSingleTop = true
                            popUpTo(MainActivity.Screen.Tool.route)
                        }
                        scope.launch {
                            uiState.drawerState.close()
                        }
                    }
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(onClick = {
                    navHostController.navigate(MainActivity.Screen.Settings.name) {
                        launchSingleTop = true
                        this.popUpTo(MainActivity.Screen.Settings.name)
                    }
                    scope.launch {
                        uiState.drawerState.close()
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
    navHostController: NavHostController
) {
    var currentScreenRoute by rememberSaveable { mutableStateOf(MainActivity.Screen.Home.route) }
    val activity = LocalActivity.current as MainActivity

    navHostController.addOnDestinationChangedListener { _, destination, _ ->
        currentScreenRoute = destination.route.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Spacer(Modifier.height(0.dp))
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
            NavigationRailItem(
                selected = currentScreenRoute.startsWith(MainActivity.Screen.Level.route),
                onClick = {
                    navHostController.navigate(MainActivity.Screen.Level.route) {
                        launchSingleTop = true
                        popUpTo(MainActivity.Screen.Level.route)
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = null
                    )
                },
                label = { Text(text = stringResource(R.string.level)) }
            )
            NavigationRailItem(
                selected = currentScreenRoute == MainActivity.Screen.Leaderboard.route,
                onClick = {
                    navHostController.navigate(MainActivity.Screen.Leaderboard.route) {
                        launchSingleTop = true
                        popUpTo(MainActivity.Screen.Leaderboard.route)
                    }
                },
                icon = {
                    Icon(imageVector = Icons.Default.Leaderboard, contentDescription = null)
                },
                label = { Text(text = stringResource(R.string.leaderboard)) }
            )
            NavigationRailItem(
                selected = currentScreenRoute.startsWith(MainActivity.Screen.Tool.route),
                onClick = {
                    navHostController.navigate(MainActivity.Screen.Tool.route) {
                        launchSingleTop = true
                        popUpTo(MainActivity.Screen.Tool.route)
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Construction,
                        contentDescription = null
                    )
                },
                label = { Text(text = stringResource(R.string.tool)) }
            )
            Spacer(Modifier.height(0.dp))
        }
        HorizontalDivider(modifier = Modifier.width(64.dp))
        Column {
            Spacer(Modifier.height(8.dp))
            NavigationRailItem(
                selected = currentScreenRoute == MainActivity.Screen.Settings.route,
                onClick = {
                    navHostController.navigate(MainActivity.Screen.Settings.name) {
                        launchSingleTop = true
                        this.popUpTo(MainActivity.Screen.Settings.name)
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
            Spacer(Modifier.height(8.dp))
            Button(onClick = { activity.finish() }) {
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MainContent(
    navHostController: NavHostController,
    mainActivityUIState: MainActivityUIState
) {
    SharedTransitionLayout {
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
            composable(MainActivity.Screen.Home.route) {
                val coroutinesScope = rememberCoroutineScope()
                HomeScreen(navController = navHostController, onDrawerButtonClick = {
                    coroutinesScope.launch(Dispatchers.Main) {
                        mainActivityUIState.drawerState.open()
                    }
                })
            }
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
            composable(MainActivity.Screen.Analytics.route + "/{shortcutPreset}") {
                AnalyticsScreen(
                    navController = navHostController,
                    navBackStackEntry = it,
                    withShortcutPreset = true
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
            composable(MainActivity.Screen.Profile.route + "/shortcut") {
                ProfileScreen(
                    navController = navHostController,
                    navBackStackEntry = it,
                    withShortcut = true
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
            composable(MainActivity.Screen.About.route) {
                AboutScreen(navController = navHostController)
            }
            composable(MainActivity.Screen.Level.route) {
                LevelScreen(
                    navController = navHostController,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this
                )
            }
            composable(MainActivity.Screen.Tool.route) {
                ToolScreen()
            }
            composable(MainActivity.Screen.LevelDetail.route) {
                LevelDetailScreen(
                    navController = navHostController,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this
                )
            }
            composable(MainActivity.Screen.Leaderboard.route) {
                LeaderboardScreen()
            }
            composable(MainActivity.Screen.WebView.route + "/{initialUrl}") {
                WebViewScreen(navHostController, it)
            }
        }
    }
}

class MainActivityUIState(
    val drawerState: DrawerState = DrawerState(DrawerValue.Closed)
)