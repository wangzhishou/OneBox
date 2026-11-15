/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.presentation.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboardArrowDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText
import com.t8rin.imagetoolbox.core.settings.domain.model.DomainFontFamily
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.t8rin.imagetoolbox.core.settings.presentation.model.toUiFont
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsManager
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.controls.selection.PickFontFamilySheet
import com.wanbaohe.core.ui.widget.icon_shape.IconShapeContainer
import com.wanbaohe.core.ui.widget.preferences.PreferenceRow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 字体行：与「图片创作 / 文字卡片」同一套公共组件 —— 当前字体名直接用该字体渲染，
 * 点击打开全局 [PickFontFamilySheet]（可下载字体 + 已导入字体 + 导入/移除/导出）。
 *
 * 之前属性面板用的是 FontSelector 的水墨横向 chip 墙，和图片创作的字体入口不一致，
 * 也放不下用户自己导入的字体。
 */
@Composable
fun FontStyleRow(
    font: FontType?,
    onFontSelected: (FontType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showFontSheet by rememberSaveable { mutableStateOf(false) }
    val selectedFont = font.toUiFont()
    val settingsManager = LocalSettingsManager.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val exportFontsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
        onResult = { uri ->
            uri ?: return@rememberLauncherForActivityResult
            scope.launch {
                runCatching {
                    val cache = settingsManager.createCustomFontsExport() ?: return@runCatching
                    context.contentResolver.openInputStream(cache.toUri())?.use { input ->
                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            input.copyTo(output)
                        }
                    }
                }.onFailure {
                    AppToastHost.showActivateFilesToast()
                }
            }
        }
    )

    PreferenceRow(
        title = stringResource(R.string.font),
        onClick = { showFontSheet = true },
        modifier = modifier.fillMaxWidth(),
        applyHorizontalPadding = false,
        resultModifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        startContent = {
            IconShapeContainer(enabled = true) {
                Icon(
                    imageVector = Icons.Outlined.LineText,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(Modifier.width(12.dp))
        },
        endContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = selectedFont.name ?: stringResource(R.string.system),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = selectedFont.fontFamily,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 140.dp),
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Outlined.LineKeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        },
    )

    PickFontFamilySheet(
        visible = showFontSheet,
        onDismiss = { showFontSheet = false },
        onFontSelected = { selected ->
            onFontSelected(selected.type)
            showFontSheet = false
        },
        onAddFont = { uri ->
            scope.launch {
                val imported = settingsManager.importCustomFont(uri.toString())
                if (imported != null) {
                    onFontSelected(FontType.File(imported.filePath))
                    AppToastHost.showConfetti()
                } else {
                    AppToastHost.showToast(
                        message = context.getString(R.string.wrong_font),
                        icon = Icons.Outlined.LineText,
                    )
                }
            }
        },
        onRemoveFont = { custom ->
            scope.launch {
                settingsManager.removeCustomFont(custom.asDomain() as DomainFontFamily.Custom)
            }
        },
        onExportFonts = {
            runCatching {
                val timeStamp = SimpleDateFormat(
                    "yyyy-MM-dd_HH-mm-ss",
                    Locale.getDefault(),
                ).format(Date())
                exportFontsLauncher.launch("FONTS_EXPORT_$timeStamp.zip")
            }.onFailure {
                AppToastHost.showActivateFilesToast()
            }
        },
    )
}
