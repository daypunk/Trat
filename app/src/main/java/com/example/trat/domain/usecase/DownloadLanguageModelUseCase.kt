package com.example.trat.domain.usecase

import com.example.trat.data.models.SupportedLanguage
import com.example.trat.domain.repository.ChatRepositoryInterface
import com.example.trat.utils.Constants
import com.example.trat.utils.LanguageModelManager
import com.example.trat.services.TranslationService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadLanguageModelUseCase @Inject constructor(
    private val languageModelManager: LanguageModelManager,
    private val chatRepository: ChatRepositoryInterface,
    private val translationService: TranslationService
) {
    
    /**
     * 특정 채팅방에 필요한 언어 모델들을 다운로드합니다
     */
    suspend fun downloadModelsForChat(
        chatId: String,
        requireWifi: Boolean = true
    ): Result<Boolean> {
        return try {
            val chat = chatRepository.getChatById(chatId)
                ?: return Result.failure(Exception("채팅방을 찾을 수 없습니다"))
            
            languageModelManager.downloadRequiredModels(
                sourceLanguage = chat.nativeLanguage,
                targetLanguage = chat.translateLanguage,
                requireWifi = requireWifi
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 특정 언어의 모델을 다운로드합니다
     */
    suspend fun downloadModel(
        language: SupportedLanguage,
        requireWifi: Boolean = true
    ): Result<Boolean> {
        return languageModelManager.downloadModel(language, requireWifi)
    }
    
    /**
     * 여러 언어의 모델을 한 번에 다운로드합니다
     */
    suspend fun downloadModels(
        languages: List<SupportedLanguage>,
        requireWifi: Boolean = true
    ): Result<Int> {
        return try {
            var successCount = 0
            val failures = mutableListOf<String>()
            
            for (language in languages) {
                val result = languageModelManager.downloadModel(language, requireWifi)
                if (result.isSuccess) {
                    successCount++
                } else {
                    failures.add("${language.displayName}: ${result.exceptionOrNull()?.message}")
                }
            }
            
            if (failures.isNotEmpty() && successCount == 0) {
                Result.failure(Exception("모든 모델 다운로드 실패: ${failures.joinToString(", ")}"))
            } else {
                Result.success(successCount)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 모든 활성 채팅방에 필요한 언어 모델들을 다운로드합니다
     */
    suspend fun downloadAllRequiredModels(requireWifi: Boolean = true): Result<Int> {
        return try {
            val chats = mutableListOf<com.example.trat.data.entities.Chat>()
            chatRepository.getAllChats().collect { chatList ->
                chats.clear()
                chats.addAll(chatList)
            }
            
            val requiredLanguages = mutableSetOf<SupportedLanguage>()
            for (chat in chats) {
                requiredLanguages.add(chat.nativeLanguage)
                requiredLanguages.add(chat.translateLanguage)
            }
            
            downloadModels(requiredLanguages.toList(), requireWifi)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 특정 언어의 모델을 삭제합니다
     */
    suspend fun deleteModel(language: SupportedLanguage): Result<Boolean> {
        return languageModelManager.deleteModel(language)
    }
    
    /**
     * 사용하지 않는 언어 모델들을 삭제합니다
     */
    suspend fun cleanupUnusedModels(): Result<Int> {
        return try {
            val chats = mutableListOf<com.example.trat.data.entities.Chat>()
            chatRepository.getAllChats().collect { chatList ->
                chats.clear()
                chats.addAll(chatList)
            }
            
            val activeLanguages = mutableSetOf<SupportedLanguage>()
            for (chat in chats) {
                activeLanguages.add(chat.nativeLanguage)
                activeLanguages.add(chat.translateLanguage)
            }
            
            languageModelManager.cleanupUnusedModels(activeLanguages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 모든 다운로드된 모델을 삭제합니다
     */
    suspend fun deleteAllModels(): Result<Int> {
        return languageModelManager.deleteAllModels()
    }
    
    /**
     * 다운로드된 모델들의 상태를 확인합니다
     */
    suspend fun getModelStatus(): Map<SupportedLanguage, Boolean> {
        return languageModelManager.getModelStatus()
    }
    
    /**
     * 특정 언어의 모델이 다운로드되어 있는지 확인합니다
     */
    suspend fun isModelDownloaded(language: SupportedLanguage): Boolean {
        return languageModelManager.isModelDownloaded(language)
    }
    
    /**
     * 특정 채팅방에 필요한 모델들이 모두 다운로드되어 있는지 확인합니다
     */
    suspend fun areModelsReadyForChat(chatId: String): Result<Boolean> {
        return try {
            val chat = chatRepository.getChatById(chatId)
                ?: return Result.failure(Exception("채팅방을 찾을 수 없습니다"))
            
            // 양방향 번역을 모두 확인
            val forwardResult = translationService.downloadModelIfNeeded(
                sourceLanguage = chat.nativeLanguage,
                targetLanguage = chat.translateLanguage,
                requireWifi = false
            )
            
            val backwardResult = translationService.downloadModelIfNeeded(
                sourceLanguage = chat.translateLanguage,
                targetLanguage = chat.nativeLanguage,
                requireWifi = false
            )
            
            Result.success(forwardResult.isSuccess && backwardResult.isSuccess)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 다운로드된 모델들의 목록을 가져옵니다
     */
    suspend fun getDownloadedModels(): Result<Set<String>> {
        return languageModelManager.getDownloadedModels()
    }
    
    /**
     * 모델 다운로드 진행 상황을 나타내는 데이터 클래스
     */
    data class DownloadProgress(
        val totalModels: Int,
        val downloadedModels: Int,
        val currentLanguage: SupportedLanguage?,
        val isCompleted: Boolean,
        val errorMessage: String? = null
    )
    
    /**
     * 채팅방별 모델 준비 상태 정보
     */
    data class ChatModelStatus(
        val chatId: String,
        val chatTitle: String,
        val nativeLanguage: SupportedLanguage,
        val translateLanguage: SupportedLanguage,
        val nativeModelReady: Boolean,
        val translateModelReady: Boolean,
        val isFullyReady: Boolean
    )
    
    /**
     * 모든 채팅방의 모델 상태를 확인합니다
     */
    suspend fun getAllChatModelStatus(): Result<List<ChatModelStatus>> {
        return try {
            val chats = mutableListOf<com.example.trat.data.entities.Chat>()
            chatRepository.getAllChats().collect { chatList ->
                chats.clear()
                chats.addAll(chatList)
            }
            
            val modelStatus = languageModelManager.getModelStatus()
            
            val chatStatusList = chats.map { chat ->
                val nativeReady = modelStatus[chat.nativeLanguage] ?: false
                val translateReady = modelStatus[chat.translateLanguage] ?: false
                
                ChatModelStatus(
                    chatId = chat.id,
                    chatTitle = chat.title,
                    nativeLanguage = chat.nativeLanguage,
                    translateLanguage = chat.translateLanguage,
                    nativeModelReady = nativeReady,
                    translateModelReady = translateReady,
                    isFullyReady = nativeReady && translateReady
                )
            }
            
            Result.success(chatStatusList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 