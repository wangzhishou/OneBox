package com.shifenmiao.ai.prompt

import android.os.Build
import com.shifenmiao.ai.BuildConfig
import com.shifenmiao.ai.R
import com.shifenmiao.ai.file.AppWorkspaceResolver
import com.t8rin.imagetoolbox.core.utils.getString
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
}
