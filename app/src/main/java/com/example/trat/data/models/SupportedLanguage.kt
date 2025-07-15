package com.example.trat.data.models

import com.google.mlkit.nl.translate.TranslateLanguage

enum class SupportedLanguage(
    val code: String,
    val displayName: String,
    val mlKitLanguage: String
) {
    KOREAN("ko", "한국어", TranslateLanguage.KOREAN),
    ENGLISH("en", "영어", TranslateLanguage.ENGLISH),
    JAPANESE("ja", "일본어", TranslateLanguage.JAPANESE),
    CHINESE("zh", "중국어", TranslateLanguage.CHINESE);
    
    companion object {
        fun fromCode(code: String): SupportedLanguage? {
            return values().find { it.code == code }
        }
        
        fun fromMlKitLanguage(mlKitLanguage: String): SupportedLanguage? {
            return values().find { it.mlKitLanguage == mlKitLanguage }
        }
    }
} 