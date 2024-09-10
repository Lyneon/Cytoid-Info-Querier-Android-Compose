package com.lyneon.cytoidinfoquerier.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyneon.cytoidinfoquerier.data.constant.OkHttpSingleton
import com.lyneon.cytoidinfoquerier.util.extension.getStatusMessageFromCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Request
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

class ToolViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ToolUIState())
    val uiState: StateFlow<ToolUIState> get() = _uiState.asStateFlow()

    fun setRatingCalculatorAccuracy(accuracy: String) {
        if (checkRatingCalculatorInput(accuracy))
            updateUIState { copy(ratingCalculatorAccuracy = accuracy) }
    }

    fun setRatingCalculatorLevel(level: String) {
        if (checkRatingCalculatorInput(level))
            updateUIState { copy(ratingCalculatorLevel = level) }
    }

    fun setRatingCalculatorRating(rating: String) {
        if (checkRatingCalculatorInput(rating))
            updateUIState { copy(ratingCalculatorRating = rating) }
    }

    fun setPingResult(pingResult: String) {
        updateUIState { copy(pingResult = pingResult) }
    }

    private fun updateUIState(update: ToolUIState.() -> ToolUIState) {
        updateUIState(_uiState.value.update())
    }

    fun updateUIState(uiState: ToolUIState) {
        _uiState.update { uiState }
    }

    fun calculateAccuracy() {
        _uiState.value.ratingCalculatorLevel.toDoubleOrNull()?.let { level ->
            _uiState.value.ratingCalculatorRating.toFloatOrNull()?.let { rating ->
                val accuracy: Double = when (val multiplier: Double = rating / level) {
                    in 0f..0.5f -> 280 * multiplier * multiplier
                    in 0.5f..0.7f -> 100 - 3 * ((10.0).pow(3.5 - 5 * multiplier))
                    in 0.7f..0.86f -> 100 - 3 * ((10.0).pow(4.375 - 6.25 * multiplier))
                    in 0.86f..0.94f -> 100 - 3 * ((10.0).pow(9.75 - 12.5 * multiplier))
                    in 0.94f..1f -> (multiplier + 199) / 2
                    else -> 0.0
                }
                setRatingCalculatorAccuracy(accuracy.toString())
            }
        }
    }

    fun calculateRating() {
        _uiState.value.ratingCalculatorLevel.toDoubleOrNull()?.let { level ->
            _uiState.value.ratingCalculatorAccuracy.toFloatOrNull()?.let { accuracy ->
                val multiplier: Double = when (accuracy) {
                    in 0f..70f -> sqrt(accuracy / 70) * 0.5
                    in 70f..97f -> 0.7 - 0.2 * log10((100 - accuracy) / 3)
                    in 97f..99.7f -> 0.7 - 0.16 * log10((100 - accuracy) / 3)
                    in 99.7f..99.97f -> 0.78 - 0.08 * log10((100 - accuracy) / 3)
                    in 99.97f..100f -> 2.0 * accuracy - 199
                    else -> 0.0
                }
                setRatingCalculatorRating((multiplier * level).toString())
            }
        }
    }

    fun pingCytoidIO() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = OkHttpSingleton.instance.newCall(
                    Request.Builder().url("https://cytoid.io/")
                        .head()
                        .removeHeader("User-Agent")
                        .addHeader(
                            "User-Agent",
                            "CytoidClient/2.1.1"
                        )
                        .build()
                ).execute()
                setPingResult("ping result:\ncytoid.io:${response.code} ${response.getStatusMessageFromCode()}")
            } catch (e: Exception) {
                setPingResult("ping failed:\n${e.message}")
            }
        }
    }

    private fun checkRatingCalculatorInput(input: String): Boolean =
        !(input.any { char -> !(char.isDigit() || char == '.') } || input.count { char -> char == '.' } > 1)
}

data class ToolUIState(
    val ratingCalculatorAccuracy: String = "",
    val ratingCalculatorLevel: String = "",
    val ratingCalculatorRating: String = "",
    val pingResult: String = ""
)