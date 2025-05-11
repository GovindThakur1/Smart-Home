package com.govind.smarthome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Light
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.govind.smarthome.api.ApiManager
import com.govind.smarthome.ui.theme.SmartHomeTheme
import kotlinx.coroutines.launch

class LivingRoomActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartHomeTheme {
                LivingRoomScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivingRoomScreen() {
    val apiManager = remember { ApiManager() }
    val coroutineScope = rememberCoroutineScope()

    val lightStatus = remember { mutableStateOf("Unknown") }
    val motionDrivenStatus = remember { mutableStateOf("Unknown") }

    // Fetch statuses on screen load
    LaunchedEffect(Unit) {
        fetchLivingRoomStatuses(apiManager, lightStatus, motionDrivenStatus)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Living Room Control") },
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Light control card
            DeviceCard(
                deviceName = "Living Room Light",
                status = lightStatus.value,
                icon = Icons.Default.Light,
                onToggle = { isOn ->
                    coroutineScope.launch {
                        if (isOn) apiManager.turnOnLivingRoomLight()
                        else apiManager.turnOffLivingRoomLight()

                        // Always fetch real status from ESP
                        lightStatus.value = apiManager.getLivingRoomLightStatus() ?: "Unknown"
                    }
                }
            )

            // Motion driven card
            DeviceCard(
                deviceName = "Motion Driven Mode",
                status = motionDrivenStatus.value,
                icon = Icons.Default.Sensors,
                onToggle = { isOn ->
                    coroutineScope.launch {
                        if (isOn) apiManager.makeLivingRoomLightMotionDriven()
                        else apiManager.makeLivingRoomLightNotMotionDriven()

                        // Always fetch real motion-driven status from ESP
                        motionDrivenStatus.value =
                            apiManager.isLivingRoomLightMotionDriven() ?: "Unknown"
                    }
                },
                isFanRunningStatus = null
            )
        }
    }
}


private suspend fun fetchLivingRoomStatuses(
    apiManager: ApiManager,
    lightStatus: MutableState<String>,
    motionDrivenStatus: MutableState<String>,
) {
    lightStatus.value = apiManager.getLivingRoomLightStatus() ?: "Unknown"
    motionDrivenStatus.value = apiManager.isLivingRoomLightMotionDriven() ?: "Unknown"
}

@Preview(showBackground = true)
@Composable
fun LivingRoomScreenPreview() {
    SmartHomeTheme {
        LivingRoomScreen()
    }
}
