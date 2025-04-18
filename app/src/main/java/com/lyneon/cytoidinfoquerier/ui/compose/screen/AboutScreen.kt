package com.lyneon.cytoidinfoquerier.ui.compose.screen

import android.os.Build
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.CytoidConstant
import com.lyneon.cytoidinfoquerier.util.extension.openInBrowser
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.about)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                scrollBehavior = topAppBarScrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
                .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AppInfoCard()
            CytoidInfoCard()
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null)
                    Text(text = stringResource(R.string.app_test_desc))
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.ColorLens, contentDescription = null)
                    Text(text = "应用图标由@xixeilm绘制并授权使用，在此表示感谢")
                }
            }
        }
    }
}


@Composable
private fun AppInfoCard() {
    val packageInfo = BaseApplication.context.packageManager.getPackageInfo(
        BaseApplication.context.packageName,
        0
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                bitmap = (packageInfo.applicationInfo?.loadIcon(BaseApplication.context.packageManager)
                    ?.toBitmap() ?: AppCompatResources.getDrawable(
                    BaseApplication.context,
                    R.drawable.sayakacry
                )!!.toBitmap()).asImageBitmap(), contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )
            Text(
                text = stringResource(id = R.string.app_name)
            )
            Text(
                text = "${stringResource(R.string.version)} ${packageInfo.versionName}(${
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        packageInfo.longVersionCode
                    } else {
                        @Suppress("DEPRECATION")
                        packageInfo.versionCode
                    }
                })"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        URL("https://github.com/Lyneon/Cytoid-Info-Querier-Android-Compose/releases").openInBrowser()
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Upgrade,
                    contentDescription = null
                )
                Text(text = stringResource(R.string.get_update))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        URL("https://github.com/Lyneon/Cytoid-Info-Querier-Android-Compose").openInBrowser()
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_github),
                    contentDescription = null
                )
                Text(text = stringResource(R.string.github_homepage))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        URL("https://github.com/Lyneon/Cytoid-Info-Querier-Android-Compose/issues/new").openInBrowser()
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null
                )
                Text(text = stringResource(R.string.feedback_issue))
            }
        }
    }
}

@Composable
private fun CytoidInfoCard() {
    val cytoidPackageInfo = try {
        BaseApplication.context.packageManager.getPackageInfo(CytoidConstant.gamePackageName, 0)
    } catch (_: Exception) {
        null
    }

    if (cytoidPackageInfo == null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null)
                Column {
                    Text(text = stringResource(R.string.not_installed))
                    Text(text = stringResource(id = R.string.cytoid_is_not_installed))
                }
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    bitmap = (cytoidPackageInfo.applicationInfo?.loadIcon(BaseApplication.context.packageManager)
                        ?.toBitmap() ?: AppCompatResources.getDrawable(
                        BaseApplication.context,
                        R.drawable.sayakacry
                    )!!.toBitmap()).asImageBitmap(), contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "Cytoid")
                    Text(
                        text = "${stringResource(R.string.version)} ${cytoidPackageInfo.versionName}(${
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                cytoidPackageInfo.longVersionCode
                            } else {
                                @Suppress("DEPRECATION")
                                cytoidPackageInfo.versionCode
                            }
                        })"
                    )
                }
            }
        }
    }
}