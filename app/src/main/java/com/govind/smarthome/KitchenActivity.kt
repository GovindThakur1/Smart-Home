package com.govind.smarthome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Light
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.govind.smarthome.api.ApiManager
import com.govind.smarthome.ui.theme.SmartHomeTheme
import kotlinx.coroutines.launch

class KitchenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartHomeTheme {
                KitchenScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenScreen() {
    val apiManager = remember { ApiManager() }
    val kitchenLightStatus = remember { mutableStateOf("Unknown") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        fetchKitchenLightStatus(apiManager, kitchenLightStatus)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kitchen Control") },
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
            DeviceCard(
                deviceName = "Kitchen Light",
                status = kitchenLightStatus.value,
                icon = Icons.Default.Light,
                onToggle = { isOn ->
                    coroutineScope.launch {
                        if (isOn) apiManager.turnOnKitchenLight() else apiManager.turnOffKitchenLight()
                        kitchenLightStatus.value = apiManager.getKitchenLightStatus() ?: "Unknown"
                    }
                }
            )
        }
    }
}

private suspend fun fetchKitchenLightStatus(
    apiManager: ApiManager,
    kitchenLightStatus: MutableState<String>
) {
    kitchenLightStatus.value = apiManager.getKitchenLightStatus() ?: "Unknown"
}

@Preview(showBackground = true)
@Composable
fun KitchenScreenPreview() {
    SmartHomeTheme {
        KitchenScreen()
    }
}
