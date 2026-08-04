package com.trozovka.pocketvdr.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.trozovka.pocketvdr.core.data.FixEntity
import com.trozovka.pocketvdr.core.data.FlagEventEntity

/**
 * Plain equirectangular plot of the track -- no map tiles, no basemap, no internet involved at
 * all. The project's non-negotiables state network is only ever allowed to matter for licensing;
 * a real online-tile map (osmdroid's default tile source) would violate that the moment someone
 * opens the review screen without signal, so this draws the raw lat/lon points scaled to fit
 * instead of fetching anything.
 */
@Composable
fun TrackCanvas(
    fixes: List<FixEntity>,
    flags: List<FlagEventEntity>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
    val trackColor = MaterialTheme.colorScheme.primary
    val flagColor = MaterialTheme.colorScheme.error
    val selectedColor = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        if (fixes.isEmpty()) return@Canvas

        val minLat = fixes.minOf { it.latitude }
        val maxLat = fixes.maxOf { it.latitude }
        val minLon = fixes.minOf { it.longitude }
        val maxLon = fixes.maxOf { it.longitude }
        val latSpan = (maxLat - minLat).takeIf { it > 1e-9 } ?: 1e-9
        val lonSpan = (maxLon - minLon).takeIf { it > 1e-9 } ?: 1e-9

        val padding = 24f
        fun project(lat: Double, lon: Double): Offset {
            val x = padding + ((lon - minLon) / lonSpan).toFloat() * (size.width - 2 * padding)
            // Latitude increases northward but canvas y increases downward -- flip.
            val y = padding + (1f - ((lat - minLat) / latSpan).toFloat()) * (size.height - 2 * padding)
            return Offset(x, y)
        }

        drawTrackLine(fixes, ::project, trackColor)

        flags.forEach { flag ->
            val lat = flag.latitude
            val lon = flag.longitude
            if (lat != null && lon != null) {
                drawCircle(color = flagColor, radius = 8f, center = project(lat, lon))
            }
        }

        fixes.getOrNull(selectedIndex)?.let { fix ->
            drawCircle(color = selectedColor, radius = 10f, center = project(fix.latitude, fix.longitude))
        }
    }
}

private fun DrawScope.drawTrackLine(
    fixes: List<FixEntity>,
    project: (Double, Double) -> Offset,
    color: Color,
) {
    for (i in 0 until fixes.size - 1) {
        val start = project(fixes[i].latitude, fixes[i].longitude)
        val end = project(fixes[i + 1].latitude, fixes[i + 1].longitude)
        drawLine(color = color, start = start, end = end, strokeWidth = 4f)
    }
}
