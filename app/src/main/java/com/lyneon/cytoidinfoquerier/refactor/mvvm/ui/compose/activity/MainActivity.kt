package com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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

        setContent {
            val navHostController = rememberNavController()

            CytoidInfoQuerierComposeTheme {
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
                    selected = navHostController.currentDestination?.route == MainActivity.Screen.Home.route,
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
                    selected = navHostController.currentDestination?.route == MainActivity.Screen.Analytics.route,
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
                    selected = navHostController.currentDestination?.route == MainActivity.Screen.Profile.route,
                    onClick = {
                        navHostController.navigate(MainActivity.Screen.Profile.route) {
                            launchSingleTop = true
                            popUpTo(MainActivity.Screen.Profile.route)
                        }
                    }
                )
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
        startDestination = MainActivity.Screen.Home.route
    ) {
        composable(MainActivity.Screen.Home.route) { HomeScreen() }
        composable(MainActivity.Screen.Analytics.route) { AnalyticsScreen() }
        composable(MainActivity.Screen.Profile.route) { ProfileScreen() }
        composable(MainActivity.Screen.Settings.route) { SettingsScreen() }
    }
}