package com.lyneon.cytoidinfoquerier.ui.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyneon.cytoidinfoquerier.data.model.webapi.LeaderboardEntry
import com.lyneon.cytoidinfoquerier.data.repository.LeaderboardRepository
import com.lyneon.cytoidinfoquerier.util.AppSettings
import com.lyneon.cytoidinfoquerier.util.CytoidUserUtils
import com.lyneon.cytoidinfoquerier.util.extension.isValidCytoidID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LeaderboardViewModel(
    val leaderboardRepository: LeaderboardRepository = LeaderboardRepository(),
    private var _leaderboard: MutableState<List<LeaderboardEntry>> = mutableStateOf(emptyList()),
    val uiState: LeaderboardScreenUIState = LeaderboardScreenUIState()
) : ViewModel() {
    val leaderboard: State<List<LeaderboardEntry>> = _leaderboard

    fun loadLeaderboardTop(limit: Int? = uiState.limit.value.toIntOrNull()) {
        if (limit == null) {
            uiState.errorMessage.value = "无效的的排名数量"
            return
        }
        uiState.isLoading.value = true
        uiState.loadingMessage.value = "获取榜首排行前${limit}位排名(1/1)"
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val leaderboard = leaderboardRepository.getLeaderboardTop(limit)
                uiState.isLoading.value = false
                this@LeaderboardViewModel._leaderboard.value = leaderboard
            } catch (e: Exception) {
                uiState.isLoading.value = false
                uiState.errorMessage.value = e.message ?: "未知错误"
            } finally {
                uiState.loadingMessage.value = null
            }
        }
    }

    fun loadLeaderboardAroundUser(
        userUid: String? = uiState.cytoidId.value,
        limit: Int? = uiState.limit.value.toIntOrNull()
    ) {
        if (!userUid.isValidCytoidID()) {
            uiState.errorMessage.value = "无效的Cytoid ID"
            return
        }
        if (limit == null) {
            uiState.errorMessage.value = "无效的的排名数量"
            return
        }
        uiState.isLoading.value = true
        uiState.loadingMessage.value = "获取${userUid}的ID(1/2)"
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = withContext(Dispatchers.IO) {
                    CytoidUserUtils.getUserIdByCytoidId(userUid)
                }
                uiState.loadingMessage.value = "获取${userUid}及附近的${limit}位排名(2/2)"
                val leaderboard = leaderboardRepository.getLeaderboardAroundUser(limit, userId)
                uiState.isLoading.value = false
                this@LeaderboardViewModel._leaderboard.value = leaderboard
            } catch (e: Exception) {
                uiState.isLoading.value = false
                uiState.errorMessage.value = e.message ?: "未知错误"
            } finally {
                uiState.loadingMessage.value = null
            }
        }
    }
}

class LeaderboardScreenUIState {
    val isLoading: MutableState<Boolean> = mutableStateOf(false)
    val loadingMessage: MutableState<String?> = mutableStateOf(null)
    val errorMessage: MutableState<String?> = mutableStateOf(null)
    val cytoidId: MutableState<String?> = mutableStateOf(AppSettings.cytoidID)
    val limit: MutableState<String> = mutableStateOf("50")
}