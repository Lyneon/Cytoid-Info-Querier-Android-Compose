package com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.ProfileGraphQL
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.webapi.ProfileComment
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.webapi.ProfileDetails
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository.ProfileCommentListRepository
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository.ProfileDetailsRepository
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository.ProfileGraphQLRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileDetailsRepository: ProfileDetailsRepository = ProfileDetailsRepository(),
    private val profileGraphQLRepository: ProfileGraphQLRepository = ProfileGraphQLRepository(),
    private val profileCommentListRepository: ProfileCommentListRepository = ProfileCommentListRepository()
) : ViewModel() {
    private val _profileDetails = MutableStateFlow<ProfileDetails?>(null)
    val profileDetails: StateFlow<ProfileDetails?> get() = _profileDetails.asStateFlow()

    private val _profileGraphQL = MutableStateFlow<ProfileGraphQL?>(null)
    val profileGraphQL: StateFlow<ProfileGraphQL?> get() = _profileGraphQL.asStateFlow()

    private val _profileCommentList = MutableStateFlow<List<ProfileComment>?>(null)
    val profileCommentList: StateFlow<List<ProfileComment>?> get() = _profileCommentList.asStateFlow()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> get() = _uiState.asStateFlow()

    fun setCytoidID(cytoidID: String) {
        updateUIState { copy(cytoidID = cytoidID) }
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

    fun setIgnoreLocalCacheData(ignoreLocalCacheData: Boolean) {
        updateUIState { copy(ignoreLocalCacheData = ignoreLocalCacheData) }
    }

    fun setKeep2DecimalPlaces(keep2DecimalPlaces: Boolean) {
        updateUIState { copy(keep2DecimalPlaces = keep2DecimalPlaces) }
    }

    fun setErrorMessage(errorMessage: String) {
        updateUIState { copy(errorMessage = errorMessage) }
    }

    fun setIsQuerying(isQuerying: Boolean) {
        updateUIState { copy(isQuerying = isQuerying) }
    }

    suspend fun enqueueQuery() {
        viewModelScope.launch(Dispatchers.IO) {
            uiState.value.let { uiState ->
                val profileDetailsJob = async {
                    _profileDetails.value = profileDetailsRepository.getProfileDetails(
                        cytoidID = uiState.cytoidID,
                        disableLocalCache = uiState.ignoreLocalCacheData
                    )
                }
                val profileGraphQLJob = async {
                    _profileGraphQL.value = profileGraphQLRepository.getProfileGraphQL(
                        cytoidID = uiState.cytoidID,
                        disableLocalCache = uiState.ignoreLocalCacheData
                    )
                }
                val profileCommentListJob = async {
                    awaitAll(profileDetailsJob)
                    _profileCommentList.value = profileCommentListRepository.getProfileCommentList(
                        cytoidID = uiState.cytoidID,
                        id = _profileDetails.value?.user?.id,
                        disableLocalCache = uiState.ignoreLocalCacheData
                    )
                }
                awaitAll(profileDetailsJob, profileGraphQLJob, profileCommentListJob)
                updateUIState { copy(isQuerying = false) }
            }
        }
    }

    private fun updateUIState(update: ProfileUiState.() -> ProfileUiState) {
        _uiState.value = _uiState.value.update()
    }
}

data class ProfileUiState(
    val cytoidID: String = "",
    val foldTextFiled: Boolean = false,
    val expandQueryOptionsDropdownMenu: Boolean = false,
    val expandAnalyticsOptionsDropdownMenu: Boolean = false,
    val ignoreLocalCacheData: Boolean = false,
    val keep2DecimalPlaces: Boolean = true,
    val errorMessage: String = "",
    val isQuerying: Boolean = false
)