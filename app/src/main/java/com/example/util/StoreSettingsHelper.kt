package com.example.util

import android.content.Context
import android.content.SharedPreferences

object StoreSettingsHelper {

    private const val PREFS_NAME = "dabasaba_store_settings"

    // General Store Info
    private const val KEY_STORE_NAME = "key_store_name"
    private const val KEY_STORE_SLOGAN = "key_store_slogan"
    private const val KEY_STORE_ADDRESS = "key_store_address"
    private const val KEY_STORE_PHONE1 = "key_store_phone1"
    private const val KEY_STORE_PHONE2 = "key_store_phone2"
    private const val KEY_STORE_EMAIL = "key_store_email"

    // Fiscal & Tax Info
    private const val KEY_TAX_NIF = "key_tax_nif"
    private const val KEY_TAX_RCCM = "key_tax_rccm"
    private const val KEY_VAT_ENABLED = "key_vat_enabled"
    private const val KEY_VAT_RATE = "key_vat_rate" // e.g. 18.0
    private const val KEY_RETURN_POLICY = "key_return_policy"

    // POS Hardware
    private const val KEY_OPEN_DRAWER_ON_SALE = "key_open_drawer_on_sale"
    private const val KEY_SCANNER_BEEP = "key_scanner_beep"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getStoreName(context: Context): String =
        getPrefs(context).getString(KEY_STORE_NAME, "BOUTIQUE DABASABA") ?: "BOUTIQUE DABASABA"

    fun setStoreName(context: Context, name: String) =
        getPrefs(context).edit().putString(KEY_STORE_NAME, name).apply()

    fun getStoreSlogan(context: Context): String =
        getPrefs(context).getString(KEY_STORE_SLOGAN, "Commerce Général & Supermarché") ?: "Commerce Général & Supermarché"

    fun setStoreSlogan(context: Context, slogan: String) =
        getPrefs(context).edit().putString(KEY_STORE_SLOGAN, slogan).apply()

    fun getStoreAddress(context: Context): String =
        getPrefs(context).getString(KEY_STORE_ADDRESS, "Grand Marché & ACI 2000, Bamako") ?: "Grand Marché & ACI 2000, Bamako"

    fun setStoreAddress(context: Context, address: String) =
        getPrefs(context).edit().putString(KEY_STORE_ADDRESS, address).apply()

    fun getStorePhone1(context: Context): String =
        getPrefs(context).getString(KEY_STORE_PHONE1, "+223 76 00 00 00") ?: "+223 76 00 00 00"

    fun setStorePhone1(context: Context, phone: String) =
        getPrefs(context).edit().putString(KEY_STORE_PHONE1, phone).apply()

    fun getStorePhone2(context: Context): String =
        getPrefs(context).getString(KEY_STORE_PHONE2, "+223 66 00 00 00") ?: "+223 66 00 00 00"

    fun setStorePhone2(context: Context, phone: String) =
        getPrefs(context).edit().putString(KEY_STORE_PHONE2, phone).apply()

    fun getStoreEmail(context: Context): String =
        getPrefs(context).getString(KEY_STORE_EMAIL, "contact@dabasaba.com") ?: "contact@dabasaba.com"

    fun setStoreEmail(context: Context, email: String) =
        getPrefs(context).edit().putString(KEY_STORE_EMAIL, email).apply()

    // Fiscal
    fun getNifNumber(context: Context): String =
        getPrefs(context).getString(KEY_TAX_NIF, "085123456M") ?: "085123456M"

    fun setNifNumber(context: Context, nif: String) =
        getPrefs(context).edit().putString(KEY_TAX_NIF, nif).apply()

    fun getRccmNumber(context: Context): String =
        getPrefs(context).getString(KEY_TAX_RCCM, "MA.BKO.2024.B.1023") ?: "MA.BKO.2024.B.1023"

    fun setRccmNumber(context: Context, rccm: String) =
        getPrefs(context).edit().putString(KEY_TAX_RCCM, rccm).apply()

    fun isVatEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_VAT_ENABLED, true)

    fun setVatEnabled(context: Context, enabled: Boolean) =
        getPrefs(context).edit().putBoolean(KEY_VAT_ENABLED, enabled).apply()

    fun getVatRate(context: Context): Float =
        getPrefs(context).getFloat(KEY_VAT_RATE, 18.0f)

    fun setVatRate(context: Context, rate: Float) =
        getPrefs(context).edit().putFloat(KEY_VAT_RATE, rate).apply()

    fun getReturnPolicy(context: Context): String =
        getPrefs(context).getString(
            KEY_RETURN_POLICY,
            "Les marchandises vendues ne sont ni reprises ni échangées après 48h. Merci de votre fidélité !"
        ) ?: "Les marchandises vendues ne sont ni reprises ni échangées après 48h. Merci de votre fidélité !"

    fun setReturnPolicy(context: Context, policy: String) =
        getPrefs(context).edit().putString(KEY_RETURN_POLICY, policy).apply()

    // POS Hardware
    fun isOpenDrawerOnSaleEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_OPEN_DRAWER_ON_SALE, true)

    fun setOpenDrawerOnSaleEnabled(context: Context, enabled: Boolean) =
        getPrefs(context).edit().putBoolean(KEY_OPEN_DRAWER_ON_SALE, enabled).apply()

    fun isScannerBeepEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_SCANNER_BEEP, true)

    fun setScannerBeepEnabled(context: Context, enabled: Boolean) =
        getPrefs(context).edit().putBoolean(KEY_SCANNER_BEEP, enabled).apply()
}
