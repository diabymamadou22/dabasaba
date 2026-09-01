package com.example.util

import android.content.Context
import android.content.SharedPreferences

enum class UserRole(val label: String, val badgeTitle: String) {
    GERANT("Gérant (Propriétaire)", "👑 Gérant"),
    CAISSIER("Caissier (Vendeur)", "👤 Caissier")
}

object UserManager {
    private const val PREFS_NAME = "dabasaba_user_prefs"
    private const val KEY_ROLE = "key_user_role"
    private const val KEY_USER_NAME = "key_user_name"
    private const val KEY_PIN = "key_manager_pin"
    private const val DEFAULT_PIN = "1234"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getCurrentRole(context: Context): UserRole {
        val roleStr = getPrefs(context).getString(KEY_ROLE, UserRole.GERANT.name) ?: UserRole.GERANT.name
        return try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            UserRole.GERANT
        }
    }

    fun isManager(context: Context): Boolean {
        return getCurrentRole(context) == UserRole.GERANT
    }

    fun getCurrentUserName(context: Context): String {
        val defaultName = if (isManager(context)) "Mamadou (Gérant)" else "Awa (Caissière)"
        return getPrefs(context).getString(KEY_USER_NAME, defaultName) ?: defaultName
    }

    fun setCurrentUser(context: Context, role: UserRole, name: String) {
        getPrefs(context).edit()
            .putString(KEY_ROLE, role.name)
            .putString(KEY_USER_NAME, name)
            .apply()
    }

    fun verifyManagerPin(context: Context, pin: String): Boolean {
        val savedPin = getPrefs(context).getString(KEY_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
        return pin.trim() == savedPin.trim()
    }

    fun setManagerPin(context: Context, newPin: String): Boolean {
        if (newPin.length < 4) return false
        getPrefs(context).edit().putString(KEY_PIN, newPin.trim()).apply()
        return true
    }
}
