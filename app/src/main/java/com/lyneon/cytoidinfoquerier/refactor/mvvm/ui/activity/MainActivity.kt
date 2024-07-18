package com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lyneon.cytoidinfoquerier.BaseActivity
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
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
                            drawerContent = { DrawerContent(navHostController) }
                        ) {
                            MainContent(navHostController = navHostController)
                        }
                    else
                        ModalNavigationDrawer(
                            drawerContent = { DrawerContent(navHostController) }
                        ) {
                            MainContent(navHostController = navHostController)
                        }
                }
            }
        }
    }

    enum class Screen(val route: String, val label: String) {
        Home("home", BaseApplication.context.getString(R.string.home)),
        Analytics(
            "analytics",
            BaseApplication.context.getString(R.string.analytics)
        ),
        Profile("profile", BaseApplication.context.getString(R.string.profile))
    }
}

@Composable
private fun DrawerContent(navHostController: NavHostController) {
    ModalDrawerSheet {
        Column {
            Button(onClick = {
                navHostController.navigate(MainActivity.Screen.Analytics.route) {
                    launchSingleTop = true
                    popUpTo(
                        MainActivity.Screen.Analytics.route
                    )
                }
            }) {
                Text(text = stringResource(R.string.analytics))
            }
            Button(onClick = {
                navHostController.navigate(MainActivity.Screen.Profile.route) {
                    launchSingleTop = true
                    popUpTo(MainActivity.Screen.Profile.route)
                }
            }) {
                Text(text = stringResource(R.string.profile))
            }
            //TODO: Add more screens here
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent(navHostController: NavHostController) {
    val navBackStackEntry by navHostController.currentBackStackEntryAsState()
    var topBarTitle by remember { mutableStateOf(MainActivity.Screen.Home.label) }

    LaunchedEffect(navBackStackEntry) {
        topBarTitle = navBackStackEntry?.destination?.label?.toString() ?: "Title"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = topBarTitle) })
        }
    ) { paddingValues ->
        NavHost(
            navController = navHostController,
            startDestination = MainActivity.Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(MainActivity.Screen.Home.route) { navBackStackEntry ->
                HomeScreen()
                navBackStackEntry.destination.label = MainActivity.Screen.Home.label
            }
            composable(MainActivity.Screen.Analytics.route) { navBackStackEntry ->
                AnalyticsScreen()
                navBackStackEntry.destination.label = MainActivity.Screen.Analytics.label
            }
            composable(MainActivity.Screen.Profile.route) { navBackStackEntry ->
                ProfileScreen()
                navBackStackEntry.destination.label = MainActivity.Screen.Profile.label
            }
        }
    }
}