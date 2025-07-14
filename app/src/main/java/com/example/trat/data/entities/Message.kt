package com.example.trat.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = Chat::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("chatId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Message(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val chatId: String,
    val originalText: String,
    val translatedText: String,
    val isUserMessage: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
) 