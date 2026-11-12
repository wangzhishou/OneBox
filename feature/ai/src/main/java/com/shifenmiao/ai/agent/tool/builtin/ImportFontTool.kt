/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.ai.agent.tool.builtin

import android.net.Uri
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.ToolDeepLink
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.model.jsonStringOf
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.core.settings.domain.model.DomainFontFamily
import java.io.File
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

/**
 * 自定义字体导入工具:列出 / 导入 / 移除 App 自定义字体,导入时可顺带设为全局字体。
 *
 * 导入逻辑复用 [SettingsManager.importCustomFont](与显示设置页同一入口):
 * 拷贝字体文件到 filesDir/customFonts/ 并校验 Typeface,重名文件会替换旧字体。
 */
class ImportFontTool @Inject constructor(
    private val settingsManager: SettingsManager,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "import_font"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_import_font)

    override val title: String = textProvider.string(R.string.agent_tool_import_font_title)

    override val summary: String = textProvider.string(R.string.agent_tool_import_font_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_import_font_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_import_font_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val parallelizable: Boolean = false

    override val sortOrder: Int = -76

    override val deepLinks: List<ToolDeepLink> = listOf(
        ToolDeepLink(
            uri = AppNavigationRegistry.buildStructuredDeeplink(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "display_settings",
            ),
            label = textProvider.string(R.string.agent_tool_import_font_deeplink_label),
            guidance = textProvider.string(R.string.agent_tool_import_font_deeplink_guidance),
            primary = true,
        )
    )

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_import_font_param_action),
                enum = listOf("list", "import", "remove"),
            ),
            "source" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_import_font_param_source),
            ),
            "name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_import_font_param_name),
            ),
            "apply" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_import_font_param_apply),
            ),
        ),
        required = listOf("action"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val params = parseArguments(arguments)
            when (params.action?.lowercase()) {
                "list", "get" -> handleList()
                "import", "add" -> handleImport(params)
                "remove", "delete" -> handleRemove(params)
                null, "" -> errorResult(
                    action = "unknown",
                    reasonCode = "missing_action",
                    message = textProvider.string(R.string.agent_tool_import_font_missing_action),
                )
                else -> errorResult(
                    action = params.action,
                    reasonCode = "unknown_action",
                    message = textProvider.string(
                        R.string.agent_tool_import_font_unknown_action,
                        params.action,
                    ),
                )
            }
        }.getOrElse { error ->
            errorResult(
                action = "unknown",
                reasonCode = "exception",
                message = textProvider.string(
                    R.string.agent_tool_import_font_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_unknown_error),
                ),
            )
        }
    }

    private suspend fun handleList(): AgentToolResult {
        val state = settingsManager.getSettingsState()
        val currentPath = (state.font as? DomainFontFamily.Custom)?.filePath
        val fonts = state.customFonts.map { font ->
            linkedMapOf<String, Any?>(
                "name" to font.name,
                "filePath" to font.filePath,
                "isCurrent" to (font.filePath == currentPath),
            )
        }
        val message = if (fonts.isEmpty()) {
            textProvider.string(R.string.agent_tool_import_font_list_empty_message)
        } else {
            textProvider.string(R.string.agent_tool_import_font_list_message)
        }
        val payload = linkedMapOf<String, Any?>(
            "status" to "ok",
            "action" to "list",
            "message" to message,
            "count" to fonts.size,
            "customFonts" to fonts,
        )
        return AgentToolResult(content = jsonStringOf(payload))
    }

    private suspend fun handleImport(params: ImportFontParams): AgentToolResult {
        val raw = params.source?.trim()
        if (raw.isNullOrEmpty()) {
            return errorResult(
                action = "import",
                reasonCode = "missing_source",
                message = textProvider.string(R.string.agent_tool_import_font_missing_source),
            )
        }
        if (!raw.startsWith("content://")) {
            val file = File(raw.removePrefix("file://"))
            if (!file.isFile) {
                return errorResult(
                    action = "import",
                    reasonCode = "source_not_found",
                    message = textProvider.string(R.string.agent_tool_import_font_source_not_found, raw),
                )
            }
            if (file.extension.lowercase() !in SUPPORTED_EXTENSIONS) {
                val isArchive = file.extension.lowercase() == "zip"
                return errorResult(
                    action = "import",
                    reasonCode = "unsupported_format",
                    message = if (isArchive) {
                        textProvider.string(R.string.agent_tool_import_font_use_unzip, raw)
                    } else {
                        textProvider.string(R.string.agent_tool_import_font_unsupported_format, raw)
                    },
                    validOptions = mapOf("extensions" to SUPPORTED_EXTENSIONS.toList()),
                )
            }
        }
        val font = settingsManager.importCustomFont(raw.toFontUri())
            ?: return errorResult(
                action = "import",
                reasonCode = "invalid_font",
                message = textProvider.string(R.string.agent_tool_import_font_invalid_font, raw),
            )
        val applied = params.apply == true
        if (applied) {
            settingsManager.setFont(font)
        }
        val message = if (applied) {
            textProvider.string(R.string.agent_tool_import_font_import_applied_message, font.displayName())
        } else {
            textProvider.string(R.string.agent_tool_import_font_import_message, font.displayName())
        }
        return successResult(action = "import", message = message, font = font, applied = applied)
    }

    private suspend fun handleRemove(params: ImportFontParams): AgentToolResult {
        val key = params.name?.trim()
        if (key.isNullOrEmpty()) {
            return errorResult(
                action = "remove",
                reasonCode = "missing_name",
                message = textProvider.string(R.string.agent_tool_import_font_missing_name),
            )
        }
        val state = settingsManager.getSettingsState()
        val target = state.customFonts.firstOrNull { font ->
            font.filePath == key || font.name.equals(key, ignoreCase = true)
        } ?: return errorResult(
            action = "remove",
            reasonCode = "font_not_found",
            message = textProvider.string(R.string.agent_tool_import_font_remove_not_found, key),
        )
        settingsManager.removeCustomFont(target)
        // 被移除的字体若是当前全局字体,回退为系统字体,避免悬空的字体路径
        if ((state.font as? DomainFontFamily.Custom)?.filePath == target.filePath) {
            settingsManager.setFont(DomainFontFamily.System)
        }
        val message = textProvider.string(
            R.string.agent_tool_import_font_remove_message,
            target.displayName(),
        )
        return successResult(action = "remove", message = message, font = target, applied = false)
    }

    private fun String.toFontUri(): String {
        if (startsWith("content://")) return this
        return Uri.fromFile(File(removePrefix("file://"))).toString()
    }

    private fun DomainFontFamily.Custom.displayName(): String = name ?: filePath

    private fun parseArguments(arguments: String): ImportFontParams {
        if (arguments.isBlank()) return ImportFontParams()
        return runCatching { AppJson.decodeFromString<ImportFontParams>(arguments) }.getOrNull() ?: ImportFontParams()
    }

    private fun successResult(
        action: String,
        message: String,
        font: DomainFontFamily.Custom,
        applied: Boolean,
    ): AgentToolResult {
        val payload = linkedMapOf<String, Any?>(
            "status" to "ok",
            "action" to action,
            "message" to message,
            "font" to linkedMapOf<String, Any?>(
                "name" to font.name,
                "filePath" to font.filePath,
            ),
            "appliedAsAppFont" to applied,
        )
        return AgentToolResult(content = jsonStringOf(payload))
    }

    private fun errorResult(
        action: String,
        reasonCode: String,
        message: String,
        validOptions: Map<String, Any?>? = null,
    ): AgentToolResult {
        val payload = linkedMapOf<String, Any?>(
            "status" to "error",
            "action" to action,
            "reasonCode" to reasonCode,
            "message" to message,
        )
        if (validOptions != null) payload["validOptions"] = validOptions
        return AgentToolResult(content = jsonStringOf(payload), isError = true)
    }

    @Serializable
    private data class ImportFontParams(
        val action: String? = null,
        val source: String? = null,
        val name: String? = null,
        val apply: Boolean? = null,
    )

    private companion object {
        val SUPPORTED_EXTENSIONS = setOf("ttf", "otf", "ttc")
    }
}
