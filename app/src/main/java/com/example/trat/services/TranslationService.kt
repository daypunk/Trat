package com.example.trat.services

import com.google.mlkit.nl.translate.*
import com.example.trat.data.models.SupportedLanguage
import com.example.trat.utils.Constants

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class TranslationService @Inject constructor() {
    
    private val translators = mutableMapOf<String, Translator>()
    
    /**
     * 두 언어 간 번역을 수행합니다
     */
    suspend fun translate(
        text: String,
        sourceLanguage: SupportedLanguage,
        targetLanguage: SupportedLanguage
    ): Result<String> {
        if (text.isBlank()) {
            return Result.failure(Exception("번역할 텍스트가 비어있습니다"))
        }
        
        if (sourceLanguage == targetLanguage) {
            return Result.success(text) // 같은 언어면 그대로 반환
        }
        
        return try {
            val translator = getOrCreateTranslator(sourceLanguage, targetLanguage)
            
            // 모델 다운로드 확인
            val isModelReady = ensureModelDownloaded(translator)
            if (!isModelReady) {
                return Result.failure(Exception(Constants.ERROR_MODEL_DOWNLOAD_FAILED))
            }
            
            // 번역 수행
            val translatedText = performTranslation(translator, text)
            Result.success(translatedText)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 언어 모델이 다운로드되어 있는지 확인하고, 없으면 다운로드합니다
     */
    suspend fun downloadModelIfNeeded(
        sourceLanguage: SupportedLanguage,
        targetLanguage: SupportedLanguage,
        requireWifi: Boolean = true
    ): Result<Boolean> {
        return try {
            val translator = getOrCreateTranslator(sourceLanguage, targetLanguage)
            val isDownloaded = ensureModelDownloaded(translator, requireWifi)
            
            if (isDownloaded) {
                Result.success(true)
            } else {
                Result.failure(Exception(Constants.ERROR_MODEL_DOWNLOAD_FAILED))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Translator 객체를 가져오거나 새로 생성합니다
     */
    private fun getOrCreateTranslator(
        sourceLanguage: SupportedLanguage,
        targetLanguage: SupportedLanguage
    ): Translator {
        val key = "${sourceLanguage.code}-${targetLanguage.code}"
        
        return translators.getOrPut(key) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage.mlKitLanguage)
                .setTargetLanguage(targetLanguage.mlKitLanguage)
                .build()
            Translation.getClient(options)
        }
    }
    
    /**
     * 모델 다운로드를 확인하고 수행합니다
     */
    private suspend fun ensureModelDownloaded(
        translator: Translator,
        requireWifi: Boolean = true
    ): Boolean {
        return withTimeoutOrNull(Constants.MODEL_DOWNLOAD_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                translator.downloadModelIfNeeded()
                    .addOnSuccessListener {
                        continuation.resume(true)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            }
        } ?: false
    }
    
    /**
     * 실제 번역을 수행합니다
     */
    private suspend fun performTranslation(translator: Translator, text: String): String {
        return withTimeoutOrNull(Constants.TRANSLATION_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                translator.translate(text)
                    .addOnSuccessListener { translatedText ->
                        continuation.resume(translatedText)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            }
        } ?: throw Exception(Constants.ERROR_TRANSLATION_FAILED)
    }
    
    /**
     * 모든 Translator 리소스를 정리합니다
     */
    fun cleanup() {
        translators.values.forEach { translator ->
            try {
                translator.close()
            } catch (e: Exception) {
                // 무시 - 이미 닫혀있을 수 있음
            }
        }
        translators.clear()
    }
    
    /**
     * 특정 언어 쌍의 Translator를 정리합니다
     */
    fun cleanupTranslator(sourceLanguage: SupportedLanguage, targetLanguage: SupportedLanguage) {
        val key = "${sourceLanguage.code}-${targetLanguage.code}"
        translators[key]?.let { translator ->
            try {
                translator.close()
            } catch (e: Exception) {
                // 무시
            }
            translators.remove(key)
        }
    }
} 