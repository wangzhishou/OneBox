package com.shifenmiao.ai.agent.tool

import android.content.Context
import androidx.annotation.ArrayRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 远程存储 AgentTool 文本提供器 —— 与 [AgentToolTextProvider] 同形，
 * 绑 [com.shifenmiao.ai] 模块 R 资源（agent_tool_* 字符串/数组/raw 描述），
 * 保证文案走资源，不在 Kotlin 硬编码。
 */
@Singleton
class CloudAgentToolTextProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun string(@StringRes resId: Int, vararg args: Any): String =
        context.getString(resId, *args)

    fun array(@ArrayRes resId: Int): List<String> =
        context.resources.getStringArray(resId).toList()

    fun raw(@RawRes resId: Int): String =
        context.resources.openRawResource(resId)
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
            .trim()
}
