package com.example.trat.utils

/**
 * 앱 전역에서 사용되는 상수들을 중앙 관리
 * 카테고리별로 구조화하여 유지보수성 향상
 */
object Constants {
    
    // ============ 번역 관련 상수 ============
    object Translation {
        const val TIMEOUT_MS = 10000L
        const val MODEL_DOWNLOAD_TIMEOUT_MS = 30000L
        const val MODEL_SIZE_MB = 30
        const val MIN_TEXT_LENGTH_FOR_DETECTION = 2
        const val TRANSLATION_DIFFERENCE_THRESHOLD = 0.2f // 20% 차이
        const val LANGUAGE_RATIO_THRESHOLD = 0.3f // 30% 비율
    }
    
    // ============ Unicode 범위 상수 ============
    object UnicodeRanges {
        // 한국어
        const val KOREAN_START = '\uAC00'
        const val KOREAN_END = '\uD7AF'
        const val KOREAN_JAMO_START = '\u1100'
        const val KOREAN_JAMO_END = '\u11FF'
        const val KOREAN_COMPAT_START = '\u3130'
        const val KOREAN_COMPAT_END = '\u318F'
        
        // 일본어
        const val HIRAGANA_START = '\u3040'
        const val HIRAGANA_END = '\u309F'
        const val KATAKANA_START = '\u30A0'
        const val KATAKANA_END = '\u30FF'
        
        // 중국어
        const val CJK_START = '\u4E00'
        const val CJK_END = '\u9FAF'
        const val CJK_EXT_A_START = '\u3400'
        const val CJK_EXT_A_END = '\u4DBF'
        const val CJK_COMPAT_START = '\uF900'
        const val CJK_COMPAT_END = '\uFAFF'
        
        // 영어
        const val LATIN_UPPER_START = 'A'
        const val LATIN_UPPER_END = 'Z'
        const val LATIN_LOWER_START = 'a'
        const val LATIN_LOWER_END = 'z'
    }
    
    // ============ 앱 생명주기 상수 ============
    object App {
        const val SPLASH_MIN_DURATION_MS = 1500L
        const val SPLASH_DELAY_MS = 500L
        const val FLOW_SUBSCRIPTION_TIMEOUT_MS = 5000L
    }
    
    // ============ 캐시 관련 상수 ============
    object Cache {
        const val MEMORY_CACHE_SIZE = 100
        const val DISK_CACHE_SIZE = 500
        const val CACHE_CLEANUP_BATCH_SIZE = 50
    }
    
    // ============ SharedPreferences 키 ============
    object Prefs {
        const val NAME = "trat_prefs"
        const val LAST_CHAT_ID = "last_chat_id"
    }
    
    // ============ 로그 TAG 상수 ============
    object LogTags {
        const val TRANSLATION_USE_CASE = "TranslationUseCase"
        const val CHAT_VIEW_MODEL = "ChatViewModel"
        const val MAIN_VIEW_MODEL = "MainViewModel"
        const val MESSAGE_TRANSLATION_USE_CASE = "MessageTranslationUseCase"
        const val TRANSLATION_CACHE = "TranslationCacheService"
        const val LANGUAGE_DETECTION = "LanguageDetectionUseCase"
        const val MODEL_MANAGER = "ModelManager"
        const val TRANSLATION_SERVICE = "TranslationService"
    }
    
    // ============ 에러 메시지 ============
    object Errors {
        const val TRANSLATION_FAILED = "번역에 실패했습니다"
        const val MODEL_DOWNLOAD_FAILED = "언어 모델 다운로드에 실패했습니다"
        const val NETWORK_REQUIRED = "모델 다운로드를 위해 인터넷 연결이 필요합니다"
        const val UNSUPPORTED_LANGUAGE = "지원하지 않는 언어입니다"
        const val MODEL_CORRUPTED = "언어 모델에 문제가 있어 재설치합니다"
        const val MODEL_REINSTALL_FAILED = "모델 재설치에 실패했습니다. 앱을 재시작해주세요"
        const val CHAT_CREATION_FAILED = "채팅방 생성에 실패했어요"
        const val CHAT_DELETION_FAILED = "채팅방 삭제에 실패했어요"
        const val CHAT_LOAD_FAILED = "채팅방 로드 중 오류가 발생했어요"
        const val TRANSLATION_REQUEST_FAILED = "번역에 실패했어요"
        const val SETTINGS_UPDATE_FAILED = "설정 업데이트 중 오류가 발생했어요"
        const val CHAT_CLEAR_FAILED = "채팅 기록 삭제에 실패했어요"
        const val CHAT_NOT_FOUND = "채팅방을 찾을 수 없어요"
        const val EMPTY_TEXT = "번역할 텍스트가 비어있습니다"
    }
    
    // ============ 성공 메시지 ============
    object Success {
        const val MODEL_DOWNLOADED = "언어 모델이 다운로드되었습니다"
        const val TRANSLATION_COMPLETED = "번역이 완료되었습니다"
    }
    
    // ============ 기본값 상수 ============
    object Defaults {
        const val CHAT_TITLE = "이름없는 번역챗"
        const val DOWNLOAD_PROGRESS = 0f
        const val INITIAL_DOWNLOAD_PROGRESS = 0f
        const val COMPLETED_DOWNLOAD_PROGRESS = 1f
    }
    
    // ============ 하위 호환성을 위한 레거시 상수 ============
    @Deprecated("Use Translation.TIMEOUT_MS instead", ReplaceWith("Translation.TIMEOUT_MS"))
    const val TRANSLATION_TIMEOUT_MS = Translation.TIMEOUT_MS
    
    @Deprecated("Use Translation.MODEL_DOWNLOAD_TIMEOUT_MS instead", ReplaceWith("Translation.MODEL_DOWNLOAD_TIMEOUT_MS"))
    const val MODEL_DOWNLOAD_TIMEOUT_MS = Translation.MODEL_DOWNLOAD_TIMEOUT_MS
    
    @Deprecated("Use Translation.MODEL_SIZE_MB instead", ReplaceWith("Translation.MODEL_SIZE_MB"))
    const val MODEL_SIZE_MB = Translation.MODEL_SIZE_MB
    
    @Deprecated("Use LogTags.TRANSLATION_USE_CASE instead", ReplaceWith("LogTags.TRANSLATION_USE_CASE"))
    const val TAG_TRANSLATION_USE_CASE = LogTags.TRANSLATION_USE_CASE
    
    @Deprecated("Use LogTags.CHAT_VIEW_MODEL instead", ReplaceWith("LogTags.CHAT_VIEW_MODEL"))
    const val TAG_CHAT_VIEW_MODEL = LogTags.CHAT_VIEW_MODEL
    
    @Deprecated("Use LogTags.MAIN_VIEW_MODEL instead", ReplaceWith("LogTags.MAIN_VIEW_MODEL"))
    const val TAG_MAIN_VIEW_MODEL = LogTags.MAIN_VIEW_MODEL
    
    @Deprecated("Use LogTags.MESSAGE_TRANSLATION_USE_CASE instead", ReplaceWith("LogTags.MESSAGE_TRANSLATION_USE_CASE"))
    const val TAG_MESSAGE_TRANSLATION_USE_CASE = LogTags.MESSAGE_TRANSLATION_USE_CASE
    
    @Deprecated("Use LogTags.TRANSLATION_CACHE instead", ReplaceWith("LogTags.TRANSLATION_CACHE"))
    const val TAG_TRANSLATION_CACHE = LogTags.TRANSLATION_CACHE
    
    @Deprecated("Use Errors.TRANSLATION_FAILED instead", ReplaceWith("Errors.TRANSLATION_FAILED"))
    const val ERROR_TRANSLATION_FAILED = Errors.TRANSLATION_FAILED
    
    @Deprecated("Use Errors.MODEL_DOWNLOAD_FAILED instead", ReplaceWith("Errors.MODEL_DOWNLOAD_FAILED"))
    const val ERROR_MODEL_DOWNLOAD_FAILED = Errors.MODEL_DOWNLOAD_FAILED
    
    @Deprecated("Use Errors.NETWORK_REQUIRED instead", ReplaceWith("Errors.NETWORK_REQUIRED"))
    const val ERROR_NETWORK_REQUIRED = Errors.NETWORK_REQUIRED
    
    @Deprecated("Use Errors.UNSUPPORTED_LANGUAGE instead", ReplaceWith("Errors.UNSUPPORTED_LANGUAGE"))
    const val ERROR_UNSUPPORTED_LANGUAGE = Errors.UNSUPPORTED_LANGUAGE
    
    @Deprecated("Use Errors.MODEL_CORRUPTED instead", ReplaceWith("Errors.MODEL_CORRUPTED"))
    const val ERROR_MODEL_CORRUPTED = Errors.MODEL_CORRUPTED
    
    @Deprecated("Use Errors.MODEL_REINSTALL_FAILED instead", ReplaceWith("Errors.MODEL_REINSTALL_FAILED"))
    const val ERROR_MODEL_REINSTALL_FAILED = Errors.MODEL_REINSTALL_FAILED
    
    @Deprecated("Use Success.MODEL_DOWNLOADED instead", ReplaceWith("Success.MODEL_DOWNLOADED"))
    const val SUCCESS_MODEL_DOWNLOADED = Success.MODEL_DOWNLOADED
    
    @Deprecated("Use Success.TRANSLATION_COMPLETED instead", ReplaceWith("Success.TRANSLATION_COMPLETED"))
    const val SUCCESS_TRANSLATION_COMPLETED = Success.TRANSLATION_COMPLETED
} 