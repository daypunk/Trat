package com.example.trat.domain.usecase

import com.example.trat.data.entities.Chat
import com.example.trat.data.models.SupportedLanguage
import com.example.trat.domain.repository.ChatRepositoryInterface
import com.example.trat.utils.Constants
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 채팅방 관리를 담당하는 UseCase
 * - 채팅방 생성, 수정, 삭제
 * - 채팅방 조회 및 목록 관리
 */
@Singleton
class ChatManagementUseCase @Inject constructor(
    private val chatRepository: ChatRepositoryInterface
) {
    
    /**
     * 새 채팅방 생성
     */
    suspend fun createChat(
        title: String,
        nativeLanguage: SupportedLanguage,
        translateLanguage: SupportedLanguage
    ): Chat {
        val chat = Chat(
            title = title.ifBlank { Constants.Defaults.CHAT_TITLE },
            nativeLanguage = nativeLanguage,
            translateLanguage = translateLanguage
        )
        chatRepository.insertChat(chat)
        return chat
    }
    
    /**
     * 채팅방 삭제
     */
    suspend fun deleteChat(chatId: String) {
        val chat = getChatById(chatId)
        if (chat != null) {
            chatRepository.deleteChat(chat)
        }
    }
    
    /**
     * 채팅방 ID로 조회
     */
    suspend fun getChatById(chatId: String): Chat? {
        return chatRepository.getChatById(chatId)
    }
    
    /**
     * 모든 채팅방 목록 조회
     */
    fun getAllChats(): Flow<List<Chat>> {
        return chatRepository.getAllChats()
    }
    
    /**
     * 채팅방의 모든 메시지 삭제
     */
    suspend fun clearChatMessages(chatId: String) {
        chatRepository.deleteMessagesByChatId(chatId)
    }
    
    /**
     * 채팅방 언어 설정 업데이트
     */
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
    
    /**
     * 채팅방 제목과 언어 설정 업데이트
     */
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
    
    /**
     * 채팅방 마지막 메시지 시간 업데이트
     */
    suspend fun updateLastMessageTime(chatId: String, timestamp: Long) {
        chatRepository.updateLastMessageTime(chatId, timestamp)
    }
} 