package com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.lyneon.cytoidinfoquerier.logic.AnalyticsImageHandler
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.BestRecords
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.RecentRecords
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.webapi.ProfileDetails
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository.BestRecordsRepository
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository.ProfileDetailsRepository
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository.RecentRecordsRepository
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.viewmodel.AnalyticsUIState.QueryType.BEST_RECORDS
import com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.viewmodel.AnalyticsUIState.QueryType.RECENT_RECORDS
import com.lyneon.cytoidinfoquerier.util.extension.saveIntoMediaStore
import com.lyneon.cytoidinfoquerier.util.extension.showToast
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

    private val _profileDetails = MutableStateFlow<ProfileDetails?>(null)
    val profileDetails: StateFlow<ProfileDetails?> get() = _profileDetails.asStateFlow()

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

    fun setIsQuerying(isQuerying: Boolean) {
        updateUIState { copy(isQuerying = isQuerying) }
    }

    suspend fun enqueueQuery() = withContext(Dispatchers.IO) {
        when (uiState.value.queryType) {
            BEST_RECORDS -> updateBestRecords()
            RECENT_RECORDS -> updateRecentRecords()
        }
        updateProfileDetails()
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

    suspend fun updateProfileDetails() {
        uiState.value.let { uiState ->
            _profileDetails.value = profileDetailsRepository.getProfileDetails(
                cytoidID = uiState.cytoidID,
                disableLocalCache = uiState.ignoreLocalCacheData
            )
        }
    }

    private fun updateUIState(update: AnalyticsUIState.() -> AnalyticsUIState) {
        _uiState.value = _uiState.value.update()
    }

    suspend fun saveRecordsAsPicture() {
        require(_profileDetails.value != null)
        withContext(Dispatchers.IO) {
            AnalyticsImageHandler.getRecordsImage(
                profileDetails = profileDetails.value!!,
                records = (if (uiState.value.queryType == BEST_RECORDS)
                    bestRecords.value!!.data.profile?.bestRecords
                else recentRecords.value!!.data.profile?.recentRecords) ?: emptyList(),
                recordsType = uiState.value.queryType,
                columnsCount = uiState.value.imageGenerationColumns.toInt(),
                keep2DecimalPlaces = uiState.value.keep2DecimalPlaces
            ).saveIntoMediaStore()
            "图片已保存至媒体库".showToast()
        }
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
    val errorMessage: String = "",
    val isQuerying: Boolean = false
) {
    enum class QueryType(val value: String) {
        BEST_RECORDS("Best Records"),
        RECENT_RECORDS("Recent Records")
    }
}