package com.wanbaohe.notification.di

import com.wanbaohe.notification.service.NotificationRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 供非 Hilt 注入场景(如个人中心设置项 Composable)经
 * EntryPointAccessors 获取消息中心仓库。
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationEntryPoint {
    fun notificationRepository(): NotificationRepository
}
