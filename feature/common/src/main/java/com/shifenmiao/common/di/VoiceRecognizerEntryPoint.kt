package com.shifenmiao.common.di

import com.shifenmiao.common.voice.VoiceRecognizer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 语音识别的 Hilt EntryPoint。
 *
 * 用途:在非 Hilt 管理的 Composable(如聊天输入栏)中获取 [VoiceRecognizer] 单例。
 * 用法参照 feature:webview 的 WebViewEntryPoint。
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface VoiceRecognizerEntryPoint {

    fun voiceRecognizer(): VoiceRecognizer
}
