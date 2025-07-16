package com.example.trat.domain.usecase

import android.util.Log
import com.example.trat.data.models.SupportedLanguage
import com.example.trat.domain.service.TranslationCacheService
import com.example.trat.services.TranslationService
import com.example.trat.utils.Constants
import com.example.trat.utils.LanguageModelManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 번역 로직을 담당하는 UseCase
 * - 텍스트 번역 수행 (캐싱 포함)
 * - 번역 결과 검증
 * - 모델 상태 확인
 */
@Singleton
class TranslationUseCase @Inject constructor(
    private val translationService: TranslationService,
    private val languageModelManager: LanguageModelManager,
    private val translationCacheService: TranslationCacheService
) {
    
    /**
     * 텍스트를 번역하고 결과를 반환 (캐싱 포함)
     */
    suspend fun translateText(
        inputText: String,
        sourceLanguage: SupportedLanguage,
        targetLanguage: SupportedLanguage
    ): String {
        // 동일한 언어면 번역 불필요
        if (sourceLanguage == targetLanguage) {
            return inputText
        }
        
        Log.d(Constants.LogTags.TRANSLATION_USE_CASE, "Translation: ${sourceLanguage.displayName} → ${targetLanguage.displayName}")
        
        // 1. 캐시에서 번역 결과 확인
        val cachedTranslation = translationCacheService.getTranslation(
            originalText = inputText,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage
        )
        
        if (cachedTranslation != null) {
            Log.d(Constants.LogTags.TRANSLATION_USE_CASE, "Using cached translation: $inputText → $cachedTranslation")
            return cachedTranslation
        }
        
        // 2. 캐시에 없으면 실제 번역 수행
        val result = translationService.translate(inputText, sourceLanguage, targetLanguage)
        
        return if (result.isSuccess) {
            val translation = result.getOrNull()
            
            val finalTranslation = if (translation != null && isDifferentEnough(inputText, translation)) {
                translation
            } else {
                inputText
            }
            
            // 3. 번역 결과를 캐시에 저장
            translationCacheService.saveTranslation(
                originalText = inputText,
                translatedText = finalTranslation,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage
            )
            
            finalTranslation
        } else {
            // 번역 실패 시 에러를 던져서 상위에서 처리하도록 함
            throw Exception(result.exceptionOrNull()?.message ?: "번역에 실패했습니다")
        }
    }
    
    /**
     * 번역 결과가 원본과 충분히 다른지 검증
     * 
     * 번역이 실제로 수행되었는지 확인하기 위해 다음 요소들을 체크:
     * 1. 언어별 문자 특성 변화 (한글 → 영어 등)
     * 2. 텍스트 길이 변화 (임계값 이상 차이)
     * 
     * @param original 원본 텍스트
     * @param translated 번역된 텍스트
     * @return true if 번역이 실제로 수행됨, false if 원본과 동일하거나 번역 실패
     */
    fun isDifferentEnough(original: String, translated: String): Boolean {
        if (original == translated) return false
        
        // 언어별 문자 범위 체크
        val originalHasKorean = original.any { it in '\uAC00'..'\uD7AF' }
        val originalHasJapanese = original.any { it in '\u3040'..'\u309F' || it in '\u30A0'..'\u30FF' || it in '\u4E00'..'\u9FAF' }
        val originalHasEnglish = original.any { it in 'A'..'Z' || it in 'a'..'z' }
        val originalHasChinese = original.any { it in '\u4E00'..'\u9FAF' }
        
        val translatedHasKorean = translated.any { it in '\uAC00'..'\uD7AF' }
        val translatedHasJapanese = translated.any { it in '\u3040'..'\u309F' || it in '\u30A0'..'\u30FF' || it in '\u4E00'..'\u9FAF' }
        val translatedHasEnglish = translated.any { it in 'A'..'Z' || it in 'a'..'z' }
        val translatedHasChinese = translated.any { it in '\u4E00'..'\u9FAF' }
        
        // 언어 조합이 바뀌었으면 번역된 것으로 간주
        return (originalHasKorean != translatedHasKorean) ||
               (originalHasJapanese != translatedHasJapanese) ||
               (originalHasEnglish != translatedHasEnglish) ||
               (originalHasChinese != translatedHasChinese) ||
               // 텍스트 길이가 많이 다르면 번역된 것으로 간주
               (kotlin.math.abs(original.length - translated.length).toFloat() / original.length > Constants.Translation.TRANSLATION_DIFFERENCE_THRESHOLD)
    }
    
    /**
     * 특정 언어 쌍의 모델이 다운로드되어 있는지 확인
     */
    suspend fun areModelsDownloaded(
        nativeLanguage: SupportedLanguage,
        translateLanguage: SupportedLanguage
    ): Boolean {
        val nativeModelDownloaded = languageModelManager.isModelDownloaded(nativeLanguage)
        val translateModelDownloaded = languageModelManager.isModelDownloaded(translateLanguage)
        
        Log.d("TranslationUseCase", "Model status: ${nativeLanguage.displayName} = $nativeModelDownloaded, ${translateLanguage.displayName} = $translateModelDownloaded")
        
        return nativeModelDownloaded && translateModelDownloaded
    }
    
    /**
     * 필요한 모델들을 다운로드
     */
    suspend fun downloadModelsIfNeeded(
        nativeLanguage: SupportedLanguage,
        translateLanguage: SupportedLanguage
    ): Boolean {
        Log.d("TranslationUseCase", "Downloading models for: ${nativeLanguage.displayName} ↔ ${translateLanguage.displayName}")
        
        val result1 = translationService.downloadModelIfNeeded(nativeLanguage, translateLanguage)
        val result2 = translationService.downloadModelIfNeeded(translateLanguage, nativeLanguage)
        
        Log.d("TranslationUseCase", "Download results: result1=${result1.isSuccess}, result2=${result2.isSuccess}")
        
        if (result1.isFailure) {
            Log.w("TranslationUseCase", "Download failed 1: ${result1.exceptionOrNull()?.message}")
        }
        if (result2.isFailure) {
            Log.w("TranslationUseCase", "Download failed 2: ${result2.exceptionOrNull()?.message}")
        }
        
        return result1.isSuccess && result2.isSuccess
    }
    
    /**
     * 번역 가능 여부 확인 (모델 상태 + 네트워크 상태 고려)
     */
    suspend fun canTranslate(
        sourceLanguage: SupportedLanguage,
        targetLanguage: SupportedLanguage
    ): Boolean {
        // 동일한 언어면 번역 불필요
        if (sourceLanguage == targetLanguage) return false
        
        // 모델 다운로드 상태 확인
        return areModelsDownloaded(sourceLanguage, targetLanguage)
    }
} 