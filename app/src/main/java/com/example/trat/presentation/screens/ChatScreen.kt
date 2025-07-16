package com.example.trat.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.trat.data.entities.Chat
import com.example.trat.data.entities.Message
import com.example.trat.presentation.components.MessageBubble
import com.example.trat.presentation.viewmodels.ChatViewModel
import com.example.trat.presentation.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.res.painterResource
import com.example.trat.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.trat.ui.theme.TossGray300
import com.example.trat.ui.theme.TossInputMessage
import androidx.compose.foundation.shape.CircleShape
import com.example.trat.data.models.SupportedLanguage
import com.example.trat.presentation.components.LanguageSettingsDialog
import com.airbnb.lottie.compose.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.DisposableEffect


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    onNavigateToChat: (String) -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentChat by viewModel.currentChat.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val chats by mainViewModel.chats.collectAsStateWithLifecycle()
    
    // STT 상태
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val recognizedText by viewModel.recognizedText.collectAsStateWithLifecycle()
    val sttError by viewModel.sttError.collectAsStateWithLifecycle()
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // 사이드 드로어 상태
    var showRightDrawer by remember { mutableStateOf(false) }
    
    // 검색 관련 상태
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(emptyList<Int>()) }
    var currentSearchIndex by remember { mutableStateOf(0) }
    
    // 메뉴 관련 상태
    var isEditMode by remember { mutableStateOf(false) }
    
    // 언어 재설정 다이얼로그 상태
    var showLanguageSettingsDialog by remember { mutableStateOf(false) }
    
    // 새 채팅 생성 다이얼로그 상태
    var showNewChatDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    // 모델 다운로드 상태 확인 및 마지막 채팅 저장
    LaunchedEffect(chatId) {
        if (chatId.isNotEmpty()) {
            viewModel.initializeChat(chatId)
            // 마지막 채팅 ID 저장
            val sharedPrefs = context.getSharedPreferences("trat_prefs", android.content.Context.MODE_PRIVATE)
            sharedPrefs.edit().putString("last_chat_id", chatId).apply()
        }
    }
    
    // 새 메시지가 추가되면 스크롤
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && !isSearching) {
            scope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    
    // 에러 메시지 토스트 표시
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearError() // 토스트 표시 후 에러 메시지 클리어
        }
    }
    
    // STT 에러 메시지 토스트 표시
    LaunchedEffect(sttError) {
        sttError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearSttError()
        }
    }
    
    // 음성 인식 결과 처리
    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotEmpty()) {
            viewModel.appendRecognizedText(recognizedText)
        }
    }
    
    // 검색 실행
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            val results = mutableListOf<Int>()
            messages.forEachIndexed { index, message ->
                if (message.originalText.contains(searchQuery, ignoreCase = true) ||
                    message.translatedText.contains(searchQuery, ignoreCase = true)) {
                    results.add(index)
                }
            }
            searchResults = results
            currentSearchIndex = if (results.isNotEmpty()) 0 else -1
            
            if (results.isNotEmpty()) {
                scope.launch {
                    listState.animateScrollToItem(results[0])
                }
            }
        } else {
            searchResults = emptyList()
            currentSearchIndex = -1
        }
    }
    
    // 메인 컨텐츠
    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        topBar = {
            if (isSearching) {
                SearchTopAppBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onSearchClose = { 
                        isSearching = false
                        searchQuery = ""
                    },
                    searchResults = searchResults,
                    currentSearchIndex = currentSearchIndex,
                    onNavigateToNext = {
                        if (searchResults.isNotEmpty()) {
                            currentSearchIndex = (currentSearchIndex + 1) % searchResults.size
                            scope.launch {
                                listState.animateScrollToItem(searchResults[currentSearchIndex])
                            }
                        }
                    },
                    onNavigateToPrevious = {
                        if (searchResults.isNotEmpty()) {
                            currentSearchIndex = if (currentSearchIndex == 0) {
                                searchResults.size - 1
                            } else {
                                currentSearchIndex - 1
                            }
                            scope.launch {
                                listState.animateScrollToItem(searchResults[currentSearchIndex])
                            }
                        }
                    }
                )
            } else {
                Surface(
                    shadowElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                TopAppBar(
                    title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // 언어 뱃지
                                currentChat?.let { chat ->
                                    Card(
                                        modifier = Modifier.clickable { showLanguageSettingsDialog = true },
                                        colors = CardDefaults.cardColors(
                                            containerColor = TossInputMessage.copy(alpha = 0.1f)
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = getLanguageFlag(chat.nativeLanguage.code),
                                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                                            )
                                            Icon(
                                                imageVector = Icons.Rounded.Refresh,
                                                contentDescription = "양방향 번역",
                                                tint = TossInputMessage,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = getLanguageFlag(chat.translateLanguage.code),
                                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                                            )
                                        }
                                    }
                                }
                                
                                // 간단한 채팅방 제목만 표시
                                Text(
                                    text = currentChat?.title ?: "채팅",
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                        }
                    },
                    actions = {
                            IconButton(onClick = { isSearching = true }) {
                                Icon(Icons.Rounded.Search, contentDescription = "검색")
                            }
                                                          IconButton(onClick = { showRightDrawer = true }) {
                                Icon(Icons.Rounded.Menu, contentDescription = "메뉴")
                        }
                    }
                )
                }
            }
        },
        bottomBar = {
            ChatInputBar(
                inputText = uiState.inputText,
                onInputChange = viewModel::updateInputText,
                onSendMessage = { viewModel.sendMessage(uiState.inputText) },
                isTranslating = uiState.isTranslating,
                isModelReady = uiState.isModelReady,
                isListening = isListening,
                onStartSpeechToText = viewModel::startSpeechToText,
                onStopSpeechToText = viewModel::stopSpeechToText
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (messages.isEmpty()) {
                // 빈 채팅 상태
                EmptyChatState(
                    currentChat = currentChat,
                    isModelReady = uiState.isModelReady
                )
            } else {
                // 메시지 목록
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageBubble(
                            message = message,
                            isHighlighted = searchResults.isNotEmpty() && 
                                searchResults.getOrNull(currentSearchIndex) == messages.indexOf(message),
                            searchQuery = if (isSearching) searchQuery else ""
                        )
                    }
                }
            }
            
            // 에러 메시지 표시
            uiState.errorMessage?.let { error ->
                LaunchedEffect(error) {
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearError()
                }
                
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("확인")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
        
        // 드로어가 열려있을 때 배경 오버레이
        if (showRightDrawer) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showRightDrawer = false }
            )
        }
        
        // 오른쪽 사이드 드로어
        AnimatedVisibility(
            visible = showRightDrawer,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(320.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                ChatMenuDrawer(
                chats = chats,
                currentChatId = chatId,
                isEditMode = isEditMode,
                onEditModeToggle = { isEditMode = it },
                onChatClick = { selectedChatId ->
                        showRightDrawer = false
                    isEditMode = false
                    if (selectedChatId != chatId) {
                            onNavigateToChat(selectedChatId)
                    }
                },
                onCreateChatClick = {
                        showRightDrawer = false
                    isEditMode = false
                        showNewChatDialog = true
                },
                onDeleteChat = { chatIdToDelete ->
                    mainViewModel.deleteChat(chatIdToDelete)
                    }
                )
            }
        }
        
        // 언어 설정 다이얼로그
        if (showLanguageSettingsDialog) {
            LanguageSettingsDialog(
                currentChat = currentChat,
                showTitleEdit = true,
                onDismiss = { showLanguageSettingsDialog = false },
                onTitleAndLanguageChanged = { title, nativeLanguage, translateLanguage ->
                    showLanguageSettingsDialog = false
                    // 현재 채팅의 타이틀과 언어 설정 업데이트
                    currentChat?.let { chat ->
                        mainViewModel.updateChatTitleAndLanguages(
                            chatId = chat.id, 
                            title = title, 
                            nativeLanguage = nativeLanguage, 
                            translateLanguage = translateLanguage
                        ) {
                            // 데이터베이스 업데이트 완료 후 ChatViewModel 새로고침
                            viewModel.refreshCurrentChat()
                        }
                    }
                }
            )
        }
        
        // 새 채팅 생성 다이얼로그
        if (showNewChatDialog) {
            LanguageSettingsDialog(
                currentChat = null,
                isNewChat = true,
                onDismiss = { showNewChatDialog = false },
                onChatCreated = { title, nativeLanguage, translateLanguage ->
                    showNewChatDialog = false
                    mainViewModel.createChat(title, nativeLanguage, translateLanguage) { chatId ->
                        // 생성된 채팅으로 이동
                        onNavigateToChat(chatId)
                    }
                }
            )
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

@Composable
private fun EmptyChatState(
    currentChat: Chat?,
    isModelReady: Boolean
) {
    // Lottie 애니메이션 설정
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.trat_lottie))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                !isModelReady -> {
                    // 모델이 준비되지 않았을 때는 빈 상태 표시 (다이얼로그에서 처리)
                    Text(
                        text = "언어 모델을 설정해주세요",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                else -> {
                    // 로티 애니메이션 (지구본 대신)
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.size(120.dp),
                    )
                    Text(
                        text = "메시지를 입력하세요",
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isTranslating: Boolean,
    isModelReady: Boolean,
    isListening: Boolean,
    onStartSpeechToText: () -> Unit,
    onStopSpeechToText: () -> Unit
) {
    val context = LocalContext.current
    
    // 마이크 클릭 시 즉시 피드백을 위한 로컬 상태
    var showPopoverImmediately by remember { mutableStateOf(false) }
    
    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onStartSpeechToText()
        } else {
            showPopoverImmediately = false // 권한 거부 시 팝오버 숨김
            Toast.makeText(context, "음성 인식을 위해 마이크 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }
    
    // isListening 상태 변화 감지하여 로컬 상태 동기화
    LaunchedEffect(isListening) {
        if (!isListening) {
            showPopoverImmediately = false
        }
    }
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier
            .shadow(
                elevation = 8.dp,
                spotColor = Color.Black.copy(alpha = 0.3f),
                ambientColor = Color.Black.copy(alpha = 0.15f)
            )
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("번역할 메시지를 입력하세요") },
                maxLines = 4,
                shape = RoundedCornerShape(20.dp),
                enabled = !isTranslating && isModelReady,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = TossGray300,
                    focusedBorderColor = TossInputMessage,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                ),
                trailingIcon = {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                if (isListening) {
                                    onStopSpeechToText()
                                } else {
                                    // 권한 확인 후 음성 인식 시작
                                    when (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    )) {
                                        PackageManager.PERMISSION_GRANTED -> {
                                            showPopoverImmediately = true // 즉시 팝오버 표시
                                            onStartSpeechToText()
                                        }
                                        else -> {
                                            showPopoverImmediately = true // 권한 요청 중에도 팝오버 표시
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                }
                            },
                            enabled = !isTranslating && isModelReady,
                            modifier = Modifier.size(48.dp) // 고정 크기로 위치 일관성 보장
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(20.dp) // 아이콘 영역 고정
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id = if (isListening) R.drawable.ic_mic_off else R.drawable.ic_mic
                                    ),
                                    contentDescription = if (isListening) "음성 인식 중지" else "음성 인식 시작",
                                    tint = if (isListening) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        // 음성 인식 중 popover - 클릭 즉시 또는 인식 중에 표시
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isListening || showPopoverImmediately,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300)),
                            exit = slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        ) {
                            SpeechRecognitionPopover()
                        }
                    }
                }
            )
            
            val isEnabled = inputText.isNotBlank() && isModelReady && !isTranslating
            FloatingActionButton(
                onClick = { if (isEnabled) onSendMessage() },
                modifier = Modifier.size(48.dp),
                containerColor = if (isEnabled) TossInputMessage else TossGray300,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp,
                    hoveredElevation = 0.dp
                )
            ) {
                if (isTranslating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Rounded.Send, 
                        contentDescription = "전송",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeechRecognitionPopover() {
    // 로딩 애니메이션 설정
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_lottie))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    
    Card(
        modifier = Modifier
            .offset(y = (-76).dp, x = (-12).dp) // 마이크 아이콘 중앙에 맞춤
            .size(72.dp, 40.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.12f) // 옅은 그린
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopAppBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClose: () -> Unit,
    searchResults: List<Int>,
    currentSearchIndex: Int,
    onNavigateToNext: () -> Unit,
    onNavigateToPrevious: () -> Unit
) {
    val context = LocalContext.current
    var lastShowToastTime by remember { mutableStateOf(0L) }
    
    // 디바운싱을 위한 LaunchedEffect
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            // 입력 후 500ms 기다림 (디바운싱)
            kotlinx.coroutines.delay(500)
            
            // 현재 시간
            val currentTime = System.currentTimeMillis()
            
            // 검색 결과가 없고, 마지막 토스트 표시 후 2초 이상 지났으면 토스트 표시
            if (searchResults.isEmpty() && currentTime - lastShowToastTime > 2000) {
            Toast.makeText(context, "검색 결과가 없습니다", Toast.LENGTH_SHORT).show()
                lastShowToastTime = currentTime
            }
        }
    }
    
    TopAppBar(
        title = {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { 
                    Text(
                        "번역 메시지 검색...",
                        style = MaterialTheme.typography.bodyMedium
                    ) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp)
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = TossGray300,
                    focusedBorderColor = TossInputMessage,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onSearchClose) {
                Icon(Icons.Rounded.Close, contentDescription = "검색 닫기")
            }
        },
        actions = {
            if (searchResults.isNotEmpty()) {
                Text(
                    text = "${currentSearchIndex + 1}/${searchResults.size}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = onNavigateToPrevious) {
                    Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = "이전 결과")
                }
                IconButton(onClick = onNavigateToNext) {
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "다음 결과")
                }
            }
        }
    )
} 

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatMenuDrawer(
    chats: List<Chat>,
    currentChatId: String,
    isEditMode: Boolean,
    onEditModeToggle: (Boolean) -> Unit,
    onChatClick: (String) -> Unit,
    onCreateChatClick: () -> Unit,
    onDeleteChat: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 64.dp)
    ) {
        // 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.trat_logo_light),
                contentDescription = "로고",
                modifier = Modifier.height(24.dp).width(95.dp),
                tint = Color.Unspecified  // 원본 색상 유지
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (!isEditMode) {
                    TextButton(onClick = onCreateChatClick) {
                        Text(
                            text = "+ 새 번역", 
                            color = TossInputMessage,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                TextButton(
                    onClick = { onEditModeToggle(!isEditMode) }
                ) {
                    Text(
                        text = if (isEditMode) "완료" else "편집",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isEditMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 채팅 리스트
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(chats.size, key = { chats[it].id }) { index ->
                val chat = chats[index]
                
                ChatItemInMenu(
                    chat = chat,
                    isCurrentChat = chat.id == currentChatId,
                    isEditMode = isEditMode,
                    onClick = { onChatClick(chat.id) },
                    onDelete = { onDeleteChat(chat.id) }
                )
                
                // 마지막 아이템이 아니면 구분선 추가
                if (index < chats.size - 1) {
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatItemInMenu(
    chat: Chat,
    isCurrentChat: Boolean,
    isEditMode: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!isEditMode) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentChat) {
                TossInputMessage.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (!isEditMode) {
                            Modifier.clickable { onClick() }
                        } else {
                            Modifier
                        }
                    ),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = chat.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp),
                    fontWeight = if (isCurrentChat) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isCurrentChat) TossInputMessage else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // 언어 정보 표시 (Refresh 아이콘 포함)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = getLanguageFlag(chat.nativeLanguage.code),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                    )
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "양방향 번역",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = getLanguageFlag(chat.translateLanguage.code),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                    )
                }
            }
            
            if (isEditMode) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "삭제",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else if (isCurrentChat) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = TossInputMessage,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}



