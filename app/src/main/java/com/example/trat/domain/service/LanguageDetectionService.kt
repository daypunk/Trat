package com.example.trat.domain.service

import com.example.trat.data.models.SupportedLanguage
import com.example.trat.utils.LanguageDetector
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 언어 감지를 담당하는 도메인 서비스
 * - 텍스트 언어 감지
 * - 언어 신뢰도 평가
 * - 다국어 텍스트 처리
 */
@Singleton
class LanguageDetectionService @Inject constructor() {
    
    /**
     * 텍스트의 언어를 감지합니다
     */
    fun detectLanguage(text: String): SupportedLanguage {
        return LanguageDetector.detectLanguage(text)
    }
    
    /**
     * 텍스트가 언어 감지에 충분한 길이인지 확인
     */
    fun isTextSufficientForDetection(text: String): Boolean {
        return text.trim().length >= 2
    }
    
    /**
     * 텍스트에서 주요 언어를 추출 (혼합 언어 텍스트 처리)
     */
    fun extractPrimaryLanguage(text: String): SupportedLanguage {
        val characteristics = analyzeCharacteristics(text)
        
        return when {
            characteristics.koreanRatio > 0.3 -> SupportedLanguage.KOREAN
            characteristics.japaneseRatio > 0.3 -> SupportedLanguage.JAPANESE
            characteristics.chineseRatio > 0.3 -> SupportedLanguage.CHINESE
            characteristics.englishRatio > 0.3 -> SupportedLanguage.ENGLISH
            else -> detectLanguage(text) // 기본 감지로 폴백
        }
    }
    
    /**
     * 텍스트의 언어별 문자 비율을 분석
     */
    private fun analyzeCharacteristics(text: String): LanguageCharacteristics {
        val totalChars = text.length.toFloat()
        if (totalChars == 0f) return LanguageCharacteristics()
        
        var koreanCount = 0
        var japaneseCount = 0
        var chineseCount = 0
        var englishCount = 0
        
        text.forEach { char ->
            when {
                char in '\uAC00'..'\uD7AF' -> koreanCount++
                char in '\u3040'..'\u309F' || char in '\u30A0'..'\u30FF' -> japaneseCount++
                char in '\u4E00'..'\u9FAF' -> chineseCount++
                char in 'A'..'Z' || char in 'a'..'z' -> englishCount++
            }
        }
        
        return LanguageCharacteristics(
            koreanRatio = koreanCount / totalChars,
            japaneseRatio = japaneseCount / totalChars,
            chineseRatio = chineseCount / totalChars,
            englishRatio = englishCount / totalChars
        )
    }
    
    /**
     * 언어별 문자 비율 데이터
     */
    private data class LanguageCharacteristics(
        val koreanRatio: Float = 0f,
        val japaneseRatio: Float = 0f,
        val chineseRatio: Float = 0f,
        val englishRatio: Float = 0f
    )
} 