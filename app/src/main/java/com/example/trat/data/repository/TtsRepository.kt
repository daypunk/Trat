package com.example.trat.data.repository

import android.content.Context
import android.content.Intent
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
                logTtsEngineInfo()
                Log.d("TTS", "TTS 초기화 성공")
            } else {
                Log.e("TTS", "TTS 초기화 실패: status=$status")
            }
        }
    }
    
    private fun logTtsEngineInfo() {
        textToSpeech?.let { tts ->
            try {
                val defaultEngine = tts.defaultEngine
                Log.d("TTS", "사용 중인 TTS 엔진: $defaultEngine")
            } catch (e: Exception) {
                Log.e("TTS", "TTS 엔진 정보 확인 중 오류", e)
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
            
            @Suppress("OVERRIDE_DEPRECATION")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                Log.e("TTS", "TTS 오류 발생")
            }
        })
    }
    
    override fun isLanguageSupported(language: SupportedLanguage): Boolean {
        // TTS 엔진이 초기화되지 않았으면 false 반환
        val tts = textToSpeech ?: return false
        if (!_isInitialized.value) return false
        
        val locale = when (language.code) {
            "ko" -> Locale.KOREA
            "en" -> Locale.US
            "ja" -> Locale.JAPAN
            "zh" -> Locale.CHINA
            else -> Locale.US
        }
        
        return try {
            // 단순하게 isLanguageAvailable만 사용
            val result = tts.isLanguageAvailable(locale)
            when (result) {
                TextToSpeech.LANG_AVAILABLE,
                TextToSpeech.LANG_COUNTRY_AVAILABLE,
                TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> true
                else -> false
            }
        } catch (e: Exception) {
            Log.e("TTS", "언어 지원 확인 중 오류: ${language.displayName}", e)
            false
        }
    }
    
    override fun speak(text: String, language: SupportedLanguage) {
        val tts = textToSpeech ?: return
        if (!_isInitialized.value) return
        
        val locale = when (language.code) {
            "ko" -> Locale.KOREA
            "en" -> Locale.US
            "ja" -> Locale.JAPAN
            "zh" -> Locale.CHINA
            else -> Locale.US
        }
        
        try {
            val result = tts.setLanguage(locale)
            if (result == TextToSpeech.LANG_AVAILABLE ||
                result == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_${System.currentTimeMillis()}")
            } else {
                Log.w("TTS", "${language.displayName} 언어 설정 실패: $result")
            }
        } catch (e: Exception) {
            Log.e("TTS", "TTS 재생 오류: ${language.displayName}", e)
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
    
    // 언어팩 다운로드 인텐트 생성
    override fun createLanguagePackDownloadIntent(): Intent {
        return Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
    }
    
    // 언어팩 설치 후 TTS 새로고침
    override fun refreshLanguageSupport() {
        // 캐시 없이 단순하게 TTS만 재초기화
        if (textToSpeech == null || !_isInitialized.value) {
            initialize()
        }
    }
    
    // TTS 재초기화 메서드 추가
    override fun reinitialize() {
        Log.d("TTS", "TTS 재초기화 시작")
        shutdown()
        initialize()
    }
} 