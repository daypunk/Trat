package com.example.trat.data.repository

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.trat.domain.repository.SpeechToTextRepositoryInterface
import com.example.trat.utils.NetworkUtils
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
    private val speechMutex = Mutex()
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // 🎯 심플한 상태 관리
    private var isActive = false
    private var timeoutJob: kotlinx.coroutines.Job? = null
    private var silenceTimeoutJob: kotlinx.coroutines.Job? = null
    
    private val _isListening = MutableStateFlow(false)
    override val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    private val _recognizedText = MutableStateFlow("")
    override val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()
    
    override suspend fun startListening(languageCode: String) = speechMutex.withLock {
        android.util.Log.d("STT_DEBUG", "🟢 Repository.startListening 호출됨 - isActive: $isActive")
        // 🚫 이미 활성화된 상태면 무시
        if (isActive) {
            android.util.Log.d("STT_DEBUG", "🚫 이미 활성화 상태 - 무시")
            return@withLock
        }
        
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            android.util.Log.e("STT_DEBUG", "❌ 음성 인식 사용 불가 (에뮬레이터에서는 지원되지 않음)")
            _error.value = "음성 인식을 사용할 수 없습니다 (실제 기기에서 테스트해주세요)"
            return@withLock
        }
        
        // 🌐 네트워크 연결 상태 확인 (Google STT는 온라인 서비스)
        if (!NetworkUtils.isNetworkAvailable(context)) {
            android.util.Log.e("STT_DEBUG", "❌ 오프라인 상태 - 음성 인식 불가")
            _error.value = "오프라인에서는 음성인식을 할 수 없습니다"
            return@withLock
        }
        
        // 🎯 상태 활성화
        android.util.Log.d("STT_DEBUG", "🎯 상태 활성화 시작")
        isActive = true
        _isListening.value = true
        _error.value = null
        _recognizedText.value = ""
        
        // 6초 타임아웃 시작
        startNoInputTimeout()
        
        // 음성 인식 시작
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(recognitionListener)
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        
        android.util.Log.d("STT_DEBUG", "🎙️ SpeechRecognizer.startListening 호출")
        speechRecognizer?.startListening(intent)
        android.util.Log.d("STT_DEBUG", "✅ Repository.startListening 완료")
    }
    
    override suspend fun stopListening() = speechMutex.withLock {
        android.util.Log.d("STT_DEBUG", "🔴 Repository.stopListening 호출됨 - isActive: $isActive")
        // 🚫 이미 비활성화된 상태면 무시
        if (!isActive) {
            android.util.Log.d("STT_DEBUG", "🚫 이미 비활성화 상태 - 무시")
            return@withLock
        }
        
        // 🎯 즉시 비활성화 (콜백 차단)
        android.util.Log.d("STT_DEBUG", "🎯 즉시 비활성화 시작")
        isActive = false
        _isListening.value = false
        
        // 모든 작업 정리
        android.util.Log.d("STT_DEBUG", "🧹 타임아웃 작업 정리")
        timeoutJob?.cancel()
        silenceTimeoutJob?.cancel()
        
        // 강제 중지 및 정리
        android.util.Log.d("STT_DEBUG", "🛑 SpeechRecognizer 중지 및 정리")
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        
        // 상태 리셋
        timeoutJob = null
        silenceTimeoutJob = null
        android.util.Log.d("STT_DEBUG", "✅ Repository.stopListening 완료")
    }
    
    override fun clearRecognizedText() {
        _recognizedText.value = ""
    }
    
    override fun clearError() {
        _error.value = null
    }
    
    // 4초 무입력 타임아웃
    private fun startNoInputTimeout() {
        timeoutJob?.cancel()
        android.util.Log.d("STT_DEBUG", "⏰ 4초 무입력 타임아웃 시작 (음성 없으면 자동 종료)")
        timeoutJob = repositoryScope.launch {
            kotlinx.coroutines.delay(4000) // 4초로 단축
            android.util.Log.d("STT_DEBUG", "⏰ 4초 무입력 타임아웃 발생 - 자동 종료")
            stopListening()
        }
    }
    
    // 1초 침묵 타임아웃 (음성 입력 후, 기존 2초에서 단축)
    private fun startSilenceTimeout() {
        silenceTimeoutJob?.cancel()
        android.util.Log.d("STT_DEBUG", "🤫 1초 침묵 타임아웃 시작 (말 끝나면 1초 후 자동 종료)")
        silenceTimeoutJob = repositoryScope.launch {
            kotlinx.coroutines.delay(1000) // 1초로 단축
            android.util.Log.d("STT_DEBUG", "🤫 1초 침묵 타임아웃 발생 - 자동 종료")
            stopListening()
        }
    }
    
    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            android.util.Log.d("STT_DEBUG", "🎤 onReadyForSpeech - isActive: $isActive")
            // 이미 활성화된 상태이므로 아무것도 하지 않음
        }
        
        override fun onBeginningOfSpeech() {
            android.util.Log.d("STT_DEBUG", "🗣️ onBeginningOfSpeech - isActive: $isActive")
            if (!isActive) {
                android.util.Log.d("STT_DEBUG", "🛡️ 비활성화 상태 - onBeginningOfSpeech 무시")
                return // 🛡️ 비활성화된 상태면 무시
            }
            
            // 음성 입력 시작 - 6초 타임아웃 취소
            android.util.Log.d("STT_DEBUG", "🗣️ 음성 감지됨 - 6초 무입력 타임아웃 취소 (정상)")
            timeoutJob?.cancel()
        }
        
        override fun onRmsChanged(rmsdB: Float) {}
        
        override fun onBufferReceived(buffer: ByteArray?) {}
        
        override fun onEndOfSpeech() {
            android.util.Log.d("STT_DEBUG", "🤐 onEndOfSpeech - isActive: $isActive")
            if (!isActive) {
                android.util.Log.d("STT_DEBUG", "🛡️ 비활성화 상태 - onEndOfSpeech 무시")
                return // 🛡️ 비활성화된 상태면 무시
            }
            
            // 음성 입력 끝 - 2초 침묵 타임아웃 시작
            android.util.Log.d("STT_DEBUG", "🤐 음성 입력 끝 감지 - 2초 침묵 타임아웃 전환")
            startSilenceTimeout()
        }
        
        override fun onError(error: Int) {
            android.util.Log.d("STT_DEBUG", "❌ onError 호출됨 - error: $error, isActive: $isActive")
            if (!isActive) {
                android.util.Log.d("STT_DEBUG", "🛡️ 비활성화 상태 - onError 무시")
                return // 🛡️ 비활성화된 상태면 무시
            }
            
            // ERROR_NO_MATCH는 정상 상황 (에러 아님)
            if (error != SpeechRecognizer.ERROR_NO_MATCH) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "오디오 오류"
                    SpeechRecognizer.ERROR_CLIENT -> "클라이언트 오류"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "권한이 없습니다"
                    SpeechRecognizer.ERROR_NETWORK -> "네트워크 오류"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 시간 초과"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "음성 인식기가 사용 중입니다"
                    SpeechRecognizer.ERROR_SERVER -> "서버 오류"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "음성 입력 시간 초과"
                    else -> "알 수 없는 오류"
                }
                android.util.Log.e("STT_DEBUG", "💥 에러 메시지 설정: $errorMessage")
                _error.value = errorMessage
            } else {
                android.util.Log.d("STT_DEBUG", "🔕 ERROR_NO_MATCH - 정상 상황")
            }
            
            // 에러 시 자동 중지
            android.util.Log.d("STT_DEBUG", "🔄 에러로 인한 자동 중지 시작")
            repositoryScope.launch { stopListening() }
        }
        
        override fun onResults(results: Bundle?) {
            android.util.Log.d("STT_DEBUG", "📝 onResults 호출됨 - isActive: $isActive")
            if (!isActive) {
                android.util.Log.d("STT_DEBUG", "🛡️ 비활성화 상태 - onResults 무시")
                return // 🛡️ 비활성화된 상태면 무시
            }
            
            // 인식 결과 처리
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                android.util.Log.d("STT_DEBUG", "✅ 인식 결과: ${matches[0]}")
                _recognizedText.value = matches[0]
            } else {
                android.util.Log.d("STT_DEBUG", "🔍 인식 결과 없음")
            }
            
            // 결과 처리 후 자동 중지
            android.util.Log.d("STT_DEBUG", "🔄 결과 처리 후 자동 중지 시작")
            repositoryScope.launch { stopListening() }
        }
        
        override fun onPartialResults(partialResults: Bundle?) {}
        
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
} 