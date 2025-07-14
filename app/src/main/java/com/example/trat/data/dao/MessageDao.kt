package com.example.trat.data.dao

import androidx.room.*
import com.example.trat.data.entities.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesByChatId(chatId: String): Flow<List<Message>>
    
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): Message?
    
    @Insert
    suspend fun insertMessage(message: Message)
    
    @Insert
    suspend fun insertMessages(messages: List<Message>)
    
    @Update
    suspend fun updateMessage(message: Message)
    
    @Delete
    suspend fun deleteMessage(message: Message)
    
    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesByChatId(chatId: String)
    
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessageByChatId(chatId: String): Message?
    
    @Query("SELECT COUNT(*) FROM messages WHERE chatId = :chatId")
    suspend fun getMessageCountByChatId(chatId: String): Int
} 