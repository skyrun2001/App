package com.focusguard.app.domain.repository

import com.focusguard.app.domain.model.AppRestriction
import kotlinx.coroutines.flow.Flow

/** Manages persistence of [AppRestriction] records. */
interface AppRestrictionRepository {

    /** Emits the full list of restrictions whenever the underlying data changes. */
    fun observeAll(): Flow<List<AppRestriction>>

    /** Returns the restriction for [packageName], or null if none exists. */
    suspend fun findByPackageName(packageName: String): AppRestriction?

    /** Inserts a new restriction or replaces an existing one. */
    suspend fun save(restriction: AppRestriction)

    /** Removes the restriction identified by [packageName]. */
    suspend fun delete(packageName: String)

    /**
     * Adds [additionalMinutes] to the accumulated usage counter for today.
     * Creates the restriction record if it does not exist yet.
     */
    suspend fun addUsageMinutes(packageName: String, additionalMinutes: Int)

    /** Resets the daily usage counter to 0 for all restrictions. */
    suspend fun resetDailyUsage()
}
