/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.common.di

import com.shifenmiao.common.blog.BlogRepository
import com.shifenmiao.common.blog.BlogRepositoryImpl
import com.shifenmiao.common.sync.ItemSyncManager
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.model.datasource.DataItemRemoteDataSource
import com.t8rin.imagetoolbox.core.domain.content.ContentTypeResolver
import com.t8rin.imagetoolbox.core.domain.content.DefaultContentTypeResolver
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.content.ContentRouter
import com.wanbaohe.core.ui.utils.content.DefaultContentRouter
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
