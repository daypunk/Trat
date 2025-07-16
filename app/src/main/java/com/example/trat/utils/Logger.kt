package com.example.trat.utils

import android.util.Log

/**
 * 구조화된 로깅 시스템
 * - 일관된 로그 포맷 제공
 * - 로그 레벨별 관리
 * - 성능 최적화 (디버그 빌드에서만 상세 로그)
 * - 프로덕션 빌드에서는 중요한 로그만 출력
 */
object Logger {
    
    private const val PRODUCTION_MODE = false // 프로덕션 빌드 시 true로 변경
    
    /**
     * 로그 레벨 정의
     */
    enum class Level(val priority: Int) {
        VERBOSE(Log.VERBOSE),
        DEBUG(Log.DEBUG),
        INFO(Log.INFO),
        WARNING(Log.WARN),
        ERROR(Log.ERROR)
    }
    
    /**
     * 로그 카테고리 정의
     */
    enum class Category(val tag: String) {
        TRANSLATION(Constants.LogTags.TRANSLATION_USE_CASE),
        CACHE(Constants.LogTags.TRANSLATION_CACHE),
        LANGUAGE_DETECTION(Constants.LogTags.LANGUAGE_DETECTION),
        MODEL_MANAGER(Constants.LogTags.MODEL_MANAGER),
        TRANSLATION_SERVICE(Constants.LogTags.TRANSLATION_SERVICE),
        CHAT_VM(Constants.LogTags.CHAT_VIEW_MODEL),
        MAIN_VM(Constants.LogTags.MAIN_VIEW_MODEL),
        MESSAGE_TRANSLATION(Constants.LogTags.MESSAGE_TRANSLATION_USE_CASE),
        DATABASE("TratDatabase"),
        NETWORK("TratNetwork"),
        PERFORMANCE("TratPerformance")
    }
    
    // =============== 편의 메서드들 ===============
    
    /**
     * 디버그 로그 (개발 중에만 출력)
     */
    fun d(category: Category, message: String, vararg args: Any?) {
        if (!PRODUCTION_MODE) {
            val formattedMessage = formatMessage(message, *args)
            Log.d(category.tag, formattedMessage)
        }
    }
    
    /**
     * 정보 로그
     */
    fun i(category: Category, message: String, vararg args: Any?) {
        val formattedMessage = formatMessage(message, *args)
        Log.i(category.tag, formattedMessage)
    }
    
    /**
     * 경고 로그
     */
    fun w(category: Category, message: String, throwable: Throwable? = null, vararg args: Any?) {
        val formattedMessage = formatMessage(message, *args)
        if (throwable != null) {
            Log.w(category.tag, formattedMessage, throwable)
        } else {
            Log.w(category.tag, formattedMessage)
        }
    }
    
    /**
     * 에러 로그
     */
    fun e(category: Category, message: String, throwable: Throwable? = null, vararg args: Any?) {
        val formattedMessage = formatMessage(message, *args)
        if (throwable != null) {
            Log.e(category.tag, formattedMessage, throwable)
        } else {
            Log.e(category.tag, formattedMessage)
        }
    }
    
    /**
     * 상세 로그 (디버그 빌드에서만)
     */
    fun v(category: Category, message: String, vararg args: Any?) {
        if (!PRODUCTION_MODE) {
            val formattedMessage = formatMessage(message, *args)
            Log.v(category.tag, formattedMessage)
        }
    }
    
    // =============== 전문화된 로깅 메서드들 ===============
    
    /**
     * 성능 측정 로그
     */
    fun performance(operation: String, durationMs: Long, details: String = "") {
        val message = if (details.isNotEmpty()) {
            "⏱️ $operation: ${durationMs}ms | $details"
        } else {
            "⏱️ $operation: ${durationMs}ms"
        }
        i(Category.PERFORMANCE, message)
    }
    
    /**
     * 번역 관련 로그
     */
    fun translation(source: String, target: String, text: String, result: String, fromCache: Boolean = false) {
        val cacheInfo = if (fromCache) " [CACHED]" else ""
        d(Category.TRANSLATION, "🔄 $source → $target$cacheInfo | Input: %s | Output: %s", 
          text.take(50), result.take(50))
    }
    
    /**
     * 캐시 관련 로그
     */
    fun cache(operation: String, key: String, hit: Boolean = false, size: Int = -1) {
        val hitInfo = if (hit) "✅ HIT" else "❌ MISS"
        val sizeInfo = if (size >= 0) " | Size: $size" else ""
        d(Category.CACHE, "💾 $operation | $hitInfo | Key: %s$sizeInfo", key.take(30))
    }
    
    /**
     * 데이터베이스 관련 로그
     */
    fun database(operation: String, table: String, rowCount: Int = -1, durationMs: Long = -1) {
        val countInfo = if (rowCount >= 0) " | Rows: $rowCount" else ""
        val timeInfo = if (durationMs >= 0) " | Time: ${durationMs}ms" else ""
        d(Category.DATABASE, "🗄️ $operation on $table$countInfo$timeInfo")
    }
    
    /**
     * 네트워크 관련 로그
     */
    fun network(operation: String, url: String, statusCode: Int = -1, durationMs: Long = -1) {
        val statusInfo = if (statusCode >= 0) " | Status: $statusCode" else ""
        val timeInfo = if (durationMs >= 0) " | Time: ${durationMs}ms" else ""
        d(Category.NETWORK, "🌐 $operation | URL: %s$statusInfo$timeInfo", url.take(50))
    }
    
    /**
     * 언어 감지 관련 로그
     */
    fun languageDetection(text: String, detected: String, confidence: Float = -1f) {
        val confidenceInfo = if (confidence >= 0) " | Confidence: %.2f".format(confidence) else ""
        d(Category.LANGUAGE_DETECTION, "🔍 Detected: $detected$confidenceInfo | Text: %s", text.take(30))
    }
    
    /**
     * 모델 관리 관련 로그
     */
    fun modelManagement(operation: String, language: String, success: Boolean, progress: Float = -1f) {
        val status = if (success) "✅ SUCCESS" else "❌ FAILED"
        val progressInfo = if (progress >= 0) " | Progress: %.1f%%".format(progress * 100) else ""
        i(Category.MODEL_MANAGER, "📦 $operation [$language] | $status$progressInfo")
    }
    
    // =============== 내부 유틸리티 메서드들 ===============
    
    /**
     * 메시지 포맷팅 (String.format 스타일)
     */
    private fun formatMessage(message: String, vararg args: Any?): String {
        return try {
            if (args.isNotEmpty()) {
                String.format(message, *args)
            } else {
                message
            }
        } catch (e: Exception) {
            // 포맷팅 실패 시 원본 메시지 반환
            message
        }
    }
    
    /**
     * 개발용 도구: 메서드 진입/종료 추적
     */
    inline fun <T> trace(category: Category, methodName: String, crossinline block: () -> T): T {
        val startTime = System.currentTimeMillis()
        v(category, "→ Entering $methodName")
        
        return try {
            val result = block()
            val duration = System.currentTimeMillis() - startTime
            v(category, "← Exiting $methodName (${duration}ms)")
            result
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            e(category, "💥 Exception in $methodName (${duration}ms)", e)
            throw e
        }
    }
} 