package com.example.trat.data.repository

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.trat.data.models.SupportedLanguage
import com.example.trat.domain.repository.TtsRepositoryInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : TtsRepositoryInterface {
    
    private var textToSpeech: TextToSpeech? = null
    
    private val _isInitialized = MutableStateFlow(false)
    override val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    private val _isSpeaking = MutableStateFlow(false)
    override val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()
    
    override fun initialize() {
        if (textToSpeech != null) return
        
        textToSpeech = TextToSpeech(context) { status ->
            _isInitialized.value = (status == TextToSpeech.SUCCESS)
            if (status == TextToSpeech.SUCCESS) {
                setupUtteranceListener()
                Log.d("TTS", "TTS 초기화 성공")
            } else {
                Log.e("TTS", "TTS 초기화 실패")
            }
        }
    }
    
    private fun setupUtteranceListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }
            
            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }
            
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                Log.e("TTS", "TTS 오류 발생")
            }
        })
    }
    
    override fun speak(text: String, language: SupportedLanguage) {
        textToSpeech?.let { tts ->
            val locale = when (language.code) {
                "ko" -> Locale.KOREA
                "en" -> Locale.US
                "ja" -> Locale.JAPAN
                "zh" -> Locale.CHINA
                else -> Locale.US
            }
            
            val result = tts.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("TTS", "${language.displayName} 언어가 지원되지 않습니다")
                return
            }
            
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_${System.currentTimeMillis()}")
        }
    }
    
    override fun stop() {
        textToSpeech?.stop()
        _isSpeaking.value = false
    }
    
    override fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        _isInitialized.value = false
        _isSpeaking.value = false
    }
} 