package com.example.trat.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.trat.data.models.SupportedLanguage
import java.util.UUID

@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val nativeLanguage: SupportedLanguage,
    val translateLanguage: SupportedLanguage,
    val createdAt: Long = System.currentTimeMillis(),
    val lastMessageAt: Long = System.currentTimeMillis()
) 