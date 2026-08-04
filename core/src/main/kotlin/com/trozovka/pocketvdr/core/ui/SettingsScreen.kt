package com.trozovka.pocketvdr.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.trozovka.pocketvdr.core.entitlement.EntitlementHost
import com.trozovka.pocketvdr.core.settings.AppPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val preferences = remember { AppPreferences(context) }
    var intervalSeconds by remember { mutableIntStateOf(preferences.logIntervalSeconds) }
    var statusMessage by remember { mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        statusMessage = EntitlementHost.current().statusMessage(System.currentTimeMillis())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Surface(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(statusMessage, style = MaterialTheme.typography.bodyMedium)

                Text(
                    "Log interval: ${intervalSeconds}s",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 24.dp),
                )
                Slider(
                    value = intervalSeconds.toFloat(),
                    onValueChange = {
                        intervalSeconds = it.toInt()
                        preferences.logIntervalSeconds = intervalSeconds
                    },
                    valueRange = AppPreferences.MIN_LOG_INTERVAL_SECONDS.toFloat()..AppPreferences.MAX_LOG_INTERVAL_SECONDS.toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "Takes effect on the next voyage you start.",
                    style = MaterialTheme.typography.bodySmall,
                )

                EntitlementHost.current().SettingsExtras()
            }
        }
    }
}
