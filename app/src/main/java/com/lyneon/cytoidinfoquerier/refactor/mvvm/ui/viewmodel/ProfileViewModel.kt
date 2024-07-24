package com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.webapi.ProfileComment
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.webapi.ProfileDetails
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository.ProfileCommentListRepository
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository.ProfileDetailsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel(
    private val profileDetailsRepository: ProfileDetailsRepository = ProfileDetailsRepository(),
    private val profileCommentListRepository: ProfileCommentListRepository = ProfileCommentListRepository()
) : ViewModel() {
    private val _profileDetails = MutableStateFlow<ProfileDetails?>(null)
    val profileDetails: StateFlow<ProfileDetails?> get() = _profileDetails.asStateFlow()

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

    fun setIgnoreLocalCacheData(ignoreLocalCacheData: Boolean) {
        updateUIState { copy(ignoreLocalCacheData = ignoreLocalCacheData) }
    }

    fun setKeep2DecimalPlaces(keep2DecimalPlaces: Boolean) {
        updateUIState { copy(keep2DecimalPlaces = keep2DecimalPlaces) }
    }

    suspend fun enqueueQuery() {
        uiState.value.let { uiState ->
            _profileDetails.value = profileDetailsRepository.getProfileDetails(
                cytoidID = uiState.cytoidID,
                disableLocalCache = uiState.ignoreLocalCacheData
            )
            _profileCommentList.value = profileCommentListRepository.getProfileCommentList(
                cytoidID = uiState.cytoidID,
                id = _profileDetails.value?.user?.id ?: return@let,
                disableLocalCache = uiState.ignoreLocalCacheData
            )
        }
    }

    private fun updateUIState(update: ProfileUiState.() -> ProfileUiState) {
        _uiState.value = _uiState.value.update()
    }

    data class ProfileUiState(
        val cytoidID: String = "",
        val foldTextFiled: Boolean = false,
        val expandQueryOptionsDropdownMenu: Boolean = false,
        val ignoreLocalCacheData: Boolean = false,
        val keep2DecimalPlaces: Boolean = true
    )
}