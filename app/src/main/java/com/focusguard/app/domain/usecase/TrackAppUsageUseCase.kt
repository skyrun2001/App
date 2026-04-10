package com.focusguard.app.domain.usecase

import com.focusguard.app.domain.repository.AppRestrictionRepository
import javax.inject.Inject

/**
 * Records elapsed usage time for a restricted app.
 * Called by the [AppMonitorAccessibilityService] when a session ends.
 */
class TrackAppUsageUseCase @Inject constructor(
    private val repository: AppRestrictionRepository,
) {
    suspend operator fun invoke(packageName: String, sessionMinutes: Int) {
        if (sessionMinutes <= 0) return
        repository.addUsageMinutes(packageName, sessionMinutes)
    }
}
