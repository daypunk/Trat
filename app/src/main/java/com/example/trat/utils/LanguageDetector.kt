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
            char in Constants.UnicodeRanges.KOREAN_START..Constants.UnicodeRanges.KOREAN_END || // 한글 음절
            char in Constants.UnicodeRanges.KOREAN_JAMO_START..Constants.UnicodeRanges.KOREAN_JAMO_END || // 한글 자모
            char in Constants.UnicodeRanges.KOREAN_COMPAT_START..Constants.UnicodeRanges.KOREAN_COMPAT_END    // 한글 호환 자모
        }
    }
    
    private fun containsChinese(text: String): Boolean {
        return text.any { char ->
            char in Constants.UnicodeRanges.CJK_START..Constants.UnicodeRanges.CJK_END || // CJK 통합 한자
            char in Constants.UnicodeRanges.CJK_EXT_A_START..Constants.UnicodeRanges.CJK_EXT_A_END || // CJK 확장 A
            char in Constants.UnicodeRanges.CJK_COMPAT_START..Constants.UnicodeRanges.CJK_COMPAT_END    // CJK 호환 한자
        }
    }
    
    private fun containsJapanese(text: String): Boolean {
        return text.any { char ->
            char in Constants.UnicodeRanges.HIRAGANA_START..Constants.UnicodeRanges.HIRAGANA_END || // 히라가나
            char in Constants.UnicodeRanges.KATAKANA_START..Constants.UnicodeRanges.KATAKANA_END || // 카타카나
            char in '\uFF66'..'\uFF9F'    // 반각 카타카나 (추가 범위)
        }
    }
} 