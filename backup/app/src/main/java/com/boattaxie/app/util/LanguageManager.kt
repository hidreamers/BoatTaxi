package com.boattaxie.app.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageManager {
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "selected_language"
    
    // Available languages
    val languages = listOf(
        Language("en", "English", "🇺🇸"),
        Language("es", "Español", "🇪🇸")
    )
    
    data class Language(
        val code: String,
        val name: String,
        val flag: String
    )
    
    fun getCurrentLanguage(context: Context): Language {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        return languages.find { it.code == code } ?: languages[0]
    }
    
    fun setLanguage(context: Context, languageCode: String) {
        // Save preference
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
        
        // Apply locale change
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        
        // Restart activity to apply changes
        if (context is Activity) {
            context.recreate()
        }
    }
    
    fun toggleLanguage(context: Context): Language {
        val current = getCurrentLanguage(context)
        val currentIndex = languages.indexOfFirst { it.code == current.code }
        val nextIndex = (currentIndex + 1) % languages.size
        val nextLanguage = languages[nextIndex]
        setLanguage(context, nextLanguage.code)
        return nextLanguage
    }
    
    fun getNextLanguage(context: Context): Language {
        val current = getCurrentLanguage(context)
        val currentIndex = languages.indexOfFirst { it.code == current.code }
        val nextIndex = (currentIndex + 1) % languages.size
        return languages[nextIndex]
    }
    
    // Apply saved language on app start
    fun applyLanguage(context: Context) {
        val current = getCurrentLanguage(context)
        val locale = Locale(current.code)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}
