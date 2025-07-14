package com.example.trat.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trat.data.entities.Chat
import com.example.trat.data.entities.Message
import com.example.trat.domain.usecase.ChatManagementUseCase
import com.example.trat.domain.usecase.TranslateTextUseCase
import com.example.trat.domain.usecase.DownloadLanguageModelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatManagementUseCase: ChatManagementUseCase,
    private val translateTextUseCase: TranslateTextUseCase,
    private val downloadModelUseCase: DownloadLanguageModelUseCase
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
                // 채팅방 정보 로드
                val chatResult = chatManagementUseCase.getChatById(chatId)
                if (chatResult.isSuccess) {
                    val chat = chatResult.getOrThrow()
                    _currentChat.value = chat
                    
                    // 모델 상태 확인
                    checkModelReadiness(chatId)
                    
                    // 메시지 로드
                    chatManagementUseCase.getMessagesForChat(chatId).collect { messageList ->
                        _messages.value = messageList
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "채팅방을 찾을 수 없어요"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "채팅방 로드 중 오류가 발생했어요"
                )
            }
        }
    }
    
    /**
     * 메시지 전송 및 번역
     */
    fun sendMessage(inputText: String) {
        val chatId = _currentChat.value?.id ?: return
        
        if (inputText.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "메시지를 입력해주세요")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTranslating = true, errorMessage = null)
            
            try {
                val result = translateTextUseCase.translateAndSaveMessage(
                    chatId = chatId,
                    inputText = inputText.trim()
                )
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isTranslating = false,
                        inputText = "" // 입력 필드 클리어
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isTranslating = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "번역에 실패했어요"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isTranslating = false,
                    errorMessage = "메시지 전송 중 오류가 발생했어요"
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
     * 모델 준비 상태 확인
     */
    private fun checkModelReadiness(chatId: String) {
        viewModelScope.launch {
            try {
                val result = downloadModelUseCase.areModelsReadyForChat(chatId)
                val isReady = result.getOrElse { false }
                
                _uiState.value = _uiState.value.copy(
                    isModelReady = isReady,
                    isDownloadingModel = !isReady
                )
                
                // 모델이 준비되지 않았다면 다운로드 시작
                if (!isReady) {
                    downloadModelUseCase.downloadModelsForChat(chatId, requireWifi = false)
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isModelReady = false,
                    isDownloadingModel = false
                )
            }
        }
    }
    
    /**
     * 에러 메시지 클리어
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * 채팅방 클리어 (메시지 삭제)
     */
    fun clearChat() {
        viewModelScope.launch {
            try {
                val chatId = _currentChat.value?.id ?: return@launch
                val result = chatManagementUseCase.clearChatMessages(chatId)
                
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "채팅 기록 삭제에 실패했어요"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "채팅 기록 삭제 중 오류가 발생했어요"
                )
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
    val isDownloadingModel: Boolean = false,
    val inputText: String = "",
    val errorMessage: String? = null
) 