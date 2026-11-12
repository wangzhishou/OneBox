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

package com.shifenmiao.common.backup.di

import com.shifenmiao.common.backup.destination.BackupDestination
import com.shifenmiao.common.backup.destination.DocumentBackupDestination
import com.shifenmiao.common.backup.destination.ShareBackupDestination
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

/**
 * 备份导出目标的注册表。
 *
 * 用 Set 多绑定而不是直接 new：以后加「存到我的网盘（WebDAV / S3）」时，
 * 在对应模块再加一个 `@IntoSet` 实现即可，页面拿到的列表自动变长。
 */
@Module
@InstallIn(SingletonComponent::class)
internal object BackupModule {

    @Provides
    @IntoSet
    fun provideDocumentDestination(
        destination: DocumentBackupDestination,
    ): BackupDestination = destination

    @Provides
    @IntoSet
    fun provideShareDestination(
        destination: ShareBackupDestination,
    ): BackupDestination = destination
}
