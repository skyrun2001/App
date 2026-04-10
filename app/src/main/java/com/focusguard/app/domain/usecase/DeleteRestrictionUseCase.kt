package com.focusguard.app.domain.usecase

import com.focusguard.app.domain.repository.AppRestrictionRepository
import javax.inject.Inject

/** Removes the restriction for the given package name, if any. */
class DeleteRestrictionUseCase @Inject constructor(
    private val repository: AppRestrictionRepository,
) {
    suspend operator fun invoke(packageName: String) {
        require(packageName.isNotBlank()) { "packageName must not be blank" }
        repository.delete(packageName)
    }
}
