package com.example.trat.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.trat.presentation.viewmodels.MainViewModel
import androidx.compose.ui.unit.dp
import com.example.trat.data.models.SupportedLanguage
import com.example.trat.presentation.components.LanguageSettingsDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToChat: (String) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val chats by viewModel.chats.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // 로딩 상태 관리
    var isInitialized by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) }
    var showNewChatDialog by remember { mutableStateOf(false) }
    
    // 저장된 마지막 채팅 ID 가져오기
    val sharedPrefs = context.getSharedPreferences("trat_prefs", android.content.Context.MODE_PRIVATE)
    val lastChatId = sharedPrefs.getString("last_chat_id", null)
    
    // 채팅 목록 로딩 완료 후 네비게이션
    LaunchedEffect(chats) {
        // 이미 네비게이션 했으면 중복 실행 방지
        if (hasNavigated) return@LaunchedEffect
        
        // 로딩 완료를 기다림 (채팅 데이터가 로드될 때까지)
        if (!isInitialized) {
            kotlinx.coroutines.delay(200) // 최소 지연
            isInitialized = true
        }
        
        when {
            // 저장된 마지막 채팅이 있고, 해당 채팅이 존재하면 이동
            lastChatId != null && chats.any { it.id == lastChatId } -> {
                hasNavigated = true
                onNavigateToChat(lastChatId)
            }
            // 저장된 채팅이 없거나 삭제되었지만 다른 채팅이 있으면 첫 번째로 이동
            chats.isNotEmpty() -> {
                val firstChatId = chats.first().id
                // 새로운 마지막 채팅 ID 저장
                sharedPrefs.edit().putString("last_chat_id", firstChatId).apply()
                hasNavigated = true
                onNavigateToChat(firstChatId)
            }
            // 채팅이 없는 경우 - 새 채팅 생성 다이얼로그 표시
            isInitialized -> {
                hasNavigated = true
                showNewChatDialog = true
            }
        }
    }
    
    // 새 채팅 생성 다이얼로그가 표시되지 않은 경우에만 로딩 화면 표시
    if (!showNewChatDialog) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = if (!isInitialized) "앱을 시작하는 중..." else "채팅방을 로딩중입니다...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
    
    // 새 채팅 생성 다이얼로그
    if (showNewChatDialog) {
        LanguageSettingsDialog(
            currentChat = null,
            isNewChat = true,
            onDismiss = { 
                // 첫 실행 시에는 다이얼로그를 닫을 수 없도록 함 (채팅이 없는 경우)
                if (chats.isNotEmpty()) {
                    showNewChatDialog = false
                }
            },
            onChatCreated = { title, nativeLanguage, translateLanguage ->
                showNewChatDialog = false
                viewModel.createChat(title, nativeLanguage, translateLanguage) { chatId ->
                    // 생성된 채팅으로 이동
                    onNavigateToChat(chatId)
                }
            }
        )
    }
} 