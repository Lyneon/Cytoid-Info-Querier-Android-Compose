package com.lyneon.cytoidinfoquerier.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyneon.cytoidinfoquerier.data.constant.CytoidConstant
import com.lyneon.cytoidinfoquerier.data.constant.OkHttpSingleton
import com.lyneon.cytoidinfoquerier.data.constant.SearchLevelOrder
import com.lyneon.cytoidinfoquerier.data.constant.SearchLevelSortingStrategy
import com.lyneon.cytoidinfoquerier.data.model.webapi.SearchLevelsResult
import com.lyneon.cytoidinfoquerier.data.repository.SearchLevelsRepository
import com.lyneon.cytoidinfoquerier.json
import com.lyneon.cytoidinfoquerier.util.CytoidLevelUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Request

class LevelViewModel(
    private val searchLevelsRepository: SearchLevelsRepository = SearchLevelsRepository()
) : ViewModel() {
    private val _searchResult = MutableStateFlow<List<SearchLevelsResult>?>(null)
    val searchResult get() = _searchResult.asStateFlow()

    private val _uiState = MutableStateFlow(LevelUIState())
    val uiState get() = _uiState.asStateFlow()

    fun setSearchQuery(query: String) {
        updateUIState { copy(searchQuery = query) }
    }

    fun setQuerySortStrategy(strategy: SearchLevelSortingStrategy) {
        updateUIState { copy(querySortStrategy = strategy) }
    }

    fun setQueryOrder(order: SearchLevelOrder) {
        updateUIState { copy(queryOrder = order) }
    }

    fun setQueryPage(page: Int) {
        updateUIState { copy(queryPage = page) }
    }

    fun setQueryLimit(limit: Int) {
        updateUIState { copy(queryLimit = limit) }
    }

    fun setFoldTextFiled(fold: Boolean) {
        updateUIState { copy(foldTextFiled = fold) }
    }

    fun setExpandSearchOptionsDropdownMenu(expand: Boolean) {
        updateUIState { copy(expandSearchOptionsDropdownMenu = expand) }
    }

    fun setIsSearching(isSearching: Boolean) {
        updateUIState { copy(isSearching = isSearching) }
    }

    fun setErrorMessage(message: String) {
        updateUIState { copy(errorMessage = message) }
    }

    fun setQueryFeatured(featured: Boolean) {
        updateUIState { copy(queryFeatured = featured) }
    }

    fun setQueryQualified(qualified: Boolean) {
        updateUIState { copy(queryQualified = qualified) }
    }

    fun setQueryTag(tag: String) {
        updateUIState { copy(queryTag = tag) }
    }

    fun setIsSearchByTag(isSearchByTag: Boolean) {
        updateUIState { copy(isSearchByTag = isSearchByTag) }
    }

    fun searchLevels(resetPage: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {
        updateUIState { copy(isSearching = true) }
        if (resetPage) updateUIState { copy(queryPage = 0) }
        uiState.value.run {
            val searchResult = try {
                searchLevelsRepository.searchLevelsWithPagesCount(
                    searchQuery,
                    querySortStrategy,
                    queryOrder,
                    queryPage,
                    queryLimit,
                    queryFeatured,
                    queryQualified
                )
            } catch (e: Exception) {
                updateUIState { copy(errorMessage = e.stackTraceToString()) }
                Pair(emptyList(), 1)
            }
            _searchResult.update { searchResult.first }
            updateUIState { copy(totalPages = searchResult.second) }
            updateUIState { copy(isSearching = false) }
            updateUIState { copy(foldTextFiled = true) }
        }
    }

    fun randomLevel() = viewModelScope.launch(Dispatchers.IO) {
        updateUIState { copy(isSearching = true) }
        val levelCount = CytoidLevelUtils.getLevelCount()
        val request = Request.Builder()
            .url("https://services.cytoid.io/levels?page=${(0 until levelCount).random()}&limit=1")
            .removeHeader("User-Agent")
            .addHeader("User-Agent", CytoidConstant.clientUA)
            .build()
        val response = OkHttpSingleton.instance.newCall(request).execute()
        if (response.isSuccessful) response.body?.let {
            val jsonString = it.string()
            val searchResult: List<SearchLevelsResult> = json.decodeFromString(jsonString)
            updateUIState { copy(isSearching = false, queryPage = 0, totalPages = 1) }
            _searchResult.update { searchResult }
        }
    }

    fun searchLevelsByTag(resetPage: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {
        updateUIState { copy(isSearching = true) }
        if (resetPage) updateUIState { copy(queryPage = 0) }
        uiState.value.run {
            val searchResult = try {
                searchLevelsRepository.searchLevelsByTagWithPagesCount(
                    queryTag,
                    querySortStrategy,
                    queryOrder,
                    queryPage,
                    queryLimit,
                    queryFeatured,
                    queryQualified
                )
            } catch (e: Exception) {
                updateUIState { copy(errorMessage = e.stackTraceToString()) }
                Pair(emptyList(), 1)
            }
            Log.d("LevelViewModel", "pages: ${searchResult.second}")
            _searchResult.update { searchResult.first }
            updateUIState { copy(totalPages = searchResult.second) }
            updateUIState { copy(isSearching = false) }
        }
    }

    private fun updateUIState(update: LevelUIState.() -> LevelUIState) =
        updateUIState(_uiState.value.update())

    private fun updateUIState(uiState: LevelUIState) = _uiState.update { uiState }
}

data class LevelUIState(
    val searchQuery: String = "",
    val querySortStrategy: SearchLevelSortingStrategy = SearchLevelSortingStrategy.CreationDate,
    val queryOrder: SearchLevelOrder = SearchLevelOrder.Descending,
    val queryPage: Int = 0,
    val queryLimit: Int = 18,
    val foldTextFiled: Boolean = false,
    val isSearching: Boolean = false,
    val expandSearchOptionsDropdownMenu: Boolean = false,
    val errorMessage: String = "",
    val queryFeatured: Boolean = false,
    val queryQualified: Boolean = false,
    val totalPages: Int = 1,
    val queryTag: String = "",
    val isSearchByTag: Boolean = false
)