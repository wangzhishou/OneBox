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
        return AppTheme.colorScheme.surfaceContainer.toArgb()
    }

    override fun textColor(): Int {
        return AppTheme.colorScheme.onSurface.toArgb()
    }

    override fun initColorHashMap(): ColorHashMap {
        // 从 Material3 颜色系统获取颜色来设置语法高亮
        return ColorHashMap()
            .add(AppTheme.colorScheme.outline.toArgb(), "comment", "prolog", "cdata")
            .add(AppTheme.colorScheme.primary.toArgb(), "delimiter", "boolean", "keyword", "selector", "important", "atrule")
            .add(AppTheme.colorScheme.onSurface.toArgb(), "operator", "punctuation", "attr-name")
            .add(AppTheme.colorScheme.tertiary.toArgb(), "tag", "doctype", "builtin")
            .add(AppTheme.colorScheme.primary.copy(0.8f).toArgb(), "entity", "number", "symbol")
            .add(AppTheme.colorScheme.secondary.toArgb(), "property", "constant", "variable")
            .add(AppTheme.colorScheme.tertiary.copy(0.8f).toArgb(), "string", "char")
            .add(AppTheme.colorScheme.error.toArgb(), "annotation")
            .add(AppTheme.colorScheme.secondary.copy(0.8f).toArgb(), "attr-value")
            .add(AppTheme.colorScheme.primary.toArgb(), "url")
            .add(AppTheme.colorScheme.inversePrimary.toArgb(), "function")
            .add(AppTheme.colorScheme.surfaceVariant.toArgb(), "regex")
            .add(AppTheme.colorScheme.inverseSurface.toArgb(), "inserted")
            .add(AppTheme.colorScheme.outlineVariant.toArgb(), "deleted")
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