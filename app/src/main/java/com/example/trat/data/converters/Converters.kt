package com.example.trat.data.converters

import androidx.room.TypeConverter
import com.example.trat.data.models.SupportedLanguage

class Converters {
    
    @TypeConverter
    fun fromSupportedLanguage(language: SupportedLanguage): String {
        return language.code
    }
    
    @TypeConverter
    fun toSupportedLanguage(code: String): SupportedLanguage {
        return SupportedLanguage.fromCode(code) ?: SupportedLanguage.KOREAN
    }
} 