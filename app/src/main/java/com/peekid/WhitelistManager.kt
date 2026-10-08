package com.peekid

import android.content.Context
import android.content.SharedPreferences

object WhitelistManager {

    private const val PREFS_NAME = "peekid_prefs"
    private const val KEY_WHITELIST = "whitelisted_contacts"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getWhitelistedContacts(context: Context): List<String> {
        val set = getPrefs(context).getStringSet(KEY_WHITELIST, emptySet()) ?: emptySet()
        return set.toList().sorted()
    }

    fun addContact(context: Context, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val current = getPrefs(context).getStringSet(KEY_WHITELIST, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(trimmed)
        getPrefs(context).edit().putStringSet(KEY_WHITELIST, current).apply()
    }

    fun removeContact(context: Context, name: String) {
        val current = getPrefs(context).getStringSet(KEY_WHITELIST, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.remove(name)
        getPrefs(context).edit().putStringSet(KEY_WHITELIST, current).apply()
    }

    fun isWhitelisted(context: Context, name: String): Boolean {
        val set = getPrefs(context).getStringSet(KEY_WHITELIST, emptySet()) ?: emptySet()
        return set.any { it.equals(name.trim(), ignoreCase = true) }
    }
}
