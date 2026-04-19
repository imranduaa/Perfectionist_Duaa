package com.example.theperfectionist

import android.content.Context

object TargetPrefs {
    private const val PREF_NAME = "posture_target_prefs"
    private const val KEY_TARGET_DAYS = "target_days"

    fun saveTargetDays(context: Context, days: Int) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_TARGET_DAYS, days)
            .apply()
    }

    fun getTargetDays(context: Context): Int {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_TARGET_DAYS, 14)
    }
}