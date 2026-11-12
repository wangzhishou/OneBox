package io.noties.markwon.prism4j

import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.toArgb
import com.shifenmiao.theme.AppTheme
import io.noties.markwon.core.spans.EmphasisSpan
import io.noties.markwon.core.spans.StrongEmphasisSpan
import io.noties.markwon.prism4j.syntax.Prism4jThemeBase

class Prism4jThemeMaterial3 : Prism4jThemeBase() {

    companion object {
        @JvmStatic
        fun create(): Prism4jThemeMaterial3 {
            // 默认使用 surface 作为背景色
            return Prism4jThemeMaterial3()
        }
    }

    // 构造函数不需要参数，颜色从 AppTheme 获取
    override fun background(): Int {
        return AppTheme.colorScheme.surfaceContainerLow.toArgb()
    }

    override fun textColor(): Int {
        return AppTheme.colorScheme.onSurface.toArgb()
    }

    override fun initColorHashMap(): ColorHashMap {
        // 从 Material3 颜色系统获取颜色来设置语法高亮。
        // 只取前景/容器语义色（onSurface*、on*Container、primary/tertiary/error），
        // 不要用 inversePrimary、surfaceVariant、outlineVariant 这类浅色角色：
        // 代码块是浅色玻璃背景，它们会让函数名、正则、删除行几乎看不见。
        return ColorHashMap()
            .add(AppTheme.colorScheme.onSurfaceVariant.toArgb(), "comment", "prolog", "cdata")
            .add(AppTheme.colorScheme.onPrimaryContainer.toArgb(), "delimiter", "boolean", "keyword", "selector", "important", "atrule")
            .add(AppTheme.colorScheme.onSurface.toArgb(), "operator", "punctuation", "attr-name")
            .add(AppTheme.colorScheme.tertiary.toArgb(), "tag", "doctype", "builtin")
            .add(AppTheme.colorScheme.onPrimaryContainer.toArgb(), "entity", "number", "symbol")
            .add(AppTheme.colorScheme.secondary.toArgb(), "property", "constant", "variable")
            .add(AppTheme.colorScheme.tertiary.toArgb(), "string", "char")
            .add(AppTheme.colorScheme.error.toArgb(), "annotation")
            .add(AppTheme.colorScheme.secondary.toArgb(), "attr-value")
            .add(AppTheme.colorScheme.onPrimaryContainer.toArgb(), "url")
            .add(AppTheme.colorScheme.primary.toArgb(), "function", "class-name")
            .add(AppTheme.colorScheme.onErrorContainer.toArgb(), "regex")
            .add(AppTheme.colorScheme.primary.toArgb(), "inserted")
            .add(AppTheme.colorScheme.error.toArgb(), "deleted")
    }

    override fun applyColor(
        language: String,
        type: String,
        alias: String?,
        @ColorInt color: Int,
        builder: SpannableStringBuilder,
        start: Int,
        end: Int
    ) {
        super.applyColor(language, type, alias, color, builder, start, end)

        // 保持特殊格式的应用方式
        if (isOfType("important", type, alias) || isOfType("bold", type, alias)) {
            builder.setSpan(StrongEmphasisSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        if (isOfType("italic", type, alias)) {
            builder.setSpan(EmphasisSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

}