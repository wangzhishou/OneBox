package com.shifenmiao.common.editor

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class EditorDraftModule {
    @Binds
    @Singleton
    abstract fun bindEditorDraftRepository(
        repository: MmkvEditorDraftRepository,
    ): EditorDraftRepository
}
