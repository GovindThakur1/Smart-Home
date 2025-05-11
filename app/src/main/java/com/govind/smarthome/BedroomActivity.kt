package com.govind.smarthome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Curtains
import androidx.compose.material.icons.filled.Light
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.WindPower
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.govind.smarthome.api.ApiManager
import com.govind.smarthome.ui.theme.SmartHomeTheme
import kotlinx.coroutines.launch

class BedroomActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartHomeTheme {
                BedroomScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BedroomScreen() {
    val apiManager = remember { ApiManager() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val lightStatus = remember { mutableStateOf("Unknown") }
    val fanStatus = remember { mutableStateOf("Unknown") }
    val curtainStatus = remember { mutableStateOf("Unknown") }
    val isFanRunning = remember { mutableStateOf("Unknown") }

    LaunchedEffect(Unit) {
        fetchStatuses(apiManager, lightStatus, fanStatus, curtainStatus, isFanRunning)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bedroom Control") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DeviceCard(
                deviceName = "Bedroom Light",
                status = lightStatus.value,
                icon = Icons.Default.Light,
                onToggle = { isOn ->
                    coroutineScope.launch {
                        if (isOn) apiManager.turnOnBedRoomLight() else apiManager.turnOffBedRoomLight()
                        lightStatus.value = apiManager.getBedRoomLightStatus() ?: "Unknown"
                    }
                }
            )
            DeviceCard(
                deviceName = "Bedroom Fan",
                status = fanStatus.value,
                icon = Icons.Default.WindPower,
                onToggle = { isOn ->
                    coroutineScope.launch {
                        if (isOn) apiManager.turnOnFan() else apiManager.turnOffFan()
                        fanStatus.value = apiManager.isFanTurnedOnByUser() ?: "Unknown"
                    }
                },
                isFanRunningStatus = isFanRunning.value
            )
            DeviceCard(
                deviceName = "Bedroom Curtain",
                status = curtainStatus.value,
                icon = Icons.Default.Curtains,
                onToggle = { isOn ->
                    coroutineScope.launch {
                        if (isOn) apiManager.openCurtain() else apiManager.closeCurtain()
                        curtainStatus.value = apiManager.getCurtainStatus() ?: "Unknown"
                    }
                }
            )
        }
    }
}

@Composable
fun DeviceCard(
    deviceName: String,
    status: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onToggle: (Boolean) -> Unit,
    isFanRunningStatus: String? = null,
) {
    val isOn = status.lowercase() == "on" || status.lowercase() == "open"

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isOn) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = deviceName,
                tint = if (isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = deviceName,
                style = MaterialTheme.typography.titleMedium
            )

            if (isFanRunningStatus.equals(
                    "on",
                    ignoreCase = true
                ) || isFanRunningStatus.equals("off", ignoreCase = true)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                if (isFanRunningStatus != null) {
                    Text(
                        text = "Status: ${
                            if(isFanRunningStatus.uppercase() == "ON") "Running" else "Not Running" 
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Status: ${status.uppercase()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            FilledTonalButton(
                onClick = { onToggle(!isOn) },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isOn) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isOn) Color.White else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isOn) "Turn Off" else "Turn On"
                )
            }
        }
    }
}


private suspend fun fetchStatuses(
    apiManager: ApiManager,
    lightStatus: MutableState<String>,
    fanStatus: MutableState<String>,
    curtainStatus: MutableState<String>,
    isFanRunning: MutableState<String>,
) {
    lightStatus.value = apiManager.getBedRoomLightStatus() ?: "Unknown"
    fanStatus.value = apiManager.isFanTurnedOnByUser() ?: "Unknown"
    curtainStatus.value = apiManager.getCurtainStatus() ?: "Unknown"
    isFanRunning.value = apiManager.getFanRunningStatus() ?: "Unknown"
}

@Composable
@Preview(showBackground = true)
fun BedroomScreenPreview() {
    SmartHomeTheme {
        BedroomScreen()
    }
}