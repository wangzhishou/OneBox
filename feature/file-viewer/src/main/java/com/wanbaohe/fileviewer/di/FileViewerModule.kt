package com.wanbaohe.fileviewer.di

import com.wanbaohe.fileviewer.data.ContentResolverDocumentSource
import com.wanbaohe.fileviewer.domain.DocumentSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class FileViewerModule {

    @Binds
    @Singleton
    abstract fun bindDocumentSource(impl: ContentResolverDocumentSource): DocumentSource
}
