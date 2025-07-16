package com.example.trat.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * UseCase와 도메인 서비스들을 위한 Hilt 모듈
 * 
 * 모든 UseCase와 서비스들은 @Singleton과 @Inject가 설정되어 있어
 * 자동으로 의존성 주입이 처리됩니다.
 * 
 * 포함된 클래스들:
 * - ChatManagementUseCase
 * - MessageUseCase
 * - LanguageDetectionUseCase
 * - TranslationUseCase
 * - MessageTranslationUseCase (조합 UseCase)
 * - LanguageDetectionService
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    // 모든 UseCase들이 @Singleton과 @Inject로 설정되어 있어
    // 별도의 Provider 메서드가 필요하지 않습니다.
    // Hilt가 자동으로 의존성 그래프를 생성합니다.
} 