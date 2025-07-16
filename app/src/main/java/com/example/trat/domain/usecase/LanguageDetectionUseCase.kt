package com.example.trat.domain.usecase

import android.util.Log
import com.example.trat.data.entities.Chat
import com.example.trat.data.models.SupportedLanguage
import com.example.trat.utils.LanguageDetector
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 언어 감지 및 처리를 담당하는 UseCase
 * - 입력 텍스트의 언어 자동 감지
 * - 언어별 문자 특성 분석
 * - 번역 방향 자동 결정
 */
@Singleton
class LanguageDetectionUseCase @Inject constructor() {
    
    /**
     * 입력 텍스트의 언어를 감지하고 채팅 설정에 따라 번역 방향을 결정
     */
    fun determineTranslationDirection(
        inputText: String,
        chat: Chat
    ): Pair<SupportedLanguage, SupportedLanguage> {
        // 입력 언어 감지
        val detectedLanguage = if (inputText.trim().length >= 2) {
            LanguageDetector.detectLanguage(inputText)
        } else {
            chat.nativeLanguage
        }
        
        // 양방향 번역 로직: 입력 언어에 따라 자동으로 번역 방향 결정
        return when {
            // 감지된 언어가 nativeLanguage와 같으면 translateLanguage로 번역
            detectedLanguage == chat.nativeLanguage -> chat.nativeLanguage to chat.translateLanguage
            // 감지된 언어가 translateLanguage와 같으면 nativeLanguage로 번역
            detectedLanguage == chat.translateLanguage -> chat.translateLanguage to chat.nativeLanguage
            // 감지된 언어가 둘 다 아닌 경우 문자 특성으로 판단
            else -> determineDirectionByCharacteristics(inputText, chat)
        }
    }
    
    /**
     * 텍스트의 문자 특성을 분석하여 번역 방향 결정
     */
    private fun determineDirectionByCharacteristics(
        inputText: String,
        chat: Chat
    ): Pair<SupportedLanguage, SupportedLanguage> {
        val characteristics = analyzeTextCharacteristics(inputText)
        
        return when {
            characteristics.hasKorean && (chat.nativeLanguage == SupportedLanguage.KOREAN || chat.translateLanguage == SupportedLanguage.KOREAN) -> 
                SupportedLanguage.KOREAN to (if (chat.nativeLanguage == SupportedLanguage.KOREAN) chat.translateLanguage else chat.nativeLanguage)
            characteristics.hasJapanese && (chat.nativeLanguage == SupportedLanguage.JAPANESE || chat.translateLanguage == SupportedLanguage.JAPANESE) -> 
                SupportedLanguage.JAPANESE to (if (chat.nativeLanguage == SupportedLanguage.JAPANESE) chat.translateLanguage else chat.nativeLanguage)
            characteristics.hasEnglish && (chat.nativeLanguage == SupportedLanguage.ENGLISH || chat.translateLanguage == SupportedLanguage.ENGLISH) -> 
                SupportedLanguage.ENGLISH to (if (chat.nativeLanguage == SupportedLanguage.ENGLISH) chat.translateLanguage else chat.nativeLanguage)
            characteristics.hasChinese && (chat.nativeLanguage == SupportedLanguage.CHINESE || chat.translateLanguage == SupportedLanguage.CHINESE) -> 
                SupportedLanguage.CHINESE to (if (chat.nativeLanguage == SupportedLanguage.CHINESE) chat.translateLanguage else chat.nativeLanguage)
            else -> chat.nativeLanguage to chat.translateLanguage
        }
    }
    
    /**
     * 텍스트 분석 결과를 기반으로 채팅의 언어 설정을 자동 업데이트할지 결정
     */
    fun shouldUpdateChatLanguage(
        inputText: String,
        chat: Chat
    ): SupportedLanguage? {
        // 텍스트가 너무 짧으면 감지하지 않음
        if (inputText.trim().length < 2) return null
        
        val detectedLanguage = LanguageDetector.detectLanguage(inputText)
        
        // 감지된 언어가 현재 설정된 언어들과 다른 경우에만 업데이트
        if (detectedLanguage != chat.nativeLanguage && detectedLanguage != chat.translateLanguage) {
            val characteristics = analyzeTextCharacteristics(inputText)
            
            val newNativeLanguage = when {
                characteristics.hasKorean -> SupportedLanguage.KOREAN
                characteristics.hasJapanese -> SupportedLanguage.JAPANESE
                characteristics.hasEnglish -> SupportedLanguage.ENGLISH
                characteristics.hasChinese -> SupportedLanguage.CHINESE
                else -> null
            }
            
            if (newNativeLanguage != null && newNativeLanguage != chat.nativeLanguage) {
                Log.d("LanguageDetectionUseCase", "Language change detected: ${chat.nativeLanguage.displayName} → ${newNativeLanguage.displayName}")
                return newNativeLanguage
            }
        }
        
        return null
    }
    
    /**
     * 텍스트의 문자 특성을 분석
     */
    fun analyzeTextCharacteristics(text: String): TextCharacteristics {
        return TextCharacteristics(
            hasKorean = text.any { it in '\uAC00'..'\uD7AF' },
            hasJapanese = text.any { it in '\u3040'..'\u309F' || it in '\u30A0'..'\u30FF' || it in '\u4E00'..'\u9FAF' },
            hasEnglish = text.any { it in 'A'..'Z' || it in 'a'..'z' },
            hasChinese = text.any { it in '\u4E00'..'\u9FAF' }
        )
    }
    
    /**
     * 텍스트 문자 특성 데이터 클래스
     */
    data class TextCharacteristics(
        val hasKorean: Boolean,
        val hasJapanese: Boolean,
        val hasEnglish: Boolean,
        val hasChinese: Boolean
    )
} 