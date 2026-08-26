package com.t8rin.imagetoolbox.core.settings.di

import com.t8rin.imagetoolbox.core.settings.data.FontDownloadStore
import com.t8rin.imagetoolbox.core.settings.domain.FontCatalog
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface FontsModule {

    @Binds
    fun fontCatalog(
        impl: FontDownloadStore
    ): FontCatalog
}
