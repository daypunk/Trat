package com.example.trat.domain.usecase

import android.util.Log
import com.example.trat.data.entities.Chat
import com.example.trat.data.entities.Message
import com.example.trat.data.models.SupportedLanguage
import com.example.trat.domain.repository.ChatRepositoryInterface
import com.example.trat.services.TranslationService
import com.example.trat.utils.LanguageDetector
import com.example.trat.utils.LanguageModelManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatUseCase @Inject constructor(
    private val chatRepository: ChatRepositoryInterface,
    private val translationService: TranslationService,
    private val languageModelManager: LanguageModelManager
) {
    
    // ============ 채팅 관리 ============
    
    suspend fun createChat(
        title: String,
        nativeLanguage: SupportedLanguage,
        translateLanguage: SupportedLanguage
    ): Chat {
        val chat = Chat(
            title = title.ifBlank { "이름없는 번역챗" },
            nativeLanguage = nativeLanguage,
            translateLanguage = translateLanguage
        )
        chatRepository.insertChat(chat)
        return chat
    }
    
    suspend fun deleteChat(chatId: String) {
        val chat = getChatById(chatId)
        if (chat != null) {
            chatRepository.deleteChat(chat)
        }
    }
    
    suspend fun getChatById(chatId: String): Chat? {
        return chatRepository.getChatById(chatId)
    }
    
    fun getAllChats(): Flow<List<Chat>> {
        return chatRepository.getAllChats()
    }
    
    fun getMessagesForChat(chatId: String): Flow<List<Message>> {
        return chatRepository.getMessagesByChatId(chatId)
    }
    
    suspend fun clearChatMessages(chatId: String) {
        chatRepository.deleteMessagesByChatId(chatId)
    }
    
    suspend fun updateChatLanguages(
        chatId: String,
        nativeLanguage: SupportedLanguage,
        translateLanguage: SupportedLanguage
    ) {
        val chat = getChatById(chatId) ?: return
        val updatedChat = chat.copy(
            nativeLanguage = nativeLanguage,
            translateLanguage = translateLanguage
        )
        chatRepository.updateChat(updatedChat)
    }
    
    suspend fun updateChatTitleAndLanguages(
        chatId: String,
        title: String,
        nativeLanguage: SupportedLanguage,
        translateLanguage: SupportedLanguage
    ) {
        val chat = getChatById(chatId) ?: return
        val updatedChat = chat.copy(
            title = title,
            nativeLanguage = nativeLanguage,
            translateLanguage = translateLanguage
        )
        chatRepository.updateChat(updatedChat)
    }
    
    // ============ 번역 및 메시지 ============
    
    suspend fun sendMessage(chatId: String, inputText: String): String {
        val chat = getChatById(chatId) ?: throw Exception("채팅방을 찾을 수 없습니다")
        
        // 언어 자동 감지 및 Chat 업데이트
        val updatedChat = detectAndUpdateChatLanguages(chatId, inputText, chat)
        
        // 1. 원본 메시지 저장
        val originalMessage = Message(
            chatId = chatId,
            originalText = inputText,
            translatedText = inputText,
            isUserMessage = true
        )
        chatRepository.insertMessage(originalMessage)
        
        // 2. 번역 수행 (업데이트된 채팅 설정 사용)
        val translatedText = translateText(updatedChat, inputText)
        
        // 3. 번역문이 다르면 번역 메시지도 저장
        if (translatedText != inputText) {
            val translationMessage = Message(
                chatId = chatId,
                originalText = translatedText,
                translatedText = translatedText,
                isUserMessage = false
            )
            chatRepository.insertMessage(translationMessage)
            chatRepository.updateLastMessageTime(chatId, translationMessage.timestamp)
        } else {
            chatRepository.updateLastMessageTime(chatId, originalMessage.timestamp)
        }
        
        return translatedText
    }
    
    private suspend fun detectAndUpdateChatLanguages(chatId: String, inputText: String, chat: Chat): Chat {
        // 텍스트가 너무 짧으면 감지하지 않음
        if (inputText.trim().length < 2) return chat
        
        val detectedLanguage = LanguageDetector.detectLanguage(inputText)
        
        // 감지된 언어가 현재 설정된 언어들과 다른 경우에만 업데이트
        if (detectedLanguage != chat.nativeLanguage && detectedLanguage != chat.translateLanguage) {
            // 입력 텍스트의 언어 특성을 보고 더 정확하게 판단
            val hasKorean = inputText.any { it in '\uAC00'..'\uD7AF' }
            val hasJapanese = inputText.any { it in '\u3040'..'\u309F' || it in '\u30A0'..'\u30FF' || it in '\u4E00'..'\u9FAF' }
            val hasEnglish = inputText.any { it in 'A'..'Z' || it in 'a'..'z' }
            val hasChinese = inputText.any { it in '\u4E00'..'\u9FAF' }
            
            val newNativeLanguage = when {
                hasKorean -> SupportedLanguage.KOREAN
                hasJapanese -> SupportedLanguage.JAPANESE
                hasEnglish -> SupportedLanguage.ENGLISH
                hasChinese -> SupportedLanguage.CHINESE
                else -> chat.nativeLanguage
            }
            
            if (newNativeLanguage != chat.nativeLanguage) {
                val updatedChat = chat.copy(nativeLanguage = newNativeLanguage)
                chatRepository.updateChat(updatedChat)
                Log.d("ChatUseCase", "Updated chat language from ${chat.nativeLanguage.displayName} to ${newNativeLanguage.displayName}")
                return updatedChat
            }
        }
        
        return chat
    }
    
    private suspend fun translateText(chat: Chat, inputText: String): String {
        // 입력 언어 감지
        val detectedLanguage = if (inputText.trim().length >= 2) {
            LanguageDetector.detectLanguage(inputText)
        } else {
            chat.nativeLanguage
        }
        
        // 양방향 번역 로직: 입력 언어에 따라 자동으로 번역 방향 결정
        val (sourceLanguage, targetLanguage) = when {
            // 감지된 언어가 nativeLanguage와 같으면 translateLanguage로 번역
            detectedLanguage == chat.nativeLanguage -> chat.nativeLanguage to chat.translateLanguage
            // 감지된 언어가 translateLanguage와 같으면 nativeLanguage로 번역
            detectedLanguage == chat.translateLanguage -> chat.translateLanguage to chat.nativeLanguage
            // 감지된 언어가 둘 다 아닌 경우 문자 특성으로 판단
            else -> {
                val hasKorean = inputText.any { it in '\uAC00'..'\uD7AF' }
                val hasJapanese = inputText.any { it in '\u3040'..'\u309F' || it in '\u30A0'..'\u30FF' || it in '\u4E00'..'\u9FAF' }
                val hasEnglish = inputText.any { it in 'A'..'Z' || it in 'a'..'z' }
                val hasChinese = inputText.any { it in '\u4E00'..'\u9FAF' }
                
                when {
                    hasKorean && (chat.nativeLanguage == SupportedLanguage.KOREAN || chat.translateLanguage == SupportedLanguage.KOREAN) -> 
                        SupportedLanguage.KOREAN to (if (chat.nativeLanguage == SupportedLanguage.KOREAN) chat.translateLanguage else chat.nativeLanguage)
                    hasJapanese && (chat.nativeLanguage == SupportedLanguage.JAPANESE || chat.translateLanguage == SupportedLanguage.JAPANESE) -> 
                        SupportedLanguage.JAPANESE to (if (chat.nativeLanguage == SupportedLanguage.JAPANESE) chat.translateLanguage else chat.nativeLanguage)
                    hasEnglish && (chat.nativeLanguage == SupportedLanguage.ENGLISH || chat.translateLanguage == SupportedLanguage.ENGLISH) -> 
                        SupportedLanguage.ENGLISH to (if (chat.nativeLanguage == SupportedLanguage.ENGLISH) chat.translateLanguage else chat.nativeLanguage)
                    hasChinese && (chat.nativeLanguage == SupportedLanguage.CHINESE || chat.translateLanguage == SupportedLanguage.CHINESE) -> 
                        SupportedLanguage.CHINESE to (if (chat.nativeLanguage == SupportedLanguage.CHINESE) chat.translateLanguage else chat.nativeLanguage)
                    else -> chat.nativeLanguage to chat.translateLanguage
                }
            }
        }
        
        Log.d("ChatUseCase", "Translation: ${sourceLanguage.displayName} → ${targetLanguage.displayName}")
        
        // 번역 수행
        val result = translationService.translate(inputText, sourceLanguage, targetLanguage)
        
        return if (result.isSuccess) {
            val translation = result.getOrNull()
            // 번역이 성공하고 원본과 다르면 번역 결과 반환, 아니면 원본 반환
            if (translation != null && isDifferentEnough(inputText, translation)) {
                translation
            } else {
                inputText
            }
        } else {
            // 번역 실패 시 에러를 던져서 상위에서 처리하도록 함
            throw Exception(result.exceptionOrNull()?.message ?: "번역에 실패했습니다")
        }
    }
    
    private fun isDifferentEnough(original: String, translated: String): Boolean {
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
               // 텍스트 길이가 많이 다르면 번역된 것으로 간주 (20% 이상 차이)
               (kotlin.math.abs(original.length - translated.length).toFloat() / original.length > 0.2f)
    }
    
    // ============ 모델 관리 ============
    
    suspend fun areModelsDownloaded(chatId: String): Boolean {
        val chat = getChatById(chatId) ?: return false
        
        val nativeModelDownloaded = languageModelManager.isModelDownloaded(chat.nativeLanguage)
        val translateModelDownloaded = languageModelManager.isModelDownloaded(chat.translateLanguage)
        
        // 디버깅용 로그
        Log.d("ChatUseCase", "Chat languages: ${chat.nativeLanguage.displayName} ↔ ${chat.translateLanguage.displayName}")
        Log.d("ChatUseCase", "Native model downloaded: $nativeModelDownloaded")
        Log.d("ChatUseCase", "Translate model downloaded: $translateModelDownloaded")
        Log.d("ChatUseCase", "All models ready: ${nativeModelDownloaded && translateModelDownloaded}")
        
        return nativeModelDownloaded && translateModelDownloaded
    }
    
    suspend fun downloadModelsIfNeeded(chatId: String): Boolean {
        val chat = getChatById(chatId) ?: return false
        
        Log.d("ChatUseCase", "Downloading models for: ${chat.nativeLanguage.displayName} ↔ ${chat.translateLanguage.displayName}")
        
        val result1 = translationService.downloadModelIfNeeded(chat.nativeLanguage, chat.translateLanguage)
        val result2 = translationService.downloadModelIfNeeded(chat.translateLanguage, chat.nativeLanguage)
        
        Log.d("ChatUseCase", "Download results: result1=${result1.isSuccess}, result2=${result2.isSuccess}")
        
        if (result1.isFailure) {
            Log.w("ChatUseCase", "Download failed 1: ${result1.exceptionOrNull()?.message}")
        }
        if (result2.isFailure) {
            Log.w("ChatUseCase", "Download failed 2: ${result2.exceptionOrNull()?.message}")
        }
        
        return result1.isSuccess && result2.isSuccess
    }
} 