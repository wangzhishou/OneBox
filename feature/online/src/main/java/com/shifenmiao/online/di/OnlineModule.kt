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