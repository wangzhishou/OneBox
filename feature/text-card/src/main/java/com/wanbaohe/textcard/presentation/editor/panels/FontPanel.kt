package com.wanbaohe.textcard.presentation.editor.panels

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.t8rin.imagetoolbox.core.settings.presentation.model.toUiFont
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.DownloadableFont
import com.wanbaohe.textcard.presentation.screenLogic.FontDownloadState
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.RadioButtonUnchecked

/**
 * 字体面板(设计稿 03):默认字体 + 用户已导入字体(SettingsState.customFonts)
 * + 内置可下载清单(未下载/下载中/已下载三态)。
 * 字体只作用于画布当前选中的文字块;未选中文字块时显示提示并禁用选择。
 */
@Composable
fun FontPanel(component: TextCardComponent) {
    val settingsState = LocalSettingsState.current
    val target = component.selectedFontTarget()
    val currentFont = target?.font
    val enabled = target != null

    PanelTitle(R.string.textcard_font_panel_title)

    if (!enabled) {
        // 兜底:未在画布上选中文字块时提示先选中
        Text(
            text = stringResource(R.string.textcard_font_select_target_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }

    // 默认字体
    FontRow(
        name = stringResource(R.string.textcard_font_default),
        fontType = null,
        selected = currentFont == null && enabled,
        enabled = enabled,
        onClick = { component.applyFont(null) }
    )

    // 用户已导入字体
    settingsState.customFonts.forEach { custom ->
        val fontType = FontType.File(custom.filePath)
        FontRow(
            name = custom.name ?: custom.filePath.substringAfterLast('/'),
            fontType = fontType,
            selected = currentFont == fontType,
            enabled = enabled,
            onClick = { component.applyFont(fontType) }
        )
    }

    // 内置可下载字体
    component.downloadableFonts.forEach { font ->
        val state = component.fontState(font)
        val fontType = component.downloadedFontType(font)
        DownloadableFontRow(
            font = font,
            state = state,
            // 已下载的字体直接用文件字体做预览,与选中后的卡片渲染一致
            fontType = fontType,
            selected = fontType != null && fontType == currentFont,
            enabled = enabled,
            onClick = {
                when (state) {
                    FontDownloadState.Downloaded -> fontType?.let(component::applyFont)
                    FontDownloadState.NotDownloaded -> component.downloadFont(font)
                    is FontDownloadState.Downloading -> Unit
                }
            }
        )
    }
}

@Composable
private fun FontRow(
    name: String,
    fontType: FontType?,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    val contentAlpha = if (enabled) 1f else 0.4f
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.textcard_font_preview),
            fontFamily = fontType.toUiFont().fontFamily,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
            modifier = Modifier
                .weight(1.4f)
                .padding(horizontal = 8.dp)
        )
        if (trailing != null) {
            trailing()
        } else {
            SelectedRadio(selected)
        }
    }
}

@Composable
private fun DownloadableFontRow(
    font: DownloadableFont,
    state: FontDownloadState,
    fontType: FontType?,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    FontRow(
        name = stringResource(font.nameRes) + " · ${font.approxSizeMb}MB",
        fontType = fontType,
        selected = selected,
        enabled = enabled,
        onClick = onClick
    ) {
        when (state) {
            is FontDownloadState.Downloading -> CircularProgressIndicator(
                progress = { state.progress },
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )

            FontDownloadState.Downloaded -> SelectedRadio(selected)
            FontDownloadState.NotDownloaded -> Icon(
                imageVector = MaterialIcons.Outlined.CloudDownload,
                contentDescription = stringResource(R.string.textcard_font_download),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SelectedRadio(selected: Boolean) {
    Icon(
        imageVector = if (selected) {
            MaterialIcons.Outlined.RadioButtonChecked
        } else MaterialIcons.Outlined.RadioButtonUnchecked,
        contentDescription = null,
        tint = if (selected) {
            MaterialTheme.colorScheme.primary
        } else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp)
    )
}
