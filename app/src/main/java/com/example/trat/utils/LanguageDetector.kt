package com.example.trat.utils

import android.util.Log
import com.example.trat.data.models.SupportedLanguage

object LanguageDetector {
    
    /**
     * 텍스트를 분석하여 언어를 감지합니다.
     * 개선된 규칙 기반 감지로 일본어-중국어 구분 정확도 향상
     */
    fun detectLanguage(text: String): SupportedLanguage {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return SupportedLanguage.KOREAN
        
        // 1단계: 명확한 문자 특성으로 빠른 감지
        val quickDetection = detectByCharacteristics(cleanText)
        if (quickDetection != null) {
            Log.d("LanguageDetector", "감지 결과: ${quickDetection.displayName}")
            return quickDetection
        }
        
        // 2단계: 한자 문맥 분석으로 일본어-중국어 구분
        return detectByAdvancedRules(cleanText)
    }
    
    /**
     * 문자 특성 기반 빠른 감지
     */
    private fun detectByCharacteristics(text: String): SupportedLanguage? {
        // 한국어 (한글이 포함된 경우 100% 한국어)
        if (containsKorean(text)) {
            return SupportedLanguage.KOREAN
        }
        
        // 일본어 (히라가나나 카타카나가 포함된 경우 100% 일본어)
        if (containsJapaneseScript(text)) {
            return SupportedLanguage.JAPANESE
        }
        
        // 영어 (라틴 문자만 있고 한자가 없는 경우)
        if (containsOnlyLatin(text)) {
            return SupportedLanguage.ENGLISH
        }
        
        // 애매한 경우 (한자만 있는 경우 등) null 반환
        return null
    }
    
    /**
     * 개선된 규칙 기반 감지 (한자 문맥 분석)
     */
    private fun detectByAdvancedRules(text: String): SupportedLanguage {
        val hasKanji = containsKanji(text)
        val hasLatin = containsLatin(text)
        
        return when {
            hasKanji && hasLatin -> SupportedLanguage.JAPANESE // 한자+영어 조합은 일본어 가능성 높음
            hasKanji -> {
                // 한자만 있는 경우: 문맥적 단서로 판단
                val kanjiCount = text.count { it in '\u4E00'..'\u9FAF' }
                val textLength = text.length
                val kanjiRatio = kanjiCount.toFloat() / textLength
                
                // 한자 비율이 높고 짧은 텍스트면 일본어 가능성 높음
                if (kanjiRatio > 0.7f && textLength <= 10) {
                    SupportedLanguage.JAPANESE
                } else {
                    SupportedLanguage.CHINESE
                }
            }
            hasLatin -> SupportedLanguage.ENGLISH
            else -> SupportedLanguage.KOREAN // 기본값
        }
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
    
    /**
     * 일본어 고유 문자 (히라가나, 카타카나) 확인
     */
    private fun containsJapaneseScript(text: String): Boolean {
        return text.any { char ->
            char in Constants.UnicodeRanges.HIRAGANA_START..Constants.UnicodeRanges.HIRAGANA_END || // 히라가나
            char in Constants.UnicodeRanges.KATAKANA_START..Constants.UnicodeRanges.KATAKANA_END || // 카타카나
            char in '\uFF66'..'\uFF9F'    // 반각 카타카나
        }
    }
    
    /**
     * 한자 확인
     */
    private fun containsKanji(text: String): Boolean {
        return text.any { char ->
            char in Constants.UnicodeRanges.CJK_START..Constants.UnicodeRanges.CJK_END
        }
    }
    
    /**
     * 라틴 문자 확인
     */
    private fun containsLatin(text: String): Boolean {
        return text.any { char ->
            char in 'A'..'Z' || char in 'a'..'z'
        }
    }
    
    /**
     * 라틴 문자만 포함 (한자 없음)
     */
    private fun containsOnlyLatin(text: String): Boolean {
        val hasLatin = containsLatin(text)
        val hasKanji = containsKanji(text)
        val hasKorean = containsKorean(text)
        val hasJapaneseScript = containsJapaneseScript(text)
        
        return hasLatin && !hasKanji && !hasKorean && !hasJapaneseScript
    }
} 