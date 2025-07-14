package com.example.trat.domain.usecase

import com.example.trat.data.entities.Chat
import com.example.trat.data.entities.Message
import com.example.trat.data.models.SupportedLanguage
import com.example.trat.domain.repository.ChatRepositoryInterface
import com.example.trat.services.TranslationService
import com.example.trat.utils.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslateTextUseCase @Inject constructor(
    private val translationService: TranslationService,
    private val chatRepository: ChatRepositoryInterface
) {
    
    /**
     * 텍스트를 번역하고 메시지로 저장합니다
     */
    suspend fun translateAndSaveMessage(
        chatId: String,
        inputText: String,
        isUserMessage: Boolean = true
    ): Result<Message> {
        return try {
            // 채팅방 정보 가져오기
            val chat = chatRepository.getChatById(chatId)
                ?: return Result.failure(Exception("채팅방을 찾을 수 없습니다"))
            
            // 입력 텍스트 검증
            if (inputText.isBlank()) {
                return Result.failure(Exception("번역할 텍스트가 비어있습니다"))
            }
            
            // 1. 원문 메시지 저장 (사용자 메시지)
            val originalMessage = Message(
                chatId = chatId,
                originalText = inputText,
                translatedText = inputText, // 원문은 번역문과 동일
                isUserMessage = true
            )
            chatRepository.insertMessage(originalMessage)
            
            // 2. 번역 수행
            val translationResult = performTranslation(chat, inputText)
            if (translationResult.isFailure) {
                return Result.failure(translationResult.exceptionOrNull() ?: Exception("번역 실패"))
            }
            
            val translatedText = translationResult.getOrThrow()
            
            // 3. 번역문이 원문과 다른 경우에만 번역 메시지 저장 (시스템 메시지)
            val translationMessage = if (translatedText != inputText) {
                val message = Message(
                    chatId = chatId,
                    originalText = translatedText,
                    translatedText = translatedText, // 번역 메시지는 번역문만 표시
                    isUserMessage = false
                )
                chatRepository.insertMessage(message)
                message
            } else {
                originalMessage // 번역문이 원문과 같으면 원문 메시지만 반환
            }
            
            // 4. 채팅방의 마지막 메시지 시간 업데이트
            chatRepository.updateLastMessageTime(chatId, translationMessage.timestamp)
            
            Result.success(translationMessage)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 실시간 번역 (저장하지 않음)
     */
    suspend fun translateText(
        sourceLanguage: SupportedLanguage,
        targetLanguage: SupportedLanguage,
        text: String
    ): Result<String> {
        return translationService.translate(text, sourceLanguage, targetLanguage)
    }
    
    /**
     * 채팅방 설정에 따라 번역을 수행합니다
     */
    private suspend fun performTranslation(chat: Chat, inputText: String): Result<String> {
        return try {
            // 두 방향 모두 번역 시도
            val nativeToTranslateResult = translationService.translate(
                inputText, 
                chat.nativeLanguage, 
                chat.translateLanguage
            )
            
            val translateToNativeResult = translationService.translate(
                inputText, 
                chat.translateLanguage, 
                chat.nativeLanguage
            )
            
            // 두 방향 번역 결과 평가
            val nativeToTranslateText = if (nativeToTranslateResult.isSuccess) {
                nativeToTranslateResult.getOrThrow()
            } else null
            
            val translateToNativeText = if (translateToNativeResult.isSuccess) {
                translateToNativeResult.getOrThrow()
            } else null
            
            // 우선순위: 원본과 다른 번역 결과를 선택
            when {
                // translate -> native 방향 결과가 원본과 다르면 우선 선택 (역방향 번역 우선)
                translateToNativeText != null && translateToNativeText != inputText -> {
                    Result.success(translateToNativeText)
                }
                // native -> translate 방향 결과가 원본과 다르면 선택
                nativeToTranslateText != null && nativeToTranslateText != inputText -> {
                    Result.success(nativeToTranslateText)
                }
                // 둘 다 원본과 같거나 실패한 경우, 성공한 것 중 하나라도 반환
                translateToNativeText != null -> {
                    Result.success(translateToNativeText)
                }
                nativeToTranslateText != null -> {
                    Result.success(nativeToTranslateText)
                }
                // 모두 실패한 경우 원본 반환
                else -> {
                    Result.success(inputText)
                }
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 채팅방의 모든 메시지를 다시 번역합니다
     */
    suspend fun retranslateAllMessages(chatId: String): Result<Int> {
        return try {
            val chat = chatRepository.getChatById(chatId)
                ?: return Result.failure(Exception("채팅방을 찾을 수 없습니다"))
            
            val messages = mutableListOf<Message>()
            chatRepository.getMessagesByChatId(chatId).collect { messageList ->
                messages.clear()
                messages.addAll(messageList)
            }
            
            var successCount = 0
            for (message in messages) {
                val translationResult = performTranslation(chat, message.originalText)
                if (translationResult.isSuccess) {
                    val updatedMessage = message.copy(
                        translatedText = translationResult.getOrThrow()
                    )
                    chatRepository.updateMessage(updatedMessage)
                    successCount++
                }
            }
            
            Result.success(successCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 번역 가능 여부를 확인합니다
     */
    suspend fun checkTranslationAvailability(
        sourceLanguage: SupportedLanguage,
        targetLanguage: SupportedLanguage
    ): Result<Boolean> {
        return try {
            // 언어 모델 다운로드 시도
            val downloadResult = translationService.downloadModelIfNeeded(
                sourceLanguage, 
                targetLanguage
            )
            
            if (downloadResult.isFailure) {
                return Result.failure(Exception(Constants.ERROR_MODEL_DOWNLOAD_FAILED))
            }
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 번역 미리보기 (메시지 저장 없이 번역만 수행)
     */
    suspend fun previewTranslation(
        chatId: String,
        inputText: String
    ): Result<String> {
        return try {
            val chat = chatRepository.getChatById(chatId)
                ?: return Result.failure(Exception("채팅방을 찾을 수 없습니다"))
            
            if (inputText.isBlank()) {
                return Result.failure(Exception("번역할 텍스트가 비어있습니다"))
            }
            
            performTranslation(chat, inputText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 