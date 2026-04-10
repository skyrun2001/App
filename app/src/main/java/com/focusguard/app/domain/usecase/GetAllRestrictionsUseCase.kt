package com.focusguard.app.domain.usecase

import com.focusguard.app.domain.model.AppRestriction
import com.focusguard.app.domain.repository.AppRestrictionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Returns a live stream of all configured [AppRestriction] records. */
class GetAllRestrictionsUseCase @Inject constructor(
    private val repository: AppRestrictionRepository,
) {
    operator fun invoke(): Flow<List<AppRestriction>> = repository.observeAll()
}
