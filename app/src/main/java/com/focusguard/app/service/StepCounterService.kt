package com.focusguard.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.focusguard.app.R
import com.focusguard.app.domain.repository.StepCounterRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that keeps the step-counter sensor alive even when the
 * app is in the background.  The actual step data is collected by
 * [StepCounterRepository]; this service exists only to keep the process alive.
 */
@AndroidEntryPoint
class StepCounterService : Service() {

    @Inject lateinit var stepCounterRepository: StepCounterRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        collectSteps()
    }

    private fun collectSteps() {
        serviceScope.launch {
            stepCounterRepository.observeStepsToday().collect { steps ->
                updateNotification(steps)
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "FocusGuard Step Counter",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shows your step count while FocusGuard is running"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(steps: Int = 0): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_steps)
            .setContentTitle("FocusGuard")
            .setContentText("Steps today: $steps")
            .setOngoing(true)
            .setSilent(true)
            .build()

    private fun updateNotification(steps: Int) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(steps))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
        START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val CHANNEL_ID = "focus_guard_steps"
        private const val NOTIFICATION_ID = 1001
    }
}
