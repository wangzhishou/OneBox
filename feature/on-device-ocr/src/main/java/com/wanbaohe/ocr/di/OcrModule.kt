package com.wanbaohe.ocr.di

import com.wanbaohe.ocr.data.MmkvOcrHistoryRepository
import com.wanbaohe.ocr.domain.OcrHistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface OcrModule {
    @Binds
    @Singleton
    fun ocrHistoryRepository(
        impl: MmkvOcrHistoryRepository,
    ): OcrHistoryRepository
}
