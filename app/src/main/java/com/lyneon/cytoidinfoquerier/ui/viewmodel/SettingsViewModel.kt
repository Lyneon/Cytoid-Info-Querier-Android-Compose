package com.lyneon.cytoidinfoquerier.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.lyneon.cytoidinfoquerier.data.constant.MMKVKeys
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUIState())
    val uiState: StateFlow<SettingsUIState> get() = _uiState.asStateFlow()

    fun setEnableSentry(enable: Boolean) {
        updateUIState { copy(enableSentry = enable) }
    }

    private fun updateUIState(update: SettingsUIState.() -> SettingsUIState) {
        updateUIState(_uiState.value.update())
    }

    fun updateUIState(uiState: SettingsUIState) {
        _uiState.update { uiState }
    }
}

data class SettingsUIState(
    val enableSentry: Boolean = MMKV.defaultMMKV().decodeBool(MMKVKeys.ENABLE_SENTRY.name, true)
)