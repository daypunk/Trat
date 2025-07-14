package com.example.trat.data.models

import com.google.mlkit.nl.translate.TranslateLanguage

enum class SupportedLanguage(
    val code: String,
    val displayName: String,
    val mlKitLanguage: String
) {
    KOREAN("ko", "한국어", TranslateLanguage.KOREAN),
    ENGLISH("en", "English", TranslateLanguage.ENGLISH),
    JAPANESE("ja", "日本語", TranslateLanguage.JAPANESE),
    CHINESE("zh", "中文", TranslateLanguage.CHINESE);
    
    companion object {
        fun fromCode(code: String): SupportedLanguage? {
            return values().find { it.code == code }
        }
        
        fun fromMlKitLanguage(mlKitLanguage: String): SupportedLanguage? {
            return values().find { it.mlKitLanguage == mlKitLanguage }
        }
    }
} 