package com.trozovka.pocketvdr.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.trozovka.pocketvdr.core.data.VoyageRepository
import com.trozovka.pocketvdr.core.util.formatFileTimestamp
import com.trozovka.pocketvdr.core.util.formatUtcTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(voyageId: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { VoyageRepository(context) }
    val fixes by repository.observeFixesForVoyage(voyageId).collectAsState(initial = emptyList())
    val flags by repository.observeFlagsForVoyage(voyageId).collectAsState(initial = emptyList())
    var selectedIndex by remember { mutableStateOf(0) }
    var showExportDialog by remember { mutableStateOf(false) }
    var voyageLabel by remember { mutableStateOf("voyage") }

    LaunchedEffect(voyageId) {
        repository.getVoyage(voyageId)?.let { voyage ->
            voyageLabel = "PocketVDR_" + formatFileTimestamp(voyage.startTimeMillis)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voyage review") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (fixes.isNotEmpty()) {
                        IconButton(onClick = { showExportDialog = true }) {
                            Icon(Icons.Filled.Share, contentDescription = "Export")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Surface(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (fixes.isEmpty()) {
                    Text(
                        "No fixes recorded for this voyage.",
                        modifier = Modifier.padding(16.dp),
                    )
                } else {
                    TrackCanvas(
                        fixes = fixes,
                        flags = flags,
                        selectedIndex = selectedIndex,
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(8.dp),
                    )

                    val lastIndex = (fixes.size - 1).coerceAtLeast(0)
                    Slider(
                        value = selectedIndex.toFloat(),
                        onValueChange = { selectedIndex = it.toInt().coerceIn(0, lastIndex) },
                        valueRange = 0f..lastIndex.toFloat(),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    )

                    val selectedFix = fixes.getOrNull(selectedIndex)
                    if (selectedFix != null) {
                        Text(
                            "Fix ${selectedIndex + 1}/${fixes.size} -- ${formatUtcTimestamp(selectedFix.timestampMillis)}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                        Text(
                            "%.5f, %.5f".format(selectedFix.latitude, selectedFix.longitude),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                }

                Text(
                    "Flagged events (${flags.size})",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(16.dp),
                )
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(flags) { flag ->
                        val nearestIndex = fixes.indexOfFirst { it.timestampMillis >= flag.timestampMillis }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { if (nearestIndex >= 0) selectedIndex = nearestIndex },
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column {
                                    Text(formatUtcTimestamp(flag.timestampMillis), style = MaterialTheme.typography.bodyMedium)
                                    if (!flag.note.isNullOrBlank()) {
                                        Text(flag.note, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        ExportDialog(
            voyageName = voyageLabel,
            fixes = fixes,
            flags = flags,
            onDismiss = { showExportDialog = false },
        )
    }
}
