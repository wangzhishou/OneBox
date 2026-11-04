package com.wanbaohe.file.browser.di

import com.shifenmiao.common.search.GlobalSearchProvider
import com.wanbaohe.file.browser.search.WorkspaceFileSearchProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
object WorkspaceFileSearchProviderModule {

    @Provides
    @IntoSet
    fun provideWorkspaceFileSearchProvider(
        impl: WorkspaceFileSearchProvider,
    ): GlobalSearchProvider = impl
}
