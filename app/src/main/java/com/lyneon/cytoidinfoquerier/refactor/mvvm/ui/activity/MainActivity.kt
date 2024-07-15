package com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lyneon.cytoidinfoquerier.BaseActivity
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.MainActivityScreens
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.screen.AnalyticsScreen
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.screen.HomeScreen
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.screen.ProfileScreen
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
                            drawerContent = {
                                DrawerContent(navController = navHostController)
                            }
                        ) {
                            MainContent(navController = navHostController)
                        }
                    else
                        ModalNavigationDrawer(
                            drawerContent = {
                                DrawerContent(navController = navHostController)
                            }
                        ) {
                            MainContent(navController = navHostController)
                        }
                }
            }
        }
    }

    enum class Screen(val route: String) {
        Home("home"),
        Analytics("analytics"),
        Profile("profile")
        // TODO: Add more screens here
    }
}

@Composable
private fun DrawerContent(navController: NavHostController) {
    ModalDrawerSheet {
        Column {
            Button(onClick = { navController.navigate(MainActivityScreens.Analytics.name) }) {
                Text(text = stringResource(R.string.analytics))
            }
            Button(onClick = { navController.navigate(MainActivityScreens.Profile.name) }) {
                Text(text = stringResource(R.string.profile))
            }
            //TODO: Add more screens here
        }
    }
}

@Composable
private fun MainContent(navController: NavHostController) {
    NavHost(navController = navController, startDestination = MainActivity.Screen.Home.route) {
        composable(MainActivity.Screen.Home.route) { HomeScreen() }
        composable(MainActivity.Screen.Analytics.route) { AnalyticsScreen() }
        composable(MainActivity.Screen.Profile.route) { ProfileScreen() }
        // TODO: Add more screens here
    }
}