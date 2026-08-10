package com.shifenmiao.ai.export

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import com.google.gson.Gson
import com.shifenmiao.ai.BuildConfig
import com.shifenmiao.ai.utils.AttachmentPayloadUtils
import com.shifenmiao.common.ai.aigc.AigcInfoGenerator
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.core.R
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.storage.AppSharedStorage
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ai.AIGCInfo
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AttachmentPayloadDto
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.RoleType
import com.shifenmiao.model.channel.FlavorType
import com.shifenmiao.theme.AppTheme
import io.noties.markwon.utils.MarkdownStringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

/**
 * 导出 AI 聊天记录为 HTML 格式。
 *
 * 基于 assets 模板 + 占位符替换的方式生成，支持：
 * - Glass 毛玻璃消息气泡风格
 * - 日期分隔线（按天分组）
 * - AI 头像与模型标签
 * - 用户附件图片（通过 /local-file/ 本地拦截加载）
 * - 推理过程展示（左侧竖线 + 折叠面板）
 * - 代码块 copy 按钮
 * - AIGC 信息标识
 * - 动态主题色（light / dark）
 */
class HtmlExporter {

    /**
     * 最后一条 assistant 消息对应的 AIGCInfo JSON（明文）。
     *
     * 在 [exportToHtml] 末尾被赋值；调用 方可据此把 AIGC 元数据嵌入到 PDF metadata。
     * 若会话不含 assistant 消息则保持为空串。
     */
    var aIgcInfoString: String = ""
        private set

    private var cachedColorScheme: ColorScheme? = null
    private var cachedTailwindConfig: String? = null

    /**
     * 导出聊天记录为 HTML 字符串。
     *
     * @param conversation 会话信息
     * @param messages 消息列表（未排序，内部会按 createdAt 排序）
     * @param aiEngineCatalogManager 引擎目录管理器（用于解析模型名称）
 * @param colorScheme 当前主题色（默认跟随 AppTheme）
     * @param isDark 是否为深色模式（默认跟随 AppTheme）
     */
    suspend fun exportToHtml(
        conversation: Conversation,
        messages: List<MessageEntity>,
        aiEngineCatalogManager: AIEngineCatalogManager,
        colorScheme: ColorScheme = AppTheme.colorScheme,
        isDark: Boolean = AppTheme.isDarkTheme,
    ): String = withContext(Dispatchers.IO) {
        val template = readTemplate()
        val baseBodyMediumPx = 14f
        val fontSizePx = (baseBodyMediumPx * AppTheme.fontScale).roundToInt()

        // 以最后一条 assistant 消息（按 createdAt）为代表生成 AIGCInfo JSON，
        // 供调用方注入到导出文件的 metadata（不再依赖文本里的零宽水印）。
        aIgcInfoString = messages
            .filter { it.role == RoleType.ASSISTANT.value }
            .maxByOrNull { it.createdAt }
            ?.let { message ->
                val modelTitle = aiEngineCatalogManager.getAiModelTitleByModel(message.model)
                    .takeIf { it.isNotBlank() } ?: message.model
                AigcInfoGenerator.generateJson(
                    engine = conversation.engine,
                    model = AiModel(name = message.model, title = modelTitle),
                    completionId = message.completionId,
                    conversationId = conversation.id,
                    contentId = message.id.toString(),
                    entryTypeName = conversation.entryType.name,
                    entryRefId = conversation.entryRefId,
                )
            }
            .orEmpty()

        val tailwindConfig = buildTailwindConfig(colorScheme)
        val customCss = buildCustomCss(colorScheme, fontSizePx)
        val inlineScript = buildInlineScript()
        val darkModeClass = if (isDark) "dark" else ""
        val promptHtml = buildPromptHtml(conversation, colorScheme)
        val messagesHtml = buildMessagesHtml(messages, aiEngineCatalogManager, conversation)
        // "AI 生成内容"显式标识是国内合规要求,海外(google)渠道不展示
        val noticeBannerHtml = if (FlavorType.fromName() == FlavorType.GOOGLE) {
            ""
        } else {
            val contentNotice = escapeHtml(AppContext.getString(R.string.ai_content_notice_short))
            """<div class="ai-notice-banner" role="note" aria-label="AI 生成内容声明">$contentNotice</div>"""
        }
        val headerHtml = buildHeaderHtml(conversation, colorScheme)

        template
            .replace("{{TAILWIND_CONFIG}}", tailwindConfig)
            .replace("{{CUSTOM_CSS}}", customCss)
            .replace("{{DARK_MODE_CLASS}}", darkModeClass)
            .replace("{{TITLE}}", escapeHtml(conversation.title))
            .replace("{{HEADER_HTML}}", headerHtml)
            .replace("{{PROMPT_HTML}}", promptHtml)
            .replace("{{MESSAGES_HTML}}", messagesHtml)
            .replace("{{NOTICE_BANNER_HTML}}", noticeBannerHtml)
            .replace("{{INLINE_SCRIPT}}", inlineScript)
    }

    // ===== 模板读取 =====

    private fun readTemplate(): String {
        if (cachedTemplate != null) return cachedTemplate!!
        val content = AppContext.getContext().assets.open("ai-share/template.html")
            .bufferedReader().use { it.readText() }
        cachedTemplate = content
        return content
    }

    // ===== 消息 HTML 生成 =====

    private fun buildMessagesHtml(
        messages: List<MessageEntity>,
        aiEngineCatalogManager: AIEngineCatalogManager,
        conversation: Conversation,
    ): String {
        val dateFormat = SimpleDateFormat("yyyy年M月d日", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val sortedMessages = messages.sortedBy { it.createdAt }

        var lastDate: String? = null

        return buildString {
            sortedMessages.forEach { message ->
                val dateStr = dateFormat.format(message.createdAt)
                if (dateStr != lastDate) {
                    append(createDateDividerHtml(dateStr))
                    lastDate = dateStr
                }

                when (message.role) {
                    RoleType.USER.value -> append(createUserMessageHtml(message, timeFormat))
                    RoleType.ASSISTANT.value -> append(
                        createAssistantMessageHtml(
                            message,
                            aiEngineCatalogManager,
                            timeFormat,
                            conversation,
                        )
                    )
                }
            }
        }
    }

    private fun createDateDividerHtml(dateStr: String): String {
        return """
            <div class="date-divider"><span>$dateStr</span></div>
        """.trimIndent()
    }

    private fun createUserMessageHtml(
        message: MessageEntity,
        timeFormat: SimpleDateFormat
    ): String {
        val content = MarkdownStringUtils.convertMarkdownToHtml(message.question)
        val attachmentsHtml = createAttachmentsHtml(message)
        val timeStr = timeFormat.format(message.createdAt)

        return """
            <div class="msg-user" data-message-id="${message.completionId}">
                <div>
                    <div class="bubble-user">
                        $attachmentsHtml
                        <div class="prose">$content</div>
                    </div>
                    <div class="msg-meta-user">${escapeHtml(timeStr)}</div>
                </div>
            </div>
        """.trimIndent()
    }

    private fun createAssistantMessageHtml(
        message: MessageEntity,
        aiEngineCatalogManager: AIEngineCatalogManager,
        timeFormat: SimpleDateFormat,
        conversation: Conversation,
    ): String {
        val content = MarkdownStringUtils.convertMarkdownToHtml(message.answer)
        val reasoningHtml = createReasoningHtml(message)
        val modelTitle = aiEngineCatalogManager.getAiModelTitleByModel(message.model)
            .takeIf { it.isNotBlank() } ?: message.model
        val aigcInfo = AigcInfoGenerator.generate(
            engine = conversation.engine,
            model = AiModel(name = message.model, title = modelTitle),
            completionId = message.completionId,
            conversationId = conversation.id,
            contentId = message.id.toString(),
            entryTypeName = conversation.entryType.name,
            entryRefId = conversation.entryRefId,
        )
        val aigcHtml = renderAigcDebugTable(aigcInfo)
        val modelName = escapeHtml(modelTitle.takeIf { it.isNotBlank() } ?: "AI")
        val timeStr = timeFormat.format(message.createdAt)

        return """
            <div class="msg-ai" data-message-id="${message.completionId}">
                <div class="ai-header">
                    <div class="ai-avatar" aria-hidden="true">
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
                            <rect x="4" y="10" width="16" height="10" rx="2.5"/>
                            <circle cx="12" cy="5" r="2.5"/>
                            <path d="M12 7.5v3"/>
                            <circle cx="8.5" cy="14.5" r="1"/>
                            <circle cx="15.5" cy="14.5" r="1"/>
                        </svg>
                    </div>
                    <div class="model-tag">$modelName</div>
                </div>
                <div class="bubble-ai-wrapper">
                    <div class="bubble-ai">
                        $reasoningHtml
                        <div class="prose ${if (reasoningHtml.isNotEmpty()) "pt-2" else ""}">$content</div>
                        $aigcHtml
                    </div>
                </div>
                <div class="msg-meta-ai">${escapeHtml(timeStr)}</div>
            </div>
        """.trimIndent()
    }

    // ===== 附件处理 =====

    private fun createAttachmentsHtml(message: MessageEntity): String {
        if (message.attachmentsJson.isBlank()) return ""

        val attachments = try {
            AttachmentPayloadUtils.deserialize(message.attachmentsJson, Gson())
        } catch (_: Exception) {
            emptyList()
        }
        if (attachments.isEmpty()) return ""

        val imageAttachments = attachments.filter { it.isImage }
        val fileAttachments = attachments.filter { !it.isImage }

        val imagesHtml = if (imageAttachments.isNotEmpty()) {
            val imgs = imageAttachments.mapNotNull { dto ->
                val src = resolveAttachmentSrc(dto) ?: return@mapNotNull null
                val alt = escapeHtml(dto.name)
                """<img src="$src" alt="$alt" class="attachment-img" loading="lazy">"""
            }.joinToString("")
            if (imgs.isNotEmpty()) """<div class="attachments-grid">$imgs</div>""" else ""
        } else ""

        val filesHtml = if (fileAttachments.isNotEmpty()) {
            val chips = fileAttachments.map { dto ->
                val name = escapeHtml(dto.name.ifBlank { "file" })
                val sizeText = formatFileSize(dto.size)
                val type = escapeHtml(dto.mimeType)
                """
                    <div class="file-chip">
                        <span class="file-name">$name</span>
                        <span class="file-meta">$type · $sizeText</span>
                    </div>
                """.trimIndent()
            }.joinToString("")
            """<div class="file-chips">$chips</div>"""
        } else ""

        return imagesHtml + filesHtml
    }

    private fun resolveAttachmentSrc(dto: AttachmentPayloadDto): String? {
        return when {
            !dto.url.isNullOrBlank() -> dto.url
            !dto.localPath.isNullOrBlank() -> {
                val encodedPath = URLEncoder.encode(dto.localPath, "UTF-8")
                "https://appassets.androidplatform.net/local-file/$encodedPath"
            }
            else -> null
        }
    }

    private fun formatFileSize(size: Long): String {
        val kb = size / 1024
        return when {
            kb >= 1024 -> "${"%.1f".format(kb / 1024.0)}MB"
            kb > 0 -> "${kb}KB"
            else -> "${size}B"
        }
    }

    // ===== 推理过程 =====

    private fun createReasoningHtml(message: MessageEntity): String {
        if (message.reasoningContent.isBlank()) return ""
        // 仅在 APP 中展开思考过程时，分享页才显示
        if (!AppSharedStorage.isExpandedReasoningChat.value) return ""
        val formatted = MarkdownStringUtils.convertMarkdownToHtml(message.reasoningContent)
        val timeStr = if (message.reasoningTime > 0) " (${message.reasoningTime / 1000}s)" else ""
        return """
            <div class="reasoning-block">
                <div class="reasoning-label">思考过程$timeStr</div>
                <div class="reasoning-content prose">$formatted</div>
            </div>
        """.trimIndent()
    }

    // ===== AIGC 信息 =====

    /**
     * DEBUG 构建专用：在消息气泡内插入 AIGC 信息表格，便于核对水印字段。
     * Release 构建直接返回空串，不污染最终页面。
     */
    private fun renderAigcDebugTable(aigcInfo: AIGCInfo): String {
        if (!BuildConfig.DEBUG) return ""

        val labelText = when (aigcInfo.label) {
            "1" -> "属于人工智能生成合成内容"
            "2" -> "可能为人工智能生成合成内容"
            "3" -> "疑似为人工智能生成合成内容"
            else -> "未知标签"
        }

        return buildString {
            appendLine("""<div class="aigc-info">""")
            appendLine("""  <h4>🤖 AI生成内容标识</h4>""")
            appendLine("""  <table>""")
            appendLine("""    <tr><td>标签</td><td>$labelText</td></tr>""")
            if (aigcInfo.contentProducer.isNotEmpty()) {
                appendLine("""    <tr><td>服务提供者</td><td>${aigcInfo.contentProducer}</td></tr>""")
            }
            appendLine("""    <tr><td>制作编号</td><td class="font-mono text-xs">${aigcInfo.produceID}</td></tr>""")
            appendLine("""    <tr><td>传播服务提供者</td><td>${aigcInfo.contentPropagator}</td></tr>""")
            appendLine("""    <tr><td>传播编号</td><td class="font-mono text-xs">${aigcInfo.propagateID}</td></tr>""")
            if (aigcInfo.reservedCode1.isNotEmpty()) {
                appendLine("""    <tr><td>预留字段1</td><td>${aigcInfo.reservedCode1}</td></tr>""")
            }
            if (aigcInfo.reservedCode2.isNotEmpty()) {
                appendLine("""    <tr><td>预留字段2</td><td>${aigcInfo.reservedCode2}</td></tr>""")
            }
            appendLine("""  </table>""")
            appendLine("""</div>""")
        }
    }

    // ===== 头部 / Prompt =====

    private fun buildHeaderHtml(conversation: Conversation, colorScheme: ColorScheme): String {
        if (conversation.title.isBlank()) return ""
        val title = escapeHtml(conversation.title)
        val subtitle = when (conversation.entryType) {
            com.shifenmiao.model.ai.AIConversationEntryType.ASSISTANT -> "助手对话"
            com.shifenmiao.model.ai.AIConversationEntryType.AGENT -> "智能体对话"
            com.shifenmiao.model.ai.AIConversationEntryType.PROMPT -> "提示词对话"
            else -> ""
        }
        val subtitleHtml = if (subtitle.isNotBlank()) {
            """<div class="subtitle">${escapeHtml(subtitle)}</div>"""
        } else ""
        return """
            <div class="chat-header">
                <h1>$title</h1>
                $subtitleHtml
            </div>
        """.trimIndent()
    }

    private fun buildPromptHtml(conversation: Conversation, colorScheme: ColorScheme): String {
        if (conversation.prompt.isBlank()) return ""
        val formatted = MarkdownStringUtils.convertMarkdownToHtml(conversation.prompt)
        return """
            <div class="prompt-block glass">
                <div class="prompt-header">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3v18"/><path d="M3 12h18"/></svg>
                    <span>会话提示词</span>
                </div>
                <div class="prose">$formatted</div>
            </div>
        """.trimIndent()
    }

    // ===== CSS / JS =====

    private fun buildCustomCss(colorScheme: ColorScheme, fontSizePx: Int): String {
        fun Int.toHexColor() = String.format(Locale.US, "#%06X", this and 0xFFFFFF)
        val bg = colorScheme.background.toArgb().toHexColor()
        val onSurface = colorScheme.onSurface.toArgb().toHexColor()
        val onSurfaceVariant = colorScheme.onSurfaceVariant.toArgb().toHexColor()
        val primary = colorScheme.primary.toArgb().toHexColor()
        val secondary = colorScheme.secondary.toArgb().toHexColor()
        val primaryContainer = colorScheme.primaryContainer.toArgb().toHexColor()
        val onPrimaryContainer = colorScheme.onPrimaryContainer.toArgb().toHexColor()
        val surfaceContainer = colorScheme.surfaceContainer.toArgb().toHexColor()
        val surfaceContainerHigh = colorScheme.surfaceContainerHigh.toArgb().toHexColor()
        val surfaceContainerHighest = colorScheme.surfaceContainerHighest.toArgb().toHexColor()
        val outline = colorScheme.outline.toArgb().toHexColor()
        val outlineVariant = colorScheme.outlineVariant.toArgb().toHexColor()
        val tertiary = colorScheme.tertiary.toArgb().toHexColor()

        return """
:root {
  --base-font-size: ${fontSizePx}px;
  /* AI notice banner 灰色文字,无背景 */
  --aigc-fg: $outline;
}
html { font-size: var(--base-font-size); }

body {
  font-family: ui-sans-serif, system-ui, -apple-system, "Segoe UI", Roboto, "Noto Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif;
  background-color: $bg;
  color: $onSurface;
  line-height: 1.6;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* Background overlay - solid surface color */
body::before {
  content: '';
  position: fixed; inset: 0;
  background: $bg;
  z-index: -2; pointer-events: none;
}

/* Main container */
.main-container { max-width: 800px; margin: 0 auto; padding: 1.25rem 1rem 2.5rem; }

/* Header */
.chat-header { text-align: center; margin-bottom: 1.25rem; }
.chat-header h1 { font-size: 1.25rem; font-weight: 600; color: $onSurface; margin: 0 0 0.25rem; line-height: 1.3; }
.chat-header .subtitle { font-size: 0.875rem; color: $onSurfaceVariant; }

/* Prompt — glass style */
.prompt-block {
  background: ${surfaceContainer}80;
  backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px);
  border: 1px solid ${outlineVariant}60;
  border-radius: 1rem;
  padding: 1rem;
  margin-bottom: 2rem;
  box-shadow: 0 4px 16px ${onSurface}06;
}
.prompt-block .prompt-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: ${colorScheme.secondary.toArgb().toHexColor()};
  margin-bottom: 0.75rem;
}
.prompt-block .prompt-header svg {
  width: 1rem; height: 1rem;
  color: ${colorScheme.secondary.toArgb().toHexColor()};
}

/* Messages layout */
.messages-wrapper { display: flex; flex-direction: column; gap: 1.25rem; }

/* Date divider */
.date-divider { display: flex; justify-content: center; margin: 0.75rem 0 1rem; min-height: 1.5rem; }
.date-divider span { padding: 0.5rem 1.25rem; border-radius: 9999px; background: $surfaceContainerHighest; color: $onSurfaceVariant; font-size: 0.875rem; font-weight: 500; letter-spacing: 0.02em; line-height: 1.4; }

/* User message */
.msg-user { display: flex; justify-content: flex-end; min-width: 0; }
.msg-user > div { max-width: 85%; min-width: 0; }
.bubble-user {
  background: $primaryContainer;
  backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px);
  border: 1px solid ${primaryContainer}33;
  border-radius: 1rem 1rem 0.25rem 1rem;
  padding: 1rem;
  color: $onPrimaryContainer;
  box-shadow: 0 1px 3px ${onSurface}08;
  word-break: break-word;
  overflow-wrap: break-word;
  min-width: 0;
}
.msg-meta-user { text-align: right; font-size: 0.8125rem; color: $outline; margin-top: 0.375rem; padding-right: 0.5rem; opacity: 0.85; }

/* AI message — vertical layout */
.msg-ai { display: flex; flex-direction: column; gap: 0.5rem; min-width: 0; }
.ai-header { display: flex; align-items: center; gap: 0.625rem; padding-left: 0.25rem; }
.ai-avatar {
  width: 2rem; height: 2rem; border-radius: 50%; background: $primaryContainer;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; border: 1px solid ${primaryContainer}66;
  box-shadow: 0 1px 2px ${onSurface}08;
}
.ai-avatar svg { width: 1.125rem; height: 1.125rem; color: $onPrimaryContainer; }
.model-tag {
  font-size: 0.75rem; font-weight: 600; line-height: 1;
  color: $onPrimaryContainer;
  background: $primaryContainer;
  padding: 0.375rem 0.625rem;
  border-radius: 9999px;
  border: 1px solid ${primaryContainer}66;
}
.msg-meta-ai { font-size: 0.75rem; color: $outline; margin: 0.25rem 0 0; padding-left: 0.5rem; opacity: 0.6; }
.bubble-ai-wrapper { max-width: 100%; min-width: 0; }
.bubble-ai {
  background: $surfaceContainerHigh;
  backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px);
  border: 1px solid ${surfaceContainerHigh}66;
  border-radius: 0 1rem 1rem 1rem;
  padding: 1rem;
  color: $onSurfaceVariant;
  box-shadow: 0 1px 3px ${onSurface}08;
  word-break: break-word;
  overflow-wrap: break-word;
  min-width: 0;
}

/* Reasoning */
.reasoning-block { margin-bottom: 0.75rem; padding-left: 0.75rem; border-left: 2px solid ${primary}4D; }
.reasoning-label { font-size: 0.75rem; font-weight: 500; color: $onSurfaceVariant; margin-bottom: 0.25rem; }
.reasoning-content { color: $onSurfaceVariant; font-size: 0.875rem; line-height: 1.5; }
.reasoning-content .prose { font-size: inherit; }

/* Attachments */
.attachments-grid { display: flex; flex-wrap: wrap; gap: 0.5rem; margin-bottom: 0.75rem; }
.attachment-img { max-width: 140px; max-height: 140px; border-radius: 0.75rem; object-fit: cover; }
.file-chips { display: flex; flex-direction: column; gap: 0.25rem; margin-bottom: 0.75rem; }
.file-chip { display: flex; align-items: center; gap: 0.5rem; padding: 0.375rem 0.75rem; border-radius: 0.5rem; background: ${surfaceContainerHighest}80; font-size: 0.75rem; }
.file-chip .file-name { color: $onSurface; font-weight: 500; }
.file-chip .file-meta { color: $outline; }

/* Markdown prose */
.prose { line-height: 1.6; word-wrap: break-word; overflow-wrap: break-word; }
.prose p { margin-bottom: 0.75rem; }
.prose p:last-child { margin-bottom: 0; }
.prose h1, .prose h2, .prose h3, .prose h4 { color: $onSurface; font-weight: 600; margin: 1rem 0 0.5rem; }
.prose h1 { font-size: 1.25rem; }
.prose h2 { font-size: 1.125rem; }
.prose h3 { font-size: 1rem; }
.prose ul, .prose ol { padding-left: 1.5rem; margin: 0.75rem 0; }
.prose li { margin-bottom: 0.25rem; }
.prose li:last-child { margin-bottom: 0; }
.prose strong { color: $onSurface; font-weight: 600; }
.prose a { color: $primary; text-decoration: none; }
.prose a:hover { text-decoration: underline; }
.prose img { max-width: 100%; height: auto; border-radius: 0.5rem; margin: 0.5rem 0; }
.prose hr { border: none; border-top: 1px solid $outlineVariant; margin: 1rem 0; }

/* Code blocks */
pre {
  position: relative; padding: 1rem; border-radius: 0.5rem; margin: 1rem 0;
  background: $surfaceContainerHighest !important; overflow-x: auto;
}
pre code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 0.875rem; line-height: 1.5; background: transparent !important; padding: 0 !important;
}
p code, li code {
  background: $surfaceContainerHighest; padding: 0.15em 0.4em; border-radius: 0.25rem;
  font-size: 0.875em; font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

/* Copy button */
.code-copy-btn {
  position: absolute; top: 0.5rem; right: 0.5rem; padding: 0.25rem 0.625rem;
  border-radius: 0.375rem; border: none; background: $surfaceContainer;
  color: $onSurfaceVariant; font-size: 0.75rem; cursor: pointer;
  opacity: 0; transition: opacity 0.2s; z-index: 10;
}
pre:hover .code-copy-btn { opacity: 1; }
.code-copy-btn:hover { background: $surfaceContainerHigh; }
.code-copy-btn.copied { color: $primary; }

/* Tables */
table { width: 100%; border-collapse: collapse; margin: 1rem 0; overflow-x: auto; display: block; table-layout: fixed; }
table th, table td { border: 1px solid $outlineVariant; padding: 0.5rem; text-align: left; word-break: break-word; overflow-wrap: break-word; }
table th { background: $surfaceContainerHighest; font-weight: 600; }
table tr:nth-child(even) { background: ${surfaceContainerHighest}30; }

/* Blockquotes */
blockquote { border-left: 4px solid $outlineVariant; padding-left: 1rem; margin: 0.75rem 0; color: $onSurfaceVariant; font-style: italic; }

/* Footer */
footer { margin-top: 1.5rem; padding-bottom: 1rem; }
/* AI 生成内容声明 banner —— 徕卡水印风格:背景条 + 一行字 + 右对齐 + 全宽贴底。
   元素挂在 body 直接子节点(在 .main-container 之外),所以全宽 100%。
   颜色通过 CSS 变量接收主题色,无圆角/无渐变/无阴影。*/
.ai-notice-banner {
  display: flex;
  align-items: center;
  justify-content: flex-end;    /* 居中对齐 */
  margin: 0;
  padding: 48px 16px;
  color: var(--aigc-fg, $outline);  /* 灰色,主题色 outline 自适应明暗 */
  text-align: right;
  /* 字号:实际文字渲染高度 ≈ font-size × 0.85。
     短边 6% 短边的实际渲染 ≈ 6% 需要 font-size ≈ 7vmin;
     给 40px 下限防止小屏太挤。 */
  font-size: max(40px, 7vmin);
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: 0.02em;
  word-break: break-word;
  border-radius: 0;
}

/* Highlight.js colors */
.hljs-keyword, .hljs-selector-tag, .hljs-literal, .hljs-doctag { color: $primary; }
.hljs-string, .hljs-attr, .hljs-symbol, .hljs-meta, .hljs-name, .hljs-type { color: $tertiary; }
.hljs-comment, .hljs-quote { color: $outline; font-style: italic; }
.hljs-number, .hljs-regexp, .hljs-variable { color: $secondary; }
.hljs-built_in, .hljs-builtin-name, .hljs-section, .hljs-title { color: $primary; }

/* AIGC info */
.aigc-info { margin-top: 1rem; padding: 1rem; background: $surfaceContainerHighest; border: 1px solid $outlineVariant; border-radius: 0.5rem; }
.aigc-info h4 { font-size: 0.875rem; font-weight: 600; color: $onSurface; margin-bottom: 0.75rem; display: flex; align-items: center; gap: 0.375rem; }
.aigc-info table { width: 100%; font-size: 0.875rem; border-collapse: collapse; display: table; margin: 0; }
.aigc-info td { padding: 0.5rem 0; border-bottom: 1px solid ${outlineVariant}60; vertical-align: top; }
.aigc-info td:first-child { font-weight: 500; color: $onSurface; width: 30%; padding-right: 1rem; }
.aigc-info td:last-child { color: $onSurfaceVariant; }
.aigc-info tr:last-child td { border-bottom: none; }

/* Mobile responsive */
@media (max-width: 640px) {
  .main-container { padding: 0.875rem 0.75rem 2rem; }
  .chat-header { margin-bottom: 1rem; }
  .msg-user > div { max-width: 92%; }
  .bubble-ai-wrapper { max-width: 100%; }
  .attachment-img { max-width: 100px; max-height: 100px; }
}
        """.trimIndent()
    }

    private fun buildInlineScript(): String {
        return """
document.addEventListener('DOMContentLoaded', () => {
    // Initialize syntax highlighting
    if (window.hljs) { window.hljs.highlightAll(); }

    // Add copy buttons to code blocks
    document.querySelectorAll('pre').forEach(pre => {
        const code = pre.querySelector('code');
        if (!code) return;

        const btn = document.createElement('button');
        btn.className = 'code-copy-btn';
        btn.textContent = 'Copy';
        btn.setAttribute('aria-label', 'Copy code');

        const copyText = async (text) => {
            if (navigator.clipboard) {
                await navigator.clipboard.writeText(text);
            } else {
                const ta = document.createElement('textarea');
                ta.value = text;
                ta.style.cssText = 'position:fixed;opacity:0;';
                document.body.appendChild(ta);
                ta.select();
                document.execCommand('copy');
                document.body.removeChild(ta);
            }
        };

        btn.addEventListener('click', () => {
            const text = code.textContent || '';
            copyText(text).then(() => {
                btn.textContent = 'Copied!';
                btn.classList.add('copied');
                setTimeout(() => { btn.textContent = 'Copy'; btn.classList.remove('copied'); }, 2000);
            }).catch(() => {
                btn.textContent = 'Failed';
                setTimeout(() => { btn.textContent = 'Copy'; }, 2000);
            });
        });

        pre.appendChild(btn);
    });
});
        """.trimIndent()
    }

    // ===== Tailwind Config =====

    private fun buildTailwindConfig(colorScheme: ColorScheme): String {
        if (cachedColorScheme == colorScheme && cachedTailwindConfig != null) {
            return cachedTailwindConfig!!
        }
        fun Int.toHexColor() = String.format(Locale.US, "#%06X", this and 0xFFFFFF)
        val config = """
tailwind.config = {
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        'primary': '${colorScheme.primary.toArgb().toHexColor()}',
        'on-primary': '${colorScheme.onPrimary.toArgb().toHexColor()}',
        'primary-container': '${colorScheme.primaryContainer.toArgb().toHexColor()}',
        'on-primary-container': '${colorScheme.onPrimaryContainer.toArgb().toHexColor()}',
        'secondary': '${colorScheme.secondary.toArgb().toHexColor()}',
        'on-secondary': '${colorScheme.onSecondary.toArgb().toHexColor()}',
        'tertiary': '${colorScheme.tertiary.toArgb().toHexColor()}',
        'surface': '${colorScheme.surface.toArgb().toHexColor()}',
        'on-surface': '${colorScheme.onSurface.toArgb().toHexColor()}',
        'on-surface-variant': '${colorScheme.onSurfaceVariant.toArgb().toHexColor()}',
        'surface-variant': '${colorScheme.surfaceVariant.toArgb().toHexColor()}',
        'surface-container': '${colorScheme.surfaceContainer.toArgb().toHexColor()}',
        'surface-container-high': '${colorScheme.surfaceContainerHigh.toArgb().toHexColor()}',
        'surface-container-highest': '${colorScheme.surfaceContainerHighest.toArgb().toHexColor()}',
        'outline': '${colorScheme.outline.toArgb().toHexColor()}',
        'outline-variant': '${colorScheme.outlineVariant.toArgb().toHexColor()}',
        'background': '${colorScheme.background.toArgb().toHexColor()}',
      }
    }
  }
}
        """.trimIndent()
        cachedColorScheme = colorScheme
        cachedTailwindConfig = config
        return config
    }

    // ===== 工具函数 =====

    private fun Int.toHex() = String.format(Locale.US, "#%06X", this and 0xFFFFFF)

    private fun escapeHtml(text: String): String {
        return buildString(text.length) {
            text.forEach { c ->
                when (c) {
                    '&' -> append("&amp;")
                    '<' -> append("&lt;")
                    '>' -> append("&gt;")
                    '"' -> append("&quot;")
                    '\'' -> append("&#39;")
                    else -> append(c)
                }
            }
        }
    }

    companion object {
        private var cachedTemplate: String? = null
    }
}
