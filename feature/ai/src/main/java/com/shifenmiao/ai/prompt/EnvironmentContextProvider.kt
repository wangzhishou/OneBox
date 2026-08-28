package com.shifenmiao.ai.prompt

import android.os.Build
import com.shifenmiao.ai.BuildConfig
import com.shifenmiao.ai.R
import com.t8rin.imagetoolbox.core.data.workspace.AppWorkspaceResolver
import com.t8rin.imagetoolbox.core.utils.getString
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

/**
 * 提供当前运行环境上下文信息，用于注入到 LLM 系统提示词中。
 */
interface EnvironmentContextProvider {
    fun buildContextText(): String
}

/**
 * Android 平台实现。
 */
class AndroidEnvironmentContextProvider @Inject constructor(
    private val appWorkspaceResolver: AppWorkspaceResolver,
) : EnvironmentContextProvider {

    override fun buildContextText(): String = buildString {
        appendLine(getString(R.string.ai_environment_context_title))
        appendLine(
            getString(
                R.string.ai_environment_context_time,
                currentTimeText()
            )
        )
        appendLine(
            getString(
                R.string.ai_environment_context_language,
                currentLanguageText()
            )
        )
        appendLine(
            getString(
                R.string.ai_environment_context_os,
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT
            )
        )
        appendLine(
            getString(
                R.string.ai_environment_context_arch,
                Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"
            )
        )
        appendLine(
            getString(
                R.string.ai_environment_context_model,
                Build.MODEL
            )
        )
        appendLine(
            getString(
                R.string.ai_environment_context_app_version,
                BuildConfig.VersionName,
                BuildConfig.VersionCode
            )
        )

        appendLine(
            getString(
                R.string.ai_environment_context_work_dir,
                appWorkspaceResolver.resolve().file.absolutePath
            )
        )
    }.trimEnd()

    /** 例: 2026-08-11T08:18:16.717+08:00 星期二 (Asia/Shanghai) — 含时区与星期,便于 LLM 推算"今天/明天/上周五" */
    private fun currentTimeText(): String {
        val now = ZonedDateTime.now()
        val dayOfWeek = now.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        return "${now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)} $dayOfWeek (${now.zone.id})"
    }

    /** 例: 中文(简体) (zh-CN) — 告诉模型用用户语言生成内容(记账分类/笔记/待办等) */
    private fun currentLanguageText(): String {
        val locale = Locale.getDefault()
        return "${locale.getDisplayName(locale)} (${locale.toLanguageTag()})"
    }
}
