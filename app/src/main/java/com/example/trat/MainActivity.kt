package com.example.trat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.trat.presentation.navigation.TratNavigation
import com.example.trat.ui.theme.TratTheme
import com.example.trat.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var keepSplashOpened = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // 스플래시 화면 설정
        val splashScreen = installSplashScreen()
        
        // 앱 로딩이 완료될 때까지 스플래시 화면 유지
        splashScreen.setKeepOnScreenCondition {
            keepSplashOpened
        }
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 시스템 바 설정 (네비게이션 바는 표시하여 IME와의 인셋 충돌 방지)
        configureSystemBars()
        
        // 앱 초기화 시뮬레이션 (실제로는 데이터베이스, 설정 로딩 등)
        lifecycleScope.launch {
            // 최소 딜레이 동안 스플래시 화면 표시
            delay(Constants.App.SPLASH_MIN_DURATION_MS)
            
            // 추가 초기화 작업이 있다면 여기서 수행
            // 예: 데이터베이스 초기화, 설정 로드 등
            
            // 스플래시 화면 종료
            keepSplashOpened = false
        }
        
        setContent {
            TratTheme {
                TratNavigation()
            }
        }
    }
    
    /**
     * 포커스 복귀 시 시스템 UI 재숨김
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            configureSystemBars() // 포커스 복귀 시 시스템 바 설정 유지
        }
    }
    
    /**
     * 시스템 바 구성: 네비게이션 바를 표시해 IME와 정상적으로 붙도록 함
     */
    private fun configureSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }
    }
}
