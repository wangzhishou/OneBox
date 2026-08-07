package com.shifenmiao.common.di

import com.shifenmiao.common.blog.BlogRepository
import com.shifenmiao.common.blog.BlogRepositoryImpl
import com.shifenmiao.common.sync.ItemSyncManager
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.model.datasource.DataItemRemoteDataSource
import com.t8rin.imagetoolbox.core.domain.content.ContentTypeResolver
import com.t8rin.imagetoolbox.core.domain.content.DefaultContentTypeResolver
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.content.ContentRouter
import com.t8rin.imagetoolbox.core.ui.utils.content.DefaultContentRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object CommonModule {

    @Provides
    @Singleton
    fun provideContentTypeResolver(): ContentTypeResolver = DefaultContentTypeResolver()

    @Provides
    @Singleton
    fun provideContentRouter(
        contentTypeResolver: ContentTypeResolver
    ): ContentRouter = DefaultContentRouter(contentTypeResolver)

    @Provides
    @Singleton
    fun provideItemSyncManager(
        appDatabase: AppDatabase,
        remoteDataSource: DataItemRemoteDataSource,
        dispatchersHolder: DispatchersHolder,
    ): ItemSyncManager {
        return ItemSyncManager(
            appDatabase = appDatabase,
            remoteDataSource = remoteDataSource,
            dispatchersHolder = dispatchersHolder,
        )
    }

    @Provides
    @Singleton
    fun provideBlogRepository(
        impl: BlogRepositoryImpl
    ): BlogRepository = impl
}
