package com.govind.smarthome.sessionmanager

import android.content.Context
import android.content.Intent
import com.govind.smarthome.AuthActivity

object SessionManager {
    private const val PREF_NAME = "user_session"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_EXPIRY_TIME = "expiry_time"
    private const val KEY_USERNAME = "username"
    private const val KEY_FULLNAME = "fullname"


    fun login(context: Context, username: String, storedFullName: String?) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val expiryTime = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putLong(KEY_EXPIRY_TIME, expiryTime)
            .putString(KEY_USERNAME, username)
            .putString(KEY_FULLNAME, storedFullName)
            .apply()
    }

    fun signup(context: Context, username: String, fullname: String? = null) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val expiryTime = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putLong(KEY_EXPIRY_TIME, expiryTime)
            .putString(KEY_USERNAME, username)
            .putString(KEY_FULLNAME, fullname)
            .apply()
    }

    fun logout(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        val intent = Intent(context, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val expiryTime = prefs.getLong(KEY_EXPIRY_TIME, 0)
        return isLoggedIn && System.currentTimeMillis() < expiryTime
    }

    fun getFullname(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_FULLNAME, null)
    }
}
