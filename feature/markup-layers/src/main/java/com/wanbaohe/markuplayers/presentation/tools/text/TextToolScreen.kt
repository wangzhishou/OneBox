package com.wanbaohe.markuplayers.presentation.tools.text

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.domain.model.Outline
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboardArrowDown
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.t8rin.imagetoolbox.core.settings.presentation.model.UiFontFamily
import com.t8rin.imagetoolbox.core.settings.presentation.model.toUiFont
import com.t8rin.imagetoolbox.core.ui.theme.ProvideTypography
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorSelectionRow
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedChip
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.markuplayers.presentation.components.EditBox
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedSlider
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTopAppBar
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.transparencyChecker
import com.t8rin.imagetoolbox.core.ui.widget.text.marquee
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.presentation.render.LayerPreviewRenderers
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import kotlin.math.min
import kotlin.math.roundToInt
import com.t8rin.imagetoolbox.core.resources.R as CoreR

/**
 * 文字全屏工具页(设计稿「文字排版工具」):
 * 顶栏(✕ 取消 / 标题 / ✓ 确认)+ 画布(全部图层静态渲染,仅被编辑文字图层
 * 挂 EditBox 可变换)+ 右侧浮动操作(复制/删除)+ 底部 Tab 面板
 * (键盘 / 样式 / 花字占位 / 动画占位)。
 *
 * 整个页面是一次编辑会话:进入时 [MarkupLayersComponent.beginTextEditSession]
 * 已记录快照,页内全部修改走会话内 transient 接口,✓/✕ 分别经
 * [MarkupLayersComponent.commitLayerEditSession]/[MarkupLayersComponent.cancelLayerEditSession]
 * 统一结算成一次历史记录(或彻底回滚)。
 */
@Composable
fun TextToolScreen(component: MarkupLayersComponent) {
    val layer = component.layers.firstOrNull { it.id == component.editSessionLayerId }
    val textType = layer?.type as? LayerType.Text
    if (layer == null || textType == null) {
        // 防御:会话图层不存在(理论上不会走到),清会话退出
        LaunchedEffect(Unit) {
            component.cancelLayerEditSession()
            component.setActiveTool(null)
        }
        return
    }

    val onConfirm = {
        component.commitLayerEditSession()
        component.setActiveTool(null)
    }
    val onCancel = {
        component.cancelLayerEditSession()
        component.setActiveTool(null)
    }
    BackHandler(onBack = onCancel)

    val updateText: ((LayerType.Text) -> LayerType.Text) -> Unit = { transform ->
        component.updateLayerInEditSession(layer.id) { current ->
            (current.type as? LayerType.Text)
                ?.let { current.copy(type = transform(it)) }
                ?: current
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TextTopBar(
            isNewLayer = component.isEditSessionNewLayer,
            onCancel = onCancel,
            onConfirm = onConfirm
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            TextCanvas(
                component = component,
                editingLayerId = layer.id,
                modifier = Modifier.fillMaxSize()
            )
            TextFloatingActions(
                onDuplicate = { component.duplicateLayerInEditSession(layer.id) },
                onDelete = {
                    component.removeLayerInEditSession(layer.id)
                    component.commitLayerEditSession()
                    component.setActiveTool(null)
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            )
        }
        TextBottomPanel(
            text = textType,
            startOnKeyboard = component.isEditSessionNewLayer,
            onTextChange = updateText
        )
    }
}

@Composable
private fun TextTopBar(
    isNewLayer: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    EnhancedTopAppBar(
        title = {
            Text(
                text = stringResource(
                    if (isNewLayer) R.string.markup_add_text else R.string.markup_edit_text
                ),
                modifier = Modifier.marquee()
            )
        },
        navigationIcon = {
            EnhancedIconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.markup_cancel)
                )
            }
        },
        actions = {
            EnhancedIconButton(onClick = onConfirm) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.markup_confirm),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

/** 画布:底图 + 全部图层;仅被编辑文字图层挂 EditBox 响应手势,其余静态渲染 */
@Composable
private fun TextCanvas(
    component: MarkupLayersComponent,
    editingLayerId: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = component.bitmap ?: return@Box
        val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val fitScale = min(
                constraints.maxWidth / bitmap.width.toFloat(),
                constraints.maxHeight / bitmap.height.toFloat()
            )
            val canvasWidthPx = bitmap.width * fitScale
            val canvasHeightPx = bitmap.height * fitScale
            val density = LocalDensity.current

            Box(
                modifier = Modifier.size(
                    width = with(density) { canvasWidthPx.toDp() },
                    height = with(density) { canvasHeightPx.toDp() }
                )
            ) {
                Picture(
                    model = imageBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .matchParentSize()
                        .clipToBounds()
                        .transparencyChecker()
                )
                BoxWithConstraints(
                    modifier = Modifier.matchParentSize()
                ) {
                    val layerCanvasWidth = constraints.maxWidth.toFloat()
                    val layerCanvasHeight = constraints.maxHeight.toFloat()
                    component.layers.forEach { layer ->
                        key(layer.id) {
                            if (layer.id == editingLayerId) {
                                EditBox(
                                    transform = layer.transform,
                                    isSelected = true,
                                    onSelect = {},
                                    onTransformEnd = { newTransform ->
                                        component.updateLayerInEditSession(layer.id) {
                                            it.copy(transform = newTransform)
                                        }
                                    }
                                ) {
                                    LayerPreviewRenderers.Content(
                                        layer = layer,
                                        canvasWidthPx = layerCanvasWidth,
                                        canvasHeightPx = layerCanvasHeight
                                    )
                                }
                            } else {
                                StaticLayer(
                                    layer = layer,
                                    canvasWidth = layerCanvasWidth,
                                    canvasHeight = layerCanvasHeight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 静态渲染非编辑图层:套用 transform,不响应任何手势(同画笔页) */
@Composable
private fun BoxWithConstraintsScope.StaticLayer(
    layer: MarkupLayer,
    canvasWidth: Float,
    canvasHeight: Float,
) {
    val transform = layer.transform
    if (!transform.visible) return
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .graphicsLayer {
                scaleX = transform.scale
                scaleY = transform.scale
                rotationZ = transform.rotation
                translationX = (transform.centerX - 0.5f) * canvasWidth
                translationY = (transform.centerY - 0.5f) * canvasHeight
                alpha = transform.alpha
            },
        contentAlignment = Alignment.Center
    ) {
        LayerPreviewRenderers.Content(
            layer = layer,
            canvasWidthPx = canvasWidth,
            canvasHeightPx = canvasHeight
        )
    }
}

/** 画布右侧浮动操作:复制(编辑对象切到副本)/删除(删除并退页) */
@Composable
private fun TextFloatingActions(
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .clip(ShapeDefaults.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f))
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        FloatingActionItem(
            icon = Icons.Rounded.ContentCopy,
            labelRes = R.string.markup_text_duplicate,
            onClick = onDuplicate
        )
        FloatingActionItem(
            icon = Icons.Outlined.Delete,
            labelRes = R.string.markup_delete,
            onClick = onDelete
        )
    }
}

@Composable
private fun FloatingActionItem(
    icon: ImageVector,
    @StringRes labelRes: Int,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(ShapeDefaults.default)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(labelRes),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

private enum class TextToolTab(@StringRes val titleRes: Int) {
    Keyboard(R.string.markup_text_tab_keyboard),
    Style(R.string.markup_text_tab_style),
    WordArt(R.string.markup_text_tab_wordart),
    Animation(R.string.markup_text_tab_animation)
}

@Composable
private fun TextBottomPanel(
    text: LayerType.Text,
    startOnKeyboard: Boolean,
    onTextChange: ((LayerType.Text) -> LayerType.Text) -> Unit,
) {
    var selectedTab by rememberSaveable {
        mutableStateOf(if (startOnKeyboard) TextToolTab.Keyboard else TextToolTab.Style)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        TextToolTabRow(
            selected = selectedTab,
            onSelect = { selectedTab = it }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            when (selectedTab) {
                TextToolTab.Keyboard -> TextKeyboardTab(
                    text = text,
                    onTextChange = onTextChange
                )
                TextToolTab.Style -> TextStyleTab(
                    text = text,
                    onTextChange = onTextChange
                )
                else -> Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = stringResource(R.string.markup_coming_soon),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TextToolTabRow(
    selected: TextToolTab,
    onSelect: (TextToolTab) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        TextToolTab.entries.forEach { tab ->
            val isSelected = tab == selected
            val contentColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else MaterialTheme.colorScheme.onSurfaceVariant
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(ShapeDefaults.small)
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(tab.titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    color = contentColor,
                    maxLines = 1
                )
                Spacer(Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(2.dp)
                        .clip(ShapeDefaults.small)
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else Color.Transparent
                        )
                )
            }
        }
    }
}

/** 「键盘」Tab:多行输入,实时更新图层文本 */
@Composable
private fun TextKeyboardTab(
    text: LayerType.Text,
    onTextChange: ((LayerType.Text) -> LayerType.Text) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    // 新建空文字图层进入页面时自动弹键盘
    val autoFocus = remember { text.text.isEmpty() }
    OutlinedTextField(
        value = text.text,
        onValueChange = { value -> onTextChange { it.copy(text = value) } },
        placeholder = { Text(stringResource(R.string.markup_text_input_hint)) },
        shape = ShapeDefaults.default,
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
    )
    if (autoFocus) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
}

/** 「样式」Tab:字体/字号/颜色/对齐/行距/字间距/加粗等装饰/背景/描边 */
@Composable
private fun TextStyleTab(
    text: LayerType.Text,
    onTextChange: ((LayerType.Text) -> LayerType.Text) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        FontRow(
            selectedFont = text.font,
            onFontChange = { font -> onTextChange { it.copy(font = font) } }
        )

        TextSliderRow(
            label = stringResource(R.string.markup_text_font_size),
            value = text.fontSizeRatio,
            valueRange = 0.01f..0.3f,
            valueText = "${(text.fontSizeRatio * 100).roundToInt()}%",
            onValueChange = { value -> onTextChange { it.copy(fontSizeRatio = value) } }
        )

        SettingRow(label = stringResource(R.string.markup_text_color)) {
            ColorSelectionRow(
                value = Color(text.color),
                onValueChange = { color ->
                    onTextChange { it.copy(color = color.toArgb()) }
                },
                allowAlpha = false,
                modifier = Modifier.weight(1f)
            )
        }

        AlignmentRow(
            alignment = text.alignment,
            onAlignmentChange = { alignment ->
                onTextChange { it.copy(alignment = alignment) }
            }
        )

        TextSliderRow(
            label = stringResource(R.string.markup_text_line_height),
            value = text.lineHeight,
            valueRange = 1f..2f,
            valueText = formatDecimal((text.lineHeight * 10).roundToInt() / 10f),
            onValueChange = { value -> onTextChange { it.copy(lineHeight = value) } }
        )
        TextSliderRow(
            label = stringResource(R.string.markup_text_letter_spacing),
            value = text.letterSpacingEm,
            valueRange = -0.05f..0.3f,
            valueText = formatDecimal((text.letterSpacingEm * 100).roundToInt() / 100f),
            onValueChange = { value -> onTextChange { it.copy(letterSpacingEm = value) } }
        )

        DecorationRow(
            decorations = text.decorations,
            onToggle = { decoration ->
                onTextChange {
                    it.copy(
                        decorations = if (decoration in it.decorations) {
                            it.decorations - decoration
                        } else it.decorations + decoration
                    )
                }
            }
        )

        BackgroundRow(
            backgroundColor = text.backgroundColor,
            onBackgroundChange = { color ->
                onTextChange { it.copy(backgroundColor = color) }
            }
        )

        OutlineRow(
            outline = text.outline,
            onOutlineChange = { outline ->
                onTextChange { it.copy(outline = outline) }
            }
        )
        Spacer(Modifier.height(4.dp))
    }
}

/** 字体行:常用字体 chips 横排(可滑动),末尾下拉展示全部字体 */
@Composable
private fun FontRow(
    selectedFont: FontType?,
    onFontChange: (FontType?) -> Unit,
) {
    val fonts = UiFontFamily.entries
    val selected = selectedFont.toUiFont()
    var menuExpanded by remember { mutableStateOf(false) }
    SettingRow(label = stringResource(R.string.markup_text_font)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            items(fonts) { font ->
                ProvideTypography(font) {
                    EnhancedChip(
                        selected = font == selected,
                        onClick = { onFontChange(font.type) },
                        selectedColor = MaterialTheme.colorScheme.secondary,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = font.name ?: stringResource(CoreR.string.system),
                            maxLines = 1
                        )
                    }
                }
            }
        }
        Box {
            EnhancedIconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Outlined.LineKeyboardArrowDown,
                    contentDescription = stringResource(R.string.markup_text_more_fonts)
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                fonts.forEach { font ->
                    DropdownMenuItem(
                        text = {
                            ProvideTypography(font) {
                                Text(
                                    text = font.name
                                        ?: stringResource(CoreR.string.system),
                                    maxLines = 1
                                )
                            }
                        },
                        onClick = {
                            onFontChange(font.type)
                            menuExpanded = false
                        }
                    )
                }
            }
        }
    }
}

/** 对齐行:左/居中/右/两端 4 分段按钮(图标为手绘对齐线段) */
@Composable
private fun AlignmentRow(
    alignment: LayerType.Text.TextAlignment,
    onAlignmentChange: (LayerType.Text.TextAlignment) -> Unit,
) {
    SettingRow(label = stringResource(R.string.markup_text_alignment)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .weight(1f)
                .clip(ShapeDefaults.default)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(4.dp)
        ) {
            LayerType.Text.TextAlignment.entries.forEach { item ->
                val isSelected = item == alignment
                val contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else MaterialTheme.colorScheme.onSurfaceVariant
                val description = stringResource(
                    when (item) {
                        LayerType.Text.TextAlignment.Left -> R.string.markup_text_align_left
                        LayerType.Text.TextAlignment.Center -> R.string.markup_text_align_center
                        LayerType.Text.TextAlignment.Right -> R.string.markup_text_align_right
                        LayerType.Text.TextAlignment.Justify -> R.string.markup_text_align_justify
                    }
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(ShapeDefaults.small)
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else Color.Transparent
                        )
                        .clickable { onAlignmentChange(item) }
                        .semantics { contentDescription = description }
                        .padding(vertical = 8.dp)
                ) {
                    AlignmentGlyph(
                        alignment = item,
                        tint = contentColor
                    )
                }
            }
        }
    }
}

/** 对齐示意图标:三条横线按对齐方式排布(两端对齐为三条等长) */
@Composable
private fun AlignmentGlyph(
    alignment: LayerType.Text.TextAlignment,
    tint: Color,
) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val strokeWidth = 2.dp.toPx()
        val widthFractions = when (alignment) {
            LayerType.Text.TextAlignment.Justify -> listOf(1f, 1f, 1f)
            else -> listOf(1f, 0.7f, 0.85f)
        }
        val yFractions = listOf(0.2f, 0.5f, 0.8f)
        widthFractions.forEachIndexed { index, widthFraction ->
            val lineWidth = size.width * widthFraction
            val startX = when (alignment) {
                LayerType.Text.TextAlignment.Left,
                LayerType.Text.TextAlignment.Justify -> 0f
                LayerType.Text.TextAlignment.Center -> (size.width - lineWidth) / 2f
                LayerType.Text.TextAlignment.Right -> size.width - lineWidth
            }
            val y = size.height * yFractions[index]
            drawLine(
                color = tint,
                start = Offset(startX, y),
                end = Offset(startX + lineWidth, y),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

/** 样式按钮行:B 加粗 / I 斜体 / U 下划线 / S 删除线(toggle) */
@Composable
private fun DecorationRow(
    decorations: Set<LayerType.Text.Decoration>,
    onToggle: (LayerType.Text.Decoration) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        DecorationChip(
            glyph = "B",
            labelRes = R.string.markup_text_bold,
            selected = LayerType.Text.Decoration.Bold in decorations,
            onClick = { onToggle(LayerType.Text.Decoration.Bold) },
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        DecorationChip(
            glyph = "I",
            labelRes = R.string.markup_text_italic,
            selected = LayerType.Text.Decoration.Italic in decorations,
            onClick = { onToggle(LayerType.Text.Decoration.Italic) },
            fontStyle = FontStyle.Italic,
            modifier = Modifier.weight(1f)
        )
        DecorationChip(
            glyph = "U",
            labelRes = R.string.markup_text_underline,
            selected = LayerType.Text.Decoration.Underline in decorations,
            onClick = { onToggle(LayerType.Text.Decoration.Underline) },
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.weight(1f)
        )
        DecorationChip(
            glyph = "S",
            labelRes = R.string.markup_text_strikethrough,
            selected = LayerType.Text.Decoration.LineThrough in decorations,
            onClick = { onToggle(LayerType.Text.Decoration.LineThrough) },
            textDecoration = TextDecoration.LineThrough,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DecorationChip(
    glyph: String,
    @StringRes labelRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    textDecoration: TextDecoration? = null,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(ShapeDefaults.default)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .then(
                if (selected) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = ShapeDefaults.default
                    )
                } else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Text(
            text = glyph,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = fontWeight,
                fontStyle = fontStyle,
                textDecoration = textDecoration
            ),
            color = contentColor,
            maxLines = 1
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1
        )
    }
}

/** 文字背景色:开关 + 颜色行(支持透明);完全透明视为无背景 */
@Composable
private fun BackgroundRow(
    backgroundColor: Int,
    onBackgroundChange: (Int) -> Unit,
) {
    SettingRow(label = stringResource(R.string.markup_text_background)) {
        Spacer(Modifier.weight(1f))
        Switch(
            checked = backgroundColor != 0,
            onCheckedChange = { checked ->
                onBackgroundChange(if (checked) 0x66000000.toInt() else 0)
            }
        )
    }
    if (backgroundColor != 0) {
        ColorSelectionRow(
            value = Color(backgroundColor),
            onValueChange = { onBackgroundChange(it.toArgb()) },
            allowAlpha = true
        )
    }
}

/** 描边:开关 + 颜色行 + 粗细滑杆(粗细为相对底图宽的比例) */
@Composable
private fun OutlineRow(
    outline: Outline?,
    onOutlineChange: (Outline?) -> Unit,
) {
    SettingRow(label = stringResource(R.string.markup_text_outline)) {
        Spacer(Modifier.weight(1f))
        Switch(
            checked = outline != null,
            onCheckedChange = { checked ->
                onOutlineChange(
                    if (checked) {
                        Outline(color = 0xFFFFFFFF.toInt(), width = 0.004f)
                    } else null
                )
            }
        )
    }
    if (outline != null) {
        ColorSelectionRow(
            value = Color(outline.color),
            onValueChange = { onOutlineChange(outline.copy(color = it.toArgb())) },
            allowAlpha = false
        )
        TextSliderRow(
            label = stringResource(R.string.markup_shape_stroke_width),
            value = outline.width,
            valueRange = 0.001f..0.02f,
            valueText = "${formatDecimal((outline.width * 1000).roundToInt() / 10f)}%",
            onValueChange = { onOutlineChange(outline.copy(width = it)) }
        )
    }
}

/** 标签在左的设置行(与设计稿行式一致) */
@Composable
private fun SettingRow(
    label: String,
    control: @Composable RowScope.() -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            modifier = Modifier.widthIn(min = 52.dp)
        )
        Spacer(Modifier.width(8.dp))
        control()
    }
}

@Composable
private fun TextSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueText: String,
    onValueChange: (Float) -> Unit,
) {
    SettingRow(label = label) {
        EnhancedSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valueText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.widthIn(min = 40.dp)
        )
    }
}

/** 小数展示:去掉尾随的 ".0"(0.0 → "0",1.2 → "1.2") */
private fun formatDecimal(value: Float): String = value.toString().removeSuffix(".0")
