package com.govind.smarthome.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class ApiManager {

    private val esp32Ip = "http://192.168.1.100"
//    private val esp32Ip = "http://192.168.152.233"
//    private val esp32Ip = "http://172.22.20.103"


    // fetch all sensor data
    suspend fun fetchSensorData(): Map<String, String>? {
        val response = makeApiCall("/sensorData")
        return if (response != null) parseSensorData(response) else null
    }

    // open door
    suspend fun openDoor(): String? {
        return makeApiCall("/door/open")
    }

    // close door
    suspend fun closeDoor(): String? {
        return makeApiCall("/door/close")
    }

    // get door status
    suspend fun getDoorStatus(): String? {
        return makeApiCall("/door/status")
    }

    // activate surveillance
    suspend fun enableSurveillance(): String? {
        return makeApiCall("/surveillance/enable")
    }

    // inactivate surveillance
    suspend fun disableSurveillance(): String? {
        return makeApiCall("/surveillance/disable")
    }

    // get surveillance status
    suspend fun getSurveillanceStatus(): String? {
        return makeApiCall("/surveillance/status")
    }

    // get all rfid key value
    suspend fun getAllRfids(): String? {
        return makeApiCall("/all-rfids")
    }

    // add thr user and rfid id
    suspend fun addRfid(owner: String, rfid: String): String? = withContext(Dispatchers.IO) {
        val url = URL("$esp32Ip/add-rfid")
        val postData = "owner=${URLEncoder.encode(owner, "UTF-8")}&rfid=${URLEncoder.encode(rfid, "UTF-8")}"

        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            outputStream.write(postData.toByteArray())
        }

        connection.inputStream.bufferedReader().use { it.readText() }
    }


    // get the scanned rfid from esp
    suspend fun rfidScanFlag(): String? {
        return makeApiCall("/rfid/scan-flag")
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


    // parse sensor data if json
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
