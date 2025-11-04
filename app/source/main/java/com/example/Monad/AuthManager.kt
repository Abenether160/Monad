package com.example.burglaralert

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object AuthManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_ACTIVATED = "activated"

    private fun getEncryptedPrefs(context: Context) =
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    fun isActivated(context: Context): Boolean {
        val prefs = getEncryptedPrefs(context)
        return prefs.getBoolean(KEY_ACTIVATED, false)
    }

    fun setActivated(context: Context, v: Boolean) {
        val prefs = getEncryptedPrefs(context)
        prefs.edit().putBoolean(KEY_ACTIVATED, v).apply()
    }
}