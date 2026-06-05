package com.aplicacionesmoviles.alamutt_running.features.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LanguageManager {

    fun setLanguage(
        context: Context,
        languageCode: String
    ) {

        val prefs = context.getSharedPreferences(
            "settings",
            Context.MODE_PRIVATE
        )

        prefs.edit()
            .putString("language", languageCode)
            .apply()

        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageCode)
        )

        val activity = context as? Activity ?: return

        activity.finish()

        activity.startActivity(
            Intent(
                activity,
                activity::class.java
            )
        )
    }
}

