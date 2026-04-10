package com.focusguard.app.domain.usecase

import com.focusguard.app.domain.model.AppRestriction
import com.focusguard.app.domain.repository.AppRestrictionRepository
import javax.inject.Inject

/** Validates and persists a single [AppRestriction]. */
class SaveRestrictionUseCase @Inject constructor(
    private val repository: AppRestrictionRepository,
) {
    suspend operator fun invoke(restriction: AppRestriction) {
        require(restriction.packageName.isNotBlank()) { "packageName must not be blank" }
        require(restriction.requiredStepCount >= 0) { "requiredStepCount must be >= 0" }
        require(restriction.dailyTimeLimitMinutes >= 0) { "dailyTimeLimitMinutes must be >= 0" }
        repository.save(restriction)
    }
}
