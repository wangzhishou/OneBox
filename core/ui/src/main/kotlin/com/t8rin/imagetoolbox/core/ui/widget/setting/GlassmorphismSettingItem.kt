package com.t8rin.imagetoolbox.core.ui.widget.setting

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.toBitmap
import com.shifenmiao.core.R
import com.shifenmiao.theme.AppTheme
import com.t8rin.dynamic.theme.ColorTuple
import com.t8rin.dynamic.theme.LocalDynamicThemeState
import com.t8rin.dynamic.theme.calculateSecondaryColor
import com.t8rin.dynamic.theme.calculateSurfaceColor
import com.t8rin.dynamic.theme.calculateTertiaryColor
import com.t8rin.dynamic.theme.extractPrimaryColor
import com.t8rin.imagetoolbox.core.resources.icons.ImageSync
import com.t8rin.imagetoolbox.core.settings.domain.model.GradientBackgroundStyle
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsManager
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.widget.sliders.custom_slider.CustomSlider
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Transparency
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBlurOn
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGradientMaker

/**
 * 玻璃透明开关 —— 控制全局 GlassCard / GlassSurface / GlassModifier 是否启用。
 * 关闭后所有毛玻璃组件降级为普通不透明背景。
 */
@Composable
fun GlassmorphismSettingItem() {
    val localSettingsState = LocalSettingsState.current
    val settingsInteractor = LocalSettingsManager.current
    val scope = rememberCoroutineScope()
    var isEnabled by remember(localSettingsState.isGlassAlphaEnabled) {
        mutableStateOf(localSettingsState.isGlassAlphaEnabled)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Filled.Transparency,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.glass_alpha_effect),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = isEnabled,
            onCheckedChange = {
                isEnabled = it
                scope.launch {
                    settingsInteractor.toggleGlassmorphismEnabled()
                }
            },
            thumbContent = {
                if (isEnabled) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            },
            colors = AppTheme.colors.switchColors()
        )
    }
}

/**
 * 液态玻璃开关 —— 控制是否使用 Liquid Glass（高斯模糊 + 方向性高光 + 内阴影）。
 * 开启后所有 glassBackground / glassThin / glassRegular 等自动切换到 Liquid Glass 实现。
 */
@Composable
fun LiquidGlassSettingItem(
    modifier: Modifier = Modifier,
) {
    val localSettingsState = LocalSettingsState.current
    val settingsInteractor = LocalSettingsManager.current
    val scope = rememberCoroutineScope()
    var isEnabled by remember(localSettingsState.isLiquidGlassEnabled) {
        mutableStateOf(localSettingsState.isLiquidGlassEnabled)
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBlurOn,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.liquid_glass_effect),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = isEnabled,
            onCheckedChange = {
                isEnabled = it
                scope.launch {
                    settingsInteractor.toggleLiquidGlassEnabled()
                }
            },
            thumbContent = {
                if (isEnabled) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            },
            colors = AppTheme.colors.switchColors()
        )
    }
}


/**
 * 渐变背景风格选择 + 自定义背景 —— 合并为一个设置项，复用同一标题。
 * 在 Classic / Aurora / Ocean / Sunset 之间切换，同时支持选择自定义背景图片。
 * 选择背景图片后自动提取主色并应用为当前主题。
 * 已设置自定义背景时显示遮罩透明度滑块。
 */
@Composable
fun GradientBackgroundStyleSettingItem(
    modifier: Modifier = Modifier
) {
    val localSettingsState = LocalSettingsState.current
    val settingsInteractor = LocalSettingsManager.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val themeState = LocalDynamicThemeState.current

    // ---- 自定义背景相关 ----
    val hasCustomBg = localSettingsState.customBackgroundImageUri != null
    val imagePicker = rememberImagePicker { uri: Uri ->
        scope.launch {
            // 1. 保存自定义背景 URI
            settingsInteractor.setCustomBackgroundImageUri(uri.toString())
            // 2. 从图片提取主色，新建主题并保存 + 应用
            try {
                val result = context.imageLoader.execute(
                    ImageRequest.Builder(context).data(uri).build()
                )
                result.image?.toBitmap()?.let { bitmap ->
                    // 运行时立即切换颜色
                    themeState.updateColorByImage(bitmap)
                    // 提取主色，并根据主色自动计算 secondary / tertiary / surface
                    val primaryColor = bitmap.extractPrimaryColor()
                    val newTuple = ColorTuple(
                        primary = primaryColor,
                        secondary = Color(primaryColor.calculateSecondaryColor()),
                        tertiary = Color(primaryColor.calculateTertiaryColor()),
                        surface = Color(primaryColor.calculateSurfaceColor())
                    )
                    // 持久化：设为当前主题
                    val tupleString = newTuple.run {
                        "${primary.toArgb()}*${secondary?.toArgb()}*${tertiary?.toArgb()}*${surface?.toArgb()}"
                    }
                    settingsInteractor.setColorTuple(tupleString)
                    // 持久化：追加到主题列表
                    val tuplesString =
                        (localSettingsState.colorTupleList + newTuple).joinToString(separator = "*") { tuple ->
                            "${tuple.primary.toArgb()}/${tuple.secondary?.toArgb()}/${tuple.tertiary?.toArgb()}/${tuple.surface?.toArgb()}"
                        }
                    settingsInteractor.setColorTuples(tuplesString)
                }
            } catch (_: Exception) {
                // 图片加载失败时忽略颜色提取
            }
        }
    }

    // ---- 遮罩透明度 ----
    var overlayAlpha by remember(localSettingsState.customBackgroundOverlayAlpha) {
        mutableFloatStateOf(localSettingsState.customBackgroundOverlayAlpha)
    }

    // ---- 渐变风格相关 ----
    val options = listOf(
        GradientBackgroundStyle.Classic to stringResource(R.string.gradient_style_classic),
        GradientBackgroundStyle.Aurora to stringResource(R.string.gradient_style_aurora),
        GradientBackgroundStyle.Ocean to stringResource(R.string.gradient_style_ocean),
        GradientBackgroundStyle.Sunset to stringResource(R.string.gradient_style_sunset),
        GradientBackgroundStyle.SakuraMist to stringResource(R.string.gradient_style_sakura_mist),
        GradientBackgroundStyle.MintBreeze to stringResource(R.string.gradient_style_mint_breeze),
        GradientBackgroundStyle.StarryNight to stringResource(R.string.gradient_style_starry_night),
        GradientBackgroundStyle.Lavender to stringResource(R.string.gradient_style_lavender),
        GradientBackgroundStyle.WarmGlow to stringResource(R.string.gradient_style_warm_glow),
        GradientBackgroundStyle.Ethereal to stringResource(R.string.gradient_style_ethereal),
        GradientBackgroundStyle.NeonCyber to stringResource(R.string.gradient_style_neon_cyber),
    )

    var selectedIndex by remember(localSettingsState.gradientBackgroundStyle) {
        mutableIntStateOf(
            options.indexOfFirst { it.first == localSettingsState.gradientBackgroundStyle }
                .coerceAtLeast(0)
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 标题行（复用一个标题，同时显示清除自定义背景按钮）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGradientMaker,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.gradient_style_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (hasCustomBg) {
                Text(
                    text = stringResource(R.string.custom_background_clear),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.clickable {
                        scope.launch {
                            settingsInteractor.setCustomBackgroundImageUri(null)
                        }
                    }
                )
            }
        }

        // 渐变风格选择 —— FlowRow 自动换行，适配 9 种风格
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            options.forEachIndexed { index, (style, label) ->
                FilterChip(
                    selected = index == selectedIndex,
                    onClick = {
                        if (index != selectedIndex) {
                            selectedIndex = index
                            scope.launch {
                                settingsInteractor.setGradientBackgroundStyle(style)
                            }
                        }
                    },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    leadingIcon = if (index == selectedIndex) {
                        {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = Color.Transparent,
                        selectedBorderColor = Color.Transparent,
                        enabled = true,
                        selected = index == selectedIndex,
                    ),
                )
            }
        }

        // 自定义背景图片选择入口（未设置时显示）
        if (!hasCustomBg) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { imagePicker.pickImage() }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ImageSync,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.custom_background_pick),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 已设置自定义背景时 → 遮罩透明度滑块
        if (hasCustomBg) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.custom_background_overlay_alpha),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(overlayAlpha * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                CustomSlider(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 0.dp, horizontal = 6.dp),
                    value = overlayAlpha,
                    onValueChange = { overlayAlpha = it },
                    onValueChangeFinished = {
                        scope.launch {
                            settingsInteractor.setCustomBackgroundOverlayAlpha(overlayAlpha)
                        }
                    },
                    valueRange = 0f..1f
                )
            }
        }
    }
}