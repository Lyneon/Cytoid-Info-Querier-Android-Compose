package com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.BestRecords
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.RecentRecords
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository.BestRecordsRepository
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository.RecentRecordsRepository
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.viewmodel.AnalyticsUIState.QueryType.BEST_RECORDS
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.viewmodel.AnalyticsUIState.QueryType.RECENT_RECORDS

class AnalyticsViewModel(
    private val bestRecordsRepository: BestRecordsRepository,
    private val recentRecordsRepository: RecentRecordsRepository
) : ViewModel() {
    private val _bestRecords = MutableLiveData<BestRecords?>().apply { value = null }
    val bestRecords: LiveData<BestRecords?> get() = _bestRecords

    private val _recentRecords = MutableLiveData<RecentRecords?>().apply { value = null }
    val recentRecords: LiveData<RecentRecords?> get() = _recentRecords

    private val _uiState = MutableLiveData<AnalyticsUIState>().apply {
        value = AnalyticsUIState(
            cytoidID = "",
            foldTextFiled = false,
            expandQueryOptionsDropdownMenu = false,
            queryType = BEST_RECORDS,
            queryAmount = 30,
            ignoreLocalCacheData = false,
            keep2DecimalPlaces = true,
            imageGenerationColumns = 6
        )
    }
    val uiState: LiveData<AnalyticsUIState> get() = _uiState

    fun setCytoidID(cytoidID: String) {
        updateUIState { copy(cytoidID = cytoidID) }
    }

    fun setFoldTextFiled(foldTextFiled: Boolean) {
        updateUIState { copy(foldTextFiled = foldTextFiled) }
    }

    fun setExpandQueryOptionsDropdownMenu(expandQueryOptionsDropdownMenu: Boolean) {
        updateUIState { copy(expandQueryOptionsDropdownMenu = expandQueryOptionsDropdownMenu) }
    }

    fun setQueryType(queryType: AnalyticsUIState.QueryType) {
        updateUIState { copy(queryType = queryType) }
    }

    fun setQueryAmount(queryAmount: Int) {
        updateUIState { copy(queryAmount = queryAmount) }
    }

    fun setIgnoreLocalCacheData(ignoreLocalCacheData: Boolean) {
        updateUIState { copy(ignoreLocalCacheData = ignoreLocalCacheData) }
    }

    fun setKeep2DecimalPlaces(keep2DecimalPlaces: Boolean) {
        updateUIState { copy(keep2DecimalPlaces = keep2DecimalPlaces) }
    }

    fun setImageGenerationColumns(imageGenerationColumns: Int) {
        updateUIState { copy(imageGenerationColumns = imageGenerationColumns) }
    }

    suspend fun enqueueQuery() = when (uiState.value?.queryType) {
        BEST_RECORDS -> updateBestRecords()
        RECENT_RECORDS -> updateRecentRecords()
        null -> Unit
    }

    private suspend fun updateBestRecords() {
        uiState.value?.let { uiState ->
            _bestRecords.value = bestRecordsRepository.getBestRecords(
                cytoidID = uiState.cytoidID,
                count = uiState.queryAmount,
                disableLocalCache = uiState.ignoreLocalCacheData
            )
        }
    }

    private suspend fun updateRecentRecords() {
        uiState.value?.let { uiState ->
            _recentRecords.value = recentRecordsRepository.getRecentRecords(
                cytoidID = uiState.cytoidID,
                count = uiState.queryAmount,
                disableLocalCache = uiState.ignoreLocalCacheData
            )
        }
    }

    private fun updateUIState(update: AnalyticsUIState.() -> AnalyticsUIState) {
        _uiState.value = _uiState.value?.update()
    }
}

data class AnalyticsUIState(
    val cytoidID: String,
    val foldTextFiled: Boolean,
    val expandQueryOptionsDropdownMenu: Boolean,
    val queryType: QueryType,
    val queryAmount: Int,
    val ignoreLocalCacheData: Boolean,
    val keep2DecimalPlaces: Boolean,
    val imageGenerationColumns: Int
) {
    enum class QueryType {
        BEST_RECORDS,
        RECENT_RECORDS
    }
}