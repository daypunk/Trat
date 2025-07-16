package com.example.trat.domain.usecase

import android.util.Log
import com.example.trat.domain.service.LanguageDetectionService
import com.example.trat.utils.Constants
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 메시지 전송과 번역을 조합하여 처리하는 UseCase
 * - 여러 UseCase들을 조합하여 복잡한 비즈니스 로직 수행
 * - 메시지 저장 + 언어 감지 + 번역 + 채팅 업데이트 통합 처리
 */
@Singleton
class MessageTranslationUseCase @Inject constructor(
    private val chatManagementUseCase: ChatManagementUseCase,
    private val messageUseCase: MessageUseCase,
    private val languageDetectionUseCase: LanguageDetectionUseCase,
    private val translationUseCase: TranslationUseCase,
    private val languageDetectionService: LanguageDetectionService
) {
    
    /**
     * 메시지를 전송하고 번역하여 저장하는 통합 프로세스
     */
    suspend fun sendMessage(chatId: String, inputText: String): String {
        // 1. 채팅방 정보 조회
        val chat = chatManagementUseCase.getChatById(chatId) 
            ?: throw Exception(Constants.Errors.CHAT_NOT_FOUND)
        
        // 2. 언어 자동 감지 및 채팅 설정 업데이트
        val updatedChat = updateChatLanguageIfNeeded(chatId, inputText, chat)
        
        // 3. 사용자 메시지 저장
        val userMessage = messageUseCase.saveMessageAndUpdateChatTime(
            chatId = chatId,
            originalText = inputText,
            translatedText = inputText,
            isUserMessage = true
        )
        
        // 4. 번역 방향 결정
        val (sourceLanguage, targetLanguage) = languageDetectionUseCase.determineTranslationDirection(
            inputText = inputText,
            chat = updatedChat
        )
        
        // 5. 번역 수행
        val translatedText = translationUseCase.translateText(
            inputText = inputText,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage
        )
        
        // 6. 번역 결과가 원본과 다르면 번역 메시지도 저장
        if (translatedText != inputText) {
            messageUseCase.saveMessageAndUpdateChatTime(
                chatId = chatId,
                originalText = translatedText,
                translatedText = translatedText,
                isUserMessage = false
            )
        }
        
        Log.d("MessageTranslationUseCase", "Message processed: $inputText → $translatedText")
        return translatedText
    }
    
    /**
     * 언어 감지 결과에 따라 채팅 설정을 업데이트
     */
    private suspend fun updateChatLanguageIfNeeded(
        chatId: String,
        inputText: String,
        chat: com.example.trat.data.entities.Chat
    ): com.example.trat.data.entities.Chat {
        // 언어 감지 및 업데이트 필요성 확인
        val newLanguage = languageDetectionUseCase.shouldUpdateChatLanguage(inputText, chat)
        
        return if (newLanguage != null) {
            // 채팅 언어 설정 업데이트
            chatManagementUseCase.updateChatLanguages(
                chatId = chatId,
                nativeLanguage = newLanguage,
                translateLanguage = chat.translateLanguage
            )
            
            // 업데이트된 채팅 정보 반환
            chatManagementUseCase.getChatById(chatId) ?: chat
        } else {
            chat
        }
    }
    
    /**
     * 번역 가능 여부 확인 (모델 상태 체크)
     */
    suspend fun canTranslateInChat(chatId: String): Boolean {
        val chat = chatManagementUseCase.getChatById(chatId) ?: return false
        return translationUseCase.canTranslate(chat.nativeLanguage, chat.translateLanguage)
    }
    
    /**
     * 채팅에 필요한 모델들 다운로드
     */
    suspend fun downloadModelsForChat(chatId: String): Boolean {
        val chat = chatManagementUseCase.getChatById(chatId) ?: return false
        return translationUseCase.downloadModelsIfNeeded(chat.nativeLanguage, chat.translateLanguage)
    }
} 