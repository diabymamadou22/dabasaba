package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import java.util.Locale

object LicenseManager {

    private const val PREFS_NAME = "dabasaba_license_prefs"
    private const val KEY_LICENSE_KEY = "key_license_key"
    private const val KEY_IS_ACTIVATED = "key_is_activated"
    private const val KEY_PLAN_NAME = "key_plan_name"
    private const val KEY_ACTIVATION_DATE = "key_activation_date"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Unique hardware device fingerprint for commercial licensing
     */
    fun getDeviceId(): String {
        val model = Build.MODEL.replace(" ", "").uppercase(Locale.ROOT).take(4)
        val brand = Build.BRAND.replace(" ", "").uppercase(Locale.ROOT).take(3)
        val hash = (Build.FINGERPRINT.hashCode() and 0xFFFF).toString(16).uppercase(Locale.ROOT).padStart(4, '0')
        return "DABA-$brand-$model-$hash"
    }

    fun isActivated(context: Context): Boolean {
        // By default activated for smooth showcase / demo or user can activate with real keys
        val prefs = getPrefs(context)
        return prefs.getBoolean(KEY_IS_ACTIVATED, true)
    }

    fun getLicensePlan(context: Context): String {
        val prefs = getPrefs(context)
        return prefs.getString(KEY_PLAN_NAME, "Licence Commerciale Illimitée (Pro Mall & Retail)") ?: "Licence Commerciale Illimitée (Pro Mall & Retail)"
    }

    fun getLicenseKey(context: Context): String {
        val prefs = getPrefs(context)
        return prefs.getString(KEY_LICENSE_KEY, "DABA-PRO-2026-COMMERCIAL") ?: "DABA-PRO-2026-COMMERCIAL"
    }

    fun activateLicense(context: Context, key: String): Pair<Boolean, String> {
        val cleanKey = key.trim().uppercase(Locale.ROOT)
        if (cleanKey.isEmpty()) {
            return Pair(false, "Veuillez saisir une clé de licence valide.")
        }

        val isValid = cleanKey.startsWith("DABA-") || cleanKey.length >= 10
        if (isValid) {
            val plan = if (cleanKey.contains("MALL") || cleanKey.contains("PRO")) {
                "Licence Commerciale Pro (Centres Commerciaux & Supermarchés)"
            } else {
                "Licence Boutique Standard (Illimitée)"
            }
            getPrefs(context).edit()
                .putBoolean(KEY_IS_ACTIVATED, true)
                .putString(KEY_LICENSE_KEY, cleanKey)
                .putString(KEY_PLAN_NAME, plan)
                .putLong(KEY_ACTIVATION_DATE, System.currentTimeMillis())
                .apply()
            return Pair(true, "Licence activée avec succès pour ce terminal !")
        } else {
            return Pair(false, "Clé de licence invalide. Contactez le distributeur DabaSaba.")
        }
    }
}
