package com.github.nrfr.i18n

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

object LanguageManager {
    const val SYSTEM_LANGUAGE_TAG = ""

    private const val PREFERENCES_NAME = "language_preferences"
    private const val LANGUAGE_TAG_KEY = "language_tag"

    fun getLanguageTag(context: Context): String {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(LANGUAGE_TAG_KEY, SYSTEM_LANGUAGE_TAG)
            .orEmpty()
    }

    fun setLanguageTag(context: Context, languageTag: String) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(LANGUAGE_TAG_KEY, languageTag)
            .apply()
    }

    fun wrapContext(base: Context): Context {
        val languageTag = getLanguageTag(base)
        val systemLocale = base.resources.configuration.locales[0]

        if (languageTag.isEmpty()) {
            Locale.setDefault(systemLocale)
            return base
        }

        val locale = Locale.forLanguageTag(languageTag)
        Locale.setDefault(locale)

        val configuration = Configuration(base.resources.configuration).apply {
            setLocales(LocaleList(locale))
            setLayoutDirection(locale)
        }
        return base.createConfigurationContext(configuration)
    }
}
