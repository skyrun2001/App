package com.focusguard.app.domain.repository

import kotlinx.coroutines.flow.Flow

/** Provides access to the device's step-counter sensor data. */
interface StepCounterRepository {

    /**
     * Emits the number of steps taken today.
     * Emits 0 when the sensor is unavailable or the permission is missing.
     */
    fun observeStepsToday(): Flow<Int>

    /** Returns the current step count without starting a continuous stream. */
    suspend fun getStepsToday(): Int
}
