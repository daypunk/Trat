package com.example.trat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.trat.presentation.navigation.TratNavigation
import com.example.trat.ui.theme.TratTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 스플래시 스크린 설치
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TratTheme {
                TratNavigation()
            }
        }
    }
}
