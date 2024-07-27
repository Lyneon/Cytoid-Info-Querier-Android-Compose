package com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.activity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.compose.setContent
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
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lyneon.cytoidinfoquerier.BaseActivity
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.MainActivityScreens
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.screen.AnalyticsScreen
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.screen.HomeScreen
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.screen.ProfileScreen
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.screen.SettingsScreen
import com.lyneon.cytoidinfoquerier.ui.theme.CytoidInfoQuerierComposeTheme

class MainActivity : BaseActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val mainActivity = LocalContext.current as MainActivity
            val navHostController = rememberNavController()

            CytoidInfoQuerierComposeTheme {
                mainActivity.window.apply {
                    navigationBarColor = Color.Transparent.toArgb()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (calculateWindowSizeClass(this).widthSizeClass == WindowWidthSizeClass.Expanded)
                        PermanentNavigationDrawer(
                            drawerContent = { DrawerContent(navHostController) { this.finish() } }
                        ) {
                            MainContent(navHostController = navHostController)
                        }
                    else
                        ModalNavigationDrawer(
                            drawerContent = { DrawerContent(navHostController) { this.finish() } }
                        ) {
                            MainContent(navHostController = navHostController)
                        }
                }
            }
        }
    }

    enum class Screen(val route: String) {
        Home("home"),
        Analytics("analytics"),
        Profile("profile"),
        Settings("settings")
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
                    selected = currentScreenRoute == MainActivity.Screen.Analytics.route,
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
                    selected = currentScreenRoute == MainActivity.Screen.Profile.route,
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
        composable(MainActivity.Screen.Analytics.route) { AnalyticsScreen() }
        composable(MainActivity.Screen.Profile.route) { ProfileScreen() }
        composable(MainActivity.Screen.Settings.route) { SettingsScreen() }
    }
}