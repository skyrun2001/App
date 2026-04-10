package com.focusguard.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.focusguard.app.domain.model.AppRestriction
import com.focusguard.app.domain.model.TimeWindow

@Entity(tableName = "app_restrictions")
data class AppRestrictionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val packageName: String,
    val appName: String,
    val isEnabled: Boolean,
    val requiredStepCount: Int,
    val dailyTimeLimitMinutes: Int,
    /** Serialised via [com.focusguard.app.data.database.converter.Converters]. */
    val allowedTimeWindows: List<TimeWindow>,
    val usedMinutesToday: Int,
    /** Epoch-day of the last daily-reset so the data layer can auto-reset. */
    val lastResetEpochDay: Long,
)

fun AppRestrictionEntity.toDomain(): AppRestriction = AppRestriction(
    id = id,
    packageName = packageName,
    appName = appName,
    isEnabled = isEnabled,
    requiredStepCount = requiredStepCount,
    dailyTimeLimitMinutes = dailyTimeLimitMinutes,
    allowedTimeWindows = allowedTimeWindows,
    usedMinutesToday = usedMinutesToday,
)

fun AppRestriction.toEntity(lastResetEpochDay: Long): AppRestrictionEntity = AppRestrictionEntity(
    id = id,
    packageName = packageName,
    appName = appName,
    isEnabled = isEnabled,
    requiredStepCount = requiredStepCount,
    dailyTimeLimitMinutes = dailyTimeLimitMinutes,
    allowedTimeWindows = allowedTimeWindows,
    usedMinutesToday = usedMinutesToday,
    lastResetEpochDay = lastResetEpochDay,
)
