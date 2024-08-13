package com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HistoryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUIState())
    val uiState: StateFlow<HistoryUIState> get() = _uiState.asStateFlow()

    fun setHistoryType(historyType: HistoryUIState.HistoryType) {
        updateUIState { copy(historyType = historyType) }
    }

    private fun updateUIState(update: HistoryUIState.() -> HistoryUIState) {
        updateUIState(_uiState.value.update())
    }

    fun updateUIState(uiState: HistoryUIState) {
        _uiState.update { uiState }
    }
}

data class HistoryUIState(
    val historyType: HistoryType = HistoryType.AnalyticsBestRecords,
) {
    enum class HistoryType(
        val displayName: String,
        val directoryName: String
    ) {
        AnalyticsBestRecords("Best Records", "BestRecords"),
        AnalyticsRecentRecords("Recent Records", "RecentRecords"),
        Profile("Profile", "ProfileScreenDataModel")
    }
}