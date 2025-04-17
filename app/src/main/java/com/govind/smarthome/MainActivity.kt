package com.govind.smarthome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.govind.smarthome.api.ApiManager
import com.govind.smarthome.ui.theme.SmartHomeTheme
import com.govind.smarthome.ui.theme.Typography
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartHomeTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    SmartHomeScreen()
                }
            }
        }
    }
}


@Composable
fun SmartHomeScreen() {
    val apiManager = remember { ApiManager() }
    val scope = rememberCoroutineScope()

    var sensorData by remember { mutableStateOf<Map<String, String>?>(null) }

    val job = remember { Job() }

    LaunchedEffect(job) {
        try {
            while (true) {
                val data = apiManager.fetchSensorData()
                Log.d("sensor data", "Fetched: $data")
                sensorData = data
                delay(2000)
            }
        } finally {
            job.cancel()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            job.cancel()
        }
    }

    val temperature = sensorData?.get("temperature") ?: "--"
    val humidity = sensorData?.get("humidity") ?: "--"

    val smoke = sensorData?.get("smoke")?.toFloatOrNull() ?: 0f
    val lpg = sensorData?.get("LPG")?.toFloatOrNull() ?: 0f
    val co = sensorData?.get("CO")?.toFloatOrNull() ?: 0f
    val co2 = sensorData?.get("CO2")?.toFloatOrNull() ?: 0f
    val nh3 = sensorData?.get("NH3")?.toFloatOrNull() ?: 0f

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        HeaderSection()
        Spacer(modifier = Modifier.height(16.dp))
        ClimateInfoSection(temperature = temperature, humidity = humidity)
        GasLevelsSection(
            smokeReading = smoke.toFloat(),
            LPGReading = lpg.toFloat(),
            COReading = co.toFloat(),
            CO2Reading = co2.toFloat(),
            NH3Reading = nh3.toFloat()
        )
        Spacer(modifier = Modifier.height(16.dp))
        RoomsSection()
        Spacer(modifier = Modifier.height(16.dp))
        DeviceControlSection()
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome home,",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "Govind Thakur",
                style = MaterialTheme.typography.titleLarge
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.Red, shape = CircleShape)
        ) {}
    }
}

@Composable
fun ClimateInfoSection(temperature: String, humidity: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Temperature Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.thermostat_icon),
                    contentDescription = "Temperature Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Column {
                    Text(
                        text = "$temperature°C",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Temperature",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Humidity Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.waterdrop_icon),
                    contentDescription = "Humidity Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Column {
                    Text(
                        text = "$humidity%",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Humidity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}


// Gas Level section for displaying different gas levels

@Composable
fun GasLevelsSection(
    smokeReading: Float,
    LPGReading: Float,
    COReading: Float,
    CO2Reading: Float,
    NH3Reading: Float
) {
    val averageAQI = (smokeReading + LPGReading + COReading + CO2Reading + NH3Reading) / 5
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                val intent = Intent(context, GasChartActivity::class.java).apply {
                    putExtra("smoke", smokeReading)
                    putExtra("lpg", LPGReading)
                    putExtra("co", COReading)
                    putExtra("co2", CO2Reading)
                    putExtra("nh3", NH3Reading)
                }
                context.startActivity(intent)
            },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // AQI
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Air Quality Index (AQI)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = String.format("%.2f", averageAQI),
                    style = MaterialTheme.typography.titleLarge,
                    color = getAQIColor(averageAQI)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // gas readings
            GasLevelItem("Smoke", smokeReading, R.drawable.smoke_icon)
            GasLevelItem("LPG", LPGReading, R.drawable.lpg_icon)
            GasLevelItem("CO", COReading, R.drawable.co_icon)
            GasLevelItem("CO2", CO2Reading, R.drawable.co2_icon)
            GasLevelItem("NH3", NH3Reading, R.drawable.nh3_icon)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Click to see chart",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

        }
    }
}


@Composable
fun getAQIColor(aqi: Float): Color {
    return when {
        aqi < 50 -> Color(0xFF4CAF50)
        aqi < 100 -> Color(0xFFFFC107)
        aqi < 150 -> Color(0xFFFF9800)
        aqi < 200 -> Color(0xFFF44336)
        else -> Color(0xFF9C27B0)
    }
}


@Composable
fun GasLevelItem(gasName: String, gasLevel: Float, iconRes: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Use the image resource (PNG)
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = gasName,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = gasName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = String.format("%.2f", gasLevel),
            style = Typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


@Composable
fun RoomsSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        RoomItem("Living Room")
        RoomItem("Bathroom", isSelected = true)
        RoomItem("Bedroom")
    }
}

@Composable
fun RoomItem(name: String, isSelected: Boolean = false) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .size(100.dp)
            .padding(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )
        }
    }
}


@Composable
fun DeviceControlSection(context: Context = LocalContext.current) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Motion Surveillance Control
        MotionSurveillanceSwitch()

        // Door Management Card
        DoorManagementCard {
            val intent = Intent(context, DoorActivity::class.java)
            context.startActivity(intent)
        }
    }
}

@Composable
fun MotionSurveillanceSwitch() {
    val apiManager = remember { ApiManager() }
    val scope = rememberCoroutineScope()

    var isEnabled by remember { mutableStateOf(false) }
    var hasFetchedStatus by remember { mutableStateOf(false) }

    // Fetch current surveillance status on first composition
    LaunchedEffect(Unit) {
        if (!hasFetchedStatus) {
            val status = apiManager.getSurveillanceStatus()
            isEnabled = status == "active"
            hasFetchedStatus = true
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Motion Surveillance",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Auto motion detection for intrusion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = { checked ->
                    isEnabled = checked
                    scope.launch {
                        val result = if (checked) {
                            apiManager.enableSurveillance()
                        } else {
                            apiManager.disableSurveillance()
                        }
                        Log.d("api call", "API call result: $result")
                    }
                },
            )
        }
    }
}


@Composable
fun DoorManagementCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Doors",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap to manage doors",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SmartHomePreview() {
    SmartHomeTheme {
        SmartHomeScreen()
    }
}
