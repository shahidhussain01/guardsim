package com.guardsim.callscreen

import android.content.Context

/**
 * Simple local storage for the free tier: a manual blocklist plus one toggle
 * for silencing calls from numbers that aren't in the user's contacts.
 * No server, no account, no internet connection required.
 */
object BlocklistStore {
    private const val PREFS_NAME = "guardsim_prefs"
    private const val KEY_BLOCKED = "blocked_numbers"
    private const val KEY_SILENCE_UNKNOWN = "silence_unknown"

    fun getBlockedNumbers(context: Context): MutableSet<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return HashSet(prefs.getStringSet(KEY_BLOCKED, emptySet()) ?: emptySet())
    }

    fun addBlockedNumber(context: Context, number: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = getBlockedNumbers(context)
        current.add(normalize(number))
        prefs.edit().putStringSet(KEY_BLOCKED, current).apply()
    }

    fun removeBlockedNumber(context: Context, number: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = getBlockedNumbers(context)
        current.remove(number)
        prefs.edit().putStringSet(KEY_BLOCKED, current).apply()
    }

    fun isBlocked(context: Context, number: String): Boolean {
        return getBlockedNumbers(context).contains(normalize(number))
    }

    fun setSilenceUnknown(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SILENCE_UNKNOWN, enabled).apply()
    }

    fun isSilenceUnknownEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SILENCE_UNKNOWN, false)
    }

    fun normalize(number: String): String {
        return number.filter { it.isDigit() || it == '+' }
    }
}
