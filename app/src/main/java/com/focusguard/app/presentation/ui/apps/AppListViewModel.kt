package com.focusguard.app.presentation.ui.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusguard.app.domain.model.AppInfo
import com.focusguard.app.domain.usecase.GetInstalledAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppListUiState(
    val apps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
) {
    val filteredApps: List<AppInfo>
        get() = if (searchQuery.isBlank()) apps
        else apps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
}

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
) : ViewModel() {

    private val allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val searchQuery = MutableStateFlow("")
    private val isLoading = MutableStateFlow(true)

    val uiState = combine(allApps, searchQuery, isLoading) { apps, query, loading ->
        AppListUiState(apps = apps, searchQuery = query, isLoading = loading)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppListUiState(),
    )

    init {
        loadApps()
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun refresh() {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            isLoading.value = true
            allApps.value = getInstalledAppsUseCase()
            isLoading.value = false
        }
    }
}
