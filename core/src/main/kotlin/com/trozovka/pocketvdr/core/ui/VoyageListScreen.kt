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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.trozovka.pocketvdr.core.data.VoyageRepository
import com.trozovka.pocketvdr.core.util.formatDurationShort
import com.trozovka.pocketvdr.core.util.formatLocalTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoyageListScreen(onBack: () -> Unit, onVoyageSelected: (Long) -> Unit) {
    val context = LocalContext.current
    val repository = remember { VoyageRepository(context) }
    val voyages by repository.observeVoyages().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voyages") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (voyages.isEmpty()) {
            Text(
                "No voyages yet -- start logging from the main screen.",
                modifier = Modifier.padding(padding).padding(16.dp),
            )
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(voyages) { voyage ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { onVoyageSelected(voyage.id) },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(formatLocalTimestamp(voyage.startTimeMillis), style = MaterialTheme.typography.bodyMedium)
                                val endTime = voyage.endTimeMillis
                                Text(
                                    if (endTime != null) {
                                        "Duration: ${formatDurationShort(endTime - voyage.startTimeMillis)}"
                                    } else {
                                        "In progress"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
