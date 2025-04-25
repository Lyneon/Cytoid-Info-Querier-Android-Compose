package com.lyneon.cytoidinfoquerier.ui.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.RecordQueryOrder
import com.lyneon.cytoidinfoquerier.data.constant.RecordQuerySort
import com.lyneon.cytoidinfoquerier.data.model.graphql.BestRecords
import com.lyneon.cytoidinfoquerier.data.model.graphql.RecentRecords
import com.lyneon.cytoidinfoquerier.data.model.local.AnalyticsPreset
import com.lyneon.cytoidinfoquerier.data.model.webapi.ProfileDetails
import com.lyneon.cytoidinfoquerier.data.repository.BestRecordsRepository
import com.lyneon.cytoidinfoquerier.data.repository.ProfileDetailsRepository
import com.lyneon.cytoidinfoquerier.data.repository.RecentRecordsRepository
import com.lyneon.cytoidinfoquerier.logic.AnalyticsImageHandler
import com.lyneon.cytoidinfoquerier.util.extension.isValidCytoidID
import com.lyneon.cytoidinfoquerier.util.extension.saveIntoMediaStore
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.min

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

    var extraPresets = mutableStateOf<List<AnalyticsPreset>>(emptyList())

    val context = BaseApplication.context

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

    fun setQuerySort(querySort: RecordQuerySort) {
        updateUIState { copy(querySort = querySort) }
    }

    fun setQueryOrder(queryOrder: RecordQueryOrder) {
        updateUIState { copy(queryOrder = queryOrder) }
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

    fun setIsGenerating(isGenerating: Boolean) {
        updateUIState { copy(isGenerating = isGenerating) }
    }

    fun setGeneratingProgress(generatingProgress: Int) {
        updateUIState { copy(generatingProgress = generatingProgress) }
    }

    fun addGeneratingProgress() {
        updateUIState { copy(generatingProgress = generatingProgress + 1) }
    }

    fun enqueueQuery() {
        setIsQuerying(true)
        viewModelScope.launch(
            CoroutineExceptionHandler { _, throwable ->
                updateUIState { copy(errorMessage = throwable.message.toString()) }
            }
        ) {
            launch {
                when (uiState.value.queryType) {
                    AnalyticsUIState.QueryType.BestRecords -> updateBestRecords()
                    AnalyticsUIState.QueryType.RecentRecords -> updateRecentRecords()
                }
            }
            launch {
                updateProfileDetails()
            }
        }.invokeOnCompletion {
            setIsQuerying(false)
            setFoldTextFiled(true)
        }
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
                    sort = uiState.querySort,
                    order = uiState.queryOrder,
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
        profileDetails.value?.let { profileDetails ->
            val queryType = uiState.value.queryType
            val recordsList = when (queryType) {
                AnalyticsUIState.QueryType.BestRecords -> bestRecords.value?.data?.profile?.bestRecords
                AnalyticsUIState.QueryType.RecentRecords -> recentRecords.value?.data?.profile?.recentRecords
            }
            if (recordsList == null) {
                context.getString(R.string.save_failed).showToast()
            } else {
                viewModelScope.launch(Dispatchers.IO) {
                    setIsGenerating(true)
                    setGeneratingProgress(0)
                    val bitmap = AnalyticsImageHandler.getRecordsImageWithProgress(
                        profileDetails = profileDetails,
                        records = recordsList.subList(
                            0,
                            min(uiState.value.queryCount.toInt(), recordsList.size)
                        ),
                        recordsType = uiState.value.queryType,
                        columnsCount = uiState.value.imageGenerationColumns.toInt(),
                        keep2DecimalPlaces = uiState.value.keep2DecimalPlaces,
                        onProgressChanged = { _ ->
                            addGeneratingProgress()
                        }
                    )
                    bitmap.saveIntoMediaStore()
                    bitmap.recycle()
                    System.gc()
                    setIsGenerating(false)
                    context.getString(R.string.image_saved_into_media).showToast()
                }
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
    val querySort: RecordQuerySort = RecordQuerySort.Date,
    val queryOrder: RecordQueryOrder = RecordQueryOrder.DESC,
    val ignoreLocalCacheData: Boolean = false,
    val keep2DecimalPlaces: Boolean = true,
    val imageGenerationColumns: String = "6",
    val errorMessage: String = "",
    val isQuerying: Boolean = false,
    val isGenerating: Boolean = false,
    val generatingProgress: Int = 0,
    var showBottomSheet: MutableState<Boolean> = mutableStateOf(false)
) {
    enum class QueryType(val displayName: String) {
        BestRecords("Best Records"),
        RecentRecords("Recent Records")
    }

    fun canQuery(): Boolean = cytoidID.isValidCytoidID() && queryCount.isNotEmpty() && !isQuerying
}

enum class AnalyticsPreset {
    B30,
    R10
}