package com.example.theperfectionist

import android.content.Context

class PasswordManager(context: Context) {

    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun savePassword(password: String) {
        prefs.edit().putString("user_password", password).apply()
    }

    fun getPassword(): String? {
        return prefs.getString("user_password", null)
    }

    fun hasPassword(): Boolean {
        return !getPassword().isNullOrEmpty()
    }
}