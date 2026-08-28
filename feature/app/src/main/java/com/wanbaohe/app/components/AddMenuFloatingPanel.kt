package com.wanbaohe.app.components

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.shapes.BubbleShape
import com.t8rin.imagetoolbox.core.resources.icons.ChatPlus
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAgent
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAiChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBlessingWall
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckCircleOutline
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCodeEditor
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMarkdownEdit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMarkupLayers
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTextCard
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNote
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePrompt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineScanQrCode
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSchedule
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassDense
import com.shifenmiao.core.R as CoreR

/**
 * 快捷操作浮动面板数据类
 */
private data class AddMenuItem(
    val icon: ImageVector,
    val label: Int,
    val onClick: () -> Unit
)

private enum class AddMenuItemTheme {
    PRIMARY,
    SECONDARY,
    TERTIARY,
    SURFACE,
}

private fun addMenuItemThemeForIndex(index: Int): AddMenuItemTheme = when (index % 4) {
    0 -> AddMenuItemTheme.PRIMARY
    1 -> AddMenuItemTheme.SECONDARY
    2 -> AddMenuItemTheme.TERTIARY
    else -> AddMenuItemTheme.SURFACE
}

@Composable
private fun addMenuItemBackground(theme: AddMenuItemTheme): Color = when (theme) {
    AddMenuItemTheme.PRIMARY -> MaterialTheme.colorScheme.primaryContainer
    AddMenuItemTheme.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer
    AddMenuItemTheme.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer
    AddMenuItemTheme.SURFACE -> MaterialTheme.colorScheme.surfaceContainer
}

@Composable
private fun addMenuItemContentColor(theme: AddMenuItemTheme): Color = when (theme) {
    AddMenuItemTheme.PRIMARY -> MaterialTheme.colorScheme.primary
    AddMenuItemTheme.SECONDARY -> MaterialTheme.colorScheme.secondary
    AddMenuItemTheme.TERTIARY -> MaterialTheme.colorScheme.tertiary
    AddMenuItemTheme.SURFACE -> MaterialTheme.colorScheme.onSurfaceVariant
}

/**
 * 底部导航栏 ADD 按钮的可展开浮动面板。
 *
 * 点击 ADD 按钮后，在底部导航栏上方弹出一个毛玻璃面板，
 * 显示 2×2 网格的快捷操作入口（二维码、记事本、待办事项、智能体），
 * 中间有菱形指示器连接面板与 ADD 按钮。
 *
 * 背景遮罩跟随全局玻璃设置：
 * - **Liquid Glass 模式**：高斯模糊背景（backdrop-blur）
 * - **普通模式**：半透明深色遮罩
 *
 * @param expanded       是否展开
 * @param onDismiss      关闭面板回调
 * @param bottomBarHeight 底部导航栏高度，用于定位面板
 * @param onNavigateToQrCode    导航到二维码
 * @param onNavigateToNotebook  导航到记事本
 * @param onNavigateToMarkdown  导航到 Markdown 编辑器
 * @param onNavigateToTodoList  导航到待办事项
 * @param onNavigateToAiApp     导航到智能体
 * @param onNavigateToHabitTracker  导航到习惯打卡
 * @param onNavigateToBlessingWall  导航到祈福墙
 * @param onNavigateToImageCreation 导航到图片创作
 * @param onNavigateToTextCard 导航到文字卡片
 */
@Composable
fun AddMenuFloatingPanel(
    expanded: Boolean,
    onDismiss: () -> Unit,
    bottomBarHeight: Dp,
    onNavigateToQrCode: () -> Unit,
    onNavigateToNotebook: () -> Unit,
    onNavigateToMarkdown: () -> Unit,
    onNavigateToHtml: () -> Unit,
    onNavigateToPrompt: () -> Unit,
    onNavigateToTodoList: () -> Unit,
    onNavigateToAiApp: () -> Unit,
    onNavigateToCreateAIAgent: () -> Unit,
    onNavigateToCreateAIPrompt: () -> Unit,
    onNavigateToHabitTracker: () -> Unit,
    onNavigateToBlessingWall: () -> Unit,
    onNavigateToImageCreation: () -> Unit,
    onNavigateToTextCard: () -> Unit,
) {
    val visibleState = remember { MutableTransitionState(false) }
    visibleState.targetState = expanded
    val panelShape = remember {
        BubbleShape(
            arrowSize = 12.dp,
            arrowDirection = BubbleShape.ArrowDirection.Bottom,
            arrowAlignment = BubbleShape.ArrowAlignment.Center,
            cornerRadius = 24.dp
        )
    }

    val settingsState = LocalSettingsState.current

    // 遮罩透明度动画
    val scrimAlpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(300),
        label = "scrim_alpha"
    )

    // 全屏遮罩层 — 点击空白区域关闭面板
    if (expanded || visibleState.currentState || visibleState.isIdle.not()) {
        // 遮罩背景色：liquid glass 模式用更轻的遮罩 + 模糊，普通模式用半透明深色
        val scrimColor = if (settingsState.isLiquidGlassEnabled) {
            MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f * scrimAlpha)
        } else {
            MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f * scrimAlpha)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    // Liquid Glass + API 31+ → 高斯模糊遮罩
                    if (settingsState.isLiquidGlassEnabled
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        && expanded
                    ) {
                        Modifier.blur(radius = 12.dp)
                    } else {
                        Modifier
                    }
                )
                .background(scrimColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
        )
    }

    // 面板内容 — 定位在底部导航栏上方
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomBarHeight),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = expandVertically(
                expandFrom = Alignment.Bottom,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(200)),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Bottom,
                animationSpec = tween(250)
            ) + fadeOut(animationSpec = tween(150)),
        ) {
            // 网格菜单项
            val menuItems = listOf(
                AddMenuItem(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineScanQrCode,
                    label = CoreR.string.nav_new_qr_code,
                    onClick = {
                        onDismiss()
                        onNavigateToQrCode()
                    }
                ),
                AddMenuItem(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNote,
                    label = CoreR.string.nav_notebook,
                    onClick = {
                        onDismiss()
                        onNavigateToNotebook()
                    }
                ),
                AddMenuItem(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCodeEditor,
                    label = CoreR.string.html_create,
                    onClick = {
                        onDismiss()
                        onNavigateToHtml()
                    }
                ),
                AddMenuItem(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMarkdownEdit,
                    label = CoreR.string.markdown,
                    onClick = {
                        onDismiss()
                        onNavigateToMarkdown()
                    }
                ),
                AddMenuItem(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePrompt,
                    label = CoreR.string.prompt_create,
                    onClick = {
                        onDismiss()
                        onNavigateToPrompt()
                    }
                ),
                AddMenuItem(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSchedule,
                    label = CoreR.string.nav_todo_list,
                    onClick = {
                        onDismiss()
                        onNavigateToTodoList()
                    }
                ),
                AddMenuItem(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiChat,
                    label = CoreR.string.nav_ai_app,
                    onClick = {
                        onDismiss()
                        onNavigateToAiApp()
                    }
                ),
                AddMenuItem(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAgent,
                    label = CoreR.string.create_ai_agent_title,
                    onClick = {
                        onDismiss()
                        onNavigateToCreateAIAgent()
                    }
                ),
                AddMenuItem(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ChatPlus,
                    label = CoreR.string.create_ai_chat_prompt_title,
                    onClick = {
                        onDismiss()
                        onNavigateToCreateAIPrompt()
                    }
                ),
                AddMenuItem(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCheckCircleOutline,
                    label = CoreR.string.nav_check_in,
                    onClick = {
                        onDismiss()
                        onNavigateToHabitTracker()
                    }
                ),
                AddMenuItem(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBlessingWall,
                    label = CoreR.string.nav_blessing,
                    onClick = {
                        onDismiss()
                        onNavigateToBlessingWall()
                    }
                ),
                AddMenuItem(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMarkupLayers,
                    label = CoreR.string.nav_image_creation,
                    onClick = {
                        onDismiss()
                        onNavigateToImageCreation()
                    }
                ),
                AddMenuItem(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTextCard,
                    label = CoreR.string.nav_text_card,
                    onClick = {
                        onDismiss()
                        onNavigateToTextCard()
                    }
                ),
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = panelShape,
                    )
                    .padding(top = 20.dp, start = 16.dp, end = 16.dp, bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题
                Text(
                    text = stringResource(CoreR.string.nav_create_new_task),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 每行3个，自动分行
                menuItems.chunked(3).forEachIndexed { index, rowItems ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowItems.forEachIndexed { itemIndex, item ->
                            val themeIndex = index * 3 + itemIndex
                            AddMenuGridItem(
                                item = item,
                                themeIndex = themeIndex,
                            )
                        }
                        // 补齐空位，保持对齐
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.width(100.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 浮动面板网格项 — 圆形毛玻璃图标 + 文字标签
 */
@Composable
private fun AddMenuGridItem(
    item: AddMenuItem,
    themeIndex: Int,
) {
    val theme = addMenuItemThemeForIndex(themeIndex)
    val backgroundColor = addMenuItemBackground(theme)
    val contentColor = addMenuItemContentColor(theme)
    Column(
        modifier = Modifier
            .width(100.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = item.onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 圆形玻璃图标背景
        Box(
            modifier = Modifier
                .size(56.dp)
                .glassDense(
                    shape = MaterialTheme.shapes.large,
                    color = backgroundColor,
                    borderWidth = 0.0.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = contentColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(item.label),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

