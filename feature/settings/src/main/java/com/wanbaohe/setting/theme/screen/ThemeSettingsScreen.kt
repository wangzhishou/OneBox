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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.manager.DeleteConfirmationManager
import com.shifenmiao.base.ui.AdvancedDeleteConfirmDialog
import com.shifenmiao.base.ui.button.ConfirmButton
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.aiImageProcessPointsCost
import com.shifenmiao.common.ui.BottomSaveCancelBar
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.settings.domain.model.AppColorSystem
import com.t8rin.imagetoolbox.core.settings.domain.model.AppThemePreset
import com.t8rin.imagetoolbox.core.settings.domain.model.GradientBackgroundStyle
import com.t8rin.imagetoolbox.core.settings.domain.model.NightMode
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsManager
import com.t8rin.dynamic.theme.ColorSpecVersion
import com.t8rin.dynamic.theme.PaletteStyle
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.widget.palette_selection.getTitle
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.utils.getString
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorSelection
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.t8rin.imagetoolbox.core.ui.widget.editor.AiGenerateImageSheet
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
import kotlin.math.roundToInt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTheme
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFeatures
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDarkMode
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAiImage
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
    val isGeneratingImage by component.isGeneratingImage.collectAsState()
    val activeThemeId = LocalSettingsState.current.activeThemeId
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var pendingDeletePreset by remember { mutableStateOf<AppThemePreset?>(null) }
    // 切换预设时如果有未保存修改，先弹确认
    var pendingSelectPreset by remember { mutableStateOf<AppThemePreset?>(null) }
    // 遮罩透明度是全局设置（滑动时自动保存），单独追踪是否变更，以便使保存按钮可点击
    var overlayAlphaDirty by remember { mutableStateOf(false) }
    // 遮罩透明度滑动即落盘, 不进草稿; 记录进入页面时的原值, "放弃修改"时恢复
    val settingsManager = LocalSettingsManager.current
    val scope = rememberCoroutineScope()
    val currentSettingsState = LocalSettingsState.current
    val originalOverlayAlpha = remember {
        mutableFloatStateOf(currentSettingsState.customBackgroundOverlayAlpha)
    }
    val latestOverlayAlpha by rememberUpdatedState(currentSettingsState.customBackgroundOverlayAlpha)

    /** 放弃修改时把滑动即落盘的遮罩透明度恢复到进入页面时的值 */
    fun restoreOverlayAlphaIfDirty() {
        if (overlayAlphaDirty) {
            scope.launch { settingsManager.setCustomBackgroundOverlayAlpha(originalOverlayAlpha.floatValue) }
        }
    }

    // ── 全局色彩系统(调色板风格 / 对比度 / 色彩规范 / Expressive 动效) ──
    // 与颜色/玻璃同一套语义: 改完立即预览, 取消 / 放弃修改 / 重置 / 切预设时还原。
    // 它们不属于任何主题预设(全局生效), 因此"重置"回到的是进入页面时(或上次保存时)的值。
    val colorSystemSnapshot = remember {
        mutableStateOf(
            AppColorSystem(
                paletteStyle = currentSettingsState.themeStyle,
                contrastLevel = currentSettingsState.themeContrastLevel,
                colorSpec = currentSettingsState.themeColorSpec,
                isExpressiveTheme = currentSettingsState.isExpressiveTheme,
            )
        )
    }
    val currentColorSystem = AppColorSystem(
        paletteStyle = currentSettingsState.themeStyle,
        contrastLevel = currentSettingsState.themeContrastLevel,
        colorSpec = currentSettingsState.themeColorSpec,
        isExpressiveTheme = currentSettingsState.isExpressiveTheme,
    )
    val isColorSystemDirty = currentColorSystem != colorSystemSnapshot.value

    /** 还原色彩系统(取消 / 放弃修改 / 重置 / 切预设) */
    fun discardColorSystemChanges() {
        val snapshot = colorSystemSnapshot.value
        scope.launch {
            settingsManager.setThemeStyle(snapshot.paletteStyle.ordinal)
            settingsManager.setThemeContrast(snapshot.contrastLevel)
            settingsManager.setThemeColorSpec(snapshot.colorSpec.ordinal)
            settingsManager.setExpressiveTheme(snapshot.isExpressiveTheme)
        }
    }

    val hasUnsavedChanges = editingDraft != null &&
        (component.hasDraftChanged() || overlayAlphaDirty || isColorSystemDirty)
    val canReset = editingDraft != null && component.canResetDraft()
    val isSaveAsNew = component.isSaveAsNewMode()

    LaunchedEffect(Unit) {
        component.events.collect { event ->
            when (event) {
                is ThemeSettingsEvent.SaveSuccess -> {
                    overlayAlphaDirty = false
                    // 保存后原值基线更新为当前值, 之后的取消不再回退它
                    originalOverlayAlpha.floatValue = latestOverlayAlpha
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
                    glassBorderAlpha = draft.glassBorderAlpha,
                    // 描边只在页面真的铺了背景层时才画(见 effectiveGlassBorderAlpha),
                    // 所以没有背景层时这一行直接不出现, 不留一个"拉了没反应"的滑杆。
                    hasBackdropLayer = draft.isMeshGradientBgEnabled ||
                        draft.customBackgroundImageUri != null,
                    onGlassmorphismChange = { component.updateDraftGlassmorphism(it) },
                    onLiquidGlassChange = { component.updateDraftLiquidGlass(it) },
                    onGlassAlphaChange = { component.updateDraftGlassBaseAlpha(it) },
                    onGlassBorderAlphaChange = { component.updateDraftGlassBorderAlpha(it) },
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
                    isGeneratingImage = isGeneratingImage,
                    onGenerateBackground = { prompt, onSuccess ->
                        // 登录 + 积分预检:通过后才真正生成(同 text-card AI 生图)
                        ActionUtils.ensureLoginAndCheckPoints(
                            source = ThemeSettingsComponent.POINTS_SOURCE,
                            point = aiImageProcessPointsCost(),
                        ) {
                            component.generateBackgroundImage(prompt, onSuccess = onSuccess)
                        }
                    },
                )
            }

            // ── 全局色彩系统: 调色板风格 / 对比度 / 色彩规范 / Expressive 动效 ──
            // 这几项是即时落盘的全局设置(不属于主题草稿), 放在整页最下面:
            // 日常改主题只关心上面的配色 / 玻璃 / 背景, 色彩系统属于"高级"项。
            ColorSystemCard(
                themeStyle = currentSettingsState.themeStyle,
                contrastLevel = currentSettingsState.themeContrastLevel,
                colorSpec = currentSettingsState.themeColorSpec,
                isExpressiveTheme = currentSettingsState.isExpressiveTheme,
                onThemeStyleChange = { scope.launch { settingsManager.setThemeStyle(it.ordinal) } },
                onContrastChange = { scope.launch { settingsManager.setThemeContrast(it) } },
                onColorSpecChange = { scope.launch { settingsManager.setThemeColorSpec(it.ordinal) } },
                onExpressiveThemeChange = { scope.launch { settingsManager.setExpressiveTheme(it) } },
            )
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
            onSave = {
                // 色彩系统是即时预览(值已落盘), 保存时只把"基线"推进到当前值
                colorSystemSnapshot.value = currentColorSystem
                component.saveDraft()
            },
            extraActions = {
                if (canReset || isColorSystemDirty) {
                    TextButton(
                        onClick = {
                            component.resetDraftToSource()
                            discardColorSystemChanges()
                        }
                    ) {
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
                    restoreOverlayAlphaIfDirty()
                    overlayAlphaDirty = false
                    discardColorSystemChanges()
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
                restoreOverlayAlphaIfDirty()
                overlayAlphaDirty = false
                discardColorSystemChanges()
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    glassBorderAlpha: Float,
    hasBackdropLayer: Boolean,
    onGlassmorphismChange: (Boolean) -> Unit,
    onLiquidGlassChange: (Boolean) -> Unit,
    onGlassAlphaChange: (Float) -> Unit,
    onGlassBorderAlphaChange: (Float) -> Unit,
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
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Text(
                    text = stringResource(R.string.theme_preset_glass_section),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "${(glassBaseAlpha * 100).toInt()}%",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    CustomSlider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = OneBoxDesignSystem.microSpacing),
                        value = glassBaseAlpha,
                        onValueChange = onGlassAlphaChange,
                        valueRange = 0f..1f,
                    )

                    // 描边只对"有背景层"的页面有意义: 纯色底上卡片靠填充色明度差就分得清,
                    // 描边不起作用, 所以没有背景层时整行(标题/说明/滑杆)一并收起。
                    AnimatedVisibility(
                        visible = hasBackdropLayer,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(R.string.theme_preset_glass_border_alpha),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    text = "${(glassBorderAlpha * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Text(
                                text = stringResource(R.string.theme_preset_glass_border_alpha_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            CustomSlider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = OneBoxDesignSystem.microSpacing),
                                value = glassBorderAlpha,
                                onValueChange = onGlassBorderAlphaChange,
                                valueRange = 0f..1f,
                            )
                        }
                    }
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
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    Text(
                        text = stringResource(R.string.mesh_gradient_background),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stringResource(
                                    R.string.theme_preset_preview_gradient,
                                    selectedLabel,
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    isGeneratingImage: Boolean = false,
    onGenerateBackground: (prompt: String, onStarted: () -> Unit) -> Unit = { _, _ -> },
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
    var showAiGenerateSheet by remember { mutableStateOf(false) }

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
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    Text(
                        text = stringResource(R.string.custom_background),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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

            // 操作入口:两格宫格;生成中 AI 格显示进度并禁用重复点击
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
            ) {
                BackgroundActionTile(
                    modifier = Modifier.weight(1f),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                    label = stringResource(R.string.custom_background_pick),
                    onClick = { imagePicker.pickImage() },
                )
                BackgroundActionTile(
                    modifier = Modifier.weight(1f),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiImage,
                    label = stringResource(
                        if (isGeneratingImage) R.string.custom_background_ai_generating
                        else R.string.custom_background_ai_generate
                    ),
                    loading = isGeneratingImage,
                    onClick = { showAiGenerateSheet = true },
                )
            }

            if (hasCustomBg) {
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
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "${(overlayAlpha * 100).toInt()}%",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
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

    // AI 生成背景弹层(文生图;登录/积分预检在 onGenerateBackground 内完成)
    AiGenerateImageSheet(
        visible = showAiGenerateSheet,
        title = stringResource(R.string.custom_background_ai_title),
        editTitle = stringResource(R.string.custom_background_ai_title),
        promptHint = stringResource(R.string.custom_background_ai_hint),
        editPromptHint = stringResource(R.string.custom_background_ai_hint),
        generateLabel = stringResource(
            if (isGeneratingImage) R.string.custom_background_ai_generating
            else R.string.custom_background_ai_action
        ),
        pointsHint = stringResource(
            R.string.custom_background_ai_points_hint,
            aiImageProcessPointsCost(),
        ),
        emptyHint = stringResource(R.string.custom_background_ai_empty),
        currentLabel = "",
        historyLabel = "",
        isGenerating = isGeneratingImage,
        editImage = null,
        onGenerate = { prompt ->
            onGenerateBackground(prompt) { showAiGenerateSheet = false }
        },
        onDismiss = { showAiGenerateSheet = false },
    )
}

/** 自定义背景操作宫格:玻璃小块,图标在上文案在下,loading 时图标换转圈 */
@Composable
private fun BackgroundActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    Column(
        modifier = modifier
            .clip(OneBoxDesignSystem.compactBadgeShape)
            .glassBackground(
                style = GlassStyle.Thin,
                shape = OneBoxDesignSystem.compactBadgeShape,
            )
            .clickable(enabled = !loading, onClick = onClick)
            .padding(vertical = OneBoxDesignSystem.itemSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
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
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Text(
                    text = stringResource(R.string.theme_night_mode_section),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
//  色彩系统卡片(全局: 调色板风格 / 对比度 / 色彩规范 / Expressive 动效)
// ══════════════════════════════════════════════════════════════

@Composable
private fun ColorSystemCard(
    themeStyle: PaletteStyle,
    contrastLevel: Double,
    colorSpec: ColorSpecVersion,
    isExpressiveTheme: Boolean,
    onThemeStyleChange: (PaletteStyle) -> Unit,
    onContrastChange: (Double) -> Unit,
    onColorSpecChange: (ColorSpecVersion) -> Unit,
    onExpressiveThemeChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current

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
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTheme,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Text(
                    text = stringResource(R.string.theme_color_system_section),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = stringResource(R.string.theme_palette_style_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )

            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
            ) {
                PaletteStyle.entries.forEach { style ->
                    FilterChip(
                        selected = style == themeStyle,
                        onClick = { onThemeStyleChange(style) },
                        label = {
                            Text(
                                text = style.getTitle(context),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                            )
                        },
                        leadingIcon = if (style == themeStyle) {
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
                            selected = style == themeStyle,
                        ),
                    )
                }
            }

            Text(
                text = stringResource(R.string.theme_palette_style_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = OneBoxDesignSystem.microSpacing),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.theme_contrast_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = ((contrastLevel * 100).roundToInt() / 100.0).toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            CustomSlider(
                modifier = Modifier.fillMaxWidth(),
                value = contrastLevel.toFloat(),
                onValueChange = { onContrastChange(it.toDouble()) },
                valueRange = -1f..1f,
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = OneBoxDesignSystem.microSpacing),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            )

            SwitchRow(
                label = stringResource(R.string.theme_color_spec_2025),
                checked = colorSpec == ColorSpecVersion.Spec2025,
                onCheckedChange = {
                    onColorSpecChange(
                        if (it) ColorSpecVersion.Spec2025 else ColorSpecVersion.Spec2021
                    )
                },
            )
            Text(
                text = stringResource(R.string.theme_color_spec_2025_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SwitchRow(
                label = stringResource(R.string.theme_expressive_motion),
                checked = isExpressiveTheme,
                onCheckedChange = onExpressiveThemeChange,
            )
            Text(
                text = stringResource(R.string.theme_expressive_motion_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
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
