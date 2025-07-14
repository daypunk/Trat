package com.example.trat.data.dao

import androidx.room.*
import com.example.trat.data.entities.Chat
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    
    @Query("SELECT * FROM chats ORDER BY lastMessageAt DESC")
    fun getAllChats(): Flow<List<Chat>>
    
    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatById(chatId: String): Chat?
    
    @Insert
    suspend fun insertChat(chat: Chat)
    
    @Update
    suspend fun updateChat(chat: Chat)
    
    @Delete
    suspend fun deleteChat(chat: Chat)
    
    @Query("UPDATE chats SET lastMessageAt = :timestamp WHERE id = :chatId")
    suspend fun updateLastMessageTime(chatId: String, timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM chats")
    suspend fun getChatCount(): Int
} 