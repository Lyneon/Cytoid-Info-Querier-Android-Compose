package com.lyneon.cytoidinfoquerier.ui.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.lyneon.cytoidinfoquerier.data.model.ui.AnalyticsScreenDataModel
import com.lyneon.cytoidinfoquerier.logic.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AnalyticsViewModel(
) : ViewModel() {
    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state.asStateFlow()

    fun updateCytoidID(cytoidID: String) {
        _state.value.cytoidID.value = cytoidID
    }

    suspend fun updateAnalytics(cytoidID: String, ignoreCache: Boolean, queryCount: Int) {
        _state.value.analytics.value =
            Repository.getAnalyticsScreenDataModel(cytoidID, ignoreCache, queryCount, queryCount)
    }

    fun hideInput() {
        _state.value.hideInput.value = true
    }

    fun updateQuerySettingsMenuIsExpanded(isExpanded: Boolean) {
        _state.value.querySettingsMenuIsExpanded.value = isExpanded
    }

    fun updateQueryType(queryType: QueryType) {
        _state.value.queryType.value = queryType
    }

    fun updateQueryCount(queryCount: Int?) {
        _state.value.queryCount.value = queryCount
    }

    fun updateIgnoreCache(ignoreCache: Boolean) {
        _state.value.ignoreCache.value = ignoreCache
    }

    fun updateKeep2DecimalPlace(keep2DecimalPlace: Boolean) {
        _state.value.keep2DecimalPlace.value = keep2DecimalPlace
    }

    fun updateColumnsCount(columnsCount: Int?) {
        _state.value.columnsCount.value = columnsCount
    }

    fun updateErrorMessage(errorMessage: String) {
        _state.value.errorMessage.value = errorMessage
    }
}

class AnalyticsState {
    val analytics: MutableState<AnalyticsScreenDataModel?> = mutableStateOf(null)
    val cytoidID: MutableState<String?> = mutableStateOf(null)
    val isQueryFinished: MutableState<Boolean> = mutableStateOf(false)
    val queryType: MutableState<QueryType> = mutableStateOf(QueryType.BestRecords)
    val ignoreCache: MutableState<Boolean> = mutableStateOf(false)
    val keep2DecimalPlace: MutableState<Boolean> = mutableStateOf(true)
    val queryCount: MutableState<Int?> = mutableStateOf(30)
    val columnsCount: MutableState<Int?> = mutableStateOf(6)
    val querySettingsMenuIsExpanded: MutableState<Boolean> = mutableStateOf(false)
    val hideInput: MutableState<Boolean> = mutableStateOf(false)
    val errorMessage: MutableState<String?> = mutableStateOf(null)
}

enum class QueryType {
    BestRecords,
    RecentRecords
}