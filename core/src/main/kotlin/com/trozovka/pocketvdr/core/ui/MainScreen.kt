package com.trozovka.pocketvdr.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.trozovka.pocketvdr.core.entitlement.EntitlementHost
import com.trozovka.pocketvdr.core.logging.VoyageLoggerService
import com.trozovka.pocketvdr.core.util.appDisplayName
import com.trozovka.pocketvdr.core.util.appVersionName
import com.trozovka.pocketvdr.core.util.formatDurationShort
import com.trozovka.pocketvdr.core.util.formatLatLon
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    onStartRequested: () -> Unit,
    onStopRequested: () -> Unit,
    onFlagTapped: () -> Unit,
    onNoteSaved: (flagId: Long, note: String) -> Unit,
    onViewVoyages: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val isRunning by VoyageLoggerService.isRunning.collectAsState()
    val startTimeMillis by VoyageLoggerService.startTimeMillis.collectAsState()
    val fixCount by VoyageLoggerService.fixCount.collectAsState()
    val latestFix by VoyageLoggerService.latestFix.collectAsState()
    val lastFlagId by VoyageLoggerService.lastFlagId.collectAsState()

    var showNoteDialog by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(lastFlagId) {
        if (lastFlagId != null) {
            noteText = ""
            showNoteDialog = true
        }
    }

    LaunchedEffect(Unit) {
        statusMessage = EntitlementHost.current().statusMessage(System.currentTimeMillis())
    }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            nowMillis = System.currentTimeMillis()
            delay(1000)
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
            if (isRunning && startTimeMillis != null) {
                Text(
                    "Elapsed: ${formatDurationShort(nowMillis - startTimeMillis!!)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            latestFix?.satellitesUsed?.let { satellites ->
                Text("Satellites used: $satellites", style = MaterialTheme.typography.bodyMedium)
            }
            latestFix?.let { fix ->
                Text(
                    "Last fix: ${formatLatLon(fix.latitude, fix.longitude)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (isRunning) {
                Button(
                    onClick = onFlagTapped,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                ) {
                    Text("MARK INCIDENT", color = Color.White)
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
            TextButton(onClick = onOpenSettings) {
                Text("Settings")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text("About", style = MaterialTheme.typography.titleSmall)
            Text(
                "${appDisplayName(context)} v${appVersionName(context)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                "PocketVDR is a personal voyage data recorder -- it logs your position, speed, " +
                    "heading, and altitude offline throughout a voyage, so you have a real record " +
                    "of what happened without needing a network connection.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
            )
            Text(
                "Developed by Trozovka -- github.com/Trozovka",
                style = MaterialTheme.typography.bodySmall,
            )

            if (statusMessage.isNotBlank()) {
                Text(
                    statusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }

    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = {
                showNoteDialog = false
                VoyageLoggerService.acknowledgeFlag()
            },
            title = { Text("Incident marked") },
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
                    VoyageLoggerService.acknowledgeFlag()
                }) {
                    Text("Save note")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNoteDialog = false
                    VoyageLoggerService.acknowledgeFlag()
                }) {
                    Text("Skip")
                }
            },
        )
    }
}
