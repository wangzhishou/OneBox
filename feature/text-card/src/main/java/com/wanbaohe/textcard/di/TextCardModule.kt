package com.wanbaohe.textcard.di

import com.wanbaohe.textcard.data.canvas.PrefsCustomCanvasStore
import com.wanbaohe.textcard.data.paper.RemotePaperRepository
import com.wanbaohe.textcard.data.render.AndroidTextCardExportRenderer
import com.wanbaohe.textcard.domain.CustomCanvasStore
import com.wanbaohe.textcard.domain.TextCardExportRenderer
import com.wanbaohe.textcard.domain.TextCardPaperRepository
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
    fun paperRepository(
        impl: RemotePaperRepository
    ): TextCardPaperRepository

    @Binds
    fun customCanvasStore(
        impl: PrefsCustomCanvasStore
    ): CustomCanvasStore
}
