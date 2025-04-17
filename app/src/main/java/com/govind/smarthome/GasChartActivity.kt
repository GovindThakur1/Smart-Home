package com.govind.smarthome

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.govind.smarthome.database.FirebaseHelper
import com.govind.smarthome.ui.theme.SmartHomeTheme
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class GasChartActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sensorData = listOf(
            intent.getFloatExtra("smoke", 0f),
            intent.getFloatExtra("lpg", 0f),
            intent.getFloatExtra("co", 0f),
            intent.getFloatExtra("co2", 0f),
            intent.getFloatExtra("nh3", 0f)
        )

        Log.d("gas chart intent", "Received: $sensorData")

        setContent {
            SmartHomeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GasChartScreen(
                        modifier = Modifier.padding(innerPadding),
                        currentGasLevels = sensorData
                    )
                }
            }
        }
    }
}

fun convertEpochToHumanReadable(epochTime: Long): String {
    val date = Date(epochTime * 1000)
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return format.format(date)
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GasChartScreen(modifier: Modifier = Modifier, currentGasLevels: List<Float>) {

    val dateList = remember {
        (0..13).map {
            LocalDate.now().minusDays((13 - it + 1).toLong())
        }
    }
    Log.d("dateList", dateList.toString())

    var sensorData: Map<String, Any>? by remember { mutableStateOf(null) }
    var errorMessage: String? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch from Firebase
    LaunchedEffect(Unit) {
        FirebaseHelper.getSensorDataForLast14Days { data, error ->
            if (data != null) {
                sensorData = data
                Log.d("FirebaseData", "Fetched data: $data")
            } else {
                errorMessage = error
            }
            isLoading = false
        }
    }

    var selectedDayIndex by remember { mutableStateOf(13) }

    val parsedData = remember(sensorData) {
        dateList.map { date ->
            val dayKey = date.toString()
            val readings = sensorData?.get(dayKey) as? Map<*, *>
            val hourlyList = MutableList(24) { List(5) { 0f } }

            readings?.forEach { (epoch, raw) ->
                val timeInSec = epoch.toString().toLongOrNull() ?: return@forEach
                val hour = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(timeInSec),
                    ZoneId.of("Asia/Kathmandu")
                ).hour

                val dataMap = raw as? Map<*, *> ?: return@forEach

                val gasValues = listOf(
                    dataMap["smoke"]?.toString()?.toFloatOrNull() ?: 0f,
                    dataMap["LPG"]?.toString()?.toFloatOrNull() ?: 0f,
                    dataMap["CO"]?.toString()?.toFloatOrNull() ?: 0f,
                    dataMap["CO2"]?.toString()?.toFloatOrNull() ?: 0f,
                    dataMap["NH3"]?.toString()?.toFloatOrNull() ?: 0f
                )

                hourlyList[hour] = gasValues
            }

            date to hourlyList
        }
    }

    val selectedDayData = parsedData.getOrNull(selectedDayIndex)?.second ?: emptyList()
    val selectedDate = parsedData.getOrNull(selectedDayIndex)?.first ?: LocalDate.now()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Current Gas Levels - ${LocalDate.now().format(DateTimeFormatter.ISO_DATE)}",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        GasBarChart(currentGasLevels)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Hourly Concentration", style = MaterialTheme.typography.titleMedium)

        when {
            isLoading -> Text("Loading data...")
            errorMessage != null -> Text("Error: $errorMessage")
            selectedDayData.isEmpty() -> Text(
                "No data available for ${
                    selectedDate.format(
                        DateTimeFormatter.ISO_DATE
                    )
                }"
            )

            else -> EachDayHourlyLineChart(gasData = selectedDayData, key = selectedDate.toString())
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Previous Day",
                modifier = Modifier.clickable {
                    if (selectedDayIndex > 0) selectedDayIndex--
                }
            )
            Text(selectedDate.format(DateTimeFormatter.ISO_DATE))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Next Day",
                modifier = Modifier.clickable {
                    if (selectedDayIndex < parsedData.lastIndex) selectedDayIndex++
                }
            )
        }
    }
}


@Composable
fun EachDayHourlyLineChart(gasData: List<List<Float>>, key: String) {
    val gasLabels = listOf("Smoke", "LPG", "CO", "CO2", "NH3")
    val lineDataSets = ArrayList<ILineDataSet>()

    for (i in gasLabels.indices) {
        val entries = gasData.mapIndexed { hour, data ->
            Entry(hour.toFloat(), data[i])
        }

        val dataSet = LineDataSet(entries, gasLabels[i]).apply {
            color = ColorTemplate.COLORFUL_COLORS[i % ColorTemplate.COLORFUL_COLORS.size]
            setDrawCircles(false)
            lineWidth = 2f
            valueTextSize = 8f
        }

        lineDataSets.add(dataSet)
    }

    val hourLabels = (0..23).map { it.toString() }

    key(key) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            factory = { context ->
                LineChart(context).apply {
                    data = LineData(lineDataSets)
                    xAxis.valueFormatter = IndexAxisValueFormatter(hourLabels)
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.granularity = 1f
                    axisRight.isEnabled = false
                    description.isEnabled = false
                    legend.isEnabled = true
                    invalidate()
                }
            }
        )
    }
}


@Composable
fun GasBarChart(currentGasLevels: List<Float>) {
    val gasLabels = listOf("Smoke", "LPG", "CO", "CO2", "NH3")

    Log.d("sensor data bar", "Fetched: $currentGasLevels")

    if (currentGasLevels.isEmpty()) {
        Log.d("sensor data bar", "No data available")
        return
    }

    val entries = gasLabels.mapIndexed { index, label ->
        BarEntry(index.toFloat(), currentGasLevels.getOrElse(index) { 0f })
    }

    val dataSet = BarDataSet(entries, "Gas Levels").apply {
        colors = listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA)
        valueTextSize = 12f
    }

    val barData = BarData(dataSet)

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            BarChart(context).apply {
                data = barData
                description.isEnabled = false
                legend.isEnabled = true
                xAxis.valueFormatter = IndexAxisValueFormatter(gasLabels)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                axisLeft.setDrawGridLines(false)
                axisRight.isEnabled = false
                axisLeft.axisMinimum = 0f
                setFitBars(true)
                invalidate()
            }
        }
    )
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun GasChartActivityPreview() {
    val sensorData = listOf(
        0.38f,
        0.08f,
        0.04f,
        0.20f,
        0.41f
    )

    // Simulate passing this data to the GasChartScreen
    SmartHomeTheme {
        GasChartScreen(
            modifier = Modifier.fillMaxSize(),
            currentGasLevels = sensorData
        )
    }
}

