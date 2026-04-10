package com.focusguard.app.presentation.ui.settings

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PermissionStatus(
    val isAccessibilityEnabled: Boolean = false,
    val isUsageStatsGranted: Boolean = false,
    val isActivityRecognitionGranted: Boolean = false,
    val isOverlayGranted: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _permissionStatus = MutableStateFlow(PermissionStatus())
    val permissionStatus: StateFlow<PermissionStatus> = _permissionStatus.asStateFlow()

    init {
        refreshPermissions()
        // Poll every 2 seconds while screen is open to reflect changes made in system settings
        viewModelScope.launch {
            while (true) {
                delay(2_000)
                refreshPermissions()
            }
        }
    }

    fun refreshPermissions() {
        _permissionStatus.update {
            PermissionStatus(
                isAccessibilityEnabled = isAccessibilityEnabled(),
                isUsageStatsGranted = isUsageStatsGranted(),
                isActivityRecognitionGranted = isActivityRecognitionGranted(),
                isOverlayGranted = Settings.canDrawOverlays(context),
            )
        }
    }

    private fun isAccessibilityEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        return enabledServices.contains(context.packageName, ignoreCase = true)
    }

    private fun isUsageStatsGranted(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun isActivityRecognitionGranted(): Boolean =
        context.checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION) ==
            PackageManager.PERMISSION_GRANTED
}
