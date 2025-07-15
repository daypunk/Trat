package com.example.trat.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trat.data.entities.Chat
import com.example.trat.data.models.SupportedLanguage
import com.example.trat.domain.usecase.ChatUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val chatUseCase: ChatUseCase
) : ViewModel() {
    
    // 채팅 목록
    val chats: StateFlow<List<Chat>> = chatUseCase.getAllChats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // UI 상태
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    /**
     * 새 번역 생성
     */
    fun createChat(
        title: String,
        nativeLanguage: SupportedLanguage,
        translateLanguage: SupportedLanguage,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val chat = chatUseCase.createChat(title, nativeLanguage, translateLanguage)
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess(chat.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "채팅방 생성에 실패했어요: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 채팅방 삭제
     */
    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            try {
                chatUseCase.deleteChat(chatId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "채팅방 삭제에 실패했어요")
            }
        }
    }
    
    /**
     * 채팅방 언어 설정 업데이트
     */
    fun updateChatLanguages(
        chatId: String,
        nativeLanguage: SupportedLanguage,
        translateLanguage: SupportedLanguage
    ) {
        viewModelScope.launch {
            try {
                chatUseCase.updateChatLanguages(chatId, nativeLanguage, translateLanguage)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "언어 설정 업데이트에 실패했어요")
            }
        }
    }
    
    /**
     * 채팅방 타이틀과 언어 설정 업데이트
     */
    fun updateChatTitleAndLanguages(
        chatId: String,
        title: String,
        nativeLanguage: SupportedLanguage,
        translateLanguage: SupportedLanguage
    ) {
        viewModelScope.launch {
            try {
                chatUseCase.updateChatTitleAndLanguages(chatId, title, nativeLanguage, translateLanguage)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "설정 업데이트에 실패했어요")
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
     * 마지막 채팅방 ID 가져오기
     */
    fun getLastChatId(): String? {
        return chats.value.firstOrNull()?.id
    }
}

/**
 * Main 화면의 UI 상태
 */
data class MainUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) 