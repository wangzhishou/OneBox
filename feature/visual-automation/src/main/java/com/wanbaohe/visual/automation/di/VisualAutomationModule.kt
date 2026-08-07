package com.wanbaohe.visual.automation.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 视觉自动化模块的 Hilt 模块。
 * VisualAutomationController 和 VisualAIClient 已使用 @Inject 构造函数，
 * 无需额外 Provides 声明。
 */
@Module
@InstallIn(SingletonComponent::class)
object VisualAutomationModule
