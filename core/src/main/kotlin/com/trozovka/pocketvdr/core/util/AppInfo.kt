package com.trozovka.pocketvdr.core.util

import android.content.Context
import android.content.pm.PackageManager

/** Reads the running app's own version dynamically via PackageManager -- never hardcoded, so it
 * can't drift out of sync with the actual build (Free and Pro have their own separate versions). */
fun appVersionName(context: Context): String =
    try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
    } catch (e: PackageManager.NameNotFoundException) {
        "?"
    }

/** The actual OS-visible app label (e.g. "PocketVDR Free" or "PocketVDR Pro"), read dynamically
 * rather than assuming which tier is running -- :core has no compile-time knowledge of that. */
fun appDisplayName(context: Context): String =
    context.applicationInfo.loadLabel(context.packageManager).toString()
