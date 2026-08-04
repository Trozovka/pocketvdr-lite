package com.trozovka.pocketvdr.core.entitlement

/** Service locator installed by each :app's Application subclass at startup. */
object EntitlementHost {
    private var manager: EntitlementManager? = null

    fun install(entitlementManager: EntitlementManager) {
        manager = entitlementManager
    }

    fun current(): EntitlementManager =
        manager ?: error("EntitlementManager not installed -- call EntitlementHost.install() from Application.onCreate()")
}
