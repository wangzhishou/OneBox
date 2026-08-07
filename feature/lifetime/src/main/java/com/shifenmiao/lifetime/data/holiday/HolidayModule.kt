package com.shifenmiao.lifetime.data.holiday

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 将 [LunarHolidayProvider] 绑定到 [HolidayProvider] 接口。
 *
 * 抽象出接口便于在测试或特殊场景替换源；当前生产实现固定为 [LunarHolidayProvider]。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class HolidayModule {

    @Binds
    @Singleton
    abstract fun bindHolidayProvider(impl: LunarHolidayProvider): HolidayProvider
}
