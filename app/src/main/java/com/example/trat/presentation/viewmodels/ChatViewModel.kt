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
                val chat = chatManagementUseCase.getChatById(chatId)
                if (chat != null) {
                    _currentChat.value = chat
                    
                    // 앱 시작 시 모든 모델이 다운로드되므로 항상 준비된 상태로 설정
                    _uiState.value = _uiState.value.copy(isModelReady = true)
                    
                    // 메시지 로드
                    messageUseCase.getMessagesForChat(chatId).collect { messageList ->
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
                messageTranslationUseCase.sendMessage(chatId, inputText.trim())
                
                // 메시지 전송 후 채팅 정보 새로고침 (언어 감지로 인한 변경사항 반영)
                refreshCurrentChat()
                
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
        
        viewModelScope.launch {
            try {
                val updatedChat = chatManagementUseCase.getChatById(chatId)
                if (updatedChat != null) {
                    Log.d("ChatViewModel", "Updated chat: ${updatedChat.nativeLanguage.displayName} ↔ ${updatedChat.translateLanguage.displayName}")
                    
                    // 즉시 currentChat 업데이트 (뱃지가 바로 변경되도록)
                    _currentChat.value = updatedChat
                    
                    // 앱 시작 시 모든 모델이 다운로드되므로 항상 준비된 상태
                    _uiState.value = _uiState.value.copy(
                        isModelReady = true,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error in refreshCurrentChatAndCheckModels", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "설정 업데이트 중 오류가 발생했어요"
                )
            }
        }
    }
    

    
    /**
     * 채팅방 클리어
     */
    fun clearChat() {
        viewModelScope.launch {
            try {
                val chatId = _currentChat.value?.id ?: return@launch
                chatManagementUseCase.clearChatMessages(chatId)
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
    val isModelReady: Boolean = true, // 앱 시작 시 모든 모델이 다운로드되므로 기본값을 true로 설정
    val inputText: String = "",
    val errorMessage: String? = null
) 