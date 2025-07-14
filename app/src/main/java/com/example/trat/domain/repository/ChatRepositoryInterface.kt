package com.example.trat.domain.repository

import com.example.trat.data.entities.Chat
import com.example.trat.data.entities.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepositoryInterface {
    
    // Chat operations
    fun getAllChats(): Flow<List<Chat>>
    suspend fun getChatById(chatId: String): Chat?
    suspend fun insertChat(chat: Chat)
    suspend fun updateChat(chat: Chat)
    suspend fun deleteChat(chat: Chat)
    suspend fun updateLastMessageTime(chatId: String, timestamp: Long)
    suspend fun getChatCount(): Int
    
    // Message operations
    fun getMessagesByChatId(chatId: String): Flow<List<Message>>
    suspend fun getMessageById(messageId: String): Message?
    suspend fun insertMessage(message: Message)
    suspend fun insertMessages(messages: List<Message>)
    suspend fun updateMessage(message: Message)
    suspend fun deleteMessage(message: Message)
    suspend fun deleteMessagesByChatId(chatId: String)
    suspend fun getLastMessageByChatId(chatId: String): Message?
    suspend fun getMessageCountByChatId(chatId: String): Int
} 