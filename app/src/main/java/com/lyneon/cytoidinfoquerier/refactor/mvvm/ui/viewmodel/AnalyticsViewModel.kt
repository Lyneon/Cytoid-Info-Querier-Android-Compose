package com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.BestRecords
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.RecentRecords
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository.BestRecordsRepository
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository.ProfileDetailsRepository
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository.RecentRecordsRepository
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.viewmodel.AnalyticsUIState.QueryType.BEST_RECORDS
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.viewmodel.AnalyticsUIState.QueryType.RECENT_RECORDS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AnalyticsViewModel(
    private val bestRecordsRepository: BestRecordsRepository = BestRecordsRepository(),
    private val recentRecordsRepository: RecentRecordsRepository = RecentRecordsRepository(),
    private val profileDetailsRepository: ProfileDetailsRepository = ProfileDetailsRepository()
) : ViewModel() {
    private val _bestRecords = MutableStateFlow<BestRecords?>(null)
    val bestRecords: StateFlow<BestRecords?> get() = _bestRecords.asStateFlow()

    private val _recentRecords = MutableStateFlow<RecentRecords?>(null)
    val recentRecords: StateFlow<RecentRecords?> get() = _recentRecords.asStateFlow()

    private val _uiState = MutableStateFlow(AnalyticsUIState())
    val uiState: StateFlow<AnalyticsUIState> get() = _uiState.asStateFlow()

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

    fun setQueryCount(queryCount: String) {
        updateUIState { copy(queryCount = queryCount) }
    }

    fun setIgnoreLocalCacheData(ignoreLocalCacheData: Boolean) {
        updateUIState { copy(ignoreLocalCacheData = ignoreLocalCacheData) }
    }

    fun setKeep2DecimalPlaces(keep2DecimalPlaces: Boolean) {
        updateUIState { copy(keep2DecimalPlaces = keep2DecimalPlaces) }
    }

    fun setImageGenerationColumns(imageGenerationColumns: String) {
        updateUIState { copy(imageGenerationColumns = imageGenerationColumns) }
    }

    fun setErrorMessage(errorMessage: String) {
        updateUIState { copy(errorMessage = errorMessage) }
    }

    suspend fun enqueueQuery() = withContext(Dispatchers.IO) {
        when (uiState.value.queryType) {
            BEST_RECORDS -> updateBestRecords()
            RECENT_RECORDS -> updateRecentRecords()
        }
    }

    private suspend fun updateBestRecords() {
        uiState.value.let { uiState ->
            _bestRecords.value = bestRecordsRepository.getBestRecords(
                cytoidID = uiState.cytoidID,
                count = uiState.queryCount.toInt(),
                disableLocalCache = uiState.ignoreLocalCacheData
            )
        }
    }

    private suspend fun updateRecentRecords() {
        uiState.value.let { uiState ->
            _recentRecords.value = recentRecordsRepository.getRecentRecords(
                cytoidID = uiState.cytoidID,
                count = uiState.queryCount.toInt(),
                disableLocalCache = uiState.ignoreLocalCacheData
            )
        }
    }

    private fun updateUIState(update: AnalyticsUIState.() -> AnalyticsUIState) {
        _uiState.value = _uiState.value.update()
    }

    fun saveRecordsAsPicture() {
        //TODO: Implement this function
    }
}

data class AnalyticsUIState(
    val cytoidID: String = "",
    val foldTextFiled: Boolean = false,
    val expandQueryOptionsDropdownMenu: Boolean = false,
    val queryType: QueryType = BEST_RECORDS,
    val queryCount: String = "30",
    val ignoreLocalCacheData: Boolean = false,
    val keep2DecimalPlaces: Boolean = true,
    val imageGenerationColumns: String = "6",
    val errorMessage: String = ""
) {
    enum class QueryType {
        BEST_RECORDS,
        RECENT_RECORDS
    }
}