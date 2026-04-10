package com.focusguard.app.data.repository

import com.focusguard.app.data.database.dao.AppRestrictionDao
import com.focusguard.app.data.database.entity.toDomain
import com.focusguard.app.data.database.entity.toEntity
import com.focusguard.app.domain.model.AppRestriction
import com.focusguard.app.domain.repository.AppRestrictionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class AppRestrictionRepositoryImpl @Inject constructor(
    private val dao: AppRestrictionDao,
) : AppRestrictionRepository {

    override fun observeAll(): Flow<List<AppRestriction>> =
        dao.observeAll().map { entities ->
            val today = LocalDate.now().toEpochDay()
            entities.map { entity ->
                // Auto-reset daily usage when the calendar day has changed
                if (entity.lastResetEpochDay < today) {
                    val reset = entity.copy(usedMinutesToday = 0, lastResetEpochDay = today)
                    dao.upsert(reset)
                    reset.toDomain()
                } else {
                    entity.toDomain()
                }
            }
        }

    override suspend fun findByPackageName(packageName: String): AppRestriction? {
        val entity = dao.findByPackageName(packageName) ?: return null
        val today = LocalDate.now().toEpochDay()
        return if (entity.lastResetEpochDay < today) {
            val reset = entity.copy(usedMinutesToday = 0, lastResetEpochDay = today)
            dao.upsert(reset)
            reset.toDomain()
        } else {
            entity.toDomain()
        }
    }

    override suspend fun save(restriction: AppRestriction) {
        val today = LocalDate.now().toEpochDay()
        dao.upsert(restriction.toEntity(lastResetEpochDay = today))
    }

    override suspend fun delete(packageName: String) {
        dao.deleteByPackageName(packageName)
    }

    override suspend fun addUsageMinutes(packageName: String, additionalMinutes: Int) {
        dao.addUsageMinutes(packageName, additionalMinutes)
    }

    override suspend fun resetDailyUsage() {
        dao.resetAllDailyUsage()
    }
}
