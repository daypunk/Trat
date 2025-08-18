package com.example.trat.domain.repository

import android.content.Intent
import com.example.trat.data.models.SupportedLanguage
import kotlinx.coroutines.flow.StateFlow

interface TtsRepositoryInterface {
    val isInitialized: StateFlow<Boolean>
    val isSpeaking: StateFlow<Boolean>
    
    fun initialize()
    fun speak(text: String, language: SupportedLanguage)
    fun stop()
    fun shutdown()
    fun isLanguageSupported(language: SupportedLanguage): Boolean
    fun createLanguagePackDownloadIntent(): Intent
    fun refreshLanguageSupport()
    fun reinitialize()
}