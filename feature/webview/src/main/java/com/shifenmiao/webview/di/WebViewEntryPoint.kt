package com.shifenmiao.webview.di

import com.shifenmiao.webview.resource.WebResourceEngine
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * WebView 子系统的 Hilt EntryPoint。
 *
 * 用途：在非 Hilt 管理的 Composable / 工具类中获取 [WebResourceEngine] 等
 * feature:webview 提供的 Hilt 单例。
 *
 * 用法：
 * ```
 * val engine = EntryPointAccessors.fromApplication(
 *     context = context,
 *     entryPoint = WebViewEntryPoint::class.java,
 * ).webResourceEngine()
 * ```
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WebViewEntryPoint {

    fun webResourceEngine(): WebResourceEngine
}
