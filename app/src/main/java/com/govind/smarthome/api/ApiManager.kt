package com.govind.smarthome.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ApiManager {

    private val esp32Ip = "http://192.168.1.203"
//    private val esp32Ip = "http://192.168.152.233"
//    private val esp32Ip = "http://172.22.20.103"


    // Fetch sensor data from the ESP32
    suspend fun fetchSensorData(): Map<String, String>? {
        val response = makeApiCall("/sensorData")
        return if (response != null) parseSensorData(response) else null
    }

    // Open the door
    suspend fun openDoor(): String? {
        return makeApiCall("/door/open")
    }

    // Close the door
    suspend fun closeDoor(): String? {
        return makeApiCall("/door/close")
    }

    // Get the door status
    suspend fun getDoorStatus(): String? {
        return makeApiCall("/door/status")
    }

    // Make surveillance active
    suspend fun enableSurveillance(): String? {
        return makeApiCall("/surveillance/enable")
    }

    // Make surveillance inactive
    suspend fun disableSurveillance(): String? {
        return makeApiCall("/surveillance/disable")
    }

    // Get surveillance status if active or inactive
    suspend fun getSurveillanceStatus(): String? {
        return makeApiCall("/surveillance/status")
    }
    private suspend fun makeApiCall(endpoint: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$esp32Ip$endpoint")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val responseCode = connection.responseCode
                Log.d("ApiManager", "Response Code: $responseCode")

                if (responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("ApiManager", "Response: $response")
                    response
                } else {
                    val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e("ApiManager", "Failed: $responseCode Error: $error")
                    null
                }
            } catch (e: Exception) {
                Log.e("ApiManager", "Error: ${e.message}", e)
                null
            }
        }
    }


    // Helper function to parse sensor data
    private fun parseSensorData(response: String): Map<String, String>? {
        return try {
            val jsonObject = JSONObject(response)
            jsonObject.keys().asSequence().associateWith { jsonObject.getString(it) }
        } catch (e: Exception) {
            Log.e("ApiManager", "Error parsing JSON: ${e.message}")
            null
        }
    }
}
