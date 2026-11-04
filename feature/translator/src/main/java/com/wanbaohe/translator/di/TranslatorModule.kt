package com.wanbaohe.translator.di

import com.wanbaohe.translator.data.AiTranslationService
import com.wanbaohe.translator.data.DefaultTranslationSpeechService
import com.wanbaohe.translator.data.MmkvTranslationHistoryRepository
import com.wanbaohe.translator.domain.TranslationHistoryRepository
import com.wanbaohe.translator.domain.TranslationService
import com.wanbaohe.translator.domain.TranslationSpeechService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface TranslatorModule {
    @Binds
    @Singleton
    fun translationHistoryRepository(
        impl: MmkvTranslationHistoryRepository,
    ): TranslationHistoryRepository

    @Binds
    @Singleton
    fun translationService(
        impl: AiTranslationService,
    ): TranslationService

    @Binds
    @Singleton
    fun translationSpeechService(
        impl: DefaultTranslationSpeechService,
    ): TranslationSpeechService
}
