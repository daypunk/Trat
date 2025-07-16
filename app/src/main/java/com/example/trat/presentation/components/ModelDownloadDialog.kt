package com.example.trat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.*
import com.example.trat.R

@Composable
fun InitialModelDownloadDialog(
    downloadProgress: Float? = null,
    isDownloading: Boolean = false,
    onStartDownload: () -> Unit,
    onComplete: () -> Unit = {}
) {
    // Lottie 애니메이션 설정
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.trat_lottie))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Dialog(
        onDismissRequest = { /* 다이얼로그를 닫을 수 없도록 함 */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(350.dp), // 고정 높이
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF8F9FA) // 토스 느낌의 그레이스케일 배경
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 로티 애니메이션 (로고 대신)
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier
                        .size(80.dp),
                )
                
                // 메인 타이틀
                Text(
                    text = "트랫에 오신 걸 환영해요!",
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF2C2C2E)
                )
                
                // 설명 텍스트
                Text(
                    text = "빠른 오프라인 번역을 위해\n언어 모델이 필요해요",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                    textAlign = TextAlign.Center,
                    color = Color(0xFF6C6C70),
                    lineHeight = 22.sp
                )
                
                // 다운로드 진행 상태 (프로그레스바 제거)
                if (isDownloading) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF8E8E93).copy(alpha = 0.1f) // 그레이스케일
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color(0xFF6C6C70), // 그레이스케일
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (downloadProgress != null) {
                                        "다운로드 중... ${(downloadProgress * 100).toInt()}%"
                                    } else {
                                        "모델을 다운로드하고 있어요..."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF6C6C70) // 그레이스케일
                                )
                            }
                        }
                    }
                } else {
                    // 시작 버튼 (그레이스케일)
                    Button(
                        onClick = onStartDownload,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C6C70) // 그레이스케일
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "다운로드 시작하기",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
                
                // 추가 정보
                if (!isDownloading) {
                    Text(
                        text = "WiFi 연결 권장 • 약 120MB",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF8E8E93),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

 