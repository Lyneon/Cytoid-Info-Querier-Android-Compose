package com.lyneon.cytoidinfoquerier.ui.viewmodel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
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
    val context = BaseApplication.context

    private fun clear() {
        this._leaderboard.value = emptyList()
        uiState.errorMessage.value = null
        uiState.loadingMessage.value = null
        uiState.isLoading.value = false
    }

    fun loadLeaderboardTop(limit: Int? = uiState.limit.value.toIntOrNull()) {
        clear()
        if (limit == null) {
            uiState.errorMessage.value = context.getString(R.string.invalid_rank_count)
            return
        }
        uiState.isLoading.value = true
        uiState.loadingMessage.value =
            context.getString(R.string.fetching_top_ranks, limit.toString())
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val leaderboard = leaderboardRepository.getLeaderboardTop(limit)
                uiState.isLoading.value = false
                this@LeaderboardViewModel._leaderboard.value = leaderboard
                uiState.listState.requestScrollToItem(0)
            } catch (e: Exception) {
                uiState.isLoading.value = false
                uiState.errorMessage.value = e.message ?: context.getString(R.string.unknown_error)
            } finally {
                uiState.loadingMessage.value = null
            }
        }
    }

    fun loadLeaderboardAroundUser(
        userUid: String? = uiState.cytoidId.value,
        limit: Int? = uiState.limit.value.toIntOrNull()
    ) {
        clear()
        if (!userUid.isValidCytoidID()) {
            uiState.errorMessage.value = context.getString(R.string.invalid_cytoid_id)
            return
        }
        if (limit == null) {
            uiState.errorMessage.value = context.getString(R.string.invalid_rank_count)
            return
        }
        uiState.isLoading.value = true
        uiState.loadingMessage.value = context.getString(R.string.fetching_user_id, userUid)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = withContext(Dispatchers.IO) {
                    CytoidUserUtils.getUserIdByCytoidId(userUid)
                }
                uiState.loadingMessage.value =  context.getString(R.string.fetching_user_ranks,userUid,limit.toString())
                val leaderboard = leaderboardRepository.getLeaderboardAroundUser(limit, userId)
                uiState.isLoading.value = false
                this@LeaderboardViewModel._leaderboard.value = leaderboard
                uiState.listState.requestScrollToItem(limit)
            } catch (e: Exception) {
                uiState.isLoading.value = false
                uiState.errorMessage.value = e.message ?: context.getString(R.string.unknown_error)
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
    val listState: LazyListState = LazyListState()
}