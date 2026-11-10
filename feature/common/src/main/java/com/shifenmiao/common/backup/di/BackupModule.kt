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
