package com.example.trat.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trat.data.entities.Chat

@Composable
fun ChatListDrawer(
    chats: List<Chat>,
    onChatClick: (String) -> Unit,
    onCreateChatClick: () -> Unit,
    onDeleteChat: (String) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(320.dp)
    ) {
        // 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "빠른번역",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "오프라인 번역기",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        HorizontalDivider()
        
        // 새 채팅 생성 버튼
        Spacer(modifier = Modifier.height(8.dp))
        
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            label = { Text("새 채팅 만들기") },
            selected = false,
            onClick = onCreateChatClick,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        
        if (chats.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "내 채팅방 (${chats.size})",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // 채팅 목록
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                items(chats, key = { it.id }) { chat ->
                    DrawerChatItem(
                        chat = chat,
                        onClick = { onChatClick(chat.id) },
                        onDelete = { onDeleteChat(chat.id) }
                    )
                }
            }
        } else {
            // 빈 상태
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "아직 채팅방이 없어요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "새 채팅을 만들어보세요!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DrawerChatItem(
    chat: Chat,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Column {
                Text(
                    text = chat.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1
                )
                Text(
                    text = "${getLanguageEmoji(chat.nativeLanguage.code)} → ${getLanguageEmoji(chat.translateLanguage.code)} ${chat.nativeLanguage.displayName} → ${chat.translateLanguage.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

private fun getLanguageEmoji(languageCode: String): String {
    return when (languageCode) {
        "ko" -> "🇰🇷"
        "en" -> "🇺🇸"
        "ja" -> "🇯🇵"
        "zh" -> "🇨🇳"
        else -> "🌍"
    }
} 