package com.trozovka.pocketvdr.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import com.trozovka.pocketvdr.core.export.TextLogExporter
import com.trozovka.pocketvdr.core.util.formatUtcTimestamp

/**
 * Export a time range (or, left at its default full-width selection, the whole voyage) as GPX,
 * NMEA, or a plain text log. Only fixes/flags falling inside the selected range are written --
 * a real subset export, not a whole-voyage export with a decorative slider.
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

    fun share(fileName: String, content: String, mimeType: String) {
        val intent = ExportFileSharer.writeAndBuildShareIntent(context, fileName, content, mimeType)
        context.startActivity(intent)
        onDismiss()
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
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                    )
                }

                ExportOptionRow(
                    label = "Export as GPX",
                    description = "For mapping tools like OpenCPN or Google Earth.",
                    onClick = {
                        val selected = selectedFixes()
                        val gpx = GpxExporter.build(voyageName, selected, selectedFlags(selected))
                        share("$voyageName.gpx", gpx, "application/gpx+xml")
                    },
                )
                ExportOptionRow(
                    label = "Export as NMEA",
                    description = "Raw position sentences, compatible with ECDIS/chartplotter systems like OpenCPN.",
                    onClick = {
                        val nmea = NmeaExporter.build(selectedFixes())
                        share("$voyageName.nmea", nmea, "text/plain")
                    },
                )
                ExportOptionRow(
                    label = "Export as TXT",
                    description = "A plain, readable log for Notepad, WordPad, or printing -- not for navigation software.",
                    onClick = {
                        val selected = selectedFixes()
                        val text = TextLogExporter.build(voyageName, selected, selectedFlags(selected))
                        share("$voyageName.txt", text, "text/plain")
                    },
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun ExportOptionRow(label: String, description: String, onClick: () -> Unit) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(label)
        }
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
