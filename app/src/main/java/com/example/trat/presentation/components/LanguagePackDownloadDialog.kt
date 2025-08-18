package com.example.trat.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trat.R
import com.example.trat.data.models.SupportedLanguage
import com.example.trat.ui.theme.InputMessage

@Composable
fun LanguagePackDownloadDialog(
    language: SupportedLanguage,
    onDownloadClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_tts),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = InputMessage
                )
                Text(
                    text = "음성 출력 기능",
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 언어팩 안내 카드
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = InputMessage.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = getLanguageFlag(language.code),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${language.displayName} 음성 출력",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = InputMessage
                            )
                            Text(
                                text = "음성팩 다운로드 필요",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                
                // 설명 텍스트
                Text(
                    text = "${language.displayName} 음성팩을 다운로드하시겠습니까?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDownloadClick
            ) {
                Text(
                    text = "다운로드",
                    color = InputMessage
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("나중에")
            }
        }
    )
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
