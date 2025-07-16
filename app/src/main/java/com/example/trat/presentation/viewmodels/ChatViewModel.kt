package com.example.trat.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trat.data.entities.Chat
import com.example.trat.data.entities.Message
import com.example.trat.domain.usecase.ChatManagementUseCase
import com.example.trat.domain.usecase.MessageUseCase
import com.example.trat.domain.usecase.MessageTranslationUseCase
import com.example.trat.domain.usecase.SpeechToTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatManagementUseCase: ChatManagementUseCase,
    private val messageUseCase: MessageUseCase,
    private val messageTranslationUseCase: MessageTranslationUseCase,
    private val speechToTextUseCase: SpeechToTextUseCase
) : BaseViewModel<ChatUiState>() {
    
    // UI 상태
    override val _uiState = MutableStateFlow(ChatUiState())
    override val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    // 현재 채팅방 정보
    private val _currentChat = MutableStateFlow<Chat?>(null)
    val currentChat: StateFlow<Chat?> = _currentChat.asStateFlow()
    
    // 현재 채팅 ID
    private val _currentChatId = MutableStateFlow<String?>(null)
    
    // 메시지 목록 (자동 업데이트)
    val messages: StateFlow<List<Message>> = _currentChatId
        .filterNotNull()
        .flatMapLatest { chatId ->
            messageUseCase.getMessagesForChat(chatId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // STT 상태 (UseCase에서 직접 노출)
    val isListening: StateFlow<Boolean> = speechToTextUseCase.isListening
    val recognizedText: StateFlow<String> = speechToTextUseCase.recognizedText
    val sttError: StateFlow<String?> = speechToTextUseCase.error
    
    /**
     * 채팅방 초기화 (최적화된 버전)
     */
    fun initializeChat(chatId: String) {
        launchSimple(
            onError = { setError("채팅방 로드 중 오류가 발생했어요") }
        ) {
            val chat = chatManagementUseCase.getChatById(chatId)
            if (chat != null) {
                _currentChat.value = chat
                _currentChatId.value = chatId // 메시지 Flow 자동 트리거
                
                // 앱 시작 시 모든 모델이 다운로드되므로 항상 준비된 상태로 설정
                updateUiState { copy(isModelReady = true) }
            } else {
                setError("채팅방을 찾을 수 없어요")
            }
        }
    }
    
    /**
     * 메시지 전송 및 번역
     */
    fun sendMessage(inputText: String) {
        val chatId = _currentChat.value?.id ?: return
        
        if (inputText.isBlank()) return
        
        launchSafely(
            onStart = { updateUiState { copy(isTranslating = true, errorMessage = null) } },
            onComplete = { updateUiState { copy(isTranslating = false, inputText = "") } },
            onError = { setError("번역에 실패했어요: $it") }
        ) {
            messageTranslationUseCase.sendMessage(chatId, inputText.trim())
            
            // 메시지 전송 후 채팅 정보 새로고침 (언어 감지로 인한 변경사항 반영)
            refreshCurrentChat()
        }
    }
    
    /**
     * 입력 텍스트 업데이트
     */
    fun updateInputText(text: String) {
        updateUiState { copy(inputText = text) }
    }
    
    /**
     * 🟢 음성 인식 시작
     */
    fun startSpeechToText() {
        android.util.Log.d("STT_DEBUG", "🟢 ChatViewModel.startSpeechToText() 호출됨")
        val language = _currentChat.value?.nativeLanguage ?: run {
            android.util.Log.d("STT_DEBUG", "❌ 언어 정보 없음 - 중단")
            return
        }
        
        launchSimple(
            onError = { 
                android.util.Log.e("STT_DEBUG", "❌ 음성 인식 시작 오류: $it")
                setError("음성 인식 시작 중 오류가 발생했어요: $it") 
            }
        ) {
            android.util.Log.d("STT_DEBUG", "🟢 speechToTextUseCase.startListening 호출 시작")
            speechToTextUseCase.startListening(language)
            android.util.Log.d("STT_DEBUG", "🟢 speechToTextUseCase.startListening 완료")
        }
    }
    
    /**
     * 🔴 음성 인식 중지
     */
    fun stopSpeechToText() {
        android.util.Log.d("STT_DEBUG", "🔴 ChatViewModel.stopSpeechToText() 호출됨")
        launchSimple(
            onError = { 
                android.util.Log.e("STT_DEBUG", "❌ 음성 인식 중지 오류: $it")
                /* Repository에서 상태 관리하므로 에러 무시 */ 
            }
        ) {
            android.util.Log.d("STT_DEBUG", "🔴 speechToTextUseCase.stopListening 호출 시작")
            speechToTextUseCase.stopListening()
            android.util.Log.d("STT_DEBUG", "🔴 speechToTextUseCase.stopListening 완료")
        }
    }
    
    /**
     * 인식된 텍스트를 입력 필드에 추가
     */
    fun appendRecognizedText(recognizedText: String) {
        val currentText = _uiState.value.inputText
        val newText = if (currentText.isEmpty()) {
            recognizedText
        } else {
            "$currentText $recognizedText"
        }
        updateInputText(newText)
        speechToTextUseCase.clearRecognizedText()
    }
    
    /**
     * STT 에러 클리어
     */
    fun clearSttError() {
        speechToTextUseCase.clearError()
    }
    
    /**
     * 현재 채팅 정보 새로고침 (통합 버전)
     */
    fun refreshCurrentChat() {
        val chatId = _currentChat.value?.id ?: return
        Log.d("ChatViewModel", "refreshCurrentChat called for chatId: $chatId")
        
        launchSimple(
            onError = { setError("설정 업데이트 중 오류가 발생했어요") }
        ) {
            val updatedChat = chatManagementUseCase.getChatById(chatId)
            if (updatedChat != null) {
                Log.d("ChatViewModel", "Updated chat: ${updatedChat.nativeLanguage.displayName} ↔ ${updatedChat.translateLanguage.displayName}")
                
                // 채팅 정보 업데이트
                _currentChat.value = updatedChat
                
                // 앱 시작 시 모든 모델이 다운로드되므로 항상 준비된 상태
                updateUiState {
                    copy(
                        isModelReady = true,
                        errorMessage = null
                    )
                }
            }
        }
    }
    

    
    /**
     * 채팅방 클리어
     */
    fun clearChat() {
        launchSimple(
            onError = { setError("채팅 기록 삭제에 실패했어요") }
        ) {
            val chatId = _currentChat.value?.id ?: return@launchSimple
            chatManagementUseCase.clearChatMessages(chatId)
        }
    }
}

/**
 * Chat 화면의 UI 상태
 */
data class ChatUiState(
    val isTranslating: Boolean = false,
    val isModelReady: Boolean = true, // 앱 시작 시 모든 모델이 다운로드되므로 기본값을 true로 설정
    val inputText: String = "",
    override val errorMessage: String? = null
) : BaseUiState {
    override fun clearErrorMessage(): ChatUiState = copy(errorMessage = null)
    override fun setLoadingState(isLoading: Boolean): ChatUiState = copy(isTranslating = isLoading)
    override fun setErrorMessage(message: String): ChatUiState = copy(errorMessage = message)
} 