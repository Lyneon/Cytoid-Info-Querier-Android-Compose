package com.lyneon.cytoidinfoquerier.ui.compose.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.enums.AvatarSize
import com.lyneon.cytoidinfoquerier.data.model.webapi.LeaderboardEntry
import com.lyneon.cytoidinfoquerier.ui.compose.component.ErrorMessageCard
import com.lyneon.cytoidinfoquerier.ui.compose.component.UserAvatar
import com.lyneon.cytoidinfoquerier.ui.viewmodel.LeaderboardViewModel
import com.lyneon.cytoidinfoquerier.util.extension.setPrecision

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val isLoading by remember { uiState.isLoading }
    val loadingMessage by remember { uiState.loadingMessage }
    val errorMessage by remember { uiState.errorMessage }
    val leaderboard by remember { viewModel.leaderboard }
    val cytoidId by remember { uiState.cytoidId }
    val limit by remember { uiState.limit }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var keep2decimalPlaces by remember { mutableStateOf(false) }
    val listState = remember { uiState.listState }

    // 刚进入页面时自动加载榜首排行
    LaunchedEffect(Unit) {
        viewModel.loadLeaderboardTop()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.leaderboard)) },
                actions = {
                    var expandSettings by remember { mutableStateOf(false) }

                    IconButton(onClick = {
                        if (selectedTabIndex == 0) {
                            viewModel.loadLeaderboardTop()
                        } else {
                            viewModel.loadLeaderboardAroundUser()
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "刷新")
                    }
                    IconButton(onClick = {
                        expandSettings = true
                    }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "设置")
                        DropdownMenu(
                            expanded = expandSettings,
                            onDismissRequest = {
                                expandSettings = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    TextField(
                                        value = cytoidId ?: "",
                                        onValueChange = {
                                            uiState.cytoidId.value = it
                                        },
                                        label = { Text("Cytoid ID") }
                                    )
                                },
                                onClick = {},
                                enabled = false
                            )
                            Column {
                                DropdownMenuItem(
                                    text = {
                                        TextField(
                                            value = limit,
                                            onValueChange = {
                                                if (it.isDigitsOnly()) {
                                                    uiState.limit.value = it
                                                }
                                            },
                                            label = { Text("查询数量") }
                                        )
                                    },
                                    onClick = {},
                                    enabled = false
                                )
                                AnimatedVisibility(limit.toIntOrNull() != null && limit.toInt() > 100) {
                                    DropdownMenuItem(
                                        text = {
                                            ErrorMessageCard(BaseApplication.context.getString(R.string.leaderboard_too_large_query_count))
                                        },
                                        onClick = {},
                                        enabled = false
                                    )
                                }
                            }
                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.keep_2_decimal_places))
                                },
                                onClick = {
                                    keep2decimalPlaces = !keep2decimalPlaces
                                },
                                trailingIcon = {
                                    Switch(
                                        checked = keep2decimalPlaces,
                                        onCheckedChange = {
                                            keep2decimalPlaces = it
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding())
                .padding(horizontal = 12.dp)
        ) {
            PrimaryTabRow(selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = {
                        if (selectedTabIndex != 0) {
                            selectedTabIndex = 0
                            viewModel.loadLeaderboardTop()
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.top_leaderboard),
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = {
                        if (selectedTabIndex != 1) {
                            selectedTabIndex = 1
                            if (cytoidId == null) {
                                uiState.errorMessage.value =
                                    "请指定要查询的玩家的Cytoid ID！\n你也可以在设置中设置默认的Cytoid ID为你自己的。"
                            } else {
                                viewModel.loadLeaderboardAroundUser()
                            }
                        }
                    }
                ) {
                    Text(
                        text = "${cytoidId ?: "我"}的排名",
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(isLoading,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220, delayMillis = 90))
                            .togetherWith(fadeOut(animationSpec = tween(90)))
                    }) {
                    if (it) {
                        LoadingIndicator(loadingMessage)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            state = listState
                        ) {
                            if (errorMessage != null) {
                                item {
                                    ErrorMessageCard(errorMessage!!)
                                }
                            } else {
                                leaderboard.forEach {
                                    item {
                                        LeaderboardEntryCard(
                                            it,
                                            keep2decimalPlaces,
                                            it.uid == cytoidId
                                        )
                                    }
                                }
                                item {
                                    Spacer(
                                        modifier = Modifier.height(
                                            WindowInsets.navigationBars.asPaddingValues()
                                                .calculateBottomPadding() - 8.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingIndicator(description: String? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Card {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                modifier = Modifier
                    .padding(12.dp)
                    .animateContentSize(alignment = Alignment.Center)
            ) {
                CircularProgressIndicator()
                Text(description ?: stringResource(R.string.loading))
            }
        }
    }
}

@Composable
fun LeaderboardEntryCard(
    entry: LeaderboardEntry,
    keep2decimalPlaces: Boolean,
    highlight: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (highlight) MaterialTheme.colorScheme.primaryContainer else Color.Unspecified)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = entry.rank.toString(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                UserAvatar(
                    userUid = entry.uid,
                    remoteAvatarUrl = entry.avatar.small,
                    avatarSize = AvatarSize.Small,
                    size = 48.dp,
                    clickToOpenProfileInBrowser = false
                )
                Text(text = entry.uid)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = entry.rating.run {
                if (keep2decimalPlaces) this.setPrecision(2) else this.toString()
            }, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}