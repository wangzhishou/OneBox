package com.wanbaohe.textcard.di

import com.wanbaohe.textcard.data.font.FontDownloadStore
import com.wanbaohe.textcard.data.render.AndroidTextCardExportRenderer
import com.wanbaohe.textcard.domain.FontCatalog
import com.wanbaohe.textcard.domain.TextCardExportRenderer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface TextCardModule {

    @Binds
    fun exportRenderer(
        impl: AndroidTextCardExportRenderer
    ): TextCardExportRenderer

    @Binds
    fun fontCatalog(
        impl: FontDownloadStore
    ): FontCatalog
}
