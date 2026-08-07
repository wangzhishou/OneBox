package com.shifenmiao.marktodo.di

import com.shifenmiao.marktodo.service.MarkTodoServiceImpl
import com.shifenmiao.model.todo.MarkTodoServiceInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MarkTodoModule {

    @Singleton
    @Provides
    fun provideMarkTodoService(impl: MarkTodoServiceImpl): MarkTodoServiceInterface = impl
}
