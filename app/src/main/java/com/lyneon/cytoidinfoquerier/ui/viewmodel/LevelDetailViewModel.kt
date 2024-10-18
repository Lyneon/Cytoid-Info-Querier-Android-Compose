package com.lyneon.cytoidinfoquerier.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyneon.cytoidinfoquerier.data.model.graphql.LevelLeaderboard
import com.lyneon.cytoidinfoquerier.data.model.webapi.LevelComment
import com.lyneon.cytoidinfoquerier.data.repository.LevelCommentListRepository
import com.lyneon.cytoidinfoquerier.data.repository.LevelLeaderboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LevelDetailViewModel(
    private val levelLeaderboardRepository: LevelLeaderboardRepository = LevelLeaderboardRepository(),
    private val levelCommentListRepository: LevelCommentListRepository = LevelCommentListRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(LevelDetailUIState())
    val uiState: StateFlow<LevelDetailUIState> get() = _uiState.asStateFlow()

    private val _currentLeaderboard = MutableStateFlow<LevelLeaderboard?>(null)
    val currentLeaderboard: StateFlow<LevelLeaderboard?> get() = _currentLeaderboard.asStateFlow()

    private val _levelCommentList = MutableStateFlow<List<LevelComment>>(emptyList())
    val levelCommentList: StateFlow<List<LevelComment>> get() = _levelCommentList.asStateFlow()

    fun setSelectedLevelLeaderboardDifficultyType(difficultyType: String) {
        updateUIState { copy(selectedLevelLeaderboardDifficultyType = difficultyType) }
    }

    fun setLeaderboardStart(start: String) {
        updateUIState { copy(leaderboardStart = start) }
    }

    fun setLeaderboardEnd(end: String) {
        updateUIState { copy(leaderboardEnd = end) }
    }

    fun setDisplayLeaderboardStart(start: Int) {
        updateUIState { copy(displayLeaderboardStart = start) }
    }

    fun updateCurrentLeaderboard(
        levelUID: String,
        difficultyType: String,
        start: Int,
        limit: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _currentLeaderboard.update {
                levelLeaderboardRepository.getLevelLeaderboard(
                    levelUID,
                    difficultyType,
                    start,
                    limit
                )
            }
        }
    }

    fun updateLevelCommentList(levelUID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _levelCommentList.update {
                levelCommentListRepository.getLevelCommentList(levelUID)
            }
        }
    }

    private fun updateUIState(update: LevelDetailUIState.() -> LevelDetailUIState) {
        updateUIState(_uiState.value.update())
    }

    fun updateUIState(uiState: LevelDetailUIState) {
        _uiState.update { uiState }
    }
}

data class LevelDetailUIState(
    val selectedLevelLeaderboardDifficultyType: String? = null,
    val leaderboardStart: String = "1",
    val leaderboardEnd: String = "10",
    val displayLeaderboardStart: Int = 1
)