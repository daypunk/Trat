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
import com.example.trat.presentation.components.InitialModelDownloadDialog
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToChat: (String) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val chats by viewModel.chats.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // 로딩 상태 관리
    var isInitialized by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var modelsChecked by remember { mutableStateOf(false) }
    
    // 저장된 마지막 채팅 ID 가져오기
    val sharedPrefs = context.getSharedPreferences("trat_prefs", android.content.Context.MODE_PRIVATE)
    val lastChatId = sharedPrefs.getString("last_chat_id", null)
    
    // 앱 시작 시 모델 확인 및 다운로드 다이얼로그 표시
    LaunchedEffect(Unit) {
        delay(500) // 스플래시 화면 이후 약간의 딜레이
        
        val allModelsDownloaded = viewModel.areAllModelsDownloaded()
        modelsChecked = true
        
        if (!allModelsDownloaded && !uiState.modelsDownloaded) {
            viewModel.setShowModelDownloadDialog(true)
        }
    }
    

    
    // 채팅 목록 로딩 완료 후 네비게이션 (모델 다운로드 완료 후에만)
    LaunchedEffect(chats, uiState.modelsDownloaded, modelsChecked) {
        // 이미 네비게이션 했으면 중복 실행 방지
        if (hasNavigated) return@LaunchedEffect
        
        // 모델이 다운로드 되었거나 이미 모든 모델이 있는 경우에만 네비게이션 실행
        val shouldNavigate = (uiState.modelsDownloaded || (modelsChecked && !uiState.showModelDownloadDialog))
        if (!shouldNavigate) return@LaunchedEffect
        
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
    
    // 모델 다운로드 다이얼로그
    if (uiState.showModelDownloadDialog) {
        InitialModelDownloadDialog(
            downloadProgress = if (uiState.isDownloadingModels) uiState.downloadProgress else null,
            isDownloading = uiState.isDownloadingModels,
            onStartDownload = { 
                viewModel.downloadAllModels(context)
            },
            onComplete = {
                viewModel.setShowModelDownloadDialog(false)
            }
        )
    }
    
    // 모델 다운로드 완료 후 로딩 화면 표시 (새 채팅 다이얼로그가 표시되지 않은 경우에만)
    if (!showNewChatDialog && !uiState.showModelDownloadDialog) {
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
                // 첫 실행 시에는 앱 종료 (채팅이 없는 경우)
                if (chats.isEmpty()) {
                    (context as? androidx.activity.ComponentActivity)?.finish()
                } else {
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