package com.govind.smarthome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.govind.smarthome.api.ApiManager
import com.govind.smarthome.ui.theme.SmartHomeTheme
import kotlinx.coroutines.launch

class DoorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartHomeTheme {
                DoorControlScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoorControlScreen() {
    val scope = rememberCoroutineScope()
    val apiManager = remember { ApiManager() }

    var doorStatus by remember { mutableStateOf("Loading...") }

    // Fetch door status on launch
    LaunchedEffect(Unit) {
        updateDoorStatus(apiManager) { doorStatus = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Door Control") }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(listOf("Main Gate")) { doorName ->
                DoorControlCard(
                    doorName = doorName,
                    doorStatus = doorStatus,
                    onOpen = {
                        scope.launch {
                            val result = apiManager.openDoor()
                            doorStatus = result
                                ?.takeIf { it.equals("Open", true) }
                                ?.uppercase()
                                ?: "Failed to open door"
                        }
                    },
                    onClose = {
                        scope.launch {
                            val result = apiManager.closeDoor()
                            doorStatus = result
                                ?.takeIf { it.equals("Closed", true) }
                                ?.uppercase()
                                ?: "Failed to close door"
                        }
                    }
                )
            }
        }
    }
}


// Reusable function to update status
suspend fun updateDoorStatus(apiManager: ApiManager, onStatus: (String) -> Unit) {
    val status = apiManager.getDoorStatus()
    if (status != null && (status.equals("Open", true) || status.equals("Closed", true))) {
        onStatus(status.uppercase())
    } else {
        onStatus("Failed to fetch status")
    }
}


@Composable
fun DoorControlCard(
    doorName: String,
    doorStatus: String,
    onOpen: () -> Unit,
    onClose: () -> Unit
) {
    val statusColor =
        if (doorStatus == "Open") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val backgroundColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doorName,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )
                Text(
                    text = "Status: $doorStatus",
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusColor
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Open", color = MaterialTheme.colorScheme.onPrimary)
                }

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Close", color = MaterialTheme.colorScheme.onError)
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DoorActivityPreview() {
    SmartHomeTheme {
        DoorControlScreen()
    }
}