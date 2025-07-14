package com.example.trat.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trat.presentation.screens.ChatScreen
import com.example.trat.presentation.screens.CreateChatScreen
import com.example.trat.presentation.screens.MainScreen

// 네비게이션 라우트 정의
object TratRoutes {
    const val MAIN = "main"
    const val CHAT = "chat/{chatId}"
    const val CREATE_CHAT = "create_chat"
    
    fun chat(chatId: String) = "chat/$chatId"
}

@Composable
fun TratNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = TratRoutes.MAIN
    ) {
        composable(TratRoutes.MAIN) {
            MainScreen(
                onNavigateToChat = { chatId ->
                    navController.navigate(TratRoutes.chat(chatId))
                },
                onNavigateToCreateChat = {
                    navController.navigate(TratRoutes.CREATE_CHAT)
                }
            )
        }
        
        composable(TratRoutes.CHAT) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
            ChatScreen(
                chatId = chatId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(TratRoutes.CREATE_CHAT) {
            CreateChatScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onChatCreated = { chatId ->
                    navController.navigate(TratRoutes.chat(chatId)) {
                        popUpTo(TratRoutes.MAIN)
                    }
                }
            )
        }
    }
} 