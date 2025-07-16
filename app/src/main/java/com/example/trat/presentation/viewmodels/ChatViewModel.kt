package com.example.trat.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trat.data.entities.Chat
import com.example.trat.data.entities.Message
import com.example.trat.domain.usecase.ChatManagementUseCase
import com.example.trat.domain.usecase.MessageUseCase
import com.example.trat.domain.usecase.MessageTranslationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatManagementUseCase: ChatManagementUseCase,
    private val messageUseCase: MessageUseCase,
    private val messageTranslationUseCase: MessageTranslationUseCase
) : BaseViewModel<ChatUiState>() {
    
    // UI 상태
    override val _uiState = MutableStateFlow(ChatUiState())
    override val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    // 현재 채팅방 정보
    private val _currentChat = MutableStateFlow<Chat?>(null)
    val currentChat: StateFlow<Chat?> = _currentChat.asStateFlow()
    
    // 메시지 목록
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    /**
     * 채팅방 초기화
     */
    fun initializeChat(chatId: String) {
        launchSimple(
            onError = { setError("채팅방 로드 중 오류가 발생했어요") }
        ) {
            val chat = chatManagementUseCase.getChatById(chatId)
            if (chat != null) {
                _currentChat.value = chat
                
                // 앱 시작 시 모든 모델이 다운로드되므로 항상 준비된 상태로 설정
                updateUiState { copy(isModelReady = true) }
                
                // 메시지 로드
                messageUseCase.getMessagesForChat(chatId).collect { messageList ->
                    _messages.value = messageList
                }
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
     * 현재 채팅 정보 새로고침 (언어 설정 변경 후 사용)
     */
    fun refreshCurrentChat() {
        val chatId = _currentChat.value?.id ?: return
        viewModelScope.launch {
            try {
                val updatedChat = chatManagementUseCase.getChatById(chatId)
                if (updatedChat != null) {
                    _currentChat.value = updatedChat
                }
            } catch (e: Exception) {
                // 에러 처리 (로그만 남기고 무시)
            }
        }
    }
    
    /**
     * 현재 채팅 정보 새로고침
     */
    fun refreshCurrentChatAndCheckModels() {
        val chatId = _currentChat.value?.id ?: return
        Log.d("ChatViewModel", "refreshCurrentChatAndCheckModels called for chatId: $chatId")
        
        launchSimple(
            onError = { setError("설정 업데이트 중 오류가 발생했어요") }
        ) {
            val updatedChat = chatManagementUseCase.getChatById(chatId)
            if (updatedChat != null) {
                Log.d("ChatViewModel", "Updated chat: ${updatedChat.nativeLanguage.displayName} ↔ ${updatedChat.translateLanguage.displayName}")
                
                // 즉시 currentChat 업데이트 (뱃지가 바로 변경되도록)
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