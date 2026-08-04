package com.trozovka.pocketvdr.lite

import android.app.Application
import com.trozovka.pocketvdr.core.entitlement.EntitlementHost
import com.trozovka.pocketvdr.core.entitlement.FreeEntitlementManager

class LiteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        EntitlementHost.install(FreeEntitlementManager(this))
    }
}
