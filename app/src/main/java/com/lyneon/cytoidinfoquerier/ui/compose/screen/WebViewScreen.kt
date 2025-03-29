package com.lyneon.cytoidinfoquerier.ui.compose.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.CytoidConstant
import com.lyneon.cytoidinfoquerier.ui.activity.DownloadLevelActivity
import com.lyneon.cytoidinfoquerier.util.extension.startActivity
import java.net.URLDecoder

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(navController: NavController, navBackStackEntry: NavBackStackEntry) {
    val initialUrl = navBackStackEntry.arguments?.getString("initialUrl")
    var currentUrl by rememberSaveable { mutableStateOf(initialUrl) }
    val localContext = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "WebView")
                        Text(
                            text = currentUrl ?: "Unknown URL",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        currentUrl?.let {
                            CustomTabsIntent.Builder().setShowTitle(true)
                                .setShareState(CustomTabsIntent.SHARE_STATE_OFF).build()
                                .launchUrl(localContext, it.toUri())
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = stringResource(R.string.open_in_browser)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val context = LocalContext.current

        AndroidView(factory = {
            WebView(context).apply {
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        if (request?.url?.host?.endsWith("cytoid.io") == true) {
                            view?.loadUrl(request.url.toString())
                            currentUrl = request.url.toString()
                        }
                        if (request?.url?.scheme == "ciq") {
                            when (request.url.host) {
                                "cytoid_info_querier_download_level_activity" -> {
                                    BaseApplication.context.startActivity<DownloadLevelActivity> {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        data = request.url
                                    }
                                }
                            }
                        }
                        return true
                    }
                }
                settings.userAgentString = CytoidConstant.clientUA
                settings.javaScriptEnabled = true
                loadUrl(
                    URLDecoder.decode(
                        navBackStackEntry.arguments?.getString("initialUrl"),
                        "UTF-8"
                    ) ?: "https://cytoid.io"
                )
            }
        }, modifier = Modifier.padding(top = paddingValues.calculateTopPadding()))
    }
}