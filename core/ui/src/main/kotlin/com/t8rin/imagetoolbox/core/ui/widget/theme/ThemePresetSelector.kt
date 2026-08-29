package com.t8rin.imagetoolbox.core.ui.widget.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.resources.icons.Theme
import com.t8rin.imagetoolbox.core.settings.domain.model.AppThemePreset
import com.t8rin.imagetoolbox.core.settings.domain.model.NightMode
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsManager
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalThemeRepository
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTheme
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDarkMode
import com.t8rin.imagetoolbox.core.resources.icons.Transparency
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBlurOn

/** 主题卡片固定高度，避免选中/删除图标出现时跳动 */
private val CARD_FIXED_HEIGHT = 120.dp
private val CARD_WIDTH = 64.dp
private val CARD_SPACING = 8.dp

/**
 * 可复用的主题预设选择器 —— 横向滚动卡片行 + 新建按钮 + 可选的"更多设置"入口。
 *
 * 在 TopAppBarEmoji 弹窗、DrawerMenu、DisplaySettingItem 中共用。
 *
 * @param onNavigateToThemeSettings 点击"更多设置"或"+新建"时导航到全屏主题设置页。
 *                                  传 null 时不显示"更多设置"链接。
 */
@Composable
fun ThemePresetSelector(
    modifier: Modifier = Modifier,
    onNavigateToThemeSettings: (() -> Unit)? = null,
) {
    val settingsManager = LocalSettingsManager.current
    val themeRepository = LocalThemeRepository.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    val allThemes by themeRepository.observeAllThemes()
        .collectAsState(initial = themeRepository.themesSnapshot)
    val activeThemeId = LocalSettingsState.current.activeThemeId

    // 删除确认状态
    var pendingDeletePreset by remember { mutableStateOf<AppThemePreset?>(null) }
    var rowWidthPx by remember { mutableIntStateOf(0) }
    val cardWidthPx = remember(density) { with(density) { CARD_WIDTH.roundToPx() } }
    val cardSpacingPx = remember(density) { with(density) { CARD_SPACING.roundToPx() } }

    // 行内第 0 项是“新建主题”卡片，因此主题索引需要 +1。
    val selectedIndexInRow = remember(activeThemeId, allThemes) {
        val themeIndex = allThemes.indexOfFirst { it.id == activeThemeId }
        if (themeIndex >= 0) themeIndex + 1 else -1
    }

    LaunchedEffect(
        selectedIndexInRow,
        rowWidthPx,
    ) {
        if (selectedIndexInRow < 0 || rowWidthPx <= 0) return@LaunchedEffect

        // 不把 scrollState.maxValue 作为 key: 卡片增删导致 maxValue 变化时,
        // 不应把正在手动浏览的用户拉回选中项
        val selectedStartPx = selectedIndexInRow * (cardWidthPx + cardSpacingPx)
        val selectedCenterPx = selectedStartPx + cardWidthPx / 2f
        val targetScroll = (selectedCenterPx - rowWidthPx / 2f)
            .roundToInt()
            .coerceIn(0, scrollState.maxValue)

        if (abs(scrollState.value - targetScroll) > 1) {
            scrollState.animateScrollTo(targetScroll)
        }
    }

    Column(modifier = modifier) {
        // ── 标题行 ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTheme,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.theme_preset_section),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (onNavigateToThemeSettings != null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigateToThemeSettings() }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.theme_preset_more_settings),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── 横向滚动主题卡片 ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { rowWidthPx = it.width }
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(CARD_SPACING),
        ) {            // ── +新建主题 ──
            CreateThemeCard(
                onClick = {
                    onNavigateToThemeSettings?.invoke()
                }
            )
            allThemes.forEach { preset ->
                ThemePresetCard(
                    modifier = Modifier,
                    preset = preset,
                    isSelected = preset.id == activeThemeId,
                    onClick = {
                        scope.launch { settingsManager.applyThemePreset(preset) }
                    },
                    onDelete = if (!preset.isBuiltin) {
                        { pendingDeletePreset = preset }
                    } else null,
                )
            }
        }
    }

    // ── 删除确认弹窗 ──
    pendingDeletePreset?.let { preset ->
        AlertDialog(
            onDismissRequest = { pendingDeletePreset = null },
            title = { Text(stringResource(R.string.theme_delete_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.theme_delete_confirm_message,
                        preset.name,
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        themeRepository.deleteUserTheme(preset.id)
                        if (preset.id == activeThemeId) {
                            settingsManager.applyThemePreset(AppThemePreset.Default)
                        }
                    }
                    pendingDeletePreset = null
                }) {
                    Text(stringResource(R.string.theme_delete_confirm_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletePreset = null }) {
                    Text(stringResource(R.string.theme_delete_confirm_cancel))
                }
            },
        )
    }
}

// ══════════════════════════════════════════════════════════════
//  主题预设卡片（内部组件）
// ══════════════════════════════════════════════════════════════

@Composable
fun ThemePresetCard(
    modifier: Modifier = Modifier,
    preset: AppThemePreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null,
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceContainer

    val shape = MaterialTheme.shapes.medium
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(CARD_WIDTH)
            .height(CARD_FIXED_HEIGHT)
            .clip(shape)
            .glassBackground(
                color = containerColor,
                shape = shape
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        if (preset.isDynamicColors) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .glassBackground(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStar, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            ThemeColorPreview(preset.colorTupleString, Modifier.size(36.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = preset.displayName(),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // 特效图标
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        ) {
            if (preset.isGlassmorphismEnabled) {
                Icon(
                    com.t8rin.imagetoolbox.core.resources.Icons.Filled.Transparency, null, Modifier.size(10.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            if (preset.isLiquidGlassEnabled) {
                Icon(
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBlurOn, null, Modifier.size(10.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            if (preset.nightMode == NightMode.Dark) {
                Icon(
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDarkMode, null, Modifier.size(10.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // ── 选中 + 复制 + 删除 放在同一行 ──
        if (isSelected || onCopy != null || onDelete != null) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSelected) {
                    Icon(
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check, null,
                        Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                if (onCopy != null) {
                    if (isSelected) Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                        stringResource(R.string.theme_preset_copy),
                        Modifier
                            .size(14.dp)
                            .clickable { onCopy() },
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
                if (onDelete != null) {
                    if (isSelected || onCopy != null) Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                        stringResource(R.string.theme_preset_delete),
                        Modifier
                            .size(14.dp)
                            .clickable { onDelete() },
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeColorPreview(
    colorTupleString: String,
    modifier: Modifier = Modifier,
) {
    val colors = remember(colorTupleString) {
        if (colorTupleString.isBlank()) emptyList()
        else AppThemePreset.parseColorTuple(colorTupleString).mapNotNull { intColor ->
            intColor.takeIf { it != 0 }?.let { Color(it) }
        }
    }

    if (colors.isEmpty()) {
        Box(
            modifier = modifier.background(
                MaterialTheme.colorScheme.primaryContainer, CircleShape
            ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTheme, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    } else {
        val segmentAngle = 360f / colors.size
        Box(modifier = modifier) {
            androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                colors.forEachIndexed { index, color ->
                    rotate(degrees = index * segmentAngle) {
                        drawArc(
                            color = color, startAngle = 0f,
                            sweepAngle = segmentAngle, useCenter = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateThemeCard(onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.medium
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(CARD_WIDTH)
            .height(CARD_FIXED_HEIGHT)
            .clip(shape)
            .clickable { onClick() }
            .glassBackground(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = shape
            )
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .glassBackground(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.theme_preset_create),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}



