package com.example.trat.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
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
    ],
    indices = [
        Index(value = ["chatId"]), // 채팅방별 메시지 조회 최적화
        Index(value = ["timestamp"]), // 시간순 정렬 최적화
        Index(value = ["chatId", "timestamp"]) // 복합 쿼리 최적화
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