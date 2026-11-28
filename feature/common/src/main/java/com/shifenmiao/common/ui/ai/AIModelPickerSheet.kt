/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.common.ui.ai

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.core.R
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.base.ui.card.TonalCardPaletteDefaults
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.enhanced.EnhancedSheetHeader
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.resources.icons.Search
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQuickTiles
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.t8rin.imagetoolbox.core.resources.icons.line.LineViewList
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.Refresh


/** 模型卡片排布：Grid 每行 3 张，List 每行 2 张（单卡更宽）。同一套卡片。 */
enum class ModelLayoutMode { Grid, List }

/** 内容区固定高度，避免 Grid/List 切换时弹窗跳动 */
private val CONTENT_AREA_HEIGHT = 480.dp

private val RAPID_NAME_HINTS = listOf("flash", "mini", "lite", "haiku", "nano")
private val FLAGSHIP_NAME_HINTS = listOf("pro", "opus", "ultra", "flagship")

@Composable
fun AIModelsPickerBottomSheet(
    visible: Boolean,
    allEngines: List<AiEngine>,
    modelsByProvider: Map<String, List<AiModel>>,
    selectedEngineName: String,
    selectedModelName: String,
    onSelected: (AiEngine, AiModel) -> Unit,
    onDismiss: (Boolean) -> Unit,
    title: String? = null,
    showRefresh: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    showSettings: Boolean = false,
    onSettings: () -> Unit = {},
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss(false) },
        dragHandle = {}
    ) {
        val preferGrid = LocalSettingsState.current.groupOptionsByTypes
        var layoutMode by remember {
            mutableStateOf(if (preferGrid) ModelLayoutMode.Grid else ModelLayoutMode.List)
        }
        var selectedFilter by rememberSaveable { mutableStateOf(ENGINE_FILTER_ALL) }
        var searchQuery by rememberSaveable(visible) { mutableStateOf("") }

        // 决策类协议（Jev/Pikafish）只在象棋专用选择器出现，不进聊天/通用模型面板
        val chatEngines = remember(allEngines) {
            allEngines.filter { !it.requestProtocol.isNonChat }
        }
        val filteredModelsWithEngine by remember(
            chatEngines,
            modelsByProvider,
            selectedFilter,
            searchQuery,
            selectedEngineName,
            selectedModelName,
        ) {
            derivedStateOf {
                buildFilteredModels(
                    allEngines = chatEngines,
                    modelsByProvider = modelsByProvider,
                    selectedFilter = selectedFilter,
                    searchQuery = searchQuery,
                ).sortedByDescending { (engine, model) ->
                    engine.identityKey() == selectedEngineName && model.name == selectedModelName
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            SheetHeader(
                title = title ?: stringResource(id = R.string.ai_bottom_sheet_title),
                onDismissRequest = { onDismiss(false) },
                layoutMode = layoutMode,
                onLayoutToggle = { layoutMode = it },
                showRefresh = showRefresh,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                showSettings = showSettings,
                onSettings = onSettings,
            )

            Spacer(modifier = Modifier.height(8.dp))

            ModelSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.dimens.spaceLarge)
            )

            Spacer(modifier = Modifier.height(12.dp))
            EngineFilterChips(
                allEngines = chatEngines,
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
            )

            Spacer(modifier = Modifier.height(8.dp))

            ModelContent(
                models = filteredModelsWithEngine,
                selectedModelName = selectedModelName,
                selectedEngineName = selectedEngineName,
                layoutMode = layoutMode,
                emptyText = stringResource(R.string.ai_model_search_empty),
                onModelClick = { engine, model ->
                    onSelected(engine.copy(model = model), model)
                    onDismiss(true)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CONTENT_AREA_HEIGHT)
            )
        }
    }
}

@Composable
private fun SheetHeader(
    title: String,
    onDismissRequest: () -> Unit,
    layoutMode: ModelLayoutMode,
    onLayoutToggle: (ModelLayoutMode) -> Unit,
    showRefresh: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    showSettings: Boolean = false,
    onSettings: () -> Unit = {},
) {
    // 统一头部:DragHandle + 居中标题 + 右上角关闭;布局切换挂左,刷新/设置挂右
    EnhancedSheetHeader(
        title = title,
        onClose = onDismissRequest,
        startContent = {
            LayoutToggleButton(layoutMode = layoutMode, onToggle = onLayoutToggle)
        },
        endContent = {
            if (showRefresh) {
                ToolbarIconButton(
                    onClick = onRefresh,
                    enabled = !isRefreshing,
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (showSettings) {
                ToolbarIconButton(onClick = onSettings) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

@Composable
private fun ModelSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassOutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(text = stringResource(R.string.ai_model_search_hint))
        },
        leadingIcon = {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Search,
                contentDescription = null,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                ToolbarIconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        contentDescription = null,
                    )
                }
            }
        },
        singleLine = true,
    )
}

@Composable
private fun ToolbarIconButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(40.dp),
        content = content,
    )
}

@Composable
private fun LayoutToggleButton(
    layoutMode: ModelLayoutMode,
    onToggle: (ModelLayoutMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isGrid = layoutMode == ModelLayoutMode.Grid
    ToolbarIconButton(
        onClick = { onToggle(if (isGrid) ModelLayoutMode.List else ModelLayoutMode.Grid) },
        modifier = modifier
    ) {
        AnimatedContent(
            targetState = isGrid,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "layout_toggle"
        ) { grid ->
            Icon(
                imageVector = if (grid) {
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineViewList
                } else {
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQuickTiles
                },
                contentDescription = if (grid) {
                    stringResource(R.string.ai_switch_to_list)
                } else {
                    stringResource(R.string.ai_switch_to_grid)
                },
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ModelContent(
    models: List<Pair<AiEngine, AiModel>>,
    selectedModelName: String,
    selectedEngineName: String,
    layoutMode: ModelLayoutMode,
    emptyText: String,
    onModelClick: (AiEngine, AiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (models.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.dimens.spaceLarge),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    AnimatedContent(
        targetState = layoutMode,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "model_layout",
        modifier = modifier
    ) { mode ->
        val columns = if (mode == ModelLayoutMode.List) 2 else 3
        val spacious = mode == ModelLayoutMode.List
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTheme.dimens.spaceLarge, vertical = 8.dp),
        ) {
            FlexBox(
                modifier = Modifier.fillMaxWidth(),
                config = {
                    wrap(FlexWrap.Wrap)
                    maxItemsInEachLine(columns)
                    columnGap(8.dp)
                    rowGap(8.dp)
                },
            ) {
                models.forEach { (engine, model) ->
                    val isSelected = engine.identityKey() == selectedEngineName &&
                        model.name == selectedModelName
                    ModelPickCard(
                        engine = engine,
                        model = model,
                        isSelected = isSelected,
                        spacious = spacious,
                        onClick = { onModelClick(engine, model) },
                        modifier = Modifier.flex {
                            grow(1f)
                            basis(0f)
                        },
                    )
                }
                val remainder = models.size % columns
                if (remainder != 0) {
                    repeat(columns - remainder) {
                        Spacer(
                            Modifier.flex {
                                grow(1f)
                                basis(0f)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelPickCard(
    engine: AiEngine,
    model: AiModel,
    isSelected: Boolean,
    spacious: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = TonalCardPaletteDefaults.forIndex(
        (engine.identityKey() + ":" + model.name).hashCode(),
    )
    val padH = if (spacious) 14.dp else 10.dp
    val padV = if (spacious) 14.dp else 12.dp
    val iconSize = if (spacious) 40.dp else 32.dp
    val titleStyle = if (spacious) {
        MaterialTheme.typography.bodyMedium
    } else {
        MaterialTheme.typography.labelMedium
    }

    GlassCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = palette.containerColor,
            contentColor = palette.titleColor,
        ),
        style = if (isSelected) GlassStyle.Thick else TonalCardPaletteDefaults.containerStyle,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = padH, vertical = padV),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ModelProviderIcon(
                    engine = engine,
                    model = model,
                    modifier = Modifier.size(iconSize),
                )
                Text(
                    text = model.title,
                    style = titleStyle.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                ModelMetaRow(engine = engine, model = model)
            }
            if (isSelected) {
                SelectedMark(
                    color = palette.accentColor,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                )
            }
        }
    }
}

@Composable
private fun ModelMetaRow(
    engine: AiEngine,
    model: AiModel,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (engine.usesProxyRoute()) {
            ModelBadge(text = model.pointsMultiplierText())
        }
        ModelBadge(text = modelTierLabel(model))
    }
}

/** 倍率旁的短标签:名字里的 Pro/Opus 优先算旗舰,Flash 或低倍率算极速,其余用能力标签。 */
@Composable
private fun modelTierLabel(model: AiModel): String {
    val key = "${model.title} ${model.name}".lowercase()
    if (FLAGSHIP_NAME_HINTS.any { key.contains(it) }) {
        return stringResource(R.string.ai_model_tier_flagship)
    }
    val rapid = model.isFast ||
        model.basePoints < 0.5f ||
        RAPID_NAME_HINTS.any { key.contains(it) }
    if (rapid) return stringResource(R.string.ai_model_tier_rapid)
    return resolveModelTags(model).first().first
}

/** 与服务列表同一套胶囊:主色容器 + 玻璃薄底,不另配一套标签色。 */
@Composable
private fun ModelBadge(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .glassBackground(
                style = GlassStyle.Thin,
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(50),
                borderWidth = 0.dp,
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        maxLines = 1,
    )
}

@Composable
private fun SelectedMark(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(18.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = ModelLayoutIcons.Check,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.surface,
        )
    }
}

@Composable
private fun ModelProviderIcon(
    engine: AiEngine,
    model: AiModel,
    modifier: Modifier = Modifier,
) {
    val brandIcon = providerBrandIcon(model.provider.value)

    Box(
        modifier = modifier.glassBackground(
            style = GlassStyle.Medium,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(10.dp),
            borderWidth = 0.dp,
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (brandIcon != null) {
            Icon(
                imageVector = brandIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxSize(0.55f),
            )
        } else {
            Text(
                text = resolveProviderMonogram(engine, model),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun resolveModelTags(model: AiModel): List<Pair<String, Color>> {
    return buildList {
        if (model.canReasoning) {
            add(stringResource(R.string.ai_model_tag_reasoning) to Color(0xFFF08A5D))
        }
        if (model.isCode) {
            add(stringResource(R.string.ai_model_tag_code) to Color(0xFF4F46E5))
        }
        if (model.isFast) {
            add(stringResource(R.string.ai_model_tag_fast) to Color(0xFF6366F1))
        }
        if (model.canImage) {
            add(stringResource(R.string.ai_model_tag_vision) to Color(0xFF7B61FF))
        }
        if (model.canNetwork) {
            add(stringResource(R.string.ai_model_tag_context) to Color(0xFF3D8B7A))
        }
        if (model.free) {
            add(stringResource(R.string.price_free) to Color(0xFF6B7280))
        } else if (model.basePoints >= 1f) {
            add(stringResource(R.string.ai_model_not_free) to Color(0xFF6B7280))
        }
        if (isEmpty()) {
            add(stringResource(R.string.ai_model_tag_balanced) to MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
fun ModelGridItem(
    engine: AiEngine,
    model: AiModel,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    ModelPickCard(
        engine = engine,
        model = model,
        isSelected = isSelected,
        spacious = false,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun ModelGridItem(
    text: String = "",
    onClick: () -> Unit = {},
    isSelected: Boolean = false,
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    GlassSurface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = if (isSelected) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.onPrimaryContainer)
        } else {
            null
        },
        style = GlassStyle.None,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = if (isSelected) {
                    Modifier.basicMarquee(
                        iterations = Int.MAX_VALUE,
                        spacing = MarqueeSpacing(30.dp),
                        velocity = 30.dp,
                        repeatDelayMillis = 1000
                    )
                } else {
                    Modifier
                }
            )
        }
    }
}

private fun buildFilteredModels(
    allEngines: List<AiEngine>,
    modelsByProvider: Map<String, List<AiModel>>,
    selectedFilter: String,
    searchQuery: String,
): List<Pair<AiEngine, AiModel>> {
    val normalizedQuery = searchQuery.trim().lowercase()
    // 决策类协议（Jev/Pikafish）只在象棋专用选择器出现，不进聊天/通用模型面板
    val engines = (if (selectedFilter == ENGINE_FILTER_ALL) {
        allEngines
    } else {
        allEngines.filter { it.identityKey() == selectedFilter }
    }).filter { !it.requestProtocol.isNonChat }
    return engines.flatMap { engine ->
        val models = modelsByProvider[engine.name.lowercase()] ?: emptyList()
        models
            .filter { model ->
                model.matchesSearch(
                    engine = engine,
                    normalizedQuery = normalizedQuery
                )
            }
            .map { model -> engine to model }
    }.distinctBy { (engine, model) -> "${engine.identityKey()}_${model.name}" }
}

private fun AiModel.matchesSearch(
    engine: AiEngine,
    normalizedQuery: String,
): Boolean {
    if (normalizedQuery.isBlank()) return true

    val searchableValues = listOf(
        title,
        name,
        description,
        engine.title,
        engine.name,
        provider.value,
        resolveProviderLabel(engine, this),
    ).map { it.trim().lowercase() }

    val capabilityKeywords = buildList {
        if (canReasoning) addAll(listOf("reasoning", "推理", "思考"))
        if (isCode) addAll(listOf("code", "代码", "编程"))
        if (isFast) addAll(listOf("fast", "快速", "speed"))
        if (canImage) addAll(listOf("vision", "image", "图片", "视觉"))
        if (canNetwork) addAll(listOf("network", "联网", "搜索", "context", "长文"))
        if (free) addAll(listOf("free", "免费"))
    }

    return searchableValues.any { it.contains(normalizedQuery) } ||
            capabilityKeywords.any { it.contains(normalizedQuery) }
}

private fun resolveProviderLabel(
    engine: AiEngine,
    model: AiModel,
): String {
    return when (model.provider.value.lowercase()) {
        "doubao" -> "BYTEDANCE"
        "tencent" -> "TENCENT"
        "deepseek" -> "DEEPSEEK"
        "qwen" -> "QWEN"
        "kimi" -> "KIMI"
        "openai" -> "OPENAI"
        else -> engine.name.uppercase()
    }
}

private fun resolveProviderMonogram(
    engine: AiEngine,
    model: AiModel,
): String {
    val label = resolveProviderLabel(engine, model)
    return label.filter { it.isLetter() }.take(2).ifBlank { label.take(2) }
}

@Composable
fun PrimaryAlphaButton(
    text: String = "",
    onClick: () -> Unit = {},
    isSelected: Boolean = false,
) = ModelGridItem(text = text, onClick = onClick, isSelected = isSelected)

