package com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.screen.ProfileScreenDataModel
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository.ProfileScreenDataModelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileScreenDataModelRepository: ProfileScreenDataModelRepository = ProfileScreenDataModelRepository()
) : ViewModel() {
    private val _profileScreenDataModel = MutableStateFlow<ProfileScreenDataModel?>(null)
    val profileScreenDataModel: StateFlow<ProfileScreenDataModel?> get() = _profileScreenDataModel.asStateFlow()

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

    fun loadSpecificCacheProfileScreenDataModel(timeStamp: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            updateProfileScreenDataModel(
                profileScreenDataModelRepository.getSpecificCacheProfileScreenDataModel(
                    cytoidID = uiState.value.cytoidID,
                    timeStamp = timeStamp
                )
            )
        }
    }

    fun clearProfileScreenDataModel() = updateProfileScreenDataModel(null)

    fun updateProfileScreenDataModel(profileScreenDataModel: ProfileScreenDataModel?) {
        _profileScreenDataModel.update { profileScreenDataModel }
    }

    fun updateUIState(uiState: ProfileUiState) {
        _uiState.update { uiState }
    }

    suspend fun enqueueQuery() {
        viewModelScope.launch(Dispatchers.IO) {
            uiState.value.let { uiState ->
                async {
                    updateProfileScreenDataModel(
                        profileScreenDataModelRepository.getProfileScreenDataModel(
                            cytoidID = uiState.cytoidID,
                            disableLocalCache = uiState.ignoreLocalCacheData
                        )
                    )
                }.await()
                updateUIState { copy(isQuerying = false) }
            }
        }
    }

    private fun updateUIState(update: ProfileUiState.() -> ProfileUiState) {
        updateUIState(_uiState.value.update())
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