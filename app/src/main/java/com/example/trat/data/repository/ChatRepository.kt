package com.example.trat.data.repository

import com.example.trat.data.dao.ChatDao
import com.example.trat.data.dao.MessageDao
import com.example.trat.data.entities.Chat
import com.example.trat.data.entities.Message
import com.example.trat.domain.repository.ChatRepositoryInterface
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao
) : ChatRepositoryInterface {
    
    // Chat operations
    override fun getAllChats(): Flow<List<Chat>> = chatDao.getAllChats()
    
    override suspend fun getChatById(chatId: String): Chat? = chatDao.getChatById(chatId)
    
    override suspend fun insertChat(chat: Chat) = chatDao.insertChat(chat)
    
    override suspend fun updateChat(chat: Chat) = chatDao.updateChat(chat)
    
    override suspend fun deleteChat(chat: Chat) = chatDao.deleteChat(chat)
    
    override suspend fun updateLastMessageTime(chatId: String, timestamp: Long) = 
        chatDao.updateLastMessageTime(chatId, timestamp)
    
    override suspend fun getChatCount(): Int = chatDao.getChatCount()
    
    // Message operations
    override fun getMessagesByChatId(chatId: String): Flow<List<Message>> = 
        messageDao.getMessagesByChatId(chatId)
    
    override suspend fun getMessageById(messageId: String): Message? = 
        messageDao.getMessageById(messageId)
    
    override suspend fun insertMessage(message: Message) = messageDao.insertMessage(message)
    
    override suspend fun insertMessages(messages: List<Message>) = 
        messageDao.insertMessages(messages)
    
    override suspend fun updateMessage(message: Message) = messageDao.updateMessage(message)
    
    override suspend fun deleteMessage(message: Message) = messageDao.deleteMessage(message)
    
    override suspend fun deleteMessagesByChatId(chatId: String) = 
        messageDao.deleteMessagesByChatId(chatId)
    
    override suspend fun getLastMessageByChatId(chatId: String): Message? = 
        messageDao.getLastMessageByChatId(chatId)
    
    override suspend fun getMessageCountByChatId(chatId: String): Int = 
        messageDao.getMessageCountByChatId(chatId)
} 