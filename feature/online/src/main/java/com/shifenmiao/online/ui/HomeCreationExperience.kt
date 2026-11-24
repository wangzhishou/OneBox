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

package com.shifenmiao.online.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.base.ui.empty.EmptyStateGuide
import com.wanbaohe.base.ui.empty.EmptyStateGuideAction
import com.shifenmiao.core.R
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.storage.RemoteConfigStorage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAiChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAgent
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoAwesomeMosaic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoFix
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCodeEditor
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNote
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePrompt
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.core.ui.widget.glass.GlassTonalIconButton
import com.wanbaohe.core.ui.widget.glass.glassBackground

@Composable
fun HomeEmptyState(
    listType: ListItemType,
    onManualCreate: () -> Unit,
    onAiCreate: () -> Unit,
    modifier: Modifier = Modifier,
    isFiltered: Boolean = false,
    onClearFilter: () -> Unit = {},
    showAiCreate: Boolean = true,
) {
    val typeName = listTypeDisplayName(listType)
    EmptyStateGuide(
        icon = if (isFiltered) {
            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoFix
        } else {
            listTypeIcon(listType)
        },
        title = stringResource(
            if (isFiltered) R.string.home_filter_empty_title else R.string.home_empty_title,
            typeName,
        ),
        description = if (isFiltered) {
            stringResource(R.string.home_filter_empty_description)
        } else {
            null
        },
        actions = if (isFiltered) {
            listOf(
                EmptyStateGuideAction(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoFix,
                    title = stringResource(R.string.home_clear_filter),
                    onClick = onClearFilter,
                ),
            )
        } else {
            buildList {
                add(
                    EmptyStateGuideAction(
                        icon = Icons.Outlined.Edit,
                        title = stringResource(R.string.home_create_manual),
                        onClick = onManualCreate,
                    )
                )
                if (showAiCreate) {
                    add(
                        EmptyStateGuideAction(
                            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiChat,
                            title = stringResource(R.string.home_create_with_ai),
                            onClick = onAiCreate,
                        )
                    )
                }
            }
        },
        modifier = modifier,
    )
}

/**
 * 列表末尾的「创建」引导卡。
 *
 * 上半区是一个居中的玻璃 ＋ 底片(与「新建」同一动作),底部是按内容宽度排布、带最大宽度的
 * 两枚玻璃胶囊;只有一个创建入口的 tab(智能体 / 提示词)只保留「新建」,＋ 仍居中。
 *
 * 高度优先跟随相邻普通卡的实测高度,＋ 底片会随可用高度收缩,最矮 184dp 也不会压扁按钮。
 */
@Composable
fun CreateChoiceCard(
    onManualCreate: () -> Unit,
    onAiCreate: () -> Unit,
    modifier: Modifier = Modifier,
    /** 相邻普通卡片的实测高度,双列时据此与旁边的卡片对齐;为 null 时退回最小高度模式 */
    siblingHeight: Dp? = null,
    showAiCreate: Boolean = true,
) {
    val shape = MaterialTheme.shapes.large
    val fixedHeight = siblingHeight?.let { maxOf(it, CREATION_CARD_MIN_HEIGHT) }
    val manualLabel = stringResource(R.string.home_create_manual)
    val aiLabel = stringResource(R.string.home_create_with_ai)
    // 两枚按钮取同一个宽度:等于较宽那枚文字所需宽度,封顶 140dp,左右自然留白
    val actionWidth = creationActionWidth(manualLabel, aiLabel, showAiCreate)
    // ＋ 与按钮之间的间距固定,不随卡片高度伸缩:只剩一枚按钮时中间不会空出一大块
    val plateSize = creationPlateSize(availableHeight = fixedHeight, showAiCreate = showAiCreate)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (fixedHeight != null) Modifier.height(fixedHeight)
                else Modifier.heightIn(min = CREATION_CARD_MIN_HEIGHT)
            )
            .glassBackground(
                style = GlassStyle.Regular,
                shape = shape,
                borderWidth = 0.5.dp,
                color = MaterialTheme.colorScheme.surfaceContainer,
            )
            .clip(shape)
            .padding(CREATION_CARD_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        // ＋ 和按钮当成一整组居中;高度确定时用上下两段弹性留白把这一组压到正中
        verticalArrangement = Arrangement.Center,
    ) {
        // 高度无界(拿不到相邻卡高度)时 weight 会塌成 0,那两帧就靠 Center 兜住
        if (fixedHeight != null) {
            Spacer(Modifier.weight(1f))
        }
        CreationPlusPlate(size = plateSize, onClick = onManualCreate)
        Spacer(Modifier.height(CREATION_PLATE_GAP))

        Column(
            modifier = Modifier.width(actionWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CREATION_BUTTON_SPACING),
        ) {
            CreationActionButton(
                icon = Icons.Outlined.Edit,
                title = manualLabel,
                onClick = onManualCreate,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            if (showAiCreate) {
                CreationActionButton(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiChat,
                    title = aiLabel,
                    onClick = onAiCreate,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        if (fixedHeight != null) {
            Spacer(Modifier.weight(1f))
        }
    }
}

/**
 * ＋ 底片直径。
 *
 * 由卡片可用高度反推:先扣掉内边距、按钮和固定间距,剩下的就是 ＋ 能占的空间,再夹在
 * [CREATION_PLATE_MIN_SIZE]–[CREATION_PLATE_MAX_SIZE] 之间。这样 184dp 的矮卡片也塞得下,
 * 高的卡片上 ＋ 也不会跟着无限放大。
 */
private fun creationPlateSize(
    availableHeight: Dp?,
    showAiCreate: Boolean,
): Dp {
    val buttonCount = if (showAiCreate) 2 else 1
    val buttonsHeight = CREATION_BUTTON_HEIGHT * buttonCount +
        CREATION_BUTTON_SPACING * (buttonCount - 1)
    val innerHeight = (availableHeight ?: CREATION_CARD_MIN_HEIGHT) - CREATION_CARD_PADDING * 2
    return (innerHeight - buttonsHeight - CREATION_PLATE_GAP)
        .coerceIn(CREATION_PLATE_MIN_SIZE, CREATION_PLATE_MAX_SIZE)
}

/** 居中的 ＋:玻璃圆片 + 与「新建」胶囊同一个动作,点了不会没反应。 */
@Composable
private fun CreationPlusPlate(
    size: Dp,
    onClick: () -> Unit,
) {
    GlassTonalIconButton(
        onClick = onClick,
        // ＋ 和下方「新建」是同一个动作:对读屏隐藏,否则会读出两个同名按钮,
        // 带文字的胶囊才是可读入口。
        modifier = Modifier
            .size(size)
            .clearAndSetSemantics { },
        shape = CircleShape,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            // 卡片自身是 surfaceContainer:底片要再抬一档才有「一片玻璃」的重量,
            // 落在 surfaceContainerHigh 时和卡面几乎同色,只剩一圈描边
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        style = GlassStyle.Thick,
        glassBorderWidth = 1.dp,
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = null,
            modifier = Modifier.size(size * CREATION_PLATE_ICON_RATIO),
        )
    }
}

/**
 * 两枚创建按钮的共用宽度。
 *
 * 按最长的那条文案实测,加上图标 + 间距 + 内边距,再夹在 [CREATION_BUTTON_MIN_WIDTH] 和
 * [CREATION_BUTTON_MAX_WIDTH] 之间:短文案(中文)贴合内容,长文案(俄语/菲律宾语)顶到上限、
 * 交给按钮内 [TextAutoSize] 缩字号,不会把胶囊撑到整张卡宽。
 */
@Composable
private fun creationActionWidth(
    manualLabel: String,
    aiLabel: String,
    showAiCreate: Boolean,
): Dp {
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
    val labels = if (showAiCreate) listOf(manualLabel, aiLabel) else listOf(manualLabel)
    val widestLabelPx = labels.maxOf { label ->
        measurer.measure(
            text = AnnotatedString(label),
            style = labelStyle,
            maxLines = 1,
        ).size.width
    }
    return with(LocalDensity.current) {
        (widestLabelPx.toDp() + CREATION_BUTTON_CHROME_WIDTH)
            .coerceIn(CREATION_BUTTON_MIN_WIDTH, CREATION_BUTTON_MAX_WIDTH)
    }
}

@Composable
private fun CreationActionButton(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
) {
    GlassTonalButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(CREATION_BUTTON_HEIGHT),
        shape = RoundedCornerShape(percent = 50),
        color = containerColor,
        contentColor = contentColor,
        contentPadding = PaddingValues(horizontal = CREATION_BUTTON_HORIZONTAL_PADDING),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(CREATION_BUTTON_ICON_SIZE),
        )
        Spacer(Modifier.width(CREATION_BUTTON_ICON_GAP))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            // 按钮有宽度上限:长语种在这里缩字号,而不是换行或溢出
            autoSize = TextAutoSize.StepBased(
                minFontSize = 10.sp,
                maxFontSize = MaterialTheme.typography.labelLarge.fontSize,
                stepSize = 0.5.sp,
            ),
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val CREATION_CARD_PADDING = 16.dp
private val CREATION_CARD_MIN_HEIGHT = 180.dp
private val CREATION_PLATE_MAX_SIZE = 58.dp
private val CREATION_PLATE_MIN_SIZE = 40.dp
/** ＋ 底片与上方胶囊之间的固定间距 */
private val CREATION_PLATE_GAP = 12.dp
private const val CREATION_PLATE_ICON_RATIO = 0.44f

private val CREATION_BUTTON_HEIGHT = 44.dp
private val CREATION_BUTTON_SPACING = 8.dp
private val CREATION_BUTTON_ICON_SIZE = 16.dp
private val CREATION_BUTTON_ICON_GAP = 8.dp
private val CREATION_BUTTON_HORIZONTAL_PADDING = 20.dp
private val CREATION_BUTTON_MIN_WIDTH = 112.dp
private val CREATION_BUTTON_MAX_WIDTH = 140.dp
private val CREATION_BUTTON_CHROME_WIDTH =
    CREATION_BUTTON_ICON_SIZE + CREATION_BUTTON_ICON_GAP + CREATION_BUTTON_HORIZONTAL_PADDING * 2

@Composable
private fun listTypeDisplayName(listType: ListItemType): String = stringResource(
    when (listType) {
        ListItemType.NOTE -> R.string.record_tab_title
        ListItemType.NORMAL -> R.string.home_tab_tools
        ListItemType.AGENT -> R.string.type_agent
        ListItemType.PROMPT -> R.string.type_prompt
        ListItemType.HTML -> R.string.home_tab_web_title
        else -> R.string.placeholder_empty_title
    }
)

private fun listTypeIcon(listType: ListItemType): ImageVector = when (listType) {
    ListItemType.NOTE -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNote
    ListItemType.AGENT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAgent
    ListItemType.PROMPT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePrompt
    ListItemType.HTML -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCodeEditor
    else -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoAwesomeMosaic
}

/**
 * 各 tab「AI 创建」按钮的点击行为：
 * - 笔记/网址：跳助手 Tab 开新会话，并预填输入框；预填词与助手页快捷开始区
 *   共用 chatQuickStartPrompts(下标约定见 [QUICK_START_INDEX_NOTE_FILL_IN])，远程可配；
 * - 应用：跳助手 Tab 开新会话(无专属预填词)；
 * - 提示词：跳手动编辑器，AI 起草在编辑器内按需开启；
 * - 其余类型走 [fallback]。
 */
@Composable
fun aiCreateActionFor(
    listType: ListItemType,
    onNavigator: (Screen) -> Unit,
    fallback: () -> Unit,
): () -> Unit {
    val chatTitle = stringResource(R.string.ai_chat_title)
    // 本地兜底,与 defaultChatQuickStartPrompts 末尾两条一致;点击时远程配置优先
    val noteFillIn = stringResource(R.string.ai_chat_quick_start_17)
    val webFillIn = stringResource(R.string.ai_chat_quick_start_18)
    return when (listType) {
        // 带非空 title 会触发 startGuidedConversation:清空历史 + 新会话 id;
        // template 只预填到输入框,不自动发送
        ListItemType.NOTE, ListItemType.HTML -> {
            {
                val prompts = RemoteConfigStorage.getRemoteConfig().chatQuickStartPrompts
                val index = if (listType == ListItemType.NOTE) {
                    QUICK_START_INDEX_NOTE_FILL_IN
                } else {
                    QUICK_START_INDEX_WEB_FILL_IN
                }
                onNavigator(
                    Screen.AITabChatScreen(
                        Conversation(
                            entryType = AIConversationEntryType.ASSISTANT,
                            title = chatTitle,
                            template = prompts?.getOrNull(index)?.takeIf { it.isNotBlank() }
                                ?: if (listType == ListItemType.NOTE) noteFillIn else webFillIn,
                        )
                    )
                )
            }
        }

        ListItemType.NORMAL -> {
            {
                onNavigator(
                    Screen.AITabChatScreen(
                        Conversation(
                            entryType = AIConversationEntryType.ASSISTANT,
                            title = chatTitle,
                        )
                    )
                )
            }
        }

        ListItemType.PROMPT -> {
            { onNavigator(Screen.EditPromptItem()) }
        }

        else -> fallback
    }
}

// chatQuickStartPrompts 下标约定(见 RemoteConfig.chatQuickStartPrompts 注释):
// 16=笔记「AI 创建」预填词,17=网址「AI 创建」预填词
private const val QUICK_START_INDEX_NOTE_FILL_IN = 16
private const val QUICK_START_INDEX_WEB_FILL_IN = 17

