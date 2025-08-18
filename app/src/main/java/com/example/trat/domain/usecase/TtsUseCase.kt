package com.example.trat.domain.usecase

import android.content.Intent
import com.example.trat.data.models.SupportedLanguage
import com.example.trat.domain.repository.TtsRepositoryInterface
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class TtsUseCase @Inject constructor(
    private val ttsRepository: TtsRepositoryInterface
) {
    val isInitialized: StateFlow<Boolean> = ttsRepository.isInitialized
    val isSpeaking: StateFlow<Boolean> = ttsRepository.isSpeaking
    
    fun initialize() {
        ttsRepository.initialize()
    }
    
    fun speak(text: String, language: SupportedLanguage) {
        if (ttsRepository.isInitialized.value) {
            ttsRepository.speak(text, language)
        }
    }
    
    fun isLanguageSupported(language: SupportedLanguage): Boolean {
        return ttsRepository.isLanguageSupported(language)
    }
    
    fun stop() {
        ttsRepository.stop()
    }
    
    fun shutdown() {
        ttsRepository.shutdown()
    }
    
    fun createLanguagePackDownloadIntent(): Intent {
        return ttsRepository.createLanguagePackDownloadIntent()
    }
    
    fun refreshLanguageSupport() {
        ttsRepository.refreshLanguageSupport()
    }
    
    fun reinitialize() {
        ttsRepository.reinitialize()
    }
} 