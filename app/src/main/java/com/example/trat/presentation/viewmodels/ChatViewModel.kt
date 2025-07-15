package com.example.trat.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trat.data.entities.Chat
import com.example.trat.data.entities.Message
import com.example.trat.domain.usecase.ChatUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatUseCase: ChatUseCase
) : ViewModel() {
    
    // UI 상태
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
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
        viewModelScope.launch {
            try {
                val chat = chatUseCase.getChatById(chatId)
                if (chat != null) {
                    _currentChat.value = chat
                    
                    // 먼저 모델 상태를 true로 설정 (대부분의 경우 모델이 이미 있음)
                    _uiState.value = _uiState.value.copy(isModelReady = true)
                    
                    // 백그라운드에서 모델 다운로드 시도 (없는 경우에만 다운로드)
                    try {
                        chatUseCase.downloadModelsIfNeeded(chatId)
                    } catch (e: Exception) {
                        // 모델 다운로드 실패해도 계속 진행 (기존 모델 사용)
                    }
                    
                    // 메시지 로드
                    chatUseCase.getMessagesForChat(chatId).collect { messageList ->
                        _messages.value = messageList
                    }
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = "채팅방을 찾을 수 없어요")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "채팅방 로드 중 오류가 발생했어요")
            }
        }
    }
    
    /**
     * 메시지 전송 및 번역
     */
    fun sendMessage(inputText: String) {
        val chatId = _currentChat.value?.id ?: return
        
        if (inputText.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTranslating = true, errorMessage = null)
            
            try {
                chatUseCase.sendMessage(chatId, inputText.trim())
                _uiState.value = _uiState.value.copy(
                    isTranslating = false,
                    inputText = ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isTranslating = false,
                    errorMessage = "번역에 실패했어요: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 입력 텍스트 업데이트
     */
    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }
    
    /**
     * 에러 메시지 클리어
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * 채팅방 클리어
     */
    fun clearChat() {
        viewModelScope.launch {
            try {
                val chatId = _currentChat.value?.id ?: return@launch
                chatUseCase.clearChatMessages(chatId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "채팅 기록 삭제에 실패했어요")
            }
        }
    }
}

/**
 * Chat 화면의 UI 상태
 */
data class ChatUiState(
    val isTranslating: Boolean = false,
    val isModelReady: Boolean = false,
    val inputText: String = "",
    val errorMessage: String? = null
) 