package com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.webapi.ProfileComments
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.webapi.ProfileDetails
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository.ProfileCommentsRepository
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository.ProfileDetailsRepository

class ProfileViewModel(
    private val profileDetailsRepository: ProfileDetailsRepository,
    private val profileCommentsRepository: ProfileCommentsRepository
) {
    private val _profileDetails = MutableLiveData<ProfileDetails?>().apply { value = null }
    val profileDetails: LiveData<ProfileDetails?> get() = _profileDetails

    private val _profileComments = MutableLiveData<ProfileComments?>().apply { value = null }
    val profileComments: LiveData<ProfileComments?> get() = _profileComments

    private val _uiState = MutableLiveData<ProfileUiState>().apply {
        value = ProfileUiState(
            cytoidID = "",
            foldTextFiled = false,
            expandQueryOptionsDropdownMenu = false,
            ignoreLocalCacheData = false,
            keep2DecimalPlaces = true
        )
    }
    val uiState: LiveData<ProfileUiState> get() = _uiState

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
        uiState.value?.let { uiState ->
            _profileDetails.value = profileDetailsRepository.getProfileDetails(
                cytoidID = uiState.cytoidID,
                disableLocalCache = uiState.ignoreLocalCacheData
            )
            _profileComments.value = profileCommentsRepository.getProfileComments(
                cytoidID = uiState.cytoidID,
                id = _profileDetails.value?.user?.id ?: return@let,
                disableLocalCache = uiState.ignoreLocalCacheData
            )
        }
    }

    private fun updateUIState(update: ProfileUiState.() -> ProfileUiState) {
        _uiState.value = _uiState.value?.update()
    }

    data class ProfileUiState(
        val cytoidID: String,
        val foldTextFiled: Boolean,
        val expandQueryOptionsDropdownMenu: Boolean,
        val ignoreLocalCacheData: Boolean,
        val keep2DecimalPlaces: Boolean
    )
}