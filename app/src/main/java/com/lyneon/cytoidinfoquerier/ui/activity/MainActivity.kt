package com.lyneon.cytoidinfoquerier.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lyneon.cytoidinfoquerier.BaseActivity
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.logic.model.DrawerItem
import com.lyneon.cytoidinfoquerier.tool.showToast
import com.lyneon.cytoidinfoquerier.ui.compose.AnalyticsCompose
import com.lyneon.cytoidinfoquerier.ui.compose.HomeCompose
import com.lyneon.cytoidinfoquerier.ui.compose.NavRoute
import com.lyneon.cytoidinfoquerier.ui.compose.ProfileCompose
import com.lyneon.cytoidinfoquerier.ui.theme.CytoidInfoQuerierComposeTheme
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CytoidInfoQuerierComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet {
                                Column {
                                    Image(
                                        painter = painterResource(id = R.drawable.tutorial_background),
                                        contentDescription = stringResource(id = R.string.drawer_background)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Column {
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            val drawerItems = listOf(
                                                DrawerItem(
                                                    icon = Icons.Filled.Home,
                                                    label = stringResource(id = R.string.home),
                                                    navDestinationRoute = NavRoute.home
                                                ),
                                                DrawerItem(
                                                    icon = ImageVector.vectorResource(id = R.drawable.baseline_insights_24),
                                                    label = stringResource(id = R.string.analytics),
                                                    navDestinationRoute = NavRoute.analytics
                                                ),
                                                DrawerItem(
                                                    icon = Icons.Filled.AccountCircle,
                                                    label = stringResource(id = R.string.profile),
                                                    navDestinationRoute = NavRoute.profile
                                                )
                                            )
                                            val scope = rememberCoroutineScope()
                                            drawerItems.forEach {
                                                NavigationDrawerItem(
                                                    icon = { Icon(it.icon, it.label) },
                                                    label = { Text(text = it.label) },
                                                    selected = false,
                                                    onClick = {
                                                        navController.navigate(it.navDestinationRoute)
                                                        scope.launch {
                                                            drawerState.close()
                                                        }
                                                    },
                                                    modifier = Modifier.padding(6.dp)
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
                                                resources.getString(R.string.todo).showToast()
                                            }) {
                                                Column {
                                                    Icon(
                                                        imageVector = Icons.Filled.Settings,
                                                        contentDescription = stringResource(id = R.string.settings),
                                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                                    )
                                                    Text(text = stringResource(id = R.string.settings))
                                                }
                                            }
                                            Button(onClick = { this@MainActivity.finish() }) {
                                                Column {
                                                    Icon(
                                                        imageVector = Icons.Filled.ExitToApp,
                                                        contentDescription = stringResource(id = R.string.exit),
                                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                                    )
                                                    Text(text = stringResource(id = R.string.exit))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        Column {
                            NavHost(
                                navController = navController,
                                startDestination = NavRoute.home
                            ) {
                                composable(NavRoute.home) {
                                    HomeCompose(drawerState)
                                }
                                composable(NavRoute.analytics) {
                                    AnalyticsCompose(drawerState)
                                }
                                composable(NavRoute.profile) {
                                    ProfileCompose(drawerState)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}