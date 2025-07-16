package com.example.trat.presentation.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trat.data.entities.Chat
import com.example.trat.data.models.SupportedLanguage
import com.example.trat.domain.usecase.ChatUseCase
import com.example.trat.utils.LanguageModelManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val chatUseCase: ChatUseCase,
    private val languageModelManager: LanguageModelManager
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
        translateLanguage: SupportedLanguage,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                chatUseCase.updateChatLanguages(chatId, nativeLanguage, translateLanguage)
                onComplete?.invoke()
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
        translateLanguage: SupportedLanguage,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                chatUseCase.updateChatTitleAndLanguages(chatId, title, nativeLanguage, translateLanguage)
                onComplete?.invoke()
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
    
    /**
     * 모든 언어 모델이 다운로드되어 있는지 확인
     */
    suspend fun areAllModelsDownloaded(): Boolean {
        return try {
            val allLanguages = SupportedLanguage.values()
            allLanguages.all { language ->
                languageModelManager.isModelDownloaded(language)
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error checking models", e)
            false
        }
    }
    
    /**
     * 모든 언어 모델 다운로드
     */
    fun downloadAllModels(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDownloadingModels = true,
                downloadProgress = 0f
            )
            
            try {
                val allLanguages = SupportedLanguage.values()
                val totalModels = allLanguages.size
                
                Log.d("MainViewModel", "Starting download for all models: ${allLanguages.map { it.displayName }}")
                
                allLanguages.forEachIndexed { index, language ->
                    Log.d("MainViewModel", "Downloading model for ${language.displayName}")
                    
                    val result = languageModelManager.downloadModel(language, requireWifi = false)
                    
                    if (result.isSuccess) {
                        Log.d("MainViewModel", "Successfully downloaded ${language.displayName}")
                    } else {
                        Log.w("MainViewModel", "Failed to download ${language.displayName}: ${result.exceptionOrNull()?.message}")
                    }
                    
                    // 진행률 업데이트
                    val progress = (index + 1).toFloat() / totalModels
                    _uiState.value = _uiState.value.copy(downloadProgress = progress)
                }
                
                Log.d("MainViewModel", "All models download completed")
                
                // 다운로드 완료 - 즉시 다이얼로그 닫기
                _uiState.value = _uiState.value.copy(
                    isDownloadingModels = false,
                    modelsDownloaded = true,
                    downloadProgress = 1f,
                    showModelDownloadDialog = false
                )
                
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error downloading models", e)
                _uiState.value = _uiState.value.copy(
                    isDownloadingModels = false,
                    errorMessage = "모델 다운로드에 실패했어요: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 모델 다운로드 상태 초기화
     */
    fun resetModelDownloadState() {
        _uiState.value = _uiState.value.copy(
            showModelDownloadDialog = false,
            isDownloadingModels = false,
            modelsDownloaded = false,
            downloadProgress = 0f
        )
    }
    
    /**
     * 모델 다운로드 다이얼로그 표시 설정
     */
    fun setShowModelDownloadDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showModelDownloadDialog = show)
    }
}

/**
 * Main 화면의 UI 상태
 */
data class MainUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showModelDownloadDialog: Boolean = false,
    val isDownloadingModels: Boolean = false,
    val modelsDownloaded: Boolean = false,
    val downloadProgress: Float = 0f
) 