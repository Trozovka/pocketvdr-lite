package com.trozovka.pocketvdr.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.trozovka.pocketvdr.core.logging.VoyageLoggerService

@Composable
fun MainScreen(
    onStartRequested: () -> Unit,
    onStopRequested: () -> Unit,
    onFlagTapped: () -> Unit,
    onNoteSaved: (flagId: Long, note: String) -> Unit,
    onViewVoyages: () -> Unit,
) {
    val isRunning by VoyageLoggerService.isRunning.collectAsState()
    val fixCount by VoyageLoggerService.fixCount.collectAsState()
    val latestFix by VoyageLoggerService.latestFix.collectAsState()
    val lastFlagId by VoyageLoggerService.lastFlagId.collectAsState()

    var showNoteDialog by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }

    LaunchedEffect(lastFlagId) {
        if (lastFlagId != null) {
            noteText = ""
            showNoteDialog = true
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("PocketVDR", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Not a type-approved VDR or S-VDR under SOLAS. No legal evidentiary standing. " +
                    "Personal record-keeping only.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )

            Text(if (isRunning) "Logging voyage" else "Not logging", style = MaterialTheme.typography.titleMedium)
            Text("Fixes recorded: $fixCount", style = MaterialTheme.typography.bodyMedium)
            latestFix?.let { fix ->
                Text(
                    "Last fix: %.5f, %.5f".format(fix.latitude, fix.longitude),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (isRunning) {
                Button(
                    onClick = onFlagTapped,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                ) {
                    Text("FLAG THIS MOMENT", color = Color.White)
                }
                OutlinedButton(onClick = onStopRequested, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Text("Stop voyage")
                }
            } else {
                Button(onClick = onStartRequested, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                    Text("Start voyage")
                }
            }

            TextButton(onClick = onViewVoyages, modifier = Modifier.padding(top = 12.dp)) {
                Text("View past voyages")
            }
        }
    }

    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Moment flagged") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Optional note") },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    lastFlagId?.let { onNoteSaved(it, noteText) }
                    showNoteDialog = false
                }) {
                    Text("Save note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text("Skip")
                }
            },
        )
    }
}
