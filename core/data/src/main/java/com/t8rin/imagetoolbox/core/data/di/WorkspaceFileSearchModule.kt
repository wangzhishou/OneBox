package com.t8rin.imagetoolbox.core.data.di

import com.t8rin.imagetoolbox.core.data.workspace.search.WorkspaceFileSearchServiceImpl
import com.t8rin.imagetoolbox.core.domain.workspace.WorkspaceFileSearchService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface WorkspaceFileSearchModule {

    @Singleton
    @Binds
    fun bindWorkspaceFileSearchService(
        impl: WorkspaceFileSearchServiceImpl,
    ): WorkspaceFileSearchService
}
