package com.example.trat.domain.usecase

import com.example.trat.data.entities.Message
import com.example.trat.domain.repository.ChatRepositoryInterface
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 메시지 관리를 담당하는 UseCase
 * - 메시지 저장, 조회, 삭제
 * - 메시지 관련 비즈니스 로직
 */
@Singleton
class MessageUseCase @Inject constructor(
    private val chatRepository: ChatRepositoryInterface
) {
    
    /**
     * 채팅방의 모든 메시지 조회
     */
    fun getMessagesForChat(chatId: String): Flow<List<Message>> {
        return chatRepository.getMessagesByChatId(chatId)
    }
    
    /**
     * 사용자 메시지 저장 (원본 텍스트)
     */
    suspend fun saveUserMessage(
        chatId: String,
        originalText: String
    ): Message {
        val message = Message(
            chatId = chatId,
            originalText = originalText,
            translatedText = originalText,
            isUserMessage = true
        )
        chatRepository.insertMessage(message)
        return message
    }
    
    /**
     * 번역 메시지 저장 (번역된 텍스트)
     */
    suspend fun saveTranslationMessage(
        chatId: String,
        translatedText: String
    ): Message {
        val message = Message(
            chatId = chatId,
            originalText = translatedText,
            translatedText = translatedText,
            isUserMessage = false
        )
        chatRepository.insertMessage(message)
        return message
    }
    
    /**
     * 메시지 저장 및 채팅방 마지막 메시지 시간 업데이트
     */
    suspend fun saveMessageAndUpdateChatTime(
        chatId: String,
        originalText: String,
        translatedText: String,
        isUserMessage: Boolean
    ): Message {
        val message = Message(
            chatId = chatId,
            originalText = originalText,
            translatedText = translatedText,
            isUserMessage = isUserMessage
        )
        
        chatRepository.insertMessage(message)
        chatRepository.updateLastMessageTime(chatId, message.timestamp)
        
        return message
    }
    
    /**
     * 채팅방의 마지막 메시지 조회
     */
    suspend fun getLastMessage(chatId: String): Message? {
        return chatRepository.getLastMessageByChatId(chatId)
    }
    
    /**
     * 채팅방의 메시지 개수 조회
     */
    suspend fun getMessageCount(chatId: String): Int {
        return chatRepository.getMessageCountByChatId(chatId)
    }
} 