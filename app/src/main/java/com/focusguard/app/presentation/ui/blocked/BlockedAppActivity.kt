package com.focusguard.app.presentation.ui.blocked

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.focusguard.app.domain.model.DenialReason
import com.focusguard.app.presentation.theme.FocusGuardTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Full-screen activity displayed when an app is blocked by a restriction rule.
 *
 * It is launched by [AppMonitorAccessibilityService] via a new-task Intent and
 * immediately finishes when the user taps "Go back", which returns focus to the
 * Android home screen.
 */
@AndroidEntryPoint
class BlockedAppActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        val denialReason = intent.getStringExtra(EXTRA_DENIAL_REASON)
            ?.let { runCatching { DenialReason.valueOf(it) }.getOrNull() }
            ?: DenialReason.OUTSIDE_TIME_WINDOW
        val stepsNeeded = intent.getIntExtra(EXTRA_STEPS_NEEDED, 0)

        setContent {
            FocusGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = "App Blocked",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = denialReason.toUserMessage(stepsNeeded),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Spacer(Modifier.height(32.dp))
                        Button(onClick = { goHome() }) {
                            Text("Go back")
                        }
                    }
                }
            }
        }
    }

    private fun goHome() {
        finish()
    }

    private fun DenialReason.toUserMessage(stepsNeeded: Int): String = when (this) {
        DenialReason.OUTSIDE_TIME_WINDOW ->
            "This app is not allowed at the current time.\nCheck your configured time windows."
        DenialReason.INSUFFICIENT_STEPS ->
            "You need $stepsNeeded more steps before this app is unlocked.\nGet walking!"
        DenialReason.DAILY_LIMIT_EXCEEDED ->
            "You have reached today's usage limit for this app.\nCome back tomorrow!"
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_DENIAL_REASON = "extra_denial_reason"
        const val EXTRA_STEPS_NEEDED = "extra_steps_needed"
    }
}
