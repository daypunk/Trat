package com.example.trat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trat.data.entities.Message
import com.example.trat.ui.theme.*

@Composable
fun MessageBubble(
    message: Message,
    isHighlighted: Boolean = false,
    searchQuery: String = ""
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // 메시지 버블
        Box(
            modifier = Modifier
                .align(if (message.isUserMessage) Alignment.CenterEnd else Alignment.CenterStart)
                .widthIn(min = 60.dp, max = 280.dp)
                .background(
                    color = if (message.isUserMessage) TossInputMessage else TossOutputMessage,
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (message.isUserMessage) 18.dp else 6.dp,
                        bottomEnd = if (message.isUserMessage) 6.dp else 18.dp
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column {
                // 원본 텍스트만 표시 (토스 스타일: 간소화)
                Text(
                    text = if (searchQuery.isNotBlank()) {
                        buildHighlightedText(
                            text = if (message.originalText != message.translatedText) {
                                "${message.originalText}\n${message.translatedText}"
                            } else {
                                message.originalText
                            },
                            searchQuery = searchQuery
                        )
                    } else {
                        buildAnnotatedString {
                            append(message.originalText)
                            // 번역이 다른 경우 번역된 텍스트도 보여주되, 구분선 없이 자연스럽게
                            if (message.originalText != message.translatedText) {
                                append("\n")
                                withStyle(style = SpanStyle(fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f))) {
                                    append(message.translatedText)
                                }
                            }
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        lineHeight = 20.sp
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun buildHighlightedText(text: String, searchQuery: String) = buildAnnotatedString {
    val highlightColor = TossGray800
    var startIndex = 0
    
    while (startIndex < text.length) {
        val index = text.indexOf(searchQuery, startIndex, ignoreCase = true)
        if (index == -1) {
            append(text.substring(startIndex))
            break
        }
        
        // 하이라이트 이전 텍스트
        append(text.substring(startIndex, index))
        
        // 하이라이트된 텍스트
        withStyle(
            style = SpanStyle(
                background = Color.Yellow.copy(alpha = 0.7f),
                color = TossGray900
            )
        ) {
            append(text.substring(index, index + searchQuery.length))
        }
        
        startIndex = index + searchQuery.length
    }
} 