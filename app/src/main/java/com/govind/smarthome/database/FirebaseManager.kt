package com.govind.smarthome.database

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate

object FirebaseHelper {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    // Hardcoded email and password
    private val email = "govindt7256@gmail.com"
    private val password = "firebase123"

    fun signIn(context: Context) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Sign-in successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    context,
                    "Sign-in failed: ${it.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun signOut() {
        auth.signOut()
    }

    fun getSensorDataForSpecificTimestamp(
        year: String,
        month: String,
        day: String,
        timestamp: String,
        onResult: (Map<String, Any>?, String?) -> Unit
    ) {
        val path = "Data/reading/$year/$month/$day/$timestamp"
        database.child(path).get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val sensorData = dataSnapshot.value as? Map<String, Any>
                    onResult(sensorData, null)
                } else {
                    onResult(null, "No data found for $year/$month/$day at timestamp $timestamp")
                }
            }
            .addOnFailureListener {
                onResult(null, it.message)
            }
    }


    fun getSensorDataForSpecificMonth(
        year: String,
        month: String,
        onResult: (Map<String, Any>?, String?) -> Unit
    ) {
        val path = "Data/reading/$year/$month"
        database.child(path).get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val sensorData = dataSnapshot.value as? Map<String, Any>
                    onResult(sensorData, null)
                } else {
                    onResult(null, "No data found for $year/$month")
                }
            }
            .addOnFailureListener {
                onResult(null, it.message)
            }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getSensorDataForLast14Days(
        onResult: (Map<String, Map<String, Any>>?, String?) -> Unit
    ) {
        val currentDate = LocalDate.now(java.time.ZoneId.of("Asia/Kathmandu"))

        // Get last 14 days as LocalDate list
        val last14Days = (1..14).map {
            currentDate.minusDays(it.toLong())
        }

        val allSensorData = mutableMapOf<String, Map<String, Any>>()
        var completedRequests = 0

        last14Days.forEach { date ->
            val year = date.year.toString()
            val month = date.monthValue.toString()
            val day = date.dayOfMonth.toString()

            val path = "Data/reading/$year/$month/$day"
            Log.d("FirebasePath", "Fetching path: $path")

            database.child(path).get()
                .addOnSuccessListener { dataSnapshot ->
                    if (dataSnapshot.exists()) {
                        val sensorData = dataSnapshot.value as? Map<String, Any>
                        sensorData?.let {
                            allSensorData[date.toString()] = it
                        }
                    }
                    completedRequests++
                    if (completedRequests == last14Days.size) {
                        onResult(allSensorData, null)
                    }
                }
                .addOnFailureListener {
                    onResult(null, it.message)
                }
        }
    }

}
