package com.example.trat.data.repository

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.trat.domain.repository.SpeechToTextRepositoryInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class SpeechToTextRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SpeechToTextRepositoryInterface {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private val speechMutex = Mutex() // 동시 접근 제어를 위한 Mutex
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _isListening = MutableStateFlow(false)
    override val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    private val _recognizedText = MutableStateFlow("")
    override val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()
    
    override suspend fun startListening(languageCode: String) = speechMutex.withLock {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _error.value = "음성 인식을 사용할 수 없습니다"
            return@withLock
        }
        
        // 이미 인식 중이면 중복 실행 방지
        if (_isListening.value) {
            return@withLock
        }
        
        // 기존 인스턴스 완전 정리
        cleanup()
        
        // 약간의 딜레이 후 새 인스턴스 생성 (안드로이드 내부 정리 시간 확보)
        kotlinx.coroutines.delay(100)
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(recognitionListener)
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        
        _isListening.value = true
        _error.value = null
        _recognizedText.value = ""
        speechRecognizer?.startListening(intent)
    }
    
    override suspend fun stopListening() = speechMutex.withLock {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }
    
    override fun clearRecognizedText() {
        _recognizedText.value = ""
    }
    
    override fun clearError() {
        _error.value = null
    }
    
    private suspend fun cleanupWithLock() = speechMutex.withLock {
        cleanup()
    }
    
    private fun cleanup() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        _isListening.value = false
    }
    
    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _isListening.value = true
        }
        
        override fun onBeginningOfSpeech() {}
        
        override fun onRmsChanged(rmsdB: Float) {}
        
        override fun onBufferReceived(buffer: ByteArray?) {}
        
        override fun onEndOfSpeech() {
            _isListening.value = false
        }
        
        override fun onError(error: Int) {
            _isListening.value = false
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "오디오 오류"
                SpeechRecognizer.ERROR_CLIENT -> "클라이언트 오류"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "권한이 없습니다"
                SpeechRecognizer.ERROR_NETWORK -> "네트워크 오류"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 시간 초과"
                SpeechRecognizer.ERROR_NO_MATCH -> "음성을 인식할 수 없습니다"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "음성 인식기가 사용 중입니다"
                SpeechRecognizer.ERROR_SERVER -> "서버 오류"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "음성 입력 시간 초과"
                else -> "알 수 없는 오류"
            }
            _error.value = errorMessage
            // 에러 발생 후 리소스 정리
            repositoryScope.launch {
                cleanupWithLock()
            }
        }
        
        override fun onResults(results: Bundle?) {
            _isListening.value = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                _recognizedText.value = matches[0]
            }
            // 인식 완료 후 리소스 정리
            repositoryScope.launch {
                cleanupWithLock()
            }
        }
        
        override fun onPartialResults(partialResults: Bundle?) {
            // 실시간 결과 처리 (선택사항)
        }
        
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
} 