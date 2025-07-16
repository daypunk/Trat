package com.example.trat.domain.usecase

import com.example.trat.data.models.SupportedLanguage
import com.example.trat.domain.repository.SpeechToTextRepositoryInterface
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class SpeechToTextUseCase @Inject constructor(
    private val speechToTextRepository: SpeechToTextRepositoryInterface
) {
    val isListening: StateFlow<Boolean> = speechToTextRepository.isListening
    val recognizedText: StateFlow<String> = speechToTextRepository.recognizedText
    val error: StateFlow<String?> = speechToTextRepository.error
    
    suspend fun startListening(language: SupportedLanguage) {
        val languageCode = mapToAndroidLanguageCode(language)
        speechToTextRepository.startListening(languageCode)
    }
    
    suspend fun stopListening() {
        speechToTextRepository.stopListening()
    }
    
    fun clearRecognizedText() {
        speechToTextRepository.clearRecognizedText()
    }
    
    fun clearError() {
        speechToTextRepository.clearError()
    }
    
    private fun mapToAndroidLanguageCode(language: SupportedLanguage): String {
        return when (language.code) {
            "ko" -> "ko-KR"
            "en" -> "en-US"
            "ja" -> "ja-JP"
            "zh" -> "zh-CN"
            else -> "ko-KR"
        }
    }
} 