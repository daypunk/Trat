package com.example.trat.utils

object Constants {
    // Translation 관련 상수
    const val TRANSLATION_TIMEOUT_MS = 10000L
    const val MODEL_DOWNLOAD_TIMEOUT_MS = 30000L
    
    // 번역 모델 크기 (약 30MB)
    const val MODEL_SIZE_MB = 30
    
    // 에러 메시지
    const val ERROR_TRANSLATION_FAILED = "번역에 실패했습니다"
    const val ERROR_MODEL_DOWNLOAD_FAILED = "언어 모델 다운로드에 실패했습니다"
    const val ERROR_NETWORK_REQUIRED = "모델 다운로드를 위해 인터넷 연결이 필요합니다"
    const val ERROR_UNSUPPORTED_LANGUAGE = "지원하지 않는 언어입니다"
    const val ERROR_MODEL_CORRUPTED = "언어 모델에 문제가 있어 재설치합니다"
    const val ERROR_MODEL_REINSTALL_FAILED = "모델 재설치에 실패했습니다. 앱을 재시작해주세요"
    
    // 성공 메시지
    const val SUCCESS_MODEL_DOWNLOADED = "언어 모델이 다운로드되었습니다"
    const val SUCCESS_TRANSLATION_COMPLETED = "번역이 완료되었습니다"
} 