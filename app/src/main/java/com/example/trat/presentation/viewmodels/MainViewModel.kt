package com.example.trat.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trat.data.entities.Chat
import com.example.trat.data.models.SupportedLanguage
import com.example.trat.domain.usecase.ChatManagementUseCase
import com.example.trat.domain.usecase.DownloadLanguageModelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val chatManagementUseCase: ChatManagementUseCase,
    private val downloadModelUseCase: DownloadLanguageModelUseCase
) : ViewModel() {
    
    // UI 상태
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    // 채팅 목록
    val chats: StateFlow<List<Chat>> = chatManagementUseCase.getAllChats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        checkModelStatus()
    }
    
    /**
     * 새 채팅방 생성
     */
    fun createChat(
        title: String,
        nativeLanguage: SupportedLanguage,
        translateLanguage: SupportedLanguage
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val result = chatManagementUseCase.createChat(title, nativeLanguage, translateLanguage)
                if (result.isSuccess) {
                    val chat = result.getOrThrow()
                    // 모델 다운로드 시작
                    downloadModelsForChat(chat.id)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        lastCreatedChatId = chat.id
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "채팅방 생성 실패"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "채팅방 생성 중 오류가 발생했어요"
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
                val result = chatManagementUseCase.deleteChat(chatId)
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "채팅방 삭제에 실패했어요"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "삭제 중 오류가 발생했어요"
                )
            }
        }
    }
    
    /**
     * 특정 채팅방의 모델 다운로드
     */
    private fun downloadModelsForChat(chatId: String) {
        viewModelScope.launch {
            try {
                downloadModelUseCase.downloadModelsForChat(chatId, requireWifi = false)
            } catch (e: Exception) {
                // 모델 다운로드 실패는 사일런트하게 처리
            }
        }
    }
    
    /**
     * 모델 상태 확인
     */
    private fun checkModelStatus() {
        viewModelScope.launch {
            try {
                val modelStatus = downloadModelUseCase.getModelStatus()
                _uiState.value = _uiState.value.copy(
                    modelStatus = modelStatus
                )
            } catch (e: Exception) {
                // 모델 상태 확인 실패는 무시
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
     * 마지막 생성된 채팅 ID 클리어
     */
    fun clearLastCreatedChatId() {
        _uiState.value = _uiState.value.copy(lastCreatedChatId = null)
    }
}

/**
 * MainScreen의 UI 상태
 */
data class MainUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastCreatedChatId: String? = null,
    val modelStatus: Map<SupportedLanguage, Boolean> = emptyMap()
) 