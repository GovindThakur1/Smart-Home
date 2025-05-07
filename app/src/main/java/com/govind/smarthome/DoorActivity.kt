package com.govind.smarthome

import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.govind.smarthome.api.ApiManager
import com.govind.smarthome.ui.theme.SmartHomeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
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
    var rfidList by remember { mutableStateOf(listOf<String>()) }
    var showRfidInput by remember { mutableStateOf(false) }
    var rfidName by remember { mutableStateOf("") }
    var rfidId by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Fetch door status on launch
    LaunchedEffect(Unit) {
        updateDoorStatus(apiManager) { doorStatus = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Door Control") })
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                DoorControlCard(
                    doorName = "Main Gate",
                    doorStatus = doorStatus,
                    onOpen = {
                        scope.launch {
                            val result = apiManager.openDoor()
                            doorStatus = result?.takeIf { it.equals("Open", true) }?.uppercase()
                                ?: "Failed to open door"
                        }
                    },
                    onClose = {
                        scope.launch {
                            val result = apiManager.closeDoor()
                            doorStatus = result?.takeIf { it.equals("Closed", true) }?.uppercase()
                                ?: "Failed to close door"
                        }
                    }
                )
            }

            item {
                RfidManagerCard(
                    rfidList = rfidList,
                    onFetchRfids = {
                        scope.launch {
                            val result = apiManager.getAllRfids()
                            if (result != null) {
                                rfidList = result.split("\n").filter { it.isNotBlank() }
                            }
                        }
                    },
                    onAddRfid = {
                        showRfidInput = true
                    }
                )
            }
        }


        if (showRfidInput) {
            AddRfidDialog(
                scope = scope,
                rfidName = rfidName,
                rfidId = rfidId,
                onNameChange = { rfidName = it },
                onScanClick = {
                    scope.launch {
                        while (true) {
                            val data = apiManager.fetchSensorData()
                            apiManager.rfidScanFlag();
                            val scannedRfid = data?.get("RFID") ?: "--"
                            Log.d("scan data", data.toString())
                            if (scannedRfid != "--") {
                                rfidId = scannedRfid
                                break
                            }
                            delay(1000)
                        }
                    }
                },
                onDismiss = {
                    showRfidInput = false
                    rfidName = ""
                    rfidId = ""
                },
                onConfirm = {
                    scope.launch {
                        try {
                            if (rfidName.isBlank() || rfidId == "--" || rfidId.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Please enter a name and scan RFID",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@launch
                            }

                            val response =
                                apiManager.addRfid(rfidName.trim().uppercase(), rfidId.trim())
                            Toast.makeText(context, response ?: "No response", Toast.LENGTH_SHORT)
                                .show()

                            showRfidInput = false
                            rfidName = ""
                            rfidId = ""
                        } catch (e: Exception) {
                            Log.e("AddRFID", "Error adding RFID", e)
                            Toast.makeText(
                                context,
                                "Failed to add RFID: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            )
        }
    }
}


// update door status
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


@Composable
fun RfidManagerCard(
    rfidList: List<String>,
    onFetchRfids: () -> Unit,
    onAddRfid: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "RFID Management",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.padding(4.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = onFetchRfids) {
                        Text("View All RFIDs")
                    }
                    Button(onClick = onAddRfid) {
                        Text("Add RFID")
                    }
                }
            }


            if (rfidList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    rfidList.forEach { line ->
                        val parts = line.split("#").map { it.trim() }
                        Log.d("rfidsss", parts.toString())
                        if (parts.size == 2) {
                            val owner = parts[0]
                            val rfid = parts[1]

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Owner:", style = MaterialTheme.typography.labelMedium)
                                    Text(owner, style = MaterialTheme.typography.bodyLarge)

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("RFID:", style = MaterialTheme.typography.labelMedium)
                                    Text(rfid, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}


@Composable
fun AddRfidDialog(
    scope: CoroutineScope,
    rfidName: String,
    rfidId: String,
    onNameChange: (String) -> Unit,
    onScanClick: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Add New RFID") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = rfidName,
                    onValueChange = onNameChange,
                    label = { Text("Owner Name") }
                )
                OutlinedTextField(
                    value = rfidId,
                    onValueChange = {},
                    label = { Text("Scanned RFID") },
                    readOnly = true
                )
                Button(onClick = onScanClick) {
                    Text(if (rfidId.isEmpty() || rfidId == "--") "Scan RFID" else "Scan Again")
                }
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun DoorActivityPreview() {
    SmartHomeTheme {
        DoorControlScreen()
    }
}