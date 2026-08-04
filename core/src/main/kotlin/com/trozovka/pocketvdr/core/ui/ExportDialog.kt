package com.trozovka.pocketvdr.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.trozovka.pocketvdr.core.data.FixEntity
import com.trozovka.pocketvdr.core.data.FlagEventEntity
import com.trozovka.pocketvdr.core.export.ExportFileSharer
import com.trozovka.pocketvdr.core.export.GpxExporter
import com.trozovka.pocketvdr.core.export.NmeaExporter
import com.trozovka.pocketvdr.core.util.formatUtcTimestamp

/**
 * Export a time range (or, left at its default full-width selection, the whole voyage) as GPX
 * or NMEA. Only fixes/flags falling inside the selected range are written -- a real subset
 * export, not a whole-voyage export with a decorative slider.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDialog(
    voyageName: String,
    fixes: List<FixEntity>,
    flags: List<FlagEventEntity>,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val lastIndex = (fixes.size - 1).coerceAtLeast(0)
    var range by remember { mutableStateOf(0f..lastIndex.toFloat()) }

    val startFix = fixes.getOrNull(range.start.toInt())
    val endFix = fixes.getOrNull(range.endInclusive.toInt())

    fun selectedFixes(): List<FixEntity> {
        val fromMillis = startFix?.timestampMillis ?: return emptyList()
        val toMillis = endFix?.timestampMillis ?: return emptyList()
        return fixes.filter { it.timestampMillis in fromMillis..toMillis }
    }

    fun selectedFlags(selected: List<FixEntity>): List<FlagEventEntity> {
        val fromMillis = selected.firstOrNull()?.timestampMillis ?: return emptyList()
        val toMillis = selected.lastOrNull()?.timestampMillis ?: return emptyList()
        return flags.filter { it.timestampMillis in fromMillis..toMillis }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export voyage") },
        text = {
            Column {
                Text("Range (defaults to the whole voyage):", style = MaterialTheme.typography.bodySmall)
                if (lastIndex > 0) {
                    RangeSlider(
                        value = range,
                        onValueChange = { range = it },
                        valueRange = 0f..lastIndex.toFloat(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (startFix != null && endFix != null) {
                    Text(
                        "${formatUtcTimestamp(startFix.timestampMillis)} -> ${formatUtcTimestamp(endFix.timestampMillis)}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val selected = selectedFixes()
                val gpx = GpxExporter.build(voyageName, selected, selectedFlags(selected))
                val intent = ExportFileSharer.writeAndBuildShareIntent(
                    context = context,
                    fileName = "$voyageName.gpx",
                    content = gpx,
                    mimeType = "application/gpx+xml",
                )
                context.startActivity(intent)
                onDismiss()
            }) {
                Text("Export as GPX")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                val selected = selectedFixes()
                val nmea = NmeaExporter.build(selected)
                val intent = ExportFileSharer.writeAndBuildShareIntent(
                    context = context,
                    fileName = "$voyageName.nmea",
                    content = nmea,
                    mimeType = "text/plain",
                )
                context.startActivity(intent)
                onDismiss()
            }) {
                Text("Export as NMEA")
            }
        },
    )
}
