package com.shifenmiao.marquee.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.marquee.screen.components.MarqueePreview
import com.shifenmiao.marquee.screen.components.ModeSettingsPanel
import com.shifenmiao.marquee.screen.components.PresentationModeSelector
import com.shifenmiao.marquee.screenLogic.MarqueeComponent
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorSelectionRow
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorSelectionRowDefaults
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassFilterChip
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCustomSlider
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle

@Composable
fun MarqueeScreen(
    onGoBack: () -> Unit,
    component: MarqueeComponent
) {
    val marqueeSettingsState by component.marqueeSettingsState.collectAsState()
    val marqueeHistory by component.marqueeHistory.collectAsState()

    val placeholderText = stringResource(id = R.string.app_slogan)

    // 实时预览最多展示的字符数（避免过长导致预览卡顿/溢出；全屏仍显示完整）
    val previewMaxChars = 60


    var sliderSizeValue by remember(marqueeSettingsState.marqueeTextSize) {
        mutableFloatStateOf(marqueeSettingsState.marqueeTextSize)
    }

    val context = LocalContext.current

    fun launchPreview() {
        // 这里不要把 placeholder 文案写进 state（否则输入框会把 placeholder 当成真实文本）
        // 空文本时，全屏用默认提示兜底，避免全屏空白
        if (marqueeSettingsState.marqueeText.isBlank()) {
            component.onTextContentChange(placeholderText)
        }
        // 点击"显示字幕"时强制保存，确保字间距/速度等参数立即生效
        component.saveState()
        context.startActivity(
            android.content.Intent(
                context,
                com.shifenmiao.marquee.activity.MarqueeActivity::class.java
            )
        )
    }

    BaseScreen(
        title = stringResource(id = R.string.marquee),
        onGoBack = onGoBack,
        actions = {
            IconButton(
                onClick = { launchPreview() }
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle,
                    contentDescription = stringResource(id = R.string.marquee_preview),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        supportGlassEffect = true,
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .imePadding()
                .padding(
                    AppTheme.dimens.paddingNormal,
                    4.dp
                )
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.marquee_presentation_style),
                    style = AppTheme.typography.getCardTitle()
                )

                Spacer(modifier = Modifier.height(8.dp))

                PresentationModeSelector(
                    selectedMode = marqueeSettingsState.presentationMode,
                    onModeSelected = { mode ->
                        if (marqueeSettingsState.presentationMode != mode) {
                            component.onPresentationModeChange(mode)
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
                Text(
                    text = stringResource(id = R.string.marquee_text_content),
                    style = AppTheme.typography.getCardTitle()
                )

                GlassOutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    value = marqueeSettingsState.marqueeText.ifBlank { "" },
                    placeholder = {
                        Text(
                            text = placeholderText,
                            // 用 onSurfaceVariant，避免和背景太接近导致"看起来像没显示"
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                    },
                    trailingIcon = {
                        if (marqueeSettingsState.marqueeText.isNotEmpty()) {
                            IconButton(onClick = { component.onTextContentChange("") }) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                                    contentDescription = stringResource(id = R.string.settings_close),
                                    tint = MaterialTheme.colorScheme.surfaceContainerHighest
                                )
                            }
                        }
                    },
                    onValueChange = { newValue ->
                        component.onTextContentChange(newValue)
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { launchPreview() }),
                    singleLine = false,
                    minLines = 2,
                    maxLines = 6,
                    shape = MaterialTheme.shapes.medium,
                    colors = AppTheme.colors.getOutlinedTextFieldColors(),
                )

                if (marqueeHistory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.history),
                        style = AppTheme.typography.getCardTitle(),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(marqueeHistory.size) { index ->
                            val text = marqueeHistory[index]
                            GlassFilterChip(
                                selected = false,
                                onClick = { component.onTextContentChange(text) },
                                label = {
                                    Text(
                                        text = text,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 150.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // 实时预览区域
            item {
                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
                Text(
                    text = stringResource(id = R.string.marquee_preview_title),
                    style = AppTheme.typography.getCardTitle()
                )

                val previewSourceText = marqueeSettingsState.marqueeText.ifBlank { placeholderText }
                val previewText = remember(previewSourceText) {
                    // 预览时将换行替换为空格，保持单行显示
                    val singleLine = previewSourceText.replace("\n", " ").replace("\r", "")
                    if (singleLine.length > previewMaxChars) {
                        singleLine.take(previewMaxChars) + "…"
                    } else singleLine
                }
                val previewTruncated = previewSourceText.length > previewMaxChars

                Spacer(modifier = Modifier.height(8.dp))
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(AppTheme.dimens.cornerRadiusSmall),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(marqueeSettingsState.marqueeBackgroundColor)
                    )
                ) {
                    MarqueePreview(
                        marqueeSettings = marqueeSettingsState,
                        previewText = previewText
                    )
                }

                if (previewTruncated) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(
                            id = R.string.marquee_preview_truncated_hint,
                            previewMaxChars
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
                Text(
                    text = stringResource(id = R.string.marquee_text_color),
                    style = AppTheme.typography.getCardTitle()
                )
                ColorSelectionRow(
                    defaultColors = ColorSelectionRowDefaults.colorList.asReversed(),
                    allowAlpha = true,
                    contentPadding = PaddingValues(0.dp),
                    value = Color(marqueeSettingsState.marqueeTextColor),
                    onValueChange = {
                        component.onTextColorChange(it)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
                Text(
                    text = stringResource(id = R.string.marquee_background_color),
                    style = AppTheme.typography.getCardTitle()
                )

                ColorSelectionRow(
                    defaultColors = ColorSelectionRowDefaults.colorList.asReversed(),
                    allowAlpha = true,
                    contentPadding = PaddingValues(0.dp),
                    value = Color(marqueeSettingsState.marqueeBackgroundColor),
                    onValueChange = {
                        component.onBackgroundColorChange(it)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.marquee_text_size),
                        style = AppTheme.typography.getCardTitle()
                    )
                    Text(
                        text = sliderSizeValue.toInt().toString() + " sp",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                GlassCustomSlider(
                    value = marqueeSettingsState.marqueeTextSize,
                    onValueChange = {
                        sliderSizeValue = (it * 100).toInt() / 100f
                    },
                    valueRange = 50f..200f,
                    onValueChangeFinished = {
                        component.onTextSizeChange(sliderSizeValue)
                    },
                    steps = 15,
                )
            }

            // 字间距设置
            item {
                var letterSpacingValue by remember(marqueeSettingsState.letterSpacing) {
                    mutableFloatStateOf(marqueeSettingsState.letterSpacing)
                }
                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.marquee_letter_spacing),
                        style = AppTheme.typography.getCardTitle()
                    )
                    Text(
                        text = String.format("%.1f sp", letterSpacingValue),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                GlassCustomSlider(
                    value = letterSpacingValue,
                    onValueChange = {
                        letterSpacingValue = (it * 10).toInt() / 10f
                    },
                    valueRange = 0f..20f,
                    onValueChangeFinished = {
                        component.onLetterSpacingChange(letterSpacingValue)
                    },
                    steps = 19,
                )
            }


            // 效果设置区域
            item {
                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
                Text(
                    text = stringResource(id = R.string.marquee_effects),
                    style = AppTheme.typography.getCardTitle()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 加粗字体
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.marquee_bold),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    GlassSwitch(
                        checked = marqueeSettingsState.marqueeBoldEnabled,
                        onCheckedChange = { component.onBoldEnabledChange(it) },
                        colors = AppTheme.colors.switchColors(),
                        thumbContent = {
                            if (marqueeSettingsState.marqueeBoldEnabled) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        }
                    )
                }

                // 镜像翻转
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.marquee_mirror),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(id = R.string.marquee_mirror_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.68f)
                        )
                    }
                    GlassSwitch(
                        checked = marqueeSettingsState.marqueeMirrorEnabled,
                        onCheckedChange = { component.onMirrorEnabledChange(it) },
                        colors = AppTheme.colors.switchColors(),
                        thumbContent = {
                            if (marqueeSettingsState.marqueeMirrorEnabled) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        }
                    )
                }

                // 闪烁效果
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.marquee_blink),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    GlassSwitch(
                        checked = marqueeSettingsState.marqueeBlinkEnabled,
                        onCheckedChange = { component.onBlinkEnabledChange(it) },
                        colors = AppTheme.colors.switchColors(),
                        thumbContent = {
                            if (marqueeSettingsState.marqueeBlinkEnabled) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        }
                    )
                }

                // 背景闪烁效果
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.marquee_background_blink),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    GlassSwitch(
                        checked = marqueeSettingsState.backgroundBlinkEnabled,
                        onCheckedChange = { component.onBackgroundBlinkEnabledChange(it) },
                        colors = AppTheme.colors.switchColors(),
                        thumbContent = {
                            if (marqueeSettingsState.backgroundBlinkEnabled) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        }
                    )
                }

                // 闪烁速度（仅在任一闪烁启用时显示）
                if (marqueeSettingsState.marqueeBlinkEnabled || marqueeSettingsState.backgroundBlinkEnabled) {
                    var sliderBlinkSpeedValue by remember(marqueeSettingsState.marqueeBlinkSpeed) {
                        mutableFloatStateOf(marqueeSettingsState.marqueeBlinkSpeed)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(id = R.string.marquee_blink_speed),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = String.format(
                                LocalLocale.current.platformLocale,
                                "%.1f",
                                sliderBlinkSpeedValue
                            ) + " " + stringResource(id = R.string.marquee_blink_speed_unit),
                            modifier = Modifier.padding(start = 4.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    GlassCustomSlider(
                        value = sliderBlinkSpeedValue,
                        onValueChange = {
                            sliderBlinkSpeedValue = (it * 100).toInt() / 100f
                        },
                        valueRange = 0.5f..5f,
                        onValueChangeFinished = {
                            component.onBlinkSpeedChange(sliderBlinkSpeedValue)
                        },
                        steps = 9,
                    )
                }

                // 烟花背景
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.marquee_fireworks_background),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    GlassSwitch(
                        checked = marqueeSettingsState.fireworksEnabled,
                        onCheckedChange = { component.onFireworksEnabledChange(it) },
                        colors = AppTheme.colors.switchColors(),
                        thumbContent = {
                            if (marqueeSettingsState.fireworksEnabled) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        }
                    )
                }
            }

            // 模式专属设置面板
            item {
                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
                ModeSettingsPanel(
                    marqueeSettings = marqueeSettingsState,
                    onTextSpacingChange = { component.onTextSpacingChange(it) },
                    onTextVelocityChange = { component.onTextVelocityChange(it) },
                    onTypewriterCharsPerSecondChange = {
                        component.onTypewriterCharsPerSecondChange(
                            it
                        )
                    },
                    onOneCharHoldMillisChange = { component.onOneCharHoldMillisChange(it) },
                    onPagedAutoAdvanceEnabledChange = { component.onPagedAutoAdvanceEnabledChange(it) },
                    onPagedIntervalMillisChange = { component.onPagedIntervalMillisChange(it) },
                    onClockShowSecondsChange = { component.onClockShowSecondsChange(it) },
                    onClockShowDateChange = { component.onClockShowDateChange(it) },
                    onClockUse24HourChange = { component.onClockUse24HourChange(it) },
                    onCountdownSecondsChange = { component.onCountdownSecondsChange(it) },
                    onCountdownShowMillisChange = { component.onCountdownShowMillisChange(it) },
                    onBouncingSpeedChange = { component.onBouncingSpeedChange(it) },
                    saveState = { component.saveState() }
                )
            }
        }

        // 底部固定的操作栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.dimens.paddingNormal)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧提示文字
            Text(
                text = stringResource(id = R.string.marquee_fullscreen_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 显示字幕按钮
            GlassTonalButton(
                onClick = { launchPreview() },
                shape = MaterialTheme.shapes.large,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle,
                    contentDescription = stringResource(id = R.string.marquee_preview),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.marquee_preview),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}
