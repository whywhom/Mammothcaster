package com.mammoth.podcast.util

import android.content.Context
import android.content.SharedPreferences
import java.time.Instant

class AppTimeChecker(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE)

    companion object {
        private const val APP_PREFS = "app_prefs"
        private const val LAST_START_TIME_KEY = "last_start_time"
        private const val MILLIS_IN_24_HOURS = 24 * 60 * 60 * 1000L
    }

    /**
     * Check if current time is 24 hours later after the last start time.
     * @return true if it has been more than 24 hours since the last start, false otherwise.
     */
    fun checkIfOverTime(): Boolean {
        val lastStartTime = sharedPreferences.getLong(LAST_START_TIME_KEY, 0L)
        val currentTime = Instant.now().toEpochMilli()

        return if (currentTime - lastStartTime > MILLIS_IN_24_HOURS) {
            saveCurrentTime() // Update last start time if over 24 hours
            true
        } else {
            false
        }
    }

    /**
     * Save the current time as the last start time in SharedPreferences.
     */
    private fun saveCurrentTime() {
        sharedPreferences.edit().putLong(LAST_START_TIME_KEY, Instant.now().toEpochMilli()).apply()
    }
}