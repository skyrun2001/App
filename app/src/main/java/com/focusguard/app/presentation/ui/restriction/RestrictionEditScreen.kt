package com.focusguard.app.presentation.ui.restriction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusguard.app.domain.model.TimeWindow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestrictionEditScreen(
    packageName: String,
    onNavigateBack: () -> Unit,
    viewModel: RestrictionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.isSaved) { if (state.isSaved) onNavigateBack() }
    LaunchedEffect(state.isDeleted) { if (state.isDeleted) onNavigateBack() }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Time picker dialog state
    var showTimePickerFor by rememberSaveable { mutableStateOf<TimePickerMode?>(null) }
    var pendingStartHour by remember { mutableStateOf(8) }
    var pendingStartMinute by remember { mutableStateOf(0) }

    if (showTimePickerFor == TimePickerMode.START) {
        val pickerState = rememberTimePickerState(initialHour = 8, initialMinute = 0, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePickerFor = null },
            title = { Text("Start time") },
            text = { TimePicker(state = pickerState) },
            confirmButton = {
                TextButton(onClick = {
                    pendingStartHour = pickerState.hour
                    pendingStartMinute = pickerState.minute
                    showTimePickerFor = TimePickerMode.END
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerFor = null }) { Text("Cancel") }
            },
        )
    }

    if (showTimePickerFor == TimePickerMode.END) {
        val pickerState = rememberTimePickerState(initialHour = 22, initialMinute = 0, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePickerFor = null },
            title = { Text("End time") },
            text = { TimePicker(state = pickerState) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addTimeWindow(
                        startHour = pendingStartHour,
                        startMinute = pendingStartMinute,
                        endHour = pickerState.hour,
                        endMinute = pickerState.minute,
                    )
                    showTimePickerFor = null
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerFor = null }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.appName.ifBlank { "Edit Restriction" }) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        if (state.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            // Enable toggle
            SectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Enable restriction", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "When off, all rules are ignored",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    Switch(
                        checked = state.isEnabled,
                        onCheckedChange = viewModel::onEnabledChanged,
                    )
                }
            }

            // Step count requirement
            SectionCard {
                Text("Step count requirement", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Minimum steps before app is allowed. Set 0 to disable.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.requiredStepCount,
                    onValueChange = viewModel::onRequiredStepsChanged,
                    label = { Text("Required steps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            // Daily time limit
            SectionCard {
                Text("Daily time limit", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Maximum minutes of usage per day. Set 0 to disable.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.dailyTimeLimitMinutes,
                    onValueChange = viewModel::onDailyLimitChanged,
                    label = { Text("Limit (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            // Allowed time windows
            SectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Allowed time windows", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "App is only allowed during these periods.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    IconButton(onClick = { showTimePickerFor = TimePickerMode.START }) {
                        Icon(Icons.Default.Add, contentDescription = "Add time window")
                    }
                }

                if (state.allowedTimeWindows.isEmpty()) {
                    Text(
                        "No windows set – allowed at any time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                } else {
                    state.allowedTimeWindows.forEachIndexed { index, window ->
                        if (index > 0) HorizontalDivider()
                        TimeWindowRow(window = window, onDelete = { viewModel.removeTimeWindow(window) })
                    }
                }
            }

            // Action buttons
            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save")
            }

            Button(
                onClick = viewModel::deleteRestriction,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Text("Delete restriction", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            content()
        }
    }
}

@Composable
private fun TimeWindowRow(
    window: TimeWindow,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = window.toString(),
            style = MaterialTheme.typography.bodyMedium,
        )
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Remove time window",
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

private enum class TimePickerMode { START, END }
