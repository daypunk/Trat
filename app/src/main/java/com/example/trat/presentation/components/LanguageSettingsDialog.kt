package com.example.trat.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trat.data.entities.Chat
import com.example.trat.data.models.SupportedLanguage
import com.example.trat.ui.theme.TossGray300
import com.example.trat.ui.theme.TossInputMessage

@Composable
fun LanguageSettingsDialog(
    currentChat: Chat?,
    isNewChat: Boolean = false,
    showTitleEdit: Boolean = false,
    onDismiss: () -> Unit,
    onLanguageChanged: (SupportedLanguage, SupportedLanguage) -> Unit = { _, _ -> },
    onChatCreated: (String, SupportedLanguage, SupportedLanguage) -> Unit = { _, _, _ -> },
    onTitleAndLanguageChanged: (String, SupportedLanguage, SupportedLanguage) -> Unit = { _, _, _ -> }
) {
    var title by remember { 
        mutableStateOf(currentChat?.title ?: "") 
    }
    var fromLanguage by remember { 
        mutableStateOf(currentChat?.nativeLanguage ?: SupportedLanguage.KOREAN) 
    }
    var toLanguage by remember { 
        mutableStateOf(currentChat?.translateLanguage ?: SupportedLanguage.ENGLISH) 
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when {
                    isNewChat -> "새 번역 만들기"
                    showTitleEdit -> "번역창 설정"
                    else -> "언어 설정"
                },
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 새 채팅이거나 타이틀 편집 모드일 때 타이틀 입력 필드 표시
                if (isNewChat || showTitleEdit) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "채팅방 이름",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { Text("타이틀") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = TossGray300,
                                focusedBorderColor = TossInputMessage
                            )
                        )
                    }
                }
                
                // 양방향 번역 안내
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = TossInputMessage.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "양방향 번역",
                            tint = TossInputMessage
                        )
                        Text(
                            text = "양방향 번역이 가능해요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TossInputMessage
                        )
                    }
                }
                
                // 언어 선택
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LanguageDropdown(
                        label = "언어 1",
                        selectedLanguage = fromLanguage,
                        onLanguageSelected = { fromLanguage = it },
                        excludeLanguage = toLanguage
                    )
                    
                    LanguageDropdown(
                        label = "언어 2", 
                        selectedLanguage = toLanguage,
                        onLanguageSelected = { toLanguage = it },
                        excludeLanguage = fromLanguage
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    when {
                        isNewChat -> {
                            val finalTitle = title.ifBlank { 
                                "${fromLanguage.displayName} ↔ ${toLanguage.displayName}" 
                            }
                            onChatCreated(finalTitle, fromLanguage, toLanguage)
                        }
                        showTitleEdit -> {
                            val finalTitle = title.ifBlank { 
                                "${fromLanguage.displayName} ↔ ${toLanguage.displayName}" 
                            }
                            onTitleAndLanguageChanged(finalTitle, fromLanguage, toLanguage)
                        }
                        else -> {
                            onLanguageChanged(fromLanguage, toLanguage)
                        }
                    }
                },
                enabled = fromLanguage != toLanguage
            ) {
                Text(
                    text = if (isNewChat) "만들기" else "저장", 
                    color = TossInputMessage
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdown(
    label: String,
    selectedLanguage: SupportedLanguage,
    onLanguageSelected: (SupportedLanguage) -> Unit,
    excludeLanguage: SupportedLanguage
) {
    var expanded by remember { mutableStateOf(false) }
    val availableLanguages = SupportedLanguage.values().filter { it != excludeLanguage }
    
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = "${getLanguageFlag(selectedLanguage.code)} ${selectedLanguage.displayName}",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = TossGray300,
                    focusedBorderColor = TossInputMessage
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableLanguages.forEach { language ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = getLanguageFlag(language.code),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = language.displayName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        },
                        onClick = {
                            onLanguageSelected(language)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private fun getLanguageFlag(languageCode: String): String {
    return when (languageCode) {
        "ko" -> "🇰🇷"
        "en" -> "🇺🇸"
        "ja" -> "🇯🇵"
        "zh" -> "🇨🇳"
        else -> "🌍"
    }
} 