package com.shifenmiao.base.entrypoint

import com.shifenmiao.base.channel.ChannelConfig
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 渠道配置 EntryPoint.
 *
 * 业务模块无法直接 @Inject ChannelConfig (因为它在 Composable 里, 不在 HiltComponent 里),
 * 改用 EntryPoint 拿一次:
 *   val cfg = EntryPointAccessors.fromApplication(context, ChannelConfigEntryPoint::class.java).getChannelConfig()
 *
 * 静态 @Inject 的类 (Hilt Component 内部) 直接用 @Inject ChannelConfig 即可.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ChannelConfigEntryPoint {
    fun getChannelConfig(): ChannelConfig
}
