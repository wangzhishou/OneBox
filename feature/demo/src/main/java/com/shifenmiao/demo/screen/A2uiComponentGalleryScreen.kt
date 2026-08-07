package com.shifenmiao.demo.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.demo.screenLogic.DemoComponent
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalWindowSizeClass
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.a2ui.A2uiContentParser
import com.wanbaohe.a2ui.ui.A2uiSurfaceView
import com.wanbaohe.a2ui.ui.A2uiViewerContext
import kotlinx.serialization.json.Json
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibility

private val PrettyJson = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}

/**
 * A2UI 组件画廊 — 枚举当前渲染器支持的全部 A2UI JSON 组件，并实时渲染预览。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun A2uiComponentGalleryScreen(
    demoComponent: DemoComponent,
    onDismiss: () -> Unit,
) {
    val samples = remember { a2uiSamples() }
    var selectedSample by remember { mutableStateOf<A2uiSample?>(null) }
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = selectedSample?.title ?: "A2UI 组件画廊",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    EnhancedIconButton(
                        onClick = {
                            if (selectedSample != null) {
                                selectedSample = null
                            } else {
                                onDismiss()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        val sample = selectedSample
        if (sample != null) {
            SamplePreviewPane(
                sample = sample,
                demoComponent = demoComponent,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
            )
        } else {
            SampleListPane(
                samples = samples,
                onSelect = { selectedSample = it },
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }
}

@Composable
private fun SampleListPane(
    samples: List<A2uiSample>,
    onSelect: (A2uiSample) -> Unit,
    state: LazyListState,
    modifier: Modifier = Modifier,
) {
    val grouped by remember(samples) {
        derivedStateOf { samples.groupBy { it.category } }
    }

    LazyColumn(
        state = state,
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp,
            vertical = 12.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        grouped.forEach { (category, items) ->
            item(key = "header_$category") {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )
            }
            items(items, key = { it.id }) { sample ->
                SampleListCard(sample = sample, onSelect = { onSelect(sample) })
            }
        }
    }
}

@Composable
private fun SampleListCard(
    sample: A2uiSample,
    onSelect: () -> Unit,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = sample.title,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = sample.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            GlassTonalButton(
                onClick = onSelect,
                style = GlassStyle.Medium,
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibility,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "预览",
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun SamplePreviewPane(
    sample: A2uiSample,
    demoComponent: DemoComponent,
    modifier: Modifier = Modifier,
) {
    val surfaceId = remember(sample.id) {
        "a2ui_gallery_${sample.id}_${System.currentTimeMillis()}"
    }

    DisposableEffect(surfaceId, sample.json) {
        val message = A2uiContentParser.parse(sample.json, surfaceId)
        message?.let { demoComponent.a2uiMessageHandler.handle(it) }
        onDispose { demoComponent.a2uiSurfaceHolder.remove(surfaceId) }
    }

    val viewerContext = remember(
        demoComponent.a2uiSurfaceHolder,
        demoComponent.a2uiActionBus,
        demoComponent.a2uiThemeMapper,
        demoComponent.a2uiRegistry,
    ) {
        A2uiViewerContext(
            surfaceHolder = demoComponent.a2uiSurfaceHolder,
            actionBus = demoComponent.a2uiActionBus,
            themeMapper = demoComponent.a2uiThemeMapper,
            registry = demoComponent.a2uiRegistry,
        )
    }

    val jsonText = remember(sample.json) {
        runCatching {
            PrettyJson.encodeToString(
                PrettyJson.parseToJsonElement(sample.json)
            )
        }.getOrDefault(sample.json)
    }

    val isCompact = LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Compact

    if (isCompact) {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
        ) {
            item { JsonBlock(jsonText = jsonText) }
            item {
                PreviewBlock(
                    surfaceId = surfaceId,
                    viewerContext = viewerContext,
                    modifier = Modifier.heightIn(min = 240.dp, max = 480.dp),
                )
            }
        }
    } else {
        Row(
            modifier = modifier.verticalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            JsonBlock(
                jsonText = jsonText,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp),
            )
            PreviewBlock(
                surfaceId = surfaceId,
                viewerContext = viewerContext,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp)
                    .heightIn(min = 320.dp, max = 640.dp),
            )
        }
    }
}

@Composable
private fun JsonBlock(
    jsonText: String,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "JSON",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 240.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Text(
                    text = jsonText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.2,
                    ),
                    modifier = Modifier
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                )
            }
        }
    }
}

@Composable
private fun PreviewBlock(
    surfaceId: String,
    viewerContext: A2uiViewerContext,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "实时渲染",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
            ) {
                A2uiSurfaceView(
                    surfaceId = surfaceId,
                    viewerContext = viewerContext,
                    modifier = Modifier.imePadding().navigationBarsPadding().fillMaxSize(),
                )
            }
        }
    }
}

private data class A2uiSample(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val json: String,
)

private fun a2uiSamples(): List<A2uiSample> = listOf(
    // ── 布局容器 ──
    A2uiSample(
        id = "column",
        title = "Column",
        category = "布局容器",
        description = "垂直排列子组件，支持 padding、spacing 与玻璃背景",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_column",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "style": "medium",
                    "children": ["title", "body", "hint"]
                  },
                  {
                    "id": "title",
                    "component": "Text",
                    "text": "Column 标题",
                    "style": "titleMedium",
                    "weight": "bold"
                  },
                  {
                    "id": "body",
                    "component": "Text",
                    "text": "子组件在垂直方向依次排列。",
                    "style": "bodyMedium"
                  },
                  {
                    "id": "hint",
                    "component": "Text",
                    "text": "支持 style=medium 毛玻璃背景。",
                    "style": "bodySmall",
                    "color": "#6750A4"
                  }
                ],
                "dataModel": {}
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "row",
        title = "Row",
        category = "布局容器",
        description = "水平排列子组件，支持间距与玻璃背景",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_row",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["label", "row"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "Row 水平排列",
                    "style": "titleMedium"
                  },
                  {
                    "id": "row",
                    "component": "Row",
                    "padding": 12,
                    "spacing": 12,
                    "style": "thin",
                    "children": ["icon", "text", "badge"]
                  },
                  {
                    "id": "icon",
                    "component": "Icon",
                    "name": "Home",
                    "size": 28,
                    "color": "#6750A4"
                  },
                  {
                    "id": "text",
                    "component": "Text",
                    "text": "水平项",
                    "style": "bodyMedium"
                  },
                  {
                    "id": "badge",
                    "component": "Badge",
                    "text": "New",
                    "style": "success"
                  }
                ],
                "dataModel": {}
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "card",
        title = "Card",
        category = "布局容器",
        description = "毛玻璃卡片容器，带默认 16dp 内边距",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_card",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["card"]
                  },
                  {
                    "id": "card",
                    "component": "Card",
                    "padding": 16,
                    "children": ["card_title", "card_body", "card_action"]
                  },
                  {
                    "id": "card_title",
                    "component": "Text",
                    "text": "Card 卡片",
                    "style": "titleSmall",
                    "weight": "bold"
                  },
                  {
                    "id": "card_body",
                    "component": "Text",
                    "text": "Card 会包裹一个毛玻璃容器。",
                    "style": "bodyMedium"
                  },
                  {
                    "id": "card_action",
                    "component": "Button",
                    "label": "卡片按钮",
                    "variant": "tonal"
                  }
                ],
                "dataModel": {}
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "list",
        title = "List",
        category = "布局容器",
        description = "可滚动列表，支持模板绑定从数据模型生成子项",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_list",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["title", "list"]
                  },
                  {
                    "id": "title",
                    "component": "Text",
                    "text": "List + 模板绑定",
                    "style": "titleMedium"
                  },
                  {
                    "id": "list",
                    "component": "List",
                    "padding": 8,
                    "spacing": 8,
                    "children": {
                      "path": "/items",
                      "componentId": "item_template"
                    }
                  },
                  {
                    "id": "item_template",
                    "component": "Card",
                    "padding": 12,
                    "children": ["item_text"]
                  },
                  {
                    "id": "item_text",
                    "component": "Text",
                    "text": { "path": "name" },
                    "style": "bodyMedium"
                  }
                ],
                "dataModel": {
                  "items": [
                    { "name": "Item 1" },
                    { "name": "Item 2" },
                    { "name": "Item 3" }
                  ]
                }
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "spacer",
        title = "Spacer",
        category = "布局容器",
        description = "占用固定宽高的空白间距",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_spacer",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 8,
                    "children": ["top", "spacer", "bottom"]
                  },
                  {
                    "id": "top",
                    "component": "Text",
                    "text": "上方内容",
                    "style": "bodyMedium"
                  },
                  {
                    "id": "spacer",
                    "component": "Spacer",
                    "height": 48
                  },
                  {
                    "id": "bottom",
                    "component": "Text",
                    "text": "下方内容（中间有 48dp 间距）",
                    "style": "bodyMedium"
                  }
                ],
                "dataModel": {}
              }
            }
        """.trimIndent()
    ),

    // ── 展示控件 ──
    A2uiSample(
        id = "text",
        title = "Text",
        category = "展示控件",
        description = "文本展示，支持样式、颜色、字重、对齐与最大行数",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_text",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 8,
                    "children": ["title", "body", "caption"]
                  },
                  {
                    "id": "title",
                    "component": "Text",
                    "text": "Text 标题",
                    "style": "titleLarge",
                    "weight": "bold",
                    "color": "#6750A4"
                  },
                  {
                    "id": "body",
                    "component": "Text",
                    "text": "这是正文内容，支持多行显示与对齐控制。",
                    "style": "bodyMedium",
                    "align": "start"
                  },
                  {
                    "id": "caption",
                    "component": "Text",
                    "text": "小字说明，可设置斜体。",
                    "style": "bodySmall",
                    "italic": true,
                    "maxLines": 1
                  }
                ],
                "dataModel": {}
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "image",
        title = "Image",
        category = "展示控件",
        description = "通过 URL 加载图片，支持高度与缩放模式",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_image",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["label", "image"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "Image 图片",
                    "style": "titleMedium"
                  },
                  {
                    "id": "image",
                    "component": "Image",
                    "src": "https://picsum.photos/seed/a2ui/400/200",
                    "height": 180,
                    "scale": "crop",
                    "description": "示例图片"
                  }
                ],
                "dataModel": {}
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "icon",
        title = "Icon",
        category = "展示控件",
        description = "系统图标，支持名称、大小与颜色",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_icon",
                "components": [
                  {
                    "id": "root",
                    "component": "Row",
                    "padding": 16,
                    "spacing": 24,
                    "children": ["icon_small", "icon_large", "icon_colored"]
                  },
                  {
                    "id": "icon_small",
                    "component": "Icon",
                    "name": "Settings",
                    "size": 24,
                    "description": "设置"
                  },
                  {
                    "id": "icon_large",
                    "component": "Icon",
                    "name": "Star",
                    "size": 48
                  },
                  {
                    "id": "icon_colored",
                    "component": "Icon",
                    "name": "Home",
                    "size": 40,
                    "color": "#FF5722"
                  }
                ],
                "dataModel": {}
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "divider",
        title = "Divider",
        category = "展示控件",
        description = "水平分隔线，可设置粗细与垂直间距",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_divider",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 8,
                    "children": ["top", "divider", "bottom"]
                  },
                  {
                    "id": "top",
                    "component": "Text",
                    "text": "分隔线上方",
                    "style": "bodyMedium"
                  },
                  {
                    "id": "divider",
                    "component": "Divider",
                    "thickness": 2,
                    "padding": 12,
                    "color": "#E0E0E0"
                  },
                  {
                    "id": "bottom",
                    "component": "Text",
                    "text": "分隔线下方",
                    "style": "bodyMedium"
                  }
                ],
                "dataModel": {}
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "tabs",
        title = "Tabs",
        category = "展示控件",
        description = "标签页，子组件的 title/label/text 作为标签文字",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_tabs",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["tabs"]
                  },
                  {
                    "id": "tabs",
                    "component": "Tabs",
                    "children": ["tab1", "tab2", "tab3"]
                  },
                  {
                    "id": "tab1",
                    "component": "Column",
                    "title": "首页",
                    "padding": 12,
                    "children": ["tab1_text"]
                  },
                  {
                    "id": "tab1_text",
                    "component": "Text",
                    "text": "这是首页内容。",
                    "style": "bodyMedium"
                  },
                  {
                    "id": "tab2",
                    "component": "Column",
                    "title": "设置",
                    "padding": 12,
                    "children": ["tab2_text"]
                  },
                  {
                    "id": "tab2_text",
                    "component": "Text",
                    "text": "这是设置内容。",
                    "style": "bodyMedium"
                  },
                  {
                    "id": "tab3",
                    "component": "Column",
                    "title": "关于",
                    "padding": 12,
                    "children": ["tab3_text"]
                  },
                  {
                    "id": "tab3_text",
                    "component": "Text",
                    "text": "这是关于内容。",
                    "style": "bodyMedium"
                  }
                ],
                "dataModel": {}
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "badge",
        title = "Badge",
        category = "展示控件",
        description = "徽标，支持 success/error/warning/info 样式",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_badge",
                "components": [
                  {
                    "id": "root",
                    "component": "Row",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["b1", "b2", "b3", "b4"]
                  },
                  {
                    "id": "b1",
                    "component": "Badge",
                    "text": "成功",
                    "style": "success"
                  },
                  {
                    "id": "b2",
                    "component": "Badge",
                    "text": "错误",
                    "style": "error"
                  },
                  {
                    "id": "b3",
                    "component": "Badge",
                    "text": "警告",
                    "style": "warning"
                  },
                  {
                    "id": "b4",
                    "component": "Badge",
                    "text": "信息",
                    "style": "info"
                  }
                ],
                "dataModel": {}
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "video",
        title = "Video",
        category = "展示控件",
        description = "视频占位组件，可设置 URL 与高度",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_video",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["label", "video"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "Video 视频占位",
                    "style": "titleMedium"
                  },
                  {
                    "id": "video",
                    "component": "Video",
                    "src": "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    "height": 180
                  }
                ],
                "dataModel": {}
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "audio",
        title = "AudioPlayer",
        category = "展示控件",
        description = "音频播放占位组件，可设置 URL 与标签",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_audio",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["label", "audio"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "AudioPlayer 音频",
                    "style": "titleMedium"
                  },
                  {
                    "id": "audio",
                    "component": "AudioPlayer",
                    "src": "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                    "label": "示例音频"
                  }
                ],
                "dataModel": {}
              }
            }
        """.trimIndent()
    ),

    // ── 输入控件 ──
    A2uiSample(
        id = "button",
        title = "Button",
        category = "输入控件",
        description = "按钮，支持 filled/outlined/text/tonal 样式与点击动作",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_button",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["label", "btn_filled", "btn_outlined", "btn_text", "btn_tonal"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "Button 按钮",
                    "style": "titleMedium"
                  },
                  {
                    "id": "btn_filled",
                    "component": "Button",
                    "label": "实心按钮",
                    "variant": "filled",
                    "action": {
                      "event": { "name": "filled_click", "context": { "source": "filled" } }
                    }
                  },
                  {
                    "id": "btn_outlined",
                    "component": "Button",
                    "label": "描边按钮",
                    "variant": "outlined"
                  },
                  {
                    "id": "btn_text",
                    "component": "Button",
                    "label": "文字按钮",
                    "variant": "text"
                  },
                  {
                    "id": "btn_tonal",
                    "component": "Button",
                    "label": "色调按钮",
                    "variant": "tonal"
                  }
                ],
                "dataModel": {}
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "textfield",
        title = "TextField",
        category = "输入控件",
        description = "文本输入框，支持标签、占位符、辅助文字与双向绑定",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_textfield",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["label", "field", "result"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "TextField 输入框",
                    "style": "titleMedium"
                  },
                  {
                    "id": "field",
                    "component": "TextField",
                    "label": "用户名",
                    "placeholder": "请输入用户名",
                    "value": { "path": "/name" },
                    "supportingText": "支持双向绑定"
                  },
                  {
                    "id": "result",
                    "component": "Text",
                    "text": { "path": "/name" },
                    "style": "bodySmall",
                    "color": "#6750A4"
                  }
                ],
                "dataModel": { "name": "A2UI" }
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "checkbox",
        title = "CheckBox",
        category = "输入控件",
        description = "复选框，支持标签与双向绑定",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_checkbox",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["label", "cb1", "cb2"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "CheckBox 复选框",
                    "style": "titleMedium"
                  },
                  {
                    "id": "cb1",
                    "component": "CheckBox",
                    "label": "同意协议",
                    "checked": { "path": "/agree" }
                  },
                  {
                    "id": "cb2",
                    "component": "CheckBox",
                    "label": "禁用状态",
                    "checked": { "path": "/disabled" },
                    "enabled": false
                  }
                ],
                "dataModel": { "agree": true, "disabled": false }
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "switch",
        title = "Switch",
        category = "输入控件",
        description = "开关切换，支持标签与双向绑定",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_switch",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["label", "sw1", "sw2"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "Switch 开关",
                    "style": "titleMedium"
                  },
                  {
                    "id": "sw1",
                    "component": "Switch",
                    "label": "夜间模式",
                    "checked": { "path": "/nightMode" }
                  },
                  {
                    "id": "sw2",
                    "component": "Switch",
                    "label": "禁用开关",
                    "checked": { "path": "/disabled" },
                    "enabled": false
                  }
                ],
                "dataModel": { "nightMode": true, "disabled": false }
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "slider",
        title = "Slider",
        category = "输入控件",
        description = "滑块，支持范围、步长与双向绑定",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_slider",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["label", "slider"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "Slider 滑块",
                    "style": "titleMedium"
                  },
                  {
                    "id": "slider",
                    "component": "Slider",
                    "label": "音量",
                    "value": { "path": "/volume" },
                    "min": 0,
                    "max": 100,
                    "steps": 10
                  }
                ],
                "dataModel": { "volume": 35 }
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "stepper",
        title = "Stepper",
        category = "输入控件",
        description = "数字步进器，支持最小/最大值、步长与双向绑定",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_stepper",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["label", "stepper"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "Stepper 步进器",
                    "style": "titleMedium"
                  },
                  {
                    "id": "stepper",
                    "component": "Stepper",
                    "label": "数量",
                    "value": { "path": "/quantity" },
                    "min": 0,
                    "max": 10,
                    "step": 1
                  }
                ],
                "dataModel": { "quantity": 2 }
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "choice_picker",
        title = "ChoicePicker",
        category = "输入控件",
        description = "分段选择器，支持对象选项与双向绑定",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_choice_picker",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["label", "picker"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "ChoicePicker 分段选择",
                    "style": "titleMedium"
                  },
                  {
                    "id": "picker",
                    "component": "ChoicePicker",
                    "options": [
                      { "label": "邮件", "value": "email" },
                      { "label": "电话", "value": "phone" },
                      { "label": "短信", "value": "sms" }
                    ],
                    "selected": { "path": "/contact" }
                  }
                ],
                "dataModel": { "contact": "email" }
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "radio_group",
        title = "RadioGroup",
        category = "输入控件",
        description = "单选按钮组，支持选项数组与双向绑定",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_radio_group",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["radio"]
                  },
                  {
                    "id": "radio",
                    "component": "RadioGroup",
                    "label": "RadioGroup 单选",
                    "options": ["选项 A", "选项 B", "选项 C"],
                    "value": { "path": "/choice" }
                  }
                ],
                "dataModel": { "choice": "选项 A" }
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "date_input",
        title = "DateInput",
        category = "输入控件",
        description = "日期选择器，支持 date/datetime/daterange 模式",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_date_input",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["label", "date", "datetime", "range"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "DateInput 日期选择",
                    "style": "titleMedium"
                  },
                  {
                    "id": "date",
                    "component": "DateInput",
                    "label": "选择日期",
                    "value": { "path": "/date" },
                    "mode": "date"
                  },
                  {
                    "id": "datetime",
                    "component": "DateInput",
                    "label": "选择日期时间",
                    "value": { "path": "/datetime" },
                    "mode": "datetime"
                  },
                  {
                    "id": "range",
                    "component": "DateInput",
                    "label": "选择日期段",
                    "value": { "path": "/dateRange" },
                    "mode": "daterange"
                  }
                ],
                "dataModel": { "date": "", "datetime": "", "dateRange": "" }
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "time_input",
        title = "TimeInput",
        category = "输入控件",
        description = "时间选择器，支持 time/timerange 模式",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_time_input",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["label", "time", "range"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "TimeInput 时间选择",
                    "style": "titleMedium"
                  },
                  {
                    "id": "time",
                    "component": "TimeInput",
                    "label": "选择时间",
                    "value": { "path": "/time" },
                    "mode": "time"
                  },
                  {
                    "id": "range",
                    "component": "TimeInput",
                    "label": "选择时间段",
                    "value": { "path": "/timeRange" },
                    "mode": "timerange"
                  }
                ],
                "dataModel": { "time": "", "timeRange": "" }
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "color_picker",
        title = "ColorPicker",
        category = "输入控件",
        description = "颜色选择器，支持颜色数组与双向绑定",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_color_picker",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["label", "picker"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "ColorPicker 颜色选择",
                    "style": "titleMedium"
                  },
                  {
                    "id": "picker",
                    "component": "ColorPicker",
                    "label": "选择主题色",
                    "colors": ["#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#009688", "#4CAF50", "#FFEB3B", "#FF9800"],
                    "value": { "path": "/themeColor" }
                  }
                ],
                "dataModel": { "themeColor": "#2196F3" }
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "location_picker",
        title = "LocationPicker",
        category = "输入控件",
        description = "位置选择器，使用系统城市选择器，支持省/市/区多级",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_location_picker",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["label", "picker"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "LocationPicker 位置选择",
                    "style": "titleMedium"
                  },
                  {
                    "id": "picker",
                    "component": "LocationPicker",
                    "label": "选择城市",
                    "value": { "path": "/city" },
                    "layer": 3
                  }
                ],
                "dataModel": { "city": "" }
              }
            }
        """.trimIndent()
    ),

    // ── 选择器 ──
    A2uiSample(
        id = "row_selector",
        title = "RowSelector",
        category = "选择器",
        description = "横向标签选择器，支持多选、最大选择数与双向绑定",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_row_selector",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["label", "selector", "result"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "RowSelector 多选",
                    "style": "titleMedium"
                  },
                  {
                    "id": "selector",
                    "component": "RowSelector",
                    "label": "风格标签",
                    "value": { "path": "/styleTags" },
                    "maxSelected": 2,
                    "options": [
                      { "label": "精炼", "value": "精炼" },
                      { "label": "详细", "value": "详细" },
                      { "label": "幽默", "value": "幽默" }
                    ]
                  },
                  {
                    "id": "result",
                    "component": "Text",
                    "text": { "path": "/styleTags" },
                    "style": "bodySmall",
                    "color": "#6750A4"
                  }
                ],
                "dataModel": { "styleTags": ["精炼"] }
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "column_selector",
        title = "ColumnSelector",
        category = "选择器",
        description = "纵向标签选择器，默认单选，支持自定义输入子项",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_column_selector",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["label", "selector", "custom_input"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "ColumnSelector 单选 + 自定义",
                    "style": "titleMedium"
                  },
                  {
                    "id": "selector",
                    "component": "ColumnSelector",
                    "label": "语气",
                    "value": { "path": "/tone" },
                    "selectIndex": 0,
                    "options": [
                      { "label": "正式", "value": "正式" },
                      { "label": "口语", "value": "口语" },
                      { "label": "自定义", "value": "自定义", "kind": "custom" }
                    ],
                    "children": ["custom_input"]
                  },
                  {
                    "id": "custom_input",
                    "component": "TextField",
                    "label": "自定义语气",
                    "value": { "path": "/customTone" }
                  }
                ],
                "dataModel": { "customTone": "" }
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "grid_selector",
        title = "GridSelector",
        category = "选择器",
        description = "网格标签选择器，支持列数与多选",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_grid_selector",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["label", "selector"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "GridSelector 网格",
                    "style": "titleMedium"
                  },
                  {
                    "id": "selector",
                    "component": "GridSelector",
                    "label": "兴趣标签",
                    "value": { "path": "/gridTags" },
                    "columns": 3,
                    "maxSelected": 3,
                    "options": [
                      { "label": "旅行", "value": "旅行" },
                      { "label": "读书", "value": "读书" },
                      { "label": "音乐", "value": "音乐" },
                      { "label": "电影", "value": "电影" },
                      { "label": "运动", "value": "运动" },
                      { "label": "摄影", "value": "摄影" }
                    ]
                  }
                ],
                "dataModel": { "gridTags": ["旅行"] }
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "list_selector",
        title = "ListSelector",
        category = "选择器",
        description = "列表形式选择器，单选显示单选按钮，多选显示复选框",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_list_selector",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["label", "selector"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "ListSelector 单选列表",
                    "style": "titleMedium"
                  },
                  {
                    "id": "selector",
                    "component": "ListSelector",
                    "label": "优先级",
                    "value": { "path": "/priority" },
                    "options": [
                      { "label": "高", "value": "高" },
                      { "label": "中", "value": "中" },
                      { "label": "低", "value": "低" }
                    ]
                  }
                ],
                "dataModel": { "priority": "高" }
              }
            }
        """.trimIndent()
    ),

    // ── 反馈控件 ──
    A2uiSample(
        id = "progress",
        title = "Progress",
        category = "反馈控件",
        description = "进度指示器，支持 circular/linear 与 0-100 进度值",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_progress",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 16,
                    "children": ["label", "circular", "linear"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "Progress 进度",
                    "style": "titleMedium"
                  },
                  {
                    "id": "circular",
                    "component": "Progress",
                    "type": "circular",
                    "progress": 65
                  },
                  {
                    "id": "linear",
                    "component": "Progress",
                    "type": "linear",
                    "progress": 65
                  }
                ],
                "dataModel": {}
              }
            }
        """.trimIndent()
    ),
    A2uiSample(
        id = "modal",
        title = "Modal",
        category = "反馈控件",
        description = "模态对话框，由数据模型 visible 路径控制显隐",
        json = """
            {
              "createSurface": {
                "surfaceId": "demo_modal",
                "components": [
                  {
                    "id": "root",
                    "component": "Column",
                    "padding": 16,
                    "spacing": 12,
                    "children": ["label", "modal"]
                  },
                  {
                    "id": "label",
                    "component": "Text",
                    "text": "Modal 模态框（visible=true）",
                    "style": "titleMedium"
                  },
                  {
                    "id": "modal",
                    "component": "Modal",
                    "title": "提示",
                    "visible": { "path": "/showModal" },
                    "dismissText": "关闭",
                    "confirmText": "确认",
                    "action": {
                      "event": { "name": "modal_confirm", "context": {} }
                    },
                    "children": ["modal_body"]
                  },
                  {
                    "id": "modal_body",
                    "component": "Text",
                    "text": "这是 Modal 的内容区域。"
                  }
                ],
                "dataModel": { "showModal": true }
              }
            }
        """.trimIndent()
    )
)
