package com.example.trat.domain.usecase

import com.example.trat.data.entities.Chat
import com.example.trat.data.entities.Message
import com.example.trat.data.models.SupportedLanguage
import com.example.trat.domain.repository.ChatRepositoryInterface
import com.example.trat.services.TranslationService
import com.example.trat.utils.LanguageDetector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatUseCase @Inject constructor(
    private val chatRepository: ChatRepositoryInterface,
    private val translationService: TranslationService
) {
    
    // ============ 채팅 관리 ============
    
    suspend fun createChat(
        title: String,
        nativeLanguage: SupportedLanguage,
        translateLanguage: SupportedLanguage
    ): Chat {
        val chat = Chat(
            title = title.ifBlank { "${nativeLanguage.displayName} ↔ ${translateLanguage.displayName}" },
            nativeLanguage = nativeLanguage,
            translateLanguage = translateLanguage
        )
        chatRepository.insertChat(chat)
        return chat
    }
    
    suspend fun deleteChat(chatId: String) {
        val chat = getChatById(chatId)
        if (chat != null) {
            chatRepository.deleteChat(chat)
        }
    }
    
    suspend fun getChatById(chatId: String): Chat? {
        return chatRepository.getChatById(chatId)
    }
    
    fun getAllChats(): Flow<List<Chat>> {
        return chatRepository.getAllChats()
    }
    
    fun getMessagesForChat(chatId: String): Flow<List<Message>> {
        return chatRepository.getMessagesByChatId(chatId)
    }
    
    suspend fun clearChatMessages(chatId: String) {
        chatRepository.deleteMessagesByChatId(chatId)
    }
    
    suspend fun updateChatLanguages(
        chatId: String,
        nativeLanguage: SupportedLanguage,
        translateLanguage: SupportedLanguage
    ) {
        val chat = getChatById(chatId) ?: return
        val updatedChat = chat.copy(
            nativeLanguage = nativeLanguage,
            translateLanguage = translateLanguage
        )
        chatRepository.updateChat(updatedChat)
    }
    
    suspend fun updateChatTitleAndLanguages(
        chatId: String,
        title: String,
        nativeLanguage: SupportedLanguage,
        translateLanguage: SupportedLanguage
    ) {
        val chat = getChatById(chatId) ?: return
        val updatedChat = chat.copy(
            title = title,
            nativeLanguage = nativeLanguage,
            translateLanguage = translateLanguage
        )
        chatRepository.updateChat(updatedChat)
    }
    
    // ============ 번역 및 메시지 ============
    
    suspend fun sendMessage(chatId: String, inputText: String): String {
        val chat = getChatById(chatId) ?: throw Exception("채팅방을 찾을 수 없습니다")
        
        // 0. 언어 감지 및 자동 설정 변경
        val detectedLanguage = LanguageDetector.detectLanguage(inputText)
        var currentChat = chat
        
        if (LanguageDetector.isLanguageChangeNeeded(detectedLanguage, chat.nativeLanguage, chat.translateLanguage)) {
            // 감지된 언어로 nativeLanguage 자동 변경
            val updatedChat = chat.copy(nativeLanguage = detectedLanguage)
            chatRepository.updateChat(updatedChat)
            currentChat = updatedChat
        }
        
        // 1. 원본 메시지 저장
        val originalMessage = Message(
            chatId = chatId,
            originalText = inputText,
            translatedText = inputText,
            isUserMessage = true
        )
        chatRepository.insertMessage(originalMessage)
        
        // 2. 번역 수행 (업데이트된 채팅 설정 사용)
        val translatedText = translateText(currentChat, inputText)
        
        // 3. 번역문이 다르면 번역 메시지도 저장
        if (translatedText != inputText) {
            val translationMessage = Message(
                chatId = chatId,
                originalText = translatedText,
                translatedText = translatedText,
                isUserMessage = false
            )
            chatRepository.insertMessage(translationMessage)
            chatRepository.updateLastMessageTime(chatId, translationMessage.timestamp)
        } else {
            chatRepository.updateLastMessageTime(chatId, originalMessage.timestamp)
        }
        
        return translatedText
    }
    
    private suspend fun translateText(chat: Chat, inputText: String): String {
        // 두 방향 모두 번역 시도
        val result1 = translationService.translate(inputText, chat.nativeLanguage, chat.translateLanguage)
        val result2 = translationService.translate(inputText, chat.translateLanguage, chat.nativeLanguage)
        
        val translation1 = result1.getOrNull()
        val translation2 = result2.getOrNull()
        
        // 원본과 가장 다른 번역 선택
        return when {
            translation1 != null && isDifferentEnough(inputText, translation1) -> translation1
            translation2 != null && isDifferentEnough(inputText, translation2) -> translation2
            else -> inputText
        }
    }
    
    private fun isDifferentEnough(original: String, translated: String): Boolean {
        if (original == translated) return false
        
        // 한글/영어 비율이 다르면 번역됨
        val originalHasKorean = original.any { it in '\uAC00'..'\uD7AF' }
        val translatedHasKorean = translated.any { it in '\uAC00'..'\uD7AF' }
        
        return originalHasKorean != translatedHasKorean
    }
    
    // ============ 모델 관리 ============
    
    suspend fun downloadModelsIfNeeded(chatId: String): Boolean {
        val chat = getChatById(chatId) ?: return false
        
        val result1 = translationService.downloadModelIfNeeded(chat.nativeLanguage, chat.translateLanguage)
        val result2 = translationService.downloadModelIfNeeded(chat.translateLanguage, chat.nativeLanguage)
        
        return result1.isSuccess && result2.isSuccess
    }
} 