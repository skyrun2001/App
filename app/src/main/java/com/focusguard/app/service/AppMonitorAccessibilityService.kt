package com.focusguard.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.focusguard.app.domain.model.AccessResult
import com.focusguard.app.domain.model.DenialReason
import com.focusguard.app.domain.usecase.CheckAppAccessUseCase
import com.focusguard.app.domain.usecase.TrackAppUsageUseCase
import com.focusguard.app.presentation.ui.blocked.BlockedAppActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Monitors foreground app changes and blocks restricted apps by launching
 * [BlockedAppActivity] when conditions are not met.
 *
 * The service also tracks how long each monitored session lasts so the daily
 * usage counter can be updated when the user leaves the app.
 */
@AndroidEntryPoint
class AppMonitorAccessibilityService : AccessibilityService() {

    @Inject lateinit var checkAppAccessUseCase: CheckAppAccessUseCase
    @Inject lateinit var trackAppUsageUseCase: TrackAppUsageUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** Package currently considered "active" (non-null only for tracked apps). */
    private var activePackage: String? = null

    /** Epoch-millis when the current active session started. */
    private var sessionStartMillis: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val newPackage = event.packageName?.toString() ?: return
        if (newPackage == packageName) return // ignore our own UI
        if (newPackage == activePackage) return // same app, no change

        endCurrentSession(newPackage)
        checkAndPossiblyBlock(newPackage)
    }

    private fun endCurrentSession(incomingPackage: String) {
        val previousPackage = activePackage ?: return
        if (previousPackage == incomingPackage) return

        val elapsedMillis = System.currentTimeMillis() - sessionStartMillis
        val elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis).toInt()

        serviceScope.launch {
            trackAppUsageUseCase(previousPackage, elapsedMinutes)
        }

        activePackage = null
        sessionStartMillis = 0L
    }

    private fun checkAndPossiblyBlock(packageName: String) {
        serviceScope.launch {
            val result = checkAppAccessUseCase(packageName)
            if (result is AccessResult.Denied) {
                launchBlockScreen(packageName, result)
            } else {
                // Start tracking this session
                activePackage = packageName
                sessionStartMillis = System.currentTimeMillis()
            }
        }
    }

    private fun launchBlockScreen(blockedPackage: String, denial: AccessResult.Denied) {
        val intent = Intent(this, BlockedAppActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(BlockedAppActivity.EXTRA_PACKAGE_NAME, blockedPackage)
            putExtra(BlockedAppActivity.EXTRA_DENIAL_REASON, denial.reason.name)
            putExtra(BlockedAppActivity.EXTRA_STEPS_NEEDED, denial.stepsNeeded)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        // No ongoing operation to cancel
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
