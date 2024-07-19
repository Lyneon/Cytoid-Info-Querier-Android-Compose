package com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.compose.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.CytoidConstant
import com.lyneon.cytoidinfoquerier.util.extension.openInBrowser
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = stringResource(id = R.string.home)) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    Text(text = "此应用处于测试阶段，可能遇到错误和异常。欢迎反馈，但不一定会及时修复，你的催更也不会加快开发")
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
            Box(
                modifier = Modifier.size(64.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = null,
                    modifier = Modifier.clip(CircleShape)
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null
                )
            }
            Text(
                text = stringResource(id = R.string.app_name)
            )
            Text(text = "版本 ${packageInfo.versionName}(${packageInfo.versionCode})")
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
                Text(text = "获取更新")
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
                Text(text = "Github 仓库主页")
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
                Text(text = "问题反馈 | 意见建议")
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
                    Text(text = "未安装")
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
                    bitmap = cytoidPackageInfo.applicationInfo.loadIcon(BaseApplication.context.packageManager)
                        .toBitmap().asImageBitmap(), contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "Cytoid")
                    Text(text = "版本 ${cytoidPackageInfo.versionName}(${cytoidPackageInfo.versionCode})")
                }
            }
        }
    }
}