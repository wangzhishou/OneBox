package com.wanbaohe.markuplayers.di

import android.graphics.Bitmap
import com.wanbaohe.markuplayers.data.AndroidMarkupLayersApplier
import com.wanbaohe.markuplayers.domain.MarkupLayersApplier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface MarkupLayersModule {

    @Binds
    @Singleton
    fun applier(
        impl: AndroidMarkupLayersApplier
    ): MarkupLayersApplier<Bitmap>

}
