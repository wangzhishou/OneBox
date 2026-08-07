package com.t8rin.imagetoolbox.feature.root.presentation.components.navigation

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DemoScreenProviderModule {
    @Binds
    abstract fun bindDemoScreenProvider(impl: DemoScreenProviderImpl): DemoScreenProvider
}
