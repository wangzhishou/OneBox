package com.wanbaohe.setting.theme.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.interfaces.singleton.AppContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.settings.domain.ThemeSettingService
import com.t8rin.imagetoolbox.core.settings.domain.model.AppThemePreset
import com.t8rin.imagetoolbox.core.settings.domain.model.GradientBackgroundStyle
import com.t8rin.imagetoolbox.core.settings.domain.model.NightMode
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.theme.localizedName
import com.shifenmiao.core.R as CoreR
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import kotlin.math.abs
import kotlin.random.Random

// ══════════════════════════════════════════════════════════════
//  事件
// ══════════════════════════════════════════════════════════════

sealed interface ThemeSettingsEvent {
    data class SaveSuccess(val presetName: String) : ThemeSettingsEvent
    data class SaveFailed(val presetName: String) : ThemeSettingsEvent
    data class DeleteSuccess(val presetName: String) : ThemeSettingsEvent
    data class DeleteFailed(val presetName: String) : ThemeSettingsEvent
}

// ══════════════════════════════════════════════════════════════
//  编辑模式
// ══════════════════════════════════════════════════════════════

/**
 * 编辑模式语义：
 * - [EditingUser]  — 正在编辑用户自建主题，保存时原地更新（同 ID）。
 * - [CreatingNew]  — 正在新建主题或以某个预设为蓝本，保存时生成新 UUID。
 *                    [forkedFrom] 为 null 表示纯新建，非 null 表示基于某预设复制。
 */
sealed interface ThemeEditMode {
    data class EditingUser(val sourcePreset: AppThemePreset) : ThemeEditMode
    data class CreatingNew(val forkedFrom: AppThemePreset?) : ThemeEditMode
}

// ══════════════════════════════════════════════════════════════
//  Component
// ══════════════════════════════════════════════════════════════

class ThemeSettingsComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val themeSettingService: ThemeSettingService,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    /** 进入页面时的激活主题，用于"取消/退出"时恢复现场 */
    private var originalPreset: AppThemePreset? = null

    private val _editingDraft = MutableStateFlow<EditingDraft?>(null)
    val editingDraft: StateFlow<EditingDraft?> = _editingDraft

    private val _editMode = MutableStateFlow<ThemeEditMode?>(null)
    val editMode: StateFlow<ThemeEditMode?> = _editMode

    private val _events = Channel<ThemeSettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val allThemes: StateFlow<List<AppThemePreset>> = themeSettingService.observeThemes()
        .stateIn(componentScope, SharingStarted.Lazily, themeSettingService.themesSnapshot)

    init {
        componentScope.launch {
            val active = themeSettingService.getCurrentTheme()
            if (originalPreset == null) {
                originalPreset = active
            }
            _editMode.value = active.toInitialMode()
            _editingDraft.value = active.toDraft()
        }
    }

    // ══════════════════════════════════════════════════════════
    //  选中 / 新建 / 复制
    // ══════════════════════════════════════════════════════════

    /** 点击预设卡片：立即预览 + 进入对应编辑模式 */
    fun selectPresetForEditing(preset: AppThemePreset) {
        _editMode.value = preset.toInitialMode()
        _editingDraft.value = preset.toDraft()
        componentScope.launch { themeSettingService.applyThemePreset(preset) }
    }

    /** 复制任意预设（内置或用户）为新主题蓝本，进入新建模式 */
    fun startCopyTheme(preset: AppThemePreset) {
        _editMode.value = ThemeEditMode.CreatingNew(forkedFrom = preset)
        val copySuffix = AppContext.getString(CoreR.string.theme_preset_copy_suffix)
        _editingDraft.value = preset.toDraft().copy(name = preset.localizedName() + copySuffix)
        applyDraftLive()
    }

    /** 从空白随机配色开始新建 */
    fun startCreateTheme() {
        _editMode.value = ThemeEditMode.CreatingNew(forkedFrom = null)
        _editingDraft.value = EditingDraft(
            name = DEFAULT_THEME_NAME,
            primaryColor = randomHarmoniousColor(),
            secondaryColor = randomHarmoniousColor(),
            tertiaryColor = randomHarmoniousColor(),
            surfaceColor = randomLightColor(),
        )
        applyDraftLive()
    }

    fun deletePreset(id: String) {
        val presetName = allThemes.value.find { it.id == id }?.name ?: ""
        componentScope.launch {
            try {
                themeSettingService.deleteUserTheme(id)
                val mode = _editMode.value
                if (mode is ThemeEditMode.EditingUser && mode.sourcePreset.id == id) {
                    selectPresetForEditing(AppThemePreset.Default)
                }
                _events.send(ThemeSettingsEvent.DeleteSuccess(presetName))
            } catch (_: Exception) {
                _events.send(ThemeSettingsEvent.DeleteFailed(presetName))
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  草稿更新
    // ═════════���════════════════════════════════════════════════

    fun updateDraftName(name: String) {
        _editingDraft.value = _editingDraft.value?.copy(name = name)
    }

    fun updateDraftPrimaryColor(argb: Int) {
        // 手动改色即脱离动态取色, 预览/保存都按静态配色走
        _editingDraft.value = _editingDraft.value?.copy(primaryColor = argb, isDynamicColors = false)
        applyDraftLive()
    }

    fun updateDraftSecondaryColor(argb: Int) {
        _editingDraft.value = _editingDraft.value?.copy(secondaryColor = argb, isDynamicColors = false)
        applyDraftLive()
    }

    fun updateDraftTertiaryColor(argb: Int) {
        _editingDraft.value = _editingDraft.value?.copy(tertiaryColor = argb, isDynamicColors = false)
        applyDraftLive()
    }

    fun updateDraftSurfaceColor(argb: Int) {
        _editingDraft.value = _editingDraft.value?.copy(surfaceColor = argb, isDynamicColors = false)
        applyDraftLive()
    }

    fun updateDraftGlassmorphism(enabled: Boolean) {
        _editingDraft.value = _editingDraft.value?.copy(isGlassAlphaEnabled = enabled)
        applyDraftLive()
    }

    fun updateDraftLiquidGlass(enabled: Boolean) {
        _editingDraft.value = _editingDraft.value?.copy(isLiquidGlassEnabled = enabled)
        applyDraftLive()
    }

    fun updateDraftMeshGradient(enabled: Boolean) {
        _editingDraft.value = _editingDraft.value?.copy(isMeshGradientBgEnabled = enabled)
        applyDraftLive()
    }

    fun updateDraftGradientStyle(style: GradientBackgroundStyle) {
        _editingDraft.value = _editingDraft.value?.copy(gradientStyle = style)
        applyDraftLive()
    }

    fun updateDraftGlassBaseAlpha(alpha: Float) {
        _editingDraft.value = _editingDraft.value?.copy(glassBaseAlpha = alpha)
        applyDraftLive()
    }

    fun updateDraftCustomBackgroundUri(uri: String?) {
        _editingDraft.value = _editingDraft.value?.copy(customBackgroundImageUri = uri)
        applyDraftLive()
    }

    fun updateDraftNightMode(nightMode: NightMode) {
        _editingDraft.value = _editingDraft.value?.copy(nightMode = nightMode)
        applyDraftLive()
    }

    // ══════════════════════════════════════════════════════════
    //  脏检查 / 保存 / 重置 / 退出
    // ══════════════════════════════════════════════════════════

    /**
     * 是否有"未保存的修改"：
     * - EditingUser  → draft 与已保存状态不同
     * - CreatingNew  → 纯新建始终视为有内容待保存; 以某预设为蓝本时与蓝本对比,
     *                  只点了一下卡片预览不算"有修改"
     */
    fun hasDraftChanged(): Boolean {
        val draft = _editingDraft.value ?: return false
        return when (val mode = _editMode.value) {
            is ThemeEditMode.EditingUser -> draft != mode.sourcePreset.toDraft()
            is ThemeEditMode.CreatingNew -> mode.forkedFrom?.let { draft != it.toDraft() } ?: true
            null -> false
        }
    }

    /**
     * 是否可以"重置到来源"：
     * - EditingUser  → 有修改时可以重置到上次保存的状态
     * - CreatingNew  → 有基础预设（forkedFrom）且 draft 与之不同时可以重置
     */
    fun canResetDraft(): Boolean {
        val draft = _editingDraft.value ?: return false
        return when (val mode = _editMode.value) {
            is ThemeEditMode.EditingUser -> draft != mode.sourcePreset.toDraft()
            is ThemeEditMode.CreatingNew -> mode.forkedFrom != null && draft != mode.forkedFrom.toDraft()
            null -> false
        }
    }

    /**
     * 当前是否处于"新建/另存为"模式（保存按钮应显示"保存为新主题"）
     */
    fun isSaveAsNewMode(): Boolean = _editMode.value is ThemeEditMode.CreatingNew

    /** 将草稿重置回来源状态（EditingUser → 上次保存的值；CreatingNew → forkedFrom 的值） */
    fun resetDraftToSource() {
        val target: EditingDraft = when (val mode = _editMode.value) {
            is ThemeEditMode.EditingUser -> mode.sourcePreset.toDraft()
            is ThemeEditMode.CreatingNew -> mode.forkedFrom?.toDraft() ?: return
            null -> return
        }
        _editingDraft.value = target
        applyDraftLive()
    }

    fun saveDraft() {
        val draft = _editingDraft.value ?: return
        val mode = _editMode.value

        val preset: AppThemePreset = when (mode) {
            is ThemeEditMode.EditingUser -> draft.toPreset(id = mode.sourcePreset.id)
            is ThemeEditMode.CreatingNew, null -> draft.toPreset(id = "user_${UUID.randomUUID()}")
        }

        componentScope.launch {
            try {
                themeSettingService.saveUserTheme(preset)
                themeSettingService.applyThemePreset(preset)
                // 保存后切换为 EditingUser 模式，后续保存原地更新（不再新建）
                _editMode.value = ThemeEditMode.EditingUser(preset)
                originalPreset = preset
                _editingDraft.value = preset.toDraft()
                _events.send(ThemeSettingsEvent.SaveSuccess(preset.name))
            } catch (_: Exception) {
                _events.send(ThemeSettingsEvent.SaveFailed(preset.name))
            }
        }
    }

    fun restoreAndGoBack() {
        componentScope.launch {
            // 进入页面时的激活主题可能已在会话期间被删除, 校验后再恢复, 避免 activeThemeId 悬垂
            val original = originalPreset
                ?.takeIf { o -> themeSettingService.listThemes().any { it.id == o.id } }
                ?: AppThemePreset.Default
            try {
                themeSettingService.applyThemePreset(original)
            } catch (_: Exception) {
                themeSettingService.applyThemePreset(AppThemePreset.Default)
            }
            originalPreset = null
            _editingDraft.value = null
            onGoBack()
        }
    }

    // ══════════════════════════════════════════════════════════
    //  私有工具方法
    // ══════════════════════════════════════════════════════════

    private fun applyDraftLive() {
        val draft = _editingDraft.value ?: return
        // 预览不改写 ACTIVE_THEME_ID, 避免哨兵 id 持久化后进程死亡造成 activeThemeId 悬垂
        val tempPreset = draft.toPreset(id = AppThemePreset.CUSTOM_ID)
        componentScope.launch { themeSettingService.previewThemePreset(tempPreset) }
    }

    /** 根据 isBuiltin 决定初始编辑模式 */
    private fun AppThemePreset.toInitialMode(): ThemeEditMode =
        if (isBuiltin) ThemeEditMode.CreatingNew(forkedFrom = this)
        else ThemeEditMode.EditingUser(sourcePreset = this)

    private fun AppThemePreset.toDraft(): EditingDraft {
        val colors = AppThemePreset.parseColorTuple(colorTupleString)
        return EditingDraft(
            name = name,
            primaryColor = colors.getOrNull(0) ?: DEFAULT_PRIMARY,
            secondaryColor = colors.getOrNull(1) ?: DEFAULT_SECONDARY,
            tertiaryColor = colors.getOrNull(2) ?: DEFAULT_TERTIARY,
            surfaceColor = colors.getOrNull(3) ?: DEFAULT_SURFACE,
            isDynamicColors = isDynamicColors,
            isGlassAlphaEnabled = isGlassmorphismEnabled,
            isLiquidGlassEnabled = isLiquidGlassEnabled,
            isMeshGradientBgEnabled = isMeshGradientBackgroundEnabled,
            gradientStyle = gradientBackgroundStyle,
            glassBaseAlpha = glassBaseAlpha,
            customBackgroundImageUri = customBackgroundImageUri,
            nightMode = nightMode,
        )
    }

    private fun EditingDraft.toPreset(id: String): AppThemePreset = AppThemePreset(
        id = id,
        name = name.ifBlank { DEFAULT_THEME_NAME },
        // 动态取色主题不携带色元组(复制"千色千面"时保留动态语义, 预览不会变成无关静态配色)
        colorTupleString = if (isDynamicColors) ""
        else listOf(primaryColor, secondaryColor, tertiaryColor, surfaceColor)
            .joinToString("*"),
        nightMode = nightMode,
        isDynamicColors = isDynamicColors,
        isGlassmorphismEnabled = isGlassAlphaEnabled,
        isLiquidGlassEnabled = isLiquidGlassEnabled,
        isMeshGradientBackgroundEnabled = isMeshGradientBgEnabled,
        gradientBackgroundStyle = gradientStyle,
        glassBaseAlpha = glassBaseAlpha,
        customBackgroundImageUri = customBackgroundImageUri,
        isBuiltin = false,
    )

    companion object {
        private const val DEFAULT_PRIMARY = -16735934
        private const val DEFAULT_SECONDARY = -14046121
        private const val DEFAULT_TERTIARY = -8367522
        private const val DEFAULT_SURFACE = -591619
        const val DEFAULT_THEME_NAME = "自定义主题"

        private fun randomHarmoniousColor(): Int {
            val hue = Random.nextFloat() * 360f
            val saturation = 0.4f + Random.nextFloat() * 0.3f
            val lightness = 0.4f + Random.nextFloat() * 0.2f
            return hslToArgb(hue, saturation, lightness)
        }

        private fun randomLightColor(): Int {
            val hue = Random.nextFloat() * 360f
            val saturation = 0.05f + Random.nextFloat() * 0.15f
            val lightness = 0.92f + Random.nextFloat() * 0.06f
            return hslToArgb(hue, saturation, lightness)
        }

        private fun hslToArgb(h: Float, s: Float, l: Float): Int {
            val c = (1f - abs(2f * l - 1f)) * s
            val x = c * (1f - abs((h / 60f) % 2f - 1f))
            val m = l - c / 2f
            val (r, g, b) = when {
                h < 60f -> Triple(c, x, 0f)
                h < 120f -> Triple(x, c, 0f)
                h < 180f -> Triple(0f, c, x)
                h < 240f -> Triple(0f, x, c)
                h < 300f -> Triple(x, 0f, c)
                else -> Triple(c, 0f, x)
            }
            val ri = ((r + m) * 255f).toInt().coerceIn(0, 255)
            val gi = ((g + m) * 255f).toInt().coerceIn(0, 255)
            val bi = ((b + m) * 255f).toInt().coerceIn(0, 255)
            return (0xFF shl 24) or (ri shl 16) or (gi shl 8) or bi
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): ThemeSettingsComponent
    }
}

data class EditingDraft(
    val name: String = "",
    val primaryColor: Int = -16735934,
    val secondaryColor: Int = -14046121,
    val tertiaryColor: Int = -8367522,
    val surfaceColor: Int = -591619,
    val isDynamicColors: Boolean = false,
    val isGlassAlphaEnabled: Boolean = true,
    val isLiquidGlassEnabled: Boolean = false,
    val isMeshGradientBgEnabled: Boolean = true,
    val gradientStyle: GradientBackgroundStyle = GradientBackgroundStyle.Sunset,
    val glassBaseAlpha: Float = 1.0f,
    val customBackgroundImageUri: String? = null,
    val nightMode: NightMode = NightMode.System,
)
