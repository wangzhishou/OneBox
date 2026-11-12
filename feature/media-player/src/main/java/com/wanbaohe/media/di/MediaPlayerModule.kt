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

package com.wanbaohe.media.di

import com.wanbaohe.media.data.AndroidMediaPermissionChecker
import com.wanbaohe.media.data.ContentResolverMediaEntryResolver
import com.wanbaohe.media.data.ContentResolverMediaMetadataSource
import com.wanbaohe.media.data.MediaFavoritesStore
import com.wanbaohe.media.data.MediaStoreDeviceLibrarySource
import com.wanbaohe.media.data.RecentMediaLibraryRepository
import com.wanbaohe.media.domain.MediaDeviceLibrarySource
import com.wanbaohe.media.domain.MediaEntryResolver
import com.wanbaohe.media.domain.MediaFavoritesRepository
import com.wanbaohe.media.domain.MediaLibraryRepository
import com.wanbaohe.media.domain.MediaMetadataSource
import com.wanbaohe.media.domain.MediaPermissionChecker
import com.wanbaohe.media.domain.MediaPlaybackController
import com.wanbaohe.media.playback.ExoPlaybackEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 媒体播放模块的依赖倒置接线。
 *
 * 页面/组件只认 domain 里的接口，ExoPlayer、MediaMetadataRetriever、SharedPreferences
 * 这些具体实现全部收在这个模块里。
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class MediaPlayerModule {

    @Binds
    @Singleton
    abstract fun bindLibrary(impl: RecentMediaLibraryRepository): MediaLibraryRepository

    @Binds
    @Singleton
    abstract fun bindFavorites(impl: MediaFavoritesStore): MediaFavoritesRepository

    @Binds
    @Singleton
    abstract fun bindMetadata(impl: ContentResolverMediaMetadataSource): MediaMetadataSource

    @Binds
    @Singleton
    abstract fun bindEntryResolver(impl: ContentResolverMediaEntryResolver): MediaEntryResolver

    @Binds
    @Singleton
    abstract fun bindDeviceLibrary(impl: MediaStoreDeviceLibrarySource): MediaDeviceLibrarySource

    @Binds
    @Singleton
    abstract fun bindPermissionChecker(impl: AndroidMediaPermissionChecker): MediaPermissionChecker

    @Binds
    @Singleton
    abstract fun bindPlaybackController(impl: ExoPlaybackEngine): MediaPlaybackController
}
