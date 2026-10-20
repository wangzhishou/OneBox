package com.t8rin.imagetoolbox.feature.pick_color.di

import com.t8rin.imagetoolbox.feature.pick_color.data.MmkvColorPickHistoryRepository
import com.t8rin.imagetoolbox.feature.pick_color.domain.ColorPickHistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface PickColorModule {

    @Binds
    @Singleton
    fun colorPickHistoryRepository(
        impl: MmkvColorPickHistoryRepository
    ): ColorPickHistoryRepository
}
