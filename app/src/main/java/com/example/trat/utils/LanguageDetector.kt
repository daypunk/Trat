package com.example.trat.utils

import com.example.trat.data.models.SupportedLanguage

object LanguageDetector {
    
    /**
     * 텍스트를 분석하여 언어를 감지합니다.
     */
    fun detectLanguage(text: String): SupportedLanguage {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return SupportedLanguage.KOREAN
        
        // 한국어 감지 (한글 문자 포함)
        if (containsKorean(cleanText)) {
            return SupportedLanguage.KOREAN
        }
        
        // 중국어 감지 (중국어 문자 포함)
        if (containsChinese(cleanText)) {
            return SupportedLanguage.CHINESE
        }
        
        // 일본어 감지 (히라가나, 카타카나 포함)
        if (containsJapanese(cleanText)) {
            return SupportedLanguage.JAPANESE
        }
        
        // 영어 또는 기타 라틴 문자 (기본값)
        return SupportedLanguage.ENGLISH
    }
    
    /**
     * 현재 채팅 설정과 다른 언어인지 확인
     */
    fun isLanguageChangeNeeded(
        detectedLanguage: SupportedLanguage,
        currentNative: SupportedLanguage,
        currentTranslate: SupportedLanguage
    ): Boolean {
        return detectedLanguage != currentNative && detectedLanguage != currentTranslate
    }
    
    private fun containsKorean(text: String): Boolean {
        return text.any { char ->
            char in '\uAC00'..'\uD7AF' || // 한글 음절
            char in '\u1100'..'\u11FF' || // 한글 자모
            char in '\u3130'..'\u318F'    // 한글 호환 자모
        }
    }
    
    private fun containsChinese(text: String): Boolean {
        return text.any { char ->
            char in '\u4E00'..'\u9FFF' || // CJK 통합 한자
            char in '\u3400'..'\u4DBF' || // CJK 확장 A
            char in '\uF900'..'\uFAFF'    // CJK 호환 한자
        }
    }
    
    private fun containsJapanese(text: String): Boolean {
        return text.any { char ->
            char in '\u3040'..'\u309F' || // 히라가나
            char in '\u30A0'..'\u30FF' || // 카타카나
            char in '\uFF66'..'\uFF9F'    // 반각 카타카나
        }
    }
} 