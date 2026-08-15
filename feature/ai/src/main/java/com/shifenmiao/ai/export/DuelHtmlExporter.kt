package com.shifenmiao.ai.export

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import com.shifenmiao.ai.model.AIDuelConfig
import com.shifenmiao.ai.model.AIDuelConfigCodec
import com.shifenmiao.ai.model.DuelSpeaker
import com.shifenmiao.common.ai.aigc.AigcInfoGenerator
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.core.R
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.RoleType
import com.shifenmiao.model.channel.FlavorType
import com.shifenmiao.theme.AppTheme
import io.noties.markwon.utils.MarkdownStringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

/**
 * AI 互动（Duel）专用 HTML 导出器。
 *
 * 与通用 [HtmlExporter] 不同，本导出器：
 * - 不依赖 assets 模板，全部内联；
 * - 每条消息都显示角色名 + 模型名；
 * - 顶部提示词区按角色拆分，支持展开/收起；
 * - AIGC 元数据写入 HTML 注释，不在页面中展示。
 */
class DuelHtmlExporter {

    /**
     * 最后一条 assistant 消息对应的 AIGCInfo JSON，供调用方注入到分享文件的 metadata。
     */
    var aIgcInfoString: String = ""
        private set

    /**
     * 导出 AI 互动记录为 HTML 字符串。
     */
    suspend fun exportToHtml(
        conversation: Conversation,
        messages: List<MessageEntity>,
        aiEngineCatalogManager: AIEngineCatalogManager,
        colorScheme: ColorScheme = AppTheme.colorScheme,
        isDark: Boolean = AppTheme.isDarkTheme,
    ): String = withContext(Dispatchers.IO) {
        val baseBodyMediumPx = 14f
        val fontSizePx = (baseBodyMediumPx * AppTheme.fontScale).roundToInt()
        val config = AIDuelConfigCodec.decodeOrNull(conversation.prompt) ?: AIDuelConfig()

        aIgcInfoString = messages
            .filter { it.role == RoleType.ASSISTANT.value }
            .maxByOrNull { it.createdAt }
            ?.let { message ->
                val engineName = message.engine.takeIf { it.isNotBlank() }
                    ?: conversation.engine.name
                val modelTitle = aiEngineCatalogManager.getAiModelTitleByModel(message.model)
                    .takeIf { it.isNotBlank() } ?: message.model
                val model = AiModel(name = message.model, title = modelTitle)
                val engine = conversation.engine.copy(name = engineName, model = model)
                AigcInfoGenerator.generateJson(
                    engine = engine,
                    model = model,
                    completionId = message.completionId,
                    conversationId = conversation.id,
                    contentId = message.id.toString(),
                    entryTypeName = conversation.entryType.name,
                    entryRefId = conversation.entryRefId,
                )
            }
            .orEmpty()

        val sortedMessages = messages
            .filter { it.entryType == conversation.entryType }
            .sortedBy { it.createdAt }

        val css = buildCss(colorScheme, fontSizePx)
        val bodyClass = if (isDark) "dark" else ""
        val headerHtml = buildHeaderHtml(conversation)
        val promptsHtml = buildPromptsHtml(config)
        val messagesHtml = buildMessagesHtml(sortedMessages, config, aiEngineCatalogManager)
        // "AI 生成内容"显式标识是国内合规要求,海外(google)渠道不展示
        val noticeBannerHtml = if (FlavorType.fromName() == FlavorType.GOOGLE) {
            ""
        } else {
            val notice = escapeHtml(AppContext.getString(R.string.ai_content_notice_short))
            """<div class="ai-notice-banner" role="note" aria-label="AI 生成内容声明">$notice</div>"""
        }
        // 按 GB 45438-2025,AIGC 隐式标识写入 <head> 中的 <meta name="AIGC" content="...">
        val aigcMetaTag = if (aIgcInfoString.isNotBlank()) {
            val escaped = aIgcInfoString
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
            "<meta name=\"AIGC\" content=\"$escaped\">"
        } else ""

        """
        <!DOCTYPE html>
        <html lang="zh-CN" class="$bodyClass">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>${escapeHtml(conversation.appTitle)}</title>
            $aigcMetaTag
            <style>
            $css
            </style>
        </head>
        <body>
            <div class="container">
                $headerHtml
                $promptsHtml
                <div class="messages">
                    $messagesHtml
                </div>
            </div>
            $noticeBannerHtml
            <script>
            ${buildInlineScript()}
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    // ===== 头部 =====

    private fun buildHeaderHtml(conversation: Conversation): String {
        val title = escapeHtml(conversation.appTitle.ifBlank { conversation.title })
        return """
            <header class="page-header">
                <h1>$title</h1>
            </header>
        """.trimIndent()
    }

    // ===== 提示词区 =====

    private fun buildPromptsHtml(config: AIDuelConfig): String {
        val cardA = buildPromptCard(
            speaker = DuelSpeaker.A,
            roleName = config.roleNameA,
            promptName = config.promptNameA,
            modelTitle = config.engineA?.model?.title.orEmpty(),
            persona = config.personaA
        )
        val cardB = buildPromptCard(
            speaker = DuelSpeaker.B,
            roleName = config.roleNameB,
            promptName = config.promptNameB,
            modelTitle = config.engineB?.model?.title.orEmpty(),
            persona = config.personaB
        )
        return """
            <section class="prompts-section">
                <div class="prompts-grid">
                    $cardA
                    $cardB
                </div>
            </section>
        """.trimIndent()
    }

    private fun buildPromptCard(
        speaker: DuelSpeaker,
        roleName: String,
        promptName: String,
        modelTitle: String,
        persona: String,
    ): String {
        val displayName = roleName.ifBlank { promptName }.ifBlank {
            AppContext.getString(
                if (speaker == DuelSpeaker.A) R.string.ai_duel_speaker_a else R.string.ai_duel_speaker_b
            )
        }
        val model = escapeHtml(modelTitle.ifBlank { "AI" })
        val content = MarkdownStringUtils.convertMarkdownToHtml(persona.trim())
        val isLong = persona.trim().length > 120
        val expandableClass = if (isLong) "prompt-text expandable" else "prompt-text"
        val expandBtn = if (isLong) {
            """<button class="expand-btn" data-target="prompt-${speaker.name}">展开更多</button>"""
        } else ""
        return """
            <div class="prompt-card speaker-${speaker.name.lowercase()}">
                <div class="prompt-card-header">
                    <div class="role-badge">${escapeHtml(displayName)}</div>
                    <div class="model-badge">$model</div>
                </div>
                <div class="$expandableClass" id="prompt-${speaker.name}" data-expanded="false">
                    $content
                </div>
                $expandBtn
            </div>
        """.trimIndent()
    }

    // ===== 消息区 =====

    private fun buildMessagesHtml(
        messages: List<MessageEntity>,
        config: AIDuelConfig,
        aiEngineCatalogManager: AIEngineCatalogManager,
    ): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("yyyy年M月d日", Locale.getDefault())
        var lastDate: String? = null

        return buildString {
            messages.forEach { message ->
                val dateStr = dateFormat.format(message.createdAt)
                if (dateStr != lastDate) {
                    append("""<div class="date-divider"><span>$dateStr</span></div>""")
                    lastDate = dateStr
                }
                append(createMessageHtml(message, config, aiEngineCatalogManager, timeFormat))
            }
        }
    }

    private fun createMessageHtml(
        message: MessageEntity,
        config: AIDuelConfig,
        aiEngineCatalogManager: AIEngineCatalogManager,
        timeFormat: SimpleDateFormat,
    ): String {
        val isSpeakerA = message.role == RoleType.USER.value
        val speaker = if (isSpeakerA) DuelSpeaker.A else DuelSpeaker.B
        val displayName = resolveSpeakerDisplayName(config, speaker)
        val modelTitle = aiEngineCatalogManager.getAiModelTitleByModel(message.model)
            .takeIf { it.isNotBlank() }
            ?: (if (speaker == DuelSpeaker.A) config.engineA?.model?.title else config.engineB?.model?.title)
                ?.takeIf { it.isNotBlank() }
            ?: "AI"
        val content = if (isSpeakerA) message.question else message.answer
        val contentHtml = MarkdownStringUtils.convertMarkdownToHtml(content)
        val timeStr = escapeHtml(timeFormat.format(message.createdAt))
        val speakerClass = if (isSpeakerA) "speaker-a" else "speaker-b"
        val avatarText = escapeHtml(
            displayName.trim().firstOrNull()?.toString()
                ?: if (speaker == DuelSpeaker.A) "A" else "B"
        )
        val metaTextHtml = """
            <div class="message-meta-text">
                <div class="message-role">
                    <span class="role-name">${escapeHtml(displayName)}</span>
                </div>
                <div class="message-model">${escapeHtml(modelTitle)}</div>
            </div>
        """.trimIndent()
        val avatarHtml = """<div class="avatar">$avatarText</div>"""

        return """
            <div class="message $speakerClass" data-completion-id="${escapeHtml(message.completionId)}">
                <div class="message-meta">
                    ${if (isSpeakerA) "$metaTextHtml\n                    $avatarHtml" else "$avatarHtml\n                    $metaTextHtml"}
                </div>
                <div class="message-bubble">
                    $contentHtml
                </div>
                <div class="message-time">$timeStr</div>
            </div>
        """.trimIndent()
    }

    private fun resolveSpeakerDisplayName(config: AIDuelConfig, speaker: DuelSpeaker): String {
        return when (speaker) {
            DuelSpeaker.A -> config.roleNameA.ifBlank {
                config.promptNameA.ifBlank { AppContext.getString(R.string.ai_duel_speaker_a) }
            }
            DuelSpeaker.B -> config.roleNameB.ifBlank {
                config.promptNameB.ifBlank { AppContext.getString(R.string.ai_duel_speaker_b) }
            }
        }
    }

    // ===== AIGC =====

    // ===== CSS / JS =====

    private fun buildCss(colorScheme: ColorScheme, fontSizePx: Int): String {
        fun Int.toHexColor() = String.format(Locale.US, "#%06X", this and 0xFFFFFF)

        val bg = colorScheme.background.toArgb().toHexColor()
        val onSurface = colorScheme.onSurface.toArgb().toHexColor()
        val onSurfaceVariant = colorScheme.onSurfaceVariant.toArgb().toHexColor()
        val primary = colorScheme.primary.toArgb().toHexColor()
        val onPrimary = colorScheme.onPrimary.toArgb().toHexColor()
        val primaryContainer = colorScheme.primaryContainer.toArgb().toHexColor()
        val onPrimaryContainer = colorScheme.onPrimaryContainer.toArgb().toHexColor()
        val tertiary = colorScheme.tertiary.toArgb().toHexColor()
        val onTertiary = colorScheme.onTertiary.toArgb().toHexColor()
        val surfaceContainer = colorScheme.surfaceContainer.toArgb().toHexColor()
        val surfaceContainerHigh = colorScheme.surfaceContainerHigh.toArgb().toHexColor()
        val outline = colorScheme.outline.toArgb().toHexColor()
        val outlineVariant = colorScheme.outlineVariant.toArgb().toHexColor()

        return """
:root {
  --base-font-size: ${fontSizePx}px;
  /* AI notice banner 灰色文字,无背景 */
  --aigc-fg: $outline;
}
html { font-size: var(--base-font-size); }
* { box-sizing: border-box; }

body {
  font-family: ui-sans-serif, system-ui, -apple-system, "Segoe UI", Roboto, "Noto Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif;
  background-color: $bg;
  color: $onSurface;
  line-height: 1.6;
  margin: 0;
  padding: 0;
  -webkit-font-smoothing: antialiased;
}

.container {
  max-width: 720px;
  margin: 0 auto;
  padding: 1.25rem 1rem 2.5rem;
}

/* Header */
.page-header {
  text-align: center;
  margin-bottom: 1.25rem;
}
.page-header h1 {
  font-size: 1.375rem;
  font-weight: 600;
  margin: 0 0 0.5rem;
  color: $onSurface;
}

/* Prompts */
.prompts-section {
  margin-bottom: 1.75rem;
}
.prompts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.875rem;
}
.prompt-card {
  background: ${surfaceContainer}CC;
  backdrop-filter: blur(14px); -webkit-backdrop-filter: blur(14px);
  border: 1px solid ${outlineVariant}66;
  border-radius: 1rem;
  padding: 1rem;
  box-shadow: 0 4px 16px ${onSurface}08;
}
.prompt-card.speaker-a {
  border-top: 3px solid $primary;
}
.prompt-card.speaker-b {
  border-top: 3px solid ${colorScheme.tertiary.toArgb().toHexColor()};
}
.prompt-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}
.role-badge {
  font-size: 0.875rem;
  font-weight: 600;
  color: $onSurface;
}
.model-badge {
  font-size: 0.75rem;
  color: $onSurfaceVariant;
  background: ${surfaceContainerHigh}80;
  padding: 0.2rem 0.5rem;
  border-radius: 0.375rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 50%;
}
.prompt-text {
  font-size: 1rem;
  color: $onSurfaceVariant;
  word-break: break-word;
}
.prompt-text p { margin: 0 0 0.5rem; }
.prompt-text p:last-child { margin-bottom: 0; }
.prompt-text.expandable {
  max-height: 4.5em;
  overflow: hidden;
  position: relative;
}
.prompt-text.expandable::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2em;
  background: linear-gradient(transparent, ${surfaceContainer}CC);
  pointer-events: none;
}
.prompt-text.expanded {
  max-height: none;
}
.prompt-text.expanded::after {
  display: none;
}
.expand-btn {
  margin-top: 0.5rem;
  padding: 0;
  border: none;
  background: transparent;
  color: $primary;
  font-size: 0.8125rem;
  font-weight: 500;
  cursor: pointer;
}
.expand-btn:hover {
  text-decoration: underline;
}

/* Date divider */
.date-divider {
  display: flex;
  justify-content: center;
  margin: 1rem 0;
}
.date-divider span {
  padding: 0.25rem 0.875rem;
  border-radius: 9999px;
  background: $surfaceContainerHigh;
  color: $onSurfaceVariant;
  font-size: 0.75rem;
  font-weight: 500;
}

/* Messages */
.messages {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}
.message {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
  min-width: 0;
}
.message-meta {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  min-width: 0;
}
.speaker-a .message-meta {
  justify-content: flex-end;
}
.speaker-b .message-meta {
  justify-content: flex-start;
}
.message-meta-text {
  display: flex;
  flex-direction: column;
  gap: 0;
  min-width: 0;
  flex: 0 1 auto;
}
.speaker-a .message-meta-text {
  align-items: flex-end;
}
.speaker-b .message-meta-text {
  align-items: flex-start;
}
.avatar {
  width: 2.25rem;
  height: 2.25rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.9375rem;
  flex: 0 0 auto;
  min-width: 2.25rem;
  min-height: 2.25rem;
}
.speaker-a .avatar {
  background: $primary;
  color: $onPrimary;
}
.speaker-b .avatar {
  background: ${colorScheme.tertiary.toArgb().toHexColor()};
  color: ${colorScheme.onTertiary.toArgb().toHexColor()};
}
.role-name {
  font-size: 1rem;
  font-weight: 600;
  color: $onSurface;
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}
.message-model {
  font-size: 0.75rem;
  color: $outline;
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}
.message-bubble {
  padding: 0.875rem 1rem;
  border-radius: 0.875rem;
  font-size: 1.0625rem;
  word-break: break-word;
  overflow-wrap: break-word;
  min-width: 0;
  box-shadow: 0 4px 16px ${onSurface}08;
  backdrop-filter: blur(14px); -webkit-backdrop-filter: blur(14px);
}
/* 不圆角的一边朝向角色名/头像所在侧 */
.speaker-a .message-bubble {
  background: ${primaryContainer}CC;
  color: $onPrimaryContainer;
  border-top-right-radius: 0.25rem;
}
.speaker-b .message-bubble {
  background: ${surfaceContainerHigh}CC;
  color: $onSurface;
  border-top-left-radius: 0.25rem;
}
.message-time {
  font-size: 0.8125rem;
  color: $outline;
  flex-shrink: 0;
  opacity: 0.85;
}
.speaker-a .message-time {
  text-align: right;
}

/* Markdown */
.message-bubble .prose { line-height: 1.6; }
.message-bubble .prose p { margin: 0 0 0.625rem; }
.message-bubble .prose p:last-child { margin-bottom: 0; }
.message-bubble .prose h1, .message-bubble .prose h2, .message-bubble .prose h3, .message-bubble .prose h4 {
  margin: 0.75rem 0 0.375rem;
  font-weight: 600;
}
.message-bubble .prose h1 { font-size: 1.125rem; }
.message-bubble .prose h2 { font-size: 1.0625rem; }
.message-bubble .prose h3 { font-size: 1rem; }
.message-bubble .prose ul, .message-bubble .prose ol { padding-left: 1.25rem; margin: 0.5rem 0; }
.message-bubble .prose li { margin-bottom: 0.25rem; }
.message-bubble .prose strong { font-weight: 600; }
.message-bubble .prose a { color: $primary; text-decoration: none; }
.message-bubble .prose a:hover { text-decoration: underline; }
.message-bubble .prose img { max-width: 100%; height: auto; border-radius: 0.5rem; }
.message-bubble .prose blockquote {
  border-left: 3px solid $outlineVariant;
  padding-left: 0.75rem;
  margin: 0.5rem 0;
  color: $onSurfaceVariant;
  font-style: italic;
}
.message-bubble .prose pre {
  background: ${colorScheme.surfaceContainerHighest.toArgb().toHexColor()};
  padding: 0.75rem;
  border-radius: 0.5rem;
  overflow-x: auto;
  font-size: 0.8125rem;
}
.message-bubble .prose code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 0.85em;
}
.message-bubble .prose p code {
  background: ${colorScheme.surfaceContainerHighest.toArgb().toHexColor()};
  padding: 0.15em 0.35em;
  border-radius: 0.25rem;
}
.message-bubble .prose table {
  width: 100%;
  border-collapse: collapse;
  margin: 0.75rem 0;
  font-size: 0.875rem;
  table-layout: fixed;
}
.message-bubble .prose th, .message-bubble .prose td {
  border: 1px solid $outlineVariant;
  padding: 0.375rem 0.5rem;
  text-align: left;
  word-break: break-word;
  overflow-wrap: break-word;
}
.message-bubble .prose th { background: ${colorScheme.surfaceContainerHighest.toArgb().toHexColor()}; }

/* Footer */
/* AI 生成内容声明 banner —— 徕卡水印风格:背景条 + 一行字 + 右对齐 + 全宽贴底。
   元素挂在 body 直接子节点(在 .container 之外),所以全宽 100%。
   颜色通过 CSS 变量接收主题色,无圆角/无渐变/无阴影。*/
.ai-notice-banner {
  display: flex;
  align-items: flex-end;
  justify-content: center;    /* 居中对齐 */
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
footer { margin-top: 1.5rem; text-align: center; }
footer .notice { font-size: 0.8125rem; color: $onSurfaceVariant; opacity: 0.7; }

/* Mobile */
@media (max-width: 640px) {
  .container { padding: 0.875rem 0.75rem 2rem; }
  .page-header { margin-bottom: 1rem; }
  .prompts-grid { grid-template-columns: 1fr; }
  .page-header h1 { font-size: 1.25rem; }
}

/* Dark mode */
.dark body { background-color: $bg; color: $onSurface; }
        """.trimIndent()
    }

    private fun buildInlineScript(): String {
        return """
document.querySelectorAll('.expand-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        const target = document.getElementById(btn.dataset.target);
        if (!target) return;
        const expanded = target.dataset.expanded === 'true';
        target.dataset.expanded = String(!expanded);
        target.classList.toggle('expanded', !expanded);
        btn.textContent = expanded ? '展开更多' : '收起';
    });
});
        """.trimIndent()
    }

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
}
