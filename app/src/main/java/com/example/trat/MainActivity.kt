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
        
        // 시스템 UI 완전 숨김 (전체 화면 몰입 모드)
        hideSystemUI()
        
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
            hideSystemUI() // 포커스 복귀 시 시스템 UI 다시 숨김
        }
    }
    
    /**
     * 하단 내비게이션 바만 숨김 (상단 상태바는 유지)
     * 스와이프 표시 없음
     */
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.navigationBars()) // 하단만 숨김
            // 스와이프 표시 비활성화 (systemBarsBehavior 설정 제거)
        }
    }
}
