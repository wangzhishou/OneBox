package com.shifenmiao.common.ui.ai

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.shifenmiao.common.R as CommonR
import com.shifenmiao.core.R
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTextFieldVisualPreset
import com.t8rin.imagetoolbox.core.resources.icons.Search
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFeatures


/** 模型列表布局模式：Grid（网格）或 List（卡片列表） */
enum class ModelLayoutMode { Grid, List }

/** 内容区固定高度，避免 Grid/List 切换时弹窗跳动 */
private val CONTENT_AREA_HEIGHT = 480.dp

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
        var layoutMode by rememberSaveable { mutableStateOf(ModelLayoutMode.List) }
        var selectedFilter by rememberSaveable { mutableStateOf(ENGINE_FILTER_ALL) }
        var searchQuery by rememberSaveable(visible) { mutableStateOf("") }

        val filteredModelsWithEngine by remember(
            allEngines,
            modelsByProvider,
            selectedFilter,
            searchQuery,
            selectedEngineName,
            selectedModelName,
        ) {
            derivedStateOf {
                buildFilteredModels(
                    allEngines = allEngines,
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
                allEngines = allEngines,
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.dimens.spaceLarge, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LayoutToggleButton(layoutMode = layoutMode, onToggle = onLayoutToggle)

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

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onDismissRequest) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                contentDescription = stringResource(id = R.string.button_close),
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
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
        visualPreset = GlassTextFieldVisualPreset.Expressive,
        placeholder = {
            Text(
                text = stringResource(R.string.ai_model_search_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                ToolbarIconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = AppTheme.colors.getOutlinedTextFieldColors()
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
                imageVector = if (grid) Icons.AutoMirrored.Filled.ViewList else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFeatures,
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
        when (mode) {
            ModelLayoutMode.List -> ModelCardList(
                models = models,
                selectedModelName = selectedModelName,
                selectedEngineName = selectedEngineName,
                onModelClick = onModelClick
            )

            ModelLayoutMode.Grid -> ModelGridContent(
                models = models,
                selectedModelName = selectedModelName,
                selectedEngineName = selectedEngineName,
                onModelClick = onModelClick
            )
        }
    }
}

@Composable
private fun ModelCardList(
    models: List<Pair<AiEngine, AiModel>>,
    selectedModelName: String,
    selectedEngineName: String,
    onModelClick: (AiEngine, AiModel) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            horizontal = AppTheme.dimens.spaceLarge,
            vertical = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(
            items = models,
            key = { (engine, model) -> "${engine.identityKey()}_${model.id}" }
        ) { (engine, model) ->
            val isSelected =
                engine.identityKey() == selectedEngineName && model.name == selectedModelName
            ModelCard(
                engine = engine,
                model = model,
                isSelected = isSelected,
                onClick = { onModelClick(engine, model) }
            )
        }
    }
}

@Composable
private fun ModelCard(
    engine: AiEngine,
    model: AiModel,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val accent = resolveProviderAccentColor(model)
    val containerColor = if (isSelected) {
        accent.copy(alpha = 0.10f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.55f)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModelProviderIcon(
                engine = engine,
                model = model,
                modifier = Modifier.size(40.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = model.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    ProviderBadge(engine = engine, model = model, isSelected = isSelected)
                }

                if (model.description.isNotBlank()) {
                    Text(
                        text = model.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EngineInfoLabel(
                        title = stringResource(CommonR.string.ai_model_protocol_label),
                        value = resolveProtocolLabel(engine.requestProtocol)
                    )
                    EngineInfoLabel(
                        title = stringResource(CommonR.string.ai_model_route_label),
                        value = if (engine.isDetestPassed) {
                            stringResource(CommonR.string.ai_model_route_direct)
                        } else {
                            stringResource(CommonR.string.ai_model_route_proxy)
                        }
                    )
                }
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = accent,
                    modifier = Modifier.size(8.dp)
                ) {}
            }
        }
    }
}

@Composable
private fun resolveProtocolLabel(protocol: AiRequestProtocol): String {
    return when (protocol) {
        AiRequestProtocol.OPENAI_COMPATIBLE -> stringResource(CommonR.string.ai_model_protocol_openai)
        AiRequestProtocol.RESPONSES_COMPATIBLE -> stringResource(CommonR.string.ai_model_protocol_responses)
        AiRequestProtocol.ANTHROPIC_COMPATIBLE -> stringResource(CommonR.string.ai_model_protocol_anthropic)
        AiRequestProtocol.OWN_PROXY -> stringResource(CommonR.string.ai_model_protocol_proxy)
        AiRequestProtocol.LOCAL_ON_DEVICE -> stringResource(CommonR.string.ai_model_protocol_local_on_device)
    }
}

@Composable
private fun EngineInfoLabel(title: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ModelTagRow(
    model: AiModel,
    maxVisible: Int,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        resolveModelTags(model).take(maxVisible).forEach { (tagText, tagColor) ->
            ModelTagBadge(tagText = tagText, tagColor = tagColor)
        }
    }
}

@Composable
private fun ModelTagBadge(
    tagText: String,
    tagColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tagColor.copy(alpha = 0.16f),
        shadowElevation = 0.dp
    ) {
        Text(
            text = tagText,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
            ),
            color = tagColor.copy(alpha = 0.92f),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ModelProviderIcon(
    engine: AiEngine,
    model: AiModel,
    modifier: Modifier = Modifier,
) {
    val accent = resolveProviderAccentColor(model)
    val monogram = resolveProviderMonogram(engine, model)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = accent.copy(alpha = 0.12f),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = monogram,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = accent,
            )
        }
    }
}

@Composable
private fun ProviderBadge(
    engine: AiEngine,
    model: AiModel,
    isSelected: Boolean,
) {
    val accent = resolveProviderAccentColor(model)
    Text(
        text = resolveProviderLabel(engine, model),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
        ),
        color = if (isSelected) accent else accent.copy(alpha = 0.60f),
        maxLines = 1,
    )
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
            add(stringResource(R.string.free) to Color(0xFF6B7280))
        } else if (model.basePoints >= 1f) {
            add(stringResource(R.string.ai_model_not_free) to Color(0xFF6B7280))
        }
        if (isEmpty()) {
            add(stringResource(R.string.ai_model_tag_balanced) to MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun ModelGridContent(
    models: List<Pair<AiEngine, AiModel>>,
    selectedModelName: String,
    selectedEngineName: String,
    onModelClick: (AiEngine, AiModel) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 80.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.dimens.spaceLarge),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(count = models.size, key = { idx ->
            val (e, m) = models[idx]
            "${e.identityKey()}_${m.id}"
        }) { idx ->
            val (engine, model) = models[idx]
            val isSelected =
                engine.identityKey() == selectedEngineName && model.name == selectedModelName
            ModelGridItem(
                engine = engine,
                model = model,
                isSelected = isSelected,
                onClick = { onModelClick(engine, model) }
            )
        }
    }
}

@Composable
fun ModelGridItem(
    engine: AiEngine,
    model: AiModel,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
) {
    val accent = resolveProviderAccentColor(model)
    val containerColor = if (isSelected) {
        accent.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.55f)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ModelProviderIcon(
                engine = engine,
                model = model,
                modifier = Modifier.size(32.dp),
            )

            Text(
                text = model.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (isSelected) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = accent,
                    modifier = Modifier.size(6.dp)
                ) {}
            }
        }
    }
}

@Composable
fun ModelGridItem(
    text: String = "",
    onClick: () -> Unit = {},
    isSelected: Boolean = false,
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = if (isSelected) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
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
    val engines = if (selectedFilter == ENGINE_FILTER_ALL) {
        allEngines
    } else {
        allEngines.filter { it.identityKey() == selectedFilter }
    }
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
    }
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

@Composable
private fun resolveProviderAccentColor(model: AiModel): Color {
    return when (model.provider.value.lowercase()) {
        "doubao" -> Color(0xFF6D5DF6)
        "tencent" -> Color(0xFF4F46E5)
        "deepseek" -> Color(0xFF4A3FF0)
        "qwen" -> Color(0xFF0F766E)
        "kimi" -> Color(0xFF7C3AED)
        "openai" -> Color(0xFF10A37F)
        else -> MaterialTheme.colorScheme.primary
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

