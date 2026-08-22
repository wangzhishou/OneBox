package com.wanbaohe.setting.theme.screen

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.manager.DeleteConfirmationManager
import com.shifenmiao.base.ui.AdvancedDeleteConfirmDialog
import com.shifenmiao.base.ui.button.ConfirmButton
import com.shifenmiao.common.ui.BottomSaveCancelBar
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.settings.domain.model.AppThemePreset
import com.t8rin.imagetoolbox.core.settings.domain.model.GradientBackgroundStyle
import com.t8rin.imagetoolbox.core.settings.domain.model.NightMode
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsManager
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.utils.getString
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorSelection
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSegmentedButtonRow
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.MeshGradientBackground
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.widget.sliders.custom_slider.CustomSlider
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.theme.CreateThemeCard
import com.t8rin.imagetoolbox.core.ui.widget.theme.ThemePresetCard
import com.t8rin.imagetoolbox.core.ui.widget.theme.displayName
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.wanbaohe.settings.R
import com.t8rin.imagetoolbox.core.resources.R as CoreR
import com.wanbaohe.setting.theme.component.ThemeEditMode
import com.wanbaohe.setting.theme.component.ThemeSettingsComponent
import com.wanbaohe.setting.theme.component.ThemeSettingsEvent
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTheme
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFeatures
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDarkMode
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettingsSuggest
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibility
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLightMode

private enum class ColorSlot {
    PRIMARY, SECONDARY, TERTIARY, SURFACE
}

@Composable
fun ThemeSettingsScreen(
    component: ThemeSettingsComponent,
) {
    val editingDraft by component.editingDraft.collectAsState()
    val allThemes by component.allThemes.collectAsState()
    val editMode by component.editMode.collectAsState()
    val activeThemeId = LocalSettingsState.current.activeThemeId
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var pendingDeletePreset by remember { mutableStateOf<AppThemePreset?>(null) }
    // 切换预设时如果有未保存修改，先弹确认
    var pendingSelectPreset by remember { mutableStateOf<AppThemePreset?>(null) }
    // 遮罩透明度是全局设置（滑动时自动保存），单独追踪是否变更，以便使保存按钮可点击
    var overlayAlphaDirty by remember { mutableStateOf(false) }

    val hasUnsavedChanges = editingDraft != null && (component.hasDraftChanged() || overlayAlphaDirty)
    val canReset = editingDraft != null && component.canResetDraft()
    val isSaveAsNew = component.isSaveAsNewMode()

    LaunchedEffect(Unit) {
        component.events.collect { event ->
            when (event) {
                is ThemeSettingsEvent.SaveSuccess -> {
                    overlayAlphaDirty = false
                    AppToastHost.showToast(getString(R.string.theme_saved_success))
                }
                is ThemeSettingsEvent.SaveFailed ->
                    AppToastHost.showFailureToast(getString(R.string.theme_save_failed))
                is ThemeSettingsEvent.DeleteSuccess ->
                    AppToastHost.showToast(getString(R.string.theme_delete_success, event.presetName))
                is ThemeSettingsEvent.DeleteFailed ->
                    AppToastHost.showFailureToast(getString(R.string.theme_delete_failed, event.presetName))
            }
        }
    }

    BackHandler(enabled = hasUnsavedChanges) {
        showExitConfirmDialog = true
    }

    BaseScreen(
        title = {
            Text(
                text = stringResource(R.string.theme_settings_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        onGoBack = {
            if (hasUnsavedChanges) {
                showExitConfirmDialog = true
            } else {
                component.onGoBack()
            }
        },
        isShowDefaultActions = true,
        showNavigationBarsPadding = false,
        supportGlassEffect = true,
    ) {
        // ── 编辑模式标识条 ──
        AnimatedVisibility(
            visible = hasUnsavedChanges,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            EditModeBanner(editMode = editMode)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing),
        ) {
            Spacer(modifier = Modifier.height(OneBoxDesignSystem.microSpacing))

            // ── 横向滚动选择主题 ──
            Text(
                text = stringResource(R.string.theme_preset_section),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
            ) {
                CreateThemeCard(onClick = { component.startCreateTheme() })
                allThemes.forEach { preset ->
                    ThemePresetCard(
                        preset = preset,
                        isSelected = preset.id == activeThemeId,
                        onClick = {
                            if (hasUnsavedChanges && preset.id != activeThemeId) {
                                pendingSelectPreset = preset
                            } else {
                                component.selectPresetForEditing(preset)
                            }
                        },
                        onDelete = if (!preset.isBuiltin) {
                            { pendingDeletePreset = preset }
                        } else null,
                        onCopy = { component.startCopyTheme(preset) },
                    )
                }
            }

            val draft = editingDraft
            if (draft != null) {
                val customThemeName = stringResource(R.string.theme_preset_custom)

                NightModeCard(
                    nightMode = draft.nightMode,
                    onNightModeChange = { component.updateDraftNightMode(it) },
                )

                ThemeNameCard(
                    name = draft.name.ifBlank { customThemeName },
                    onNameChange = { component.updateDraftName(it) },
                )

                ColorSchemeCard(
                    primaryColor = Color(draft.primaryColor),
                    secondaryColor = Color(draft.secondaryColor),
                    tertiaryColor = Color(draft.tertiaryColor),
                    surfaceColor = Color(draft.surfaceColor),
                    onPrimaryColorChange = { component.updateDraftPrimaryColor(it.toArgb()) },
                    onSecondaryColorChange = { component.updateDraftSecondaryColor(it.toArgb()) },
                    onTertiaryColorChange = { component.updateDraftTertiaryColor(it.toArgb()) },
                    onSurfaceColorChange = { component.updateDraftSurfaceColor(it.toArgb()) },
                )

                GlassEffectCard(
                    isGlassmorphismEnabled = draft.isGlassAlphaEnabled,
                    isLiquidGlassEnabled = draft.isLiquidGlassEnabled,
                    glassBaseAlpha = draft.glassBaseAlpha,
                    onGlassmorphismChange = { component.updateDraftGlassmorphism(it) },
                    onLiquidGlassChange = { component.updateDraftLiquidGlass(it) },
                    onGlassAlphaChange = { component.updateDraftGlassBaseAlpha(it) },
                )

                GradientBackgroundCard(
                    isMeshGradientEnabled = draft.isMeshGradientBgEnabled,
                    gradientStyle = draft.gradientStyle,
                    onMeshGradientChange = { component.updateDraftMeshGradient(it) },
                    onGradientStyleChange = { component.updateDraftGradientStyle(it) },
                )

                CustomBackgroundCard(
                    customBackgroundImageUri = draft.customBackgroundImageUri,
                    onBackgroundUriChange = { component.updateDraftCustomBackgroundUri(it) },
                    onOverlayAlphaChanged = { overlayAlphaDirty = true },
                )
            }
        }

        // ── 底部操作栏：[取消] [重置] [保存/保存为新主题] ──
        val saveText = if (isSaveAsNew) {
            stringResource(R.string.theme_save_as_new)
        } else {
            stringResource(R.string.theme_preset_save)
        }

        BottomSaveCancelBar(
            cancelEnabled = editingDraft != null,
            saveEnabled = editingDraft != null && hasUnsavedChanges,
            saveText = saveText,
            onCancel = {
                if (hasUnsavedChanges) showExitConfirmDialog = true
                else component.onGoBack()
            },
            onSave = { component.saveDraft() },
            extraActions = {
                if (canReset) {
                    TextButton(onClick = { component.resetDraftToSource() }) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = stringResource(R.string.theme_reset_to_source),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            },
        )
    }

    // ── 切换预设确认弹窗（有未保存修改时） ──
    pendingSelectPreset?.let { targetPreset ->
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { pendingSelectPreset = null },
            title = { Text(stringResource(R.string.theme_switch_confirm_title)) },
            text = { Text(stringResource(R.string.theme_switch_confirm_message)) },
            confirmButton = {
                ConfirmButton {
                    overlayAlphaDirty = false
                    component.selectPresetForEditing(targetPreset)
                    pendingSelectPreset = null
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingSelectPreset = null }) {
                    Text(stringResource(R.string.theme_discard_dismiss))
                }
            },
        )
    }

    // ── 删除确认弹窗 ──
    val showDeleteDialog = remember(pendingDeletePreset) { mutableStateOf(pendingDeletePreset != null) }
    AdvancedDeleteConfirmDialog(
        operationType = DeleteConfirmationManager.OperationType.THEME_SETTING,
        showDialog = showDeleteDialog,
        onConfirm = {
            component.deletePreset(pendingDeletePreset?.id ?: return@AdvancedDeleteConfirmDialog)
            pendingDeletePreset = null
        },
        title = stringResource(R.string.theme_delete_confirm_title),
        message = stringResource(R.string.theme_delete_confirm_message),
        showDoNotAskAgain = false
    )

    // ── 退出确认弹窗 ──
    if (showExitConfirmDialog) {
        ExitWithoutSavingDialog(
            title = stringResource(R.string.theme_discard_title),
            text = stringResource(R.string.theme_discard_message),
            onExit = {
                showExitConfirmDialog = false
                overlayAlphaDirty = false
                component.restoreAndGoBack()
            },
            onDismiss = { showExitConfirmDialog = false },
            visible = showExitConfirmDialog
        )
    }
}

// ══════════════════════════════════════════════════════════════
//  编辑模式标识条
// ══════════════════════════════════════════════════════════════

@Composable
private fun EditModeBanner(editMode: ThemeEditMode?) {
    val (icon, text) = when (editMode) {
        is ThemeEditMode.EditingUser -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit to stringResource(R.string.theme_mode_editing_user)
        is ThemeEditMode.CreatingNew -> {
            val forkedName = editMode.forkedFrom?.displayName()
            if (forkedName != null) {
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit to stringResource(R.string.theme_mode_creating_from, forkedName)
            } else {
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit to stringResource(R.string.theme_mode_creating_new)
            }
        }
        null -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibility to stringResource(R.string.theme_preview_banner)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = OneBoxDesignSystem.screenPadding, vertical = OneBoxDesignSystem.compactSpacing),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .glassBackground(
                    style = GlassStyle.Regular,
                    shape = RoundedCornerShape(OneBoxDesignSystem.largeRadius),
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
                .padding(horizontal = OneBoxDesignSystem.itemSpacing, vertical = OneBoxDesignSystem.compactSpacing),
            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  主题名称卡片
// ══════════════════════════════════════════════════════════════

@Composable
private fun ThemeNameCard(
    name: String,
    onNameChange: (String) -> Unit,
) {
    var isEditing by remember { mutableStateOf(false) }
    val editDesc = stringResource(R.string.theme_edit_name_desc)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(OneBoxDesignSystem.cardPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
        ) {
            Text(
                text = stringResource(R.string.theme_preset_name_hint),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isEditing) {
                    OneBoxOutlinedTextField(
                        value = name,
                        onValueChange = onNameChange,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                } else {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                }
                IconButton(
                    onClick = { isEditing = !isEditing },
                    modifier = Modifier.semantics { contentDescription = editDesc },
                ) {
                    Icon(
                        imageVector = if (isEditing) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  颜色方案卡片
// ══════════════════════════════════════════════════════════════

@Composable
private fun ColorSchemeCard(
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color,
    surfaceColor: Color,
    onPrimaryColorChange: (Color) -> Unit,
    onSecondaryColorChange: (Color) -> Unit,
    onTertiaryColorChange: (Color) -> Unit,
    onSurfaceColorChange: (Color) -> Unit,
) {
    var editingSlot by remember { mutableStateOf<ColorSlot?>(null) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(OneBoxDesignSystem.cardPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
        ) {
            Text(
                text = stringResource(R.string.theme_preset_color_section),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
            ) {
                ColorBlock(
                    color = primaryColor,
                    label = stringResource(R.string.theme_preset_primary_color),
                    modifier = Modifier.weight(1f),
                    onClick = { editingSlot = ColorSlot.PRIMARY },
                )
                ColorBlock(
                    color = secondaryColor,
                    label = stringResource(R.string.theme_preset_secondary_color),
                    modifier = Modifier.weight(1f),
                    onClick = { editingSlot = ColorSlot.SECONDARY },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
            ) {
                ColorBlock(
                    color = tertiaryColor,
                    label = stringResource(R.string.theme_preset_tertiary_color),
                    modifier = Modifier.weight(1f),
                    onClick = { editingSlot = ColorSlot.TERTIARY },
                )
                ColorBlock(
                    color = surfaceColor,
                    label = stringResource(R.string.theme_preset_surface_color),
                    modifier = Modifier.weight(1f),
                    onClick = { editingSlot = ColorSlot.SURFACE },
                )
            }

            Text(
                text = stringResource(R.string.theme_dynamic_colors_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        }
    }

    editingSlot?.let { slot ->
        val currentColor = when (slot) {
            ColorSlot.PRIMARY -> primaryColor
            ColorSlot.SECONDARY -> secondaryColor
            ColorSlot.TERTIARY -> tertiaryColor
            ColorSlot.SURFACE -> surfaceColor
        }
        val onColorChange: (Color) -> Unit = when (slot) {
            ColorSlot.PRIMARY -> onPrimaryColorChange
            ColorSlot.SECONDARY -> onSecondaryColorChange
            ColorSlot.TERTIARY -> onTertiaryColorChange
            ColorSlot.SURFACE -> onSurfaceColorChange
        }
        val slotLabel = when (slot) {
            ColorSlot.PRIMARY -> stringResource(R.string.theme_preset_primary_color)
            ColorSlot.SECONDARY -> stringResource(R.string.theme_preset_secondary_color)
            ColorSlot.TERTIARY -> stringResource(R.string.theme_preset_tertiary_color)
            ColorSlot.SURFACE -> stringResource(R.string.theme_preset_surface_color)
        }
        EnhancedAlertDialog(
            visible = editingSlot != null,
            onDismissRequest = { editingSlot = null },
            title = { Text(text = slotLabel) },
            text = {
                ColorSelection(
                    value = currentColor,
                    onValueChange = onColorChange,
                )
            },
            confirmButton = {
                ConfirmButton {
                    editingSlot = null
                }
            },
        )
    }
}

@Composable
private fun ColorBlock(
    color: Color,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
    ) {
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.4f)
                .clip(RoundedCornerShape(OneBoxDesignSystem.smallRadius))
                .semantics { contentDescription = label }
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(OneBoxDesignSystem.smallRadius),
            color = color,
            style = GlassStyle.Medium,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "#${Integer.toHexString(color.toArgb()).uppercase().drop(2)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = if (colorLuminance(color) > 0.5f) Color.Black.copy(alpha = 0.7f)
                    else Color.White.copy(alpha = 0.9f),
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private fun colorLuminance(color: Color): Float {
    return color.red * 0.2126f + color.green * 0.7152f + color.blue * 0.0722f
}

// ══════════════════════════════════════════════════════════════
//  玻璃效果卡片
// ══════════════════════════════════════════════════════════════

@Composable
private fun GlassEffectCard(
    isGlassmorphismEnabled: Boolean,
    isLiquidGlassEnabled: Boolean,
    glassBaseAlpha: Float,
    onGlassmorphismChange: (Boolean) -> Unit,
    onLiquidGlassChange: (Boolean) -> Unit,
    onGlassAlphaChange: (Float) -> Unit,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(OneBoxDesignSystem.cardPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .glassBackground(
                            style = GlassStyle.Regular,
                            shape = OneBoxDesignSystem.compactBadgeShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFeatures,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = stringResource(R.string.theme_preset_glass_section),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))

            SwitchRow(
                label = stringResource(R.string.glass_alpha_effect),
                checked = isGlassmorphismEnabled,
                onCheckedChange = onGlassmorphismChange,
            )

            SwitchRow(
                label = stringResource(R.string.liquid_glass_effect),
                checked = isLiquidGlassEnabled,
                onCheckedChange = onLiquidGlassChange,
                enabled = isGlassmorphismEnabled,
            )

            AnimatedVisibility(
                visible = isGlassmorphismEnabled,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = OneBoxDesignSystem.compactSpacing),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.theme_preset_glass_alpha),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "${(glassBaseAlpha * 100).toInt()}%",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    CustomSlider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = OneBoxDesignSystem.microSpacing),
                        value = glassBaseAlpha,
                        onValueChange = onGlassAlphaChange,
                        valueRange = 0.1f..1f,
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  渐变背景卡片
// ══════════════════════════════════════════════════════════════

@Composable
private fun GradientBackgroundCard(
    isMeshGradientEnabled: Boolean,
    gradientStyle: GradientBackgroundStyle,
    onMeshGradientChange: (Boolean) -> Unit,
    onGradientStyleChange: (GradientBackgroundStyle) -> Unit,
) {
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
        GradientBackgroundStyle.PrismFlow to stringResource(R.string.gradient_style_prism_flow),
    )
    val selectedLabel = options.firstOrNull { it.first == gradientStyle }?.second ?: ""

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(OneBoxDesignSystem.cardPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .glassBackground(
                                style = GlassStyle.Regular,
                                shape = OneBoxDesignSystem.compactBadgeShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTheme,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = stringResource(R.string.mesh_gradient_background),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                GlassSwitch(
                    checked = isMeshGradientEnabled,
                    onCheckedChange = onMeshGradientChange,
                    colors = AppTheme.colors.switchColors(),
                )
            }

            AnimatedVisibility(
                visible = isMeshGradientEnabled,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
                ) {
                    Spacer(modifier = Modifier.height(OneBoxDesignSystem.microSpacing))

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
                        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
                    ) {
                        options.forEach { (style, label) ->
                            FilterChip(
                                selected = style == gradientStyle,
                                onClick = { onGradientStyleChange(style) },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1,
                                    )
                                },
                                leadingIcon = if (style == gradientStyle) {
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
                                    selected = style == gradientStyle,
                                ),
                            )
                        }
                    }

                    GlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        shape = RoundedCornerShape(OneBoxDesignSystem.smallRadius),
                        style = GlassStyle.Thin,
                    ) {
                        MeshGradientBackground(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(OneBoxDesignSystem.smallRadius)),
                        )

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(OneBoxDesignSystem.itemSpacing)
                                .glassBackground(
                                    style = GlassStyle.Medium,
                                    shape = RoundedCornerShape(OneBoxDesignSystem.largeRadius),
                                )
                                .padding(horizontal = OneBoxDesignSystem.itemSpacing, vertical = OneBoxDesignSystem.compactSpacing),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibility,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stringResource(
                                    R.string.theme_preset_preview_gradient,
                                    selectedLabel,
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  自定义背景图片卡片
// ══════════════════════════════════════════════════════════════

@Composable
private fun CustomBackgroundCard(
    customBackgroundImageUri: String?,
    onBackgroundUriChange: (String?) -> Unit,
    onOverlayAlphaChanged: () -> Unit = {},
) {
    val settingsManager = LocalSettingsManager.current
    val settingsState = LocalSettingsState.current
    val scope = rememberCoroutineScope()

    val imagePicker = rememberImagePicker { uri: Uri ->
        onBackgroundUriChange(uri.toString())
    }

    var overlayAlpha by remember(settingsState.customBackgroundOverlayAlpha) {
        mutableFloatStateOf(settingsState.customBackgroundOverlayAlpha)
    }

    val hasCustomBg = customBackgroundImageUri != null
    var previewAspectRatio by remember(customBackgroundImageUri) { mutableFloatStateOf(16f / 9f) }
    var imageLoadFailed by remember(customBackgroundImageUri) { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(OneBoxDesignSystem.cardPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .glassBackground(
                                style = GlassStyle.Regular,
                                shape = OneBoxDesignSystem.compactBadgeShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = stringResource(R.string.custom_background),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (hasCustomBg) {
                    Text(
                        text = stringResource(R.string.custom_background_clear),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .clip(RoundedCornerShape(OneBoxDesignSystem.microSpacing))
                            .clickable { onBackgroundUriChange(null) }
                            .padding(horizontal = OneBoxDesignSystem.microSpacing, vertical = OneBoxDesignSystem.microSpacing),
                    )
                }
            }

            if (!hasCustomBg) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(OneBoxDesignSystem.compactBadgeShape)
                        .glassBackground(
                            style = GlassStyle.Thin,
                            shape = OneBoxDesignSystem.compactBadgeShape,
                        )
                        .clickable { imagePicker.pickImage() }
                        .padding(horizontal = OneBoxDesignSystem.itemSpacing, vertical = OneBoxDesignSystem.compactSpacing),
                    horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(R.string.custom_background_pick),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (hasCustomBg) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(OneBoxDesignSystem.compactBadgeShape)
                        .glassBackground(
                            style = GlassStyle.Thin,
                            shape = OneBoxDesignSystem.compactBadgeShape,
                        )
                        .clickable { imagePicker.pickImage() }
                        .padding(horizontal = OneBoxDesignSystem.itemSpacing, vertical = OneBoxDesignSystem.compactSpacing),
                    horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(R.string.custom_background_pick),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.custom_background_clear),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onBackgroundUriChange(null) },
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    )
                }

                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(OneBoxDesignSystem.smallRadius)),
                    shape = RoundedCornerShape(OneBoxDesignSystem.smallRadius),
                    style = GlassStyle.Thin,
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        AsyncImage(
                            model = customBackgroundImageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(previewAspectRatio)
                                .clip(RoundedCornerShape(OneBoxDesignSystem.smallRadius)),
                            onState = { state ->
                                when (state) {
                                    is AsyncImagePainter.State.Loading -> {
                                        imageLoadFailed = false
                                    }
                                    is AsyncImagePainter.State.Success -> {
                                        imageLoadFailed = false
                                        val size = state.painter.intrinsicSize
                                        if (size.width > 0f && size.height > 0f) {
                                            previewAspectRatio = size.width / size.height
                                        }
                                    }
                                    is AsyncImagePainter.State.Error -> {
                                        imageLoadFailed = true
                                    }
                                    else -> {}
                                }
                            }
                        )

                        if (imageLoadFailed) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = stringResource(R.string.theme_image_load_failed),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                                Text(
                                    text = stringResource(R.string.theme_image_retry),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { imagePicker.pickImage() },
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.custom_background_overlay_alpha),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "${(overlayAlpha * 100).toInt()}%",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = stringResource(R.string.theme_overlay_global_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
                CustomSlider(
                    modifier = Modifier.fillMaxWidth(),
                    value = overlayAlpha,
                    onValueChange = { overlayAlpha = it },
                    onValueChangeFinished = {
                        scope.launch {
                            settingsManager.setCustomBackgroundOverlayAlpha(overlayAlpha)
                        }
                        onOverlayAlphaChanged()
                    },
                    valueRange = 0f..1f,
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  日夜模式卡片
// ══════════════════════════════════════════════════════════════

@Composable
private fun NightModeCard(
    nightMode: NightMode,
    onNightModeChange: (NightMode) -> Unit,
) {
    val options = listOf(
        Triple(
            stringResource(CoreR.string.light),
            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLightMode,
            NightMode.Light
        ),
        Triple(
            stringResource(CoreR.string.dark),
            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDarkMode,
            NightMode.Dark
        ),
        Triple(
            stringResource(CoreR.string.system),
            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettingsSuggest,
            NightMode.System
        ),
    )
    var selectedIndex by remember { mutableIntStateOf(options.indexOfFirst { it.third == nightMode }.coerceAtLeast(0)) }
    LaunchedEffect(nightMode) {
        selectedIndex = options.indexOfFirst { it.third == nightMode }.coerceAtLeast(0)
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(OneBoxDesignSystem.cardPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .glassBackground(
                            style = GlassStyle.Regular,
                            shape = OneBoxDesignSystem.compactBadgeShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDarkMode,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = stringResource(R.string.theme_night_mode_section),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            GlassSegmentedButtonRow(
                options = options,
                selectedOption = options[selectedIndex],
                onOptionSelected = { triple ->
                    val index = options.indexOf(triple)
                    if (selectedIndex != index) {
                        onNightModeChange(triple.third)
                        selectedIndex = index
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { (text, icon, _) ->
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(end = 2.dp),
                        )
                    }
                },
                rowStyle = GlassStyle.None,
                rowColor = MaterialTheme.colorScheme.surfaceContainer,
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  通用 SwitchRow
// ══════════════════════════════════════════════════════════════

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.weight(1f),
        )
        GlassSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            thumbContent = {
                if (checked) Icon(
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check, null,
                    Modifier.size(SwitchDefaults.IconSize),
                )
            },
            colors = AppTheme.colors.switchColors(),
        )
    }
}
