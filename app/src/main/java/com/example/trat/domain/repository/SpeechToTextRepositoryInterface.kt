package com.example.trat.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface SpeechToTextRepositoryInterface {
    val isListening: StateFlow<Boolean>
    val recognizedText: StateFlow<String>
    val error: StateFlow<String?>
    
    suspend fun startListening(languageCode: String)
    suspend fun stopListening()
    fun clearRecognizedText()
    fun clearError()
} 