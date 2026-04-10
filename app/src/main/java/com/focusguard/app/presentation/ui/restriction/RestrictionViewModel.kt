package com.focusguard.app.presentation.ui.restriction

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusguard.app.domain.model.AppRestriction
import com.focusguard.app.domain.model.TimeWindow
import com.focusguard.app.domain.usecase.DeleteRestrictionUseCase
import com.focusguard.app.domain.usecase.SaveRestrictionUseCase
import com.focusguard.app.domain.repository.AppRestrictionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RestrictionEditUiState(
    val packageName: String = "",
    val appName: String = "",
    val isEnabled: Boolean = true,
    val requiredStepCount: String = "0",
    val dailyTimeLimitMinutes: String = "0",
    val allowedTimeWindows: List<TimeWindow> = emptyList(),
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class RestrictionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val restrictionRepository: AppRestrictionRepository,
    private val saveRestrictionUseCase: SaveRestrictionUseCase,
    private val deleteRestrictionUseCase: DeleteRestrictionUseCase,
) : ViewModel() {

    private val packageName: String = checkNotNull(savedStateHandle["packageName"])

    private val _uiState = MutableStateFlow(RestrictionEditUiState(packageName = packageName))
    val uiState: StateFlow<RestrictionEditUiState> = _uiState.asStateFlow()

    init {
        loadExistingRestriction()
    }

    private fun loadExistingRestriction() {
        viewModelScope.launch {
            val existing = restrictionRepository.findByPackageName(packageName)
            val appName = resolveAppName(packageName)
            _uiState.update { state ->
                if (existing != null) {
                    state.copy(
                        appName = appName,
                        isEnabled = existing.isEnabled,
                        requiredStepCount = existing.requiredStepCount.toString(),
                        dailyTimeLimitMinutes = existing.dailyTimeLimitMinutes.toString(),
                        allowedTimeWindows = existing.allowedTimeWindows,
                        isLoading = false,
                    )
                } else {
                    state.copy(appName = appName, isLoading = false)
                }
            }
        }
    }

    fun onEnabledChanged(enabled: Boolean) {
        _uiState.update { it.copy(isEnabled = enabled) }
    }

    fun onRequiredStepsChanged(value: String) {
        _uiState.update { it.copy(requiredStepCount = value.filter(Char::isDigit)) }
    }

    fun onDailyLimitChanged(value: String) {
        _uiState.update { it.copy(dailyTimeLimitMinutes = value.filter(Char::isDigit)) }
    }

    fun addTimeWindow(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        val newWindow = TimeWindow(
            id = System.currentTimeMillis(),
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
        )
        _uiState.update { it.copy(allowedTimeWindows = it.allowedTimeWindows + newWindow) }
    }

    fun removeTimeWindow(window: TimeWindow) {
        _uiState.update { it.copy(allowedTimeWindows = it.allowedTimeWindows - window) }
    }

    fun save() {
        viewModelScope.launch {
            runCatching {
                val state = _uiState.value
                val restriction = AppRestriction(
                    packageName = packageName,
                    appName = state.appName,
                    isEnabled = state.isEnabled,
                    requiredStepCount = state.requiredStepCount.toIntOrNull() ?: 0,
                    dailyTimeLimitMinutes = state.dailyTimeLimitMinutes.toIntOrNull() ?: 0,
                    allowedTimeWindows = state.allowedTimeWindows,
                )
                saveRestrictionUseCase(restriction)
            }.onSuccess {
                _uiState.update { it.copy(isSaved = true) }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    fun deleteRestriction() {
        viewModelScope.launch {
            runCatching { deleteRestrictionUseCase(packageName) }
                .onSuccess { _uiState.update { it.copy(isDeleted = true) } }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.message) } }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun resolveAppName(pkg: String): String =
        runCatching {
            context.packageManager.getApplicationLabel(
                context.packageManager.getApplicationInfo(pkg, PackageManager.GET_META_DATA)
            ).toString()
        }.getOrDefault(pkg)
}
