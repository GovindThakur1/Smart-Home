package com.govind.smarthome

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.govind.smarthome.database.FirebaseHelper
import com.govind.smarthome.ui.theme.SmartHomeTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartHomeTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                    FirebaseHelper.signIn(this);
                    Toast.makeText(
                        this,
                        "Username is: " + FirebaseHelper.getCurrentUser(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}


@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val year = "2025"
    val month = "3"

    var sensorData: Map<String, Any>? by remember { mutableStateOf(null) }
    var errorMessage: String? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        FirebaseHelper.getSensorDataForSpecificMonth(year, month) { data, error ->
            if (data != null) {
                sensorData = data
            } else {
                errorMessage = error
            }
        }
    }


    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Smart Home Data", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(text = "Error: $errorMessage", fontSize = 18.sp, color = Color.Red)
        } else {


            if (sensorData != null) {
                for ((day, timestamp) in sensorData!!) {
                    for ((epochTime, readings) in timestamp as Map<*, *>) {
                        val epoc = epochTime.toString().toLong()
                        val humidity = (readings as Map<*, *>)["humidity"].toString()
                        Log.d(
                            "SensorData",
                            "Epoch: $epochTime, Time: ${convertEpochToHumanReadable(epoc)}, Humidity: $humidity"
                        )
                    }
                }
            } else {
                Text(text = "Loading data...", fontSize = 18.sp)
            }
        }
    }
}


fun convertEpochToHumanReadable(epochTime: Long): String {
    // Create a Date object from the epoch time
    val date = Date(epochTime * 1000)  // Multiply by 1000 to convert seconds to milliseconds

    // Define the desired date format
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // Return the formatted date as a string
    return format.format(date)
}

