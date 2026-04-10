package com.focusguard.app.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.focusguard.app.domain.repository.StepCounterRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class StepCounterRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : StepCounterRepository {

    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val stepCounterSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    /**
     * The Android step-counter sensor reports the total steps since the last
     * device reboot. We record the first reading of the day as a baseline so
     * that today's steps = current – baseline.
     */
    private var dailyBaseline: Int = -1

    override fun observeStepsToday(): Flow<Int> = callbackFlow {
        val sensor = stepCounterSensor ?: run {
            trySend(0)
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val totalSteps = event.values[0].toInt()
                if (dailyBaseline < 0) dailyBaseline = totalSteps
                trySend(totalSteps - dailyBaseline)
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
        .catch { emit(0) }
        .distinctUntilChanged()

    override suspend fun getStepsToday(): Int =
        suspendCancellableCoroutine { continuation ->
            val sensor = stepCounterSensor
            if (sensor == null) {
                continuation.resume(0)
                return@suspendCancellableCoroutine
            }

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val totalSteps = event.values[0].toInt()
                    if (dailyBaseline < 0) dailyBaseline = totalSteps
                    sensorManager.unregisterListener(this)
                    if (!continuation.isCompleted) {
                        continuation.resume(totalSteps - dailyBaseline)
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
            }

            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            continuation.invokeOnCancellation { sensorManager.unregisterListener(listener) }
        }
}
