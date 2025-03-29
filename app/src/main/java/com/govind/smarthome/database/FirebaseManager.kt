package com.govind.smarthome.database

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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
                Toast.makeText(context, "Sign-in failed: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
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



}
