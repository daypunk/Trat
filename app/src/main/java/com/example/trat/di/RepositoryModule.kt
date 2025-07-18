package com.example.trat.di

import com.example.trat.data.repository.ChatRepository
import com.example.trat.data.repository.SpeechToTextRepository
import com.example.trat.data.repository.TtsRepository
import com.example.trat.domain.repository.ChatRepositoryInterface
import com.example.trat.domain.repository.SpeechToTextRepositoryInterface
import com.example.trat.domain.repository.TtsRepositoryInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepository: ChatRepository
    ): ChatRepositoryInterface
    
    @Binds
    @Singleton
    abstract fun bindSpeechToTextRepository(
        speechToTextRepository: SpeechToTextRepository
    ): SpeechToTextRepositoryInterface
    
    @Binds
    @Singleton
    abstract fun bindTtsRepository(
        ttsRepository: TtsRepository
    ): TtsRepositoryInterface
} 