package com.lyneon.cytoidinfoquerier.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyneon.cytoidinfoquerier.data.model.graphql.BestRecords
import com.lyneon.cytoidinfoquerier.data.model.graphql.RecentRecords
import com.lyneon.cytoidinfoquerier.data.model.webapi.ProfileDetails
import com.lyneon.cytoidinfoquerier.data.repository.BestRecordsRepository
import com.lyneon.cytoidinfoquerier.data.repository.ProfileDetailsRepository
import com.lyneon.cytoidinfoquerier.data.repository.RecentRecordsRepository
import com.lyneon.cytoidinfoquerier.logic.AnalyticsImageHandler
import com.lyneon.cytoidinfoquerier.util.extension.saveIntoMediaStore
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
        clearBestRecords()
        clearRecentRecords()
        clearProfileDetails()
    }

    fun setFoldTextFiled(foldTextFiled: Boolean) {
        updateUIState { copy(foldTextFiled = foldTextFiled) }
    }

    fun setExpandQueryOptionsDropdownMenu(expandQueryOptionsDropdownMenu: Boolean) {
        updateUIState { copy(expandQueryOptionsDropdownMenu = expandQueryOptionsDropdownMenu) }
    }

    fun setExpandAnalyticsOptionsDropdownMenu(expandAnalyticsOptionsDropdownMenu: Boolean) {
        updateUIState { copy(expandAnalyticsOptionsDropdownMenu = expandAnalyticsOptionsDropdownMenu) }
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

    fun enqueueQuery() =
        viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            updateUIState {
                copy(errorMessage = throwable.message.toString())
            }
            setIsQuerying(false)
        }) {
            val queryRecordsJob = async {
                when (uiState.value.queryType) {
                    AnalyticsUIState.QueryType.BestRecords -> updateBestRecords()
                    AnalyticsUIState.QueryType.RecentRecords -> updateRecentRecords()
                }
            }
            val queryProfileDetailsJob = async {
                updateProfileDetails()
            }
            awaitAll(queryRecordsJob, queryProfileDetailsJob)
            setIsQuerying(false)
        }

    fun loadSpecificCacheBestRecords(timeStamp: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            updateBestRecords(
                bestRecordsRepository.getSpecificCacheBestRecords(
                    cytoidID = uiState.value.cytoidID,
                    timeStamp = timeStamp
                )
            )
        }
    }

    fun loadSpecificCacheRecentRecords(timeStamp: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            updateRecentRecords(
                recentRecordsRepository.getSpecificCacheRecentRecords(
                    cytoidID = uiState.value.cytoidID,
                    timeStamp = timeStamp
                )
            )
        }
    }

    fun clearBestRecords() = updateBestRecords(null)

    fun clearRecentRecords() = updateRecentRecords(null)

    fun clearProfileDetails() = updateProfileDetails(null)

    fun clearUIState() = updateUIState(AnalyticsUIState())

    fun clearAll() {
        clearBestRecords()
        clearRecentRecords()
        clearProfileDetails()
        clearUIState()
    }

    private suspend fun updateBestRecords() {
        uiState.value.let { uiState ->
            updateBestRecords(
                bestRecordsRepository.getBestRecords(
                    cytoidID = uiState.cytoidID,
                    count = uiState.queryCount.toInt(),
                    disableLocalCache = uiState.ignoreLocalCacheData
                )
            )
        }
    }

    private suspend fun updateRecentRecords() {
        uiState.value.let { uiState ->
            updateRecentRecords(
                recentRecordsRepository.getRecentRecords(
                    cytoidID = uiState.cytoidID,
                    count = uiState.queryCount.toInt(),
                    disableLocalCache = uiState.ignoreLocalCacheData
                )
            )
        }
    }

    suspend fun updateProfileDetails() {
        uiState.value.let { uiState ->
            updateProfileDetails(
                profileDetailsRepository.getProfileDetails(
                    cytoidID = uiState.cytoidID,
                    disableLocalCache = uiState.ignoreLocalCacheData
                )
            )
        }
    }

    fun updateProfileDetailsWithInnerScope() {
        viewModelScope.launch(Dispatchers.IO) {
            updateProfileDetails()
        }
    }

    private fun updateUIState(update: AnalyticsUIState.() -> AnalyticsUIState) {
        updateUIState(_uiState.value.update())
    }

    fun updateBestRecords(bestRecords: BestRecords?) {
        _bestRecords.update { bestRecords }
    }

    fun updateRecentRecords(recentRecords: RecentRecords?) {
        _recentRecords.update { recentRecords }
    }

    fun updateProfileDetails(profileDetails: ProfileDetails?) {
        _profileDetails.update { profileDetails }
    }

    fun updateUIState(uiState: AnalyticsUIState) {
        _uiState.update { uiState }
    }

    fun saveRecordsAsPicture() {
        viewModelScope.launch(Dispatchers.IO) {
            require(_profileDetails.value != null)
            withContext(Dispatchers.IO) {
                AnalyticsImageHandler.getRecordsImage(
                    profileDetails = profileDetails.value!!,
                    records = (if (uiState.value.queryType == AnalyticsUIState.QueryType.BestRecords)
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
}

data class AnalyticsUIState(
    val cytoidID: String = "",
    val foldTextFiled: Boolean = false,
    val expandQueryOptionsDropdownMenu: Boolean = false,
    val expandAnalyticsOptionsDropdownMenu: Boolean = false,
    val queryType: QueryType = QueryType.BestRecords,
    val queryCount: String = "30",
    val ignoreLocalCacheData: Boolean = false,
    val keep2DecimalPlaces: Boolean = true,
    val imageGenerationColumns: String = "6",
    val errorMessage: String = "",
    val isQuerying: Boolean = false
) {
    enum class QueryType(val displayName: String) {
        BestRecords("Best Records"),
        RecentRecords("Recent Records")
    }
}