package com.shifenmiao.marktodo.di

import com.shifenmiao.common.search.GlobalSearchProvider
import com.shifenmiao.marktodo.search.MarkTodoSearchProvider
import com.shifenmiao.marktodo.service.MarkTodoServiceImpl
import com.shifenmiao.model.todo.MarkTodoServiceInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MarkTodoModule {

    @Singleton
    @Provides
    fun provideMarkTodoService(impl: MarkTodoServiceImpl): MarkTodoServiceInterface = impl

    @Provides
    @IntoSet
    fun provideMarkTodoSearchProvider(provider: MarkTodoSearchProvider): GlobalSearchProvider = provider
}
