package com.focusguard.app.presentation.ui.home

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusguard.app.domain.repository.AppRestrictionRepository
import com.focusguard.app.domain.repository.StepCounterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val stepsToday: Int = 0,
    val activeRestrictionCount: Int = 0,
    val isAccessibilityEnabled: Boolean = false,
    val isLoading: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stepCounterRepository: StepCounterRepository,
    private val restrictionRepository: AppRestrictionRepository,
) : ViewModel() {

    val uiState = combine(
        stepCounterRepository.observeStepsToday(),
        restrictionRepository.observeAll(),
    ) { steps, restrictions ->
        HomeUiState(
            stepsToday = steps,
            activeRestrictionCount = restrictions.count { it.isEnabled },
            isAccessibilityEnabled = isAccessibilityServiceEnabled(),
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        return enabledServices.contains(context.packageName, ignoreCase = true)
    }
}
