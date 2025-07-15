package com.example.trat.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trat.presentation.screens.ChatScreen
import com.example.trat.presentation.screens.MainScreen
import com.example.trat.presentation.viewmodels.MainViewModel

// 네비게이션 라우트
object TratRoutes {
    const val MAIN = "main"
    const val CHAT = "chat/{chatId}"
    
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
                    navController.navigate(TratRoutes.chat(chatId)) {
                        popUpTo(TratRoutes.MAIN) { inclusive = true }
                    }
                }
            )
        }
        
        composable(TratRoutes.CHAT) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
            ChatScreen(
                chatId = chatId,
                onNavigateToChat = { newChatId ->
                    navController.navigate(TratRoutes.chat(newChatId)) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        

    }
} 