package com.example.nutritionmanager.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("NutriTrackPrefs", Context.MODE_PRIVATE)

    fun saveUser(userId: Int, username: String, fullName: String) {
        prefs.edit().apply {
            putBoolean("isLoggedIn", true)
            putInt("userId", userId)
            putString("username", username)
            putString("fullName", fullName)
            apply()
        }
    }

    fun getUserId(): Int {
        return prefs.getInt("userId", -1)
    }

    fun getUsername(): String {
        return prefs.getString("username", "") ?: ""
    }

    fun getFullName(): String {
        return prefs.getString("fullName", "") ?: ""
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("isLoggedIn", false)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun logout() {
        clearSession()
    }
}