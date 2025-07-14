package com.example.trat.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.trat.data.entities.Chat
import com.example.trat.presentation.components.ChatListDrawer
import com.example.trat.presentation.components.ChatListItem
import com.example.trat.presentation.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToChat: (String) -> Unit,
    onNavigateToCreateChat: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chats by viewModel.chats.collectAsStateWithLifecycle()
    
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // 에러 메시지 처리
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            // 에러 처리 (스낵바나 토스트로 표시 가능)
            viewModel.clearError()
        }
    }
    
    // 채팅 생성 완료 후 네비게이션
    LaunchedEffect(uiState.lastCreatedChatId) {
        uiState.lastCreatedChatId?.let { chatId ->
            onNavigateToChat(chatId)
            viewModel.clearLastCreatedChatId()
        }
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatListDrawer(
                chats = chats,
                onChatClick = { chatId ->
                    scope.launch {
                        drawerState.close()
                        onNavigateToChat(chatId)
                    }
                },
                onCreateChatClick = {
                    scope.launch {
                        drawerState.close()
                        onNavigateToCreateChat()
                    }
                },
                onDeleteChat = { chatId ->
                    viewModel.deleteChat(chatId)
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("빠른번역") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "메뉴")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToCreateChat
                ) {
                    Icon(Icons.Default.Add, contentDescription = "새 채팅")
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (chats.isEmpty()) {
                    // 채팅이 없는 상태
                    EmptyState(
                        onCreateChatClick = onNavigateToCreateChat
                    )
                } else {
                    // 채팅 목록 표시
                    ChatList(
                        chats = chats,
                        onChatClick = onNavigateToChat,
                        onDeleteChat = { chatId ->
                            viewModel.deleteChat(chatId)
                        }
                    )
                }
                
                // 로딩 상태
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    onCreateChatClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🌍",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "빠른번역에 오신 걸 환영해요!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "오프라인에서도 빠르게\n번역할 수 있어요",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onCreateChatClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("첫 번째 채팅 만들기")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "• 한국어, 영어, 일본어, 중국어 지원\n• 인터넷 없이도 번역 가능\n• 채팅 형태로 편리하게",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChatList(
    chats: List<Chat>,
    onChatClick: (String) -> Unit,
    onDeleteChat: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "내 채팅방",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(chats, key = { it.id }) { chat ->
            ChatListItem(
                chat = chat,
                onClick = { onChatClick(chat.id) },
                onDelete = { onDeleteChat(chat.id) }
            )
        }
    }
} 