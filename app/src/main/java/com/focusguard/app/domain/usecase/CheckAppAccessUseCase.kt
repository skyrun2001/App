package com.focusguard.app.domain.usecase

import com.focusguard.app.domain.model.AccessResult
import com.focusguard.app.domain.model.DenialReason
import com.focusguard.app.domain.repository.AppRestrictionRepository
import com.focusguard.app.domain.repository.StepCounterRepository
import java.util.Calendar
import javax.inject.Inject

/**
 * Determines whether the user is currently allowed to use the given app.
 *
 * Evaluation order (first failing check wins):
 * 1. Daily time limit
 * 2. Step-count requirement
 * 3. Allowed time windows
 */
class CheckAppAccessUseCase @Inject constructor(
    private val restrictionRepository: AppRestrictionRepository,
    private val stepCounterRepository: StepCounterRepository,
) {
    suspend operator fun invoke(packageName: String): AccessResult {
        val restriction = restrictionRepository.findByPackageName(packageName)
            ?: return AccessResult.Allowed
        if (!restriction.isEnabled) return AccessResult.Allowed

        // 1. Daily time limit
        if (restriction.dailyTimeLimitMinutes > 0 &&
            restriction.usedMinutesToday >= restriction.dailyTimeLimitMinutes
        ) {
            return AccessResult.Denied(DenialReason.DAILY_LIMIT_EXCEEDED)
        }

        // 2. Step-count requirement
        if (restriction.requiredStepCount > 0) {
            val currentSteps = stepCounterRepository.getStepsToday()
            if (currentSteps < restriction.requiredStepCount) {
                return AccessResult.Denied(
                    reason = DenialReason.INSUFFICIENT_STEPS,
                    stepsNeeded = restriction.requiredStepCount - currentSteps,
                )
            }
        }

        // 3. Time windows (empty list → always allowed)
        if (restriction.allowedTimeWindows.isNotEmpty()) {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val isInAnyWindow = restriction.allowedTimeWindows.any { it.contains(hour, minute) }
            if (!isInAnyWindow) {
                return AccessResult.Denied(DenialReason.OUTSIDE_TIME_WINDOW)
            }
        }

        return AccessResult.Allowed
    }
}
