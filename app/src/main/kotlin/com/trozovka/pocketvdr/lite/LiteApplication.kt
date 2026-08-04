package com.trozovka.pocketvdr.lite

import android.app.Application

class LiteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Entitlement wiring (TrialEntitlementManager) lands in Milestone 5.
    }
}
