package com.example.trat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trat.R
import com.example.trat.data.entities.Message
import com.example.trat.data.models.SupportedLanguage
import com.example.trat.ui.theme.*
import com.example.trat.utils.LanguageDetector
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageBubble(
    message: Message,
    @Suppress("UNUSED_PARAMETER") isHighlighted: Boolean = false,
    searchQuery: String = "",
    onSpeakMessage: ((String, SupportedLanguage) -> Unit)? = null,
    isTtsSupported: ((SupportedLanguage) -> Boolean)? = null,
    onRequestLanguagePack: ((SupportedLanguage) -> Unit)? = null
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = if (message.isUserMessage) Alignment.End else Alignment.Start
    ) {
        // 메시지 버블 + TTS 아이콘
        if (message.isUserMessage) {
            // User 메시지: 오른쪽 정렬, TTS 아이콘 없음
            Box(
                modifier = Modifier
                    .widthIn(min = 60.dp, max = 280.dp)
                    .background(
                        color = InputMessage,
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 6.dp
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            buildHighlightedText(
                                text = message.originalText,
                                searchQuery = searchQuery
                            )
                        } else {
                            buildAnnotatedString { append(message.originalText) }
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
        } else {
            // Output 메시지: 왼쪽 정렬, TTS 아이콘 조건부 표시
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(min = 60.dp, max = 240.dp)
                        .background(
                            color = OutputMessage,
                            shape = RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = 6.dp,
                                bottomEnd = 18.dp
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column {
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
                
                // TTS 스피커 아이콘 (항상 표시하되, 지원 여부에 따라 동작 분기)
                if (onSpeakMessage != null && isTtsSupported != null) {
                    // TTS로 재생할 텍스트의 언어 감지
                    val textToSpeakLanguage = LanguageDetector.detectLanguage(message.translatedText)
                    val isLanguageSupported = isTtsSupported(textToSpeakLanguage)
                    
                    IconButton(
                        onClick = { 
                            val textToSpeak = message.translatedText
                            if (isLanguageSupported) {
                                // 언어가 지원되면 바로 TTS 실행
                                onSpeakMessage(textToSpeak, textToSpeakLanguage)
                            } else {
                                // 언어가 지원되지 않으면 언어팩 다운로드 안내
                                onRequestLanguagePack?.invoke(textToSpeakLanguage)
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_tts),
                            contentDescription = if (isLanguageSupported) {
                                "음성으로 듣기"
                            } else {
                                "${textToSpeakLanguage.displayName} 언어팩 다운로드"
                            },
                            tint = if (isLanguageSupported) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
        
        // 타임스탬프
        Text(
            text = formatTimestamp(message.timestamp),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(
                start = if (message.isUserMessage) 0.dp else 12.dp,
                end = if (message.isUserMessage) 12.dp else 0.dp,
                top = 4.dp
            )
        )
    }
}

// 타임스탬프 포맷팅 함수
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 24 * 60 * 60 * 1000 -> { // 24시간 미만은 HH:mm
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
        diff < 7 * 24 * 60 * 60 * 1000 -> { // 7일 미만은 MM/dd HH:mm
            val formatter = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
        else -> { // 7일 이상은 yy/MM/dd
            val formatter = SimpleDateFormat("yy/MM/dd", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
    }
}

@Composable
private fun buildHighlightedText(text: String, searchQuery: String) = buildAnnotatedString {
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
                color = Gray900
            )
        ) {
            append(text.substring(index, index + searchQuery.length))
        }
        
        startIndex = index + searchQuery.length
    }
} 