package com.focusguard.app.domain.model

/**
 * Aggregates all configurable restrictions for a single app.
 *
 * @property packageName  Unique identifier of the restricted app.
 * @property appName      Human-readable label (may change between lookups).
 * @property isEnabled    Master switch – disabled rules are completely ignored.
 * @property requiredStepCount  Steps the user must have walked today before the
 *   app is allowed. 0 means no step requirement.
 * @property dailyTimeLimitMinutes  Maximum minutes the app may be used per day.
 *   0 means no daily limit.
 * @property allowedTimeWindows  List of time ranges during which the app is
 *   permitted. An empty list means the app is allowed at any time (subject to
 *   the other conditions).
 * @property usedMinutesToday  Accumulated usage minutes for the current calendar
 *   day (derived from the data layer; not persisted here directly).
 */
data class AppRestriction(
    val id: Long = 0L,
    val packageName: String,
    val appName: String,
    val isEnabled: Boolean = true,
    val requiredStepCount: Int = 0,
    val dailyTimeLimitMinutes: Int = 0,
    val allowedTimeWindows: List<TimeWindow> = emptyList(),
    val usedMinutesToday: Int = 0,
)
