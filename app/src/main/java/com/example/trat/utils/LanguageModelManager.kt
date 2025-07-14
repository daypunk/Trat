package com.example.trat.utils

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.example.trat.data.models.SupportedLanguage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class LanguageModelManager @Inject constructor() {
    
    private val modelManager = RemoteModelManager.getInstance()
    
    /**
     * 기기에 다운로드된 모든 번역 모델을 가져옵니다
     */
    suspend fun getDownloadedModels(): Result<Set<String>> {
        return try {
            withTimeoutOrNull(Constants.MODEL_DOWNLOAD_TIMEOUT_MS) {
                suspendCancellableCoroutine { continuation ->
                    modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
                        .addOnSuccessListener { models ->
                            val languageCodes = models.map { it.language }.toSet()
                            continuation.resume(languageCodes)
                        }
                        .addOnFailureListener { exception ->
                            continuation.resumeWithException(exception)
                        }
                }
            }?.let { Result.success(it) } ?: Result.failure(Exception("타임아웃"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 특정 언어의 모델이 다운로드되어 있는지 확인합니다
     */
    suspend fun isModelDownloaded(language: SupportedLanguage): Boolean {
        return try {
            val downloadedModels = getDownloadedModels().getOrNull() ?: emptySet()
            downloadedModels.contains(language.mlKitLanguage)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 특정 언어의 모델을 다운로드합니다
     */
    suspend fun downloadModel(
        language: SupportedLanguage,
        requireWifi: Boolean = true
    ): Result<Boolean> {
        return try {
            val model = TranslateRemoteModel.Builder(language.mlKitLanguage).build()
            val conditions = DownloadConditions.Builder().apply {
                if (requireWifi) requireWifi()
            }.build()
            
            withTimeoutOrNull(Constants.MODEL_DOWNLOAD_TIMEOUT_MS) {
                suspendCancellableCoroutine { continuation ->
                    modelManager.download(model, conditions)
                        .addOnSuccessListener {
                            continuation.resume(true)
                        }
                        .addOnFailureListener { exception ->
                            continuation.resumeWithException(exception)
                        }
                }
            }?.let { Result.success(it) } ?: Result.failure(Exception("다운로드 타임아웃"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 특정 언어의 모델을 삭제합니다
     */
    suspend fun deleteModel(language: SupportedLanguage): Result<Boolean> {
        return try {
            val model = TranslateRemoteModel.Builder(language.mlKitLanguage).build()
            
            withTimeoutOrNull(Constants.MODEL_DOWNLOAD_TIMEOUT_MS) {
                suspendCancellableCoroutine { continuation ->
                    modelManager.deleteDownloadedModel(model)
                        .addOnSuccessListener {
                            continuation.resume(true)
                        }
                        .addOnFailureListener { exception ->
                            continuation.resumeWithException(exception)
                        }
                }
            }?.let { Result.success(it) } ?: Result.failure(Exception("삭제 타임아웃"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 필요한 모든 언어 모델을 다운로드합니다
     */
    suspend fun downloadRequiredModels(
        sourceLanguage: SupportedLanguage,
        targetLanguage: SupportedLanguage,
        requireWifi: Boolean = true
    ): Result<Boolean> {
        return try {
            val downloadedModels = getDownloadedModels().getOrNull() ?: emptySet()
            
            // 소스 언어 모델 다운로드
            if (!downloadedModels.contains(sourceLanguage.mlKitLanguage)) {
                val sourceResult = downloadModel(sourceLanguage, requireWifi)
                if (sourceResult.isFailure) {
                    return Result.failure(Exception("${sourceLanguage.displayName} 모델 다운로드 실패"))
                }
            }
            
            // 타겟 언어 모델 다운로드
            if (!downloadedModels.contains(targetLanguage.mlKitLanguage)) {
                val targetResult = downloadModel(targetLanguage, requireWifi)
                if (targetResult.isFailure) {
                    return Result.failure(Exception("${targetLanguage.displayName} 모델 다운로드 실패"))
                }
            }
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 사용하지 않는 언어 모델들을 삭제합니다
     */
    suspend fun cleanupUnusedModels(activeLanguages: Set<SupportedLanguage>): Result<Int> {
        return try {
            val downloadedModels = getDownloadedModels().getOrNull() ?: emptySet()
            val activeLanguageCodes = activeLanguages.map { it.mlKitLanguage }.toSet()
            
            val modelsToDelete = downloadedModels.filter { modelLanguage ->
                // 지원하는 언어인지 확인
                val supportedLanguage = SupportedLanguage.fromMlKitLanguage(modelLanguage)
                supportedLanguage != null && !activeLanguageCodes.contains(modelLanguage)
            }
            
            var deletedCount = 0
            for (modelLanguageCode in modelsToDelete) {
                val language = SupportedLanguage.fromMlKitLanguage(modelLanguageCode)
                language?.let {
                    val deleteResult = deleteModel(it)
                    if (deleteResult.isSuccess) {
                        deletedCount++
                    }
                }
            }
            
            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 모든 다운로드된 번역 모델을 삭제합니다
     */
    suspend fun deleteAllModels(): Result<Int> {
        return try {
            val downloadedModels = getDownloadedModels().getOrNull() ?: emptySet()
            var deletedCount = 0
            
            for (modelLanguageCode in downloadedModels) {
                val language = SupportedLanguage.fromMlKitLanguage(modelLanguageCode)
                language?.let {
                    val deleteResult = deleteModel(it)
                    if (deleteResult.isSuccess) {
                        deletedCount++
                    }
                }
            }
            
            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 언어 모델의 상태 정보를 가져옵니다
     */
    suspend fun getModelStatus(): Map<SupportedLanguage, Boolean> {
        return try {
            val downloadedModels = getDownloadedModels().getOrNull() ?: emptySet()
            SupportedLanguage.values().associateWith { language ->
                downloadedModels.contains(language.mlKitLanguage)
            }
        } catch (e: Exception) {
            SupportedLanguage.values().associateWith { false }
        }
    }
} 