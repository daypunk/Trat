package com.example.trat.domain.usecase

import com.example.trat.data.entities.Chat
import com.example.trat.data.entities.Message
import com.example.trat.data.models.SupportedLanguage
import com.example.trat.domain.repository.ChatRepositoryInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatManagementUseCase @Inject constructor(
    private val chatRepository: ChatRepositoryInterface
) {
    
    /**
     * 새로운 채팅방을 생성합니다
     */
    suspend fun createChat(
        title: String,
        nativeLanguage: SupportedLanguage,
        translateLanguage: SupportedLanguage
    ): Result<Chat> {
        return try {
            // 입력 값 검증
            if (title.isBlank()) {
                return Result.failure(Exception("채팅방 제목을 입력해주세요"))
            }
            
            if (nativeLanguage == translateLanguage) {
                return Result.failure(Exception("원본 언어와 번역 언어가 같을 수 없습니다"))
            }
            
            // 중복 제목 확인
            val existingChats = chatRepository.getAllChats().first()
            val isDuplicateTitle = existingChats.any { it.title.equals(title, ignoreCase = true) }
            
            val finalTitle = if (isDuplicateTitle) {
                generateUniqueTitle(title, existingChats)
            } else {
                title
            }
            
            // 새 채팅방 생성
            val newChat = Chat(
                title = finalTitle,
                nativeLanguage = nativeLanguage,
                translateLanguage = translateLanguage
            )
            
            chatRepository.insertChat(newChat)
            Result.success(newChat)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 채팅방 제목을 수정합니다
     */
    suspend fun updateChatTitle(chatId: String, newTitle: String): Result<Chat> {
        return try {
            if (newTitle.isBlank()) {
                return Result.failure(Exception("채팅방 제목을 입력해주세요"))
            }
            
            val chat = chatRepository.getChatById(chatId)
                ?: return Result.failure(Exception("채팅방을 찾을 수 없습니다"))
            
            val updatedChat = chat.copy(title = newTitle)
            chatRepository.updateChat(updatedChat)
            Result.success(updatedChat)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 채팅방의 언어 설정을 변경합니다
     */
    suspend fun updateChatLanguages(
        chatId: String,
        nativeLanguage: SupportedLanguage,
        translateLanguage: SupportedLanguage
    ): Result<Chat> {
        return try {
            if (nativeLanguage == translateLanguage) {
                return Result.failure(Exception("원본 언어와 번역 언어가 같을 수 없습니다"))
            }
            
            val chat = chatRepository.getChatById(chatId)
                ?: return Result.failure(Exception("채팅방을 찾을 수 없습니다"))
            
            val updatedChat = chat.copy(
                nativeLanguage = nativeLanguage,
                translateLanguage = translateLanguage
            )
            
            chatRepository.updateChat(updatedChat)
            Result.success(updatedChat)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 채팅방을 삭제합니다
     */
    suspend fun deleteChat(chatId: String): Result<Boolean> {
        return try {
            val chat = chatRepository.getChatById(chatId)
                ?: return Result.failure(Exception("채팅방을 찾을 수 없습니다"))
            
            // 채팅방 삭제 (외래키 제약으로 인해 관련 메시지도 함께 삭제됨)
            chatRepository.deleteChat(chat)
            Result.success(true)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 모든 채팅방 목록을 가져옵니다
     */
    fun getAllChats(): Flow<List<Chat>> {
        return chatRepository.getAllChats()
    }
    
    /**
     * 특정 채팅방 정보를 가져옵니다
     */
    suspend fun getChatById(chatId: String): Result<Chat> {
        return try {
            val chat = chatRepository.getChatById(chatId)
                ?: return Result.failure(Exception("채팅방을 찾을 수 없습니다"))
            
            Result.success(chat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 특정 채팅방의 메시지들을 가져옵니다
     */
    fun getMessagesForChat(chatId: String): Flow<List<Message>> {
        return chatRepository.getMessagesByChatId(chatId)
    }
    
    /**
     * 채팅방의 메시지 수를 가져옵니다
     */
    suspend fun getMessageCount(chatId: String): Result<Int> {
        return try {
            val count = chatRepository.getMessageCountByChatId(chatId)
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 채팅방의 마지막 메시지를 가져옵니다
     */
    suspend fun getLastMessage(chatId: String): Result<Message?> {
        return try {
            val lastMessage = chatRepository.getLastMessageByChatId(chatId)
            Result.success(lastMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 모든 채팅방을 삭제합니다
     */
    suspend fun deleteAllChats(): Result<Int> {
        return try {
            val chats = chatRepository.getAllChats().first()
            var deletedCount = 0
            
            for (chat in chats) {
                try {
                    chatRepository.deleteChat(chat)
                    deletedCount++
                } catch (e: Exception) {
                    // 개별 삭제 실패는 무시하고 계속 진행
                    continue
                }
            }
            
            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 채팅방의 모든 메시지를 삭제합니다
     */
    suspend fun clearChatMessages(chatId: String): Result<Boolean> {
        return try {
            val chat = chatRepository.getChatById(chatId)
                ?: return Result.failure(Exception("채팅방을 찾을 수 없습니다"))
            
            chatRepository.deleteMessagesByChatId(chatId)
            Result.success(true)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 채팅방 통계 정보를 가져옵니다
     */
    suspend fun getChatStatistics(chatId: String): Result<ChatStatistics> {
        return try {
            val chat = chatRepository.getChatById(chatId)
                ?: return Result.failure(Exception("채팅방을 찾을 수 없습니다"))
            
            val messageCount = chatRepository.getMessageCountByChatId(chatId)
            val lastMessage = chatRepository.getLastMessageByChatId(chatId)
            
            val statistics = ChatStatistics(
                chatId = chatId,
                title = chat.title,
                nativeLanguage = chat.nativeLanguage,
                translateLanguage = chat.translateLanguage,
                messageCount = messageCount,
                createdAt = chat.createdAt,
                lastMessageAt = chat.lastMessageAt,
                hasMessages = messageCount > 0,
                lastMessageText = lastMessage?.originalText
            )
            
            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 전체 앱 통계를 가져옵니다
     */
    suspend fun getAppStatistics(): Result<AppStatistics> {
        return try {
            val totalChats = chatRepository.getChatCount()
            val chats = chatRepository.getAllChats().first()
            
            var totalMessages = 0
            var activeChats = 0
            var oldestChatDate = Long.MAX_VALUE
            val languageUsage = mutableMapOf<SupportedLanguage, Int>()
            
            for (chat in chats) {
                val messageCount = chatRepository.getMessageCountByChatId(chat.id)
                totalMessages += messageCount
                
                if (messageCount > 0) {
                    activeChats++
                }
                
                if (chat.createdAt < oldestChatDate) {
                    oldestChatDate = chat.createdAt
                }
                
                // 언어 사용 빈도 계산
                languageUsage[chat.nativeLanguage] = languageUsage.getOrDefault(chat.nativeLanguage, 0) + 1
                languageUsage[chat.translateLanguage] = languageUsage.getOrDefault(chat.translateLanguage, 0) + 1
            }
            
            val statistics = AppStatistics(
                totalChats = totalChats,
                activeChats = activeChats,
                totalMessages = totalMessages,
                oldestChatDate = if (oldestChatDate == Long.MAX_VALUE) null else oldestChatDate,
                languageUsage = languageUsage.toMap()
            )
            
            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 중복되지 않는 고유한 제목을 생성합니다
     */
    private fun generateUniqueTitle(baseTitle: String, existingChats: List<Chat>): String {
        val existingTitles = existingChats.map { it.title.lowercase() }.toSet()
        var counter = 1
        var newTitle: String
        
        do {
            newTitle = "$baseTitle ($counter)"
            counter++
        } while (existingTitles.contains(newTitle.lowercase()))
        
        return newTitle
    }
    
    /**
     * 채팅방 통계 정보 데이터 클래스
     */
    data class ChatStatistics(
        val chatId: String,
        val title: String,
        val nativeLanguage: SupportedLanguage,
        val translateLanguage: SupportedLanguage,
        val messageCount: Int,
        val createdAt: Long,
        val lastMessageAt: Long,
        val hasMessages: Boolean,
        val lastMessageText: String?
    )
    
    /**
     * 전체 앱 통계 정보 데이터 클래스
     */
    data class AppStatistics(
        val totalChats: Int,
        val activeChats: Int,
        val totalMessages: Int,
        val oldestChatDate: Long?,
        val languageUsage: Map<SupportedLanguage, Int>
    )
} 