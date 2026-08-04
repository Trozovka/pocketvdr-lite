package com.trozovka.pocketvdr.core.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * Writes an exported file into the app's cache dir and hands the caller a share Intent via
 * FileProvider -- the app never assumes a specific destination (email, cloud, Files app "save
 * to device"), since the target audience (insurer, surveyor, another chart tool) varies per use.
 * The FileProvider authority is derived from the running app's own package name at call time,
 * so this same code works unmodified whether it's built into the Free or the Pro application.
 */
object ExportFileSharer {

    fun writeAndBuildShareIntent(context: Context, fileName: String, content: String, mimeType: String): Intent {
        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportsDir, fileName)
        file.writeText(content)

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(sendIntent, "Export voyage")
    }
}
