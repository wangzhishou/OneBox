package com.shifenmiao.online.di

import com.shifenmiao.model.note.NoteService
import com.shifenmiao.model.datasource.DataItemRemoteDataSource
import com.shifenmiao.network.api.ApiService
import com.shifenmiao.online.datasource.DataItemRemoteDataSourceImpl
import com.shifenmiao.online.service.NoteServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OnlineModule {
    @Singleton
    @Provides
    fun providesDataItemRemoteDataSource(
        apiService: ApiService
    ): DataItemRemoteDataSource {
        return DataItemRemoteDataSourceImpl(apiService = apiService)
    }

    @Singleton
    @Provides
    fun provideNoteService(
        noteServiceImpl: NoteServiceImpl
    ): NoteService {
        return noteServiceImpl
    }
}