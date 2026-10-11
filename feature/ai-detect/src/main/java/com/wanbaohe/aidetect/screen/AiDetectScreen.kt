package com.wanbaohe.aidetect.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDescription
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImageSearch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineManageSearch
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassButton
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavItem
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.aidetect.R
import com.wanbaohe.aidetect.component.AiDetectComponent
import com.wanbaohe.aidetect.service.AiDetectError
import com.wanbaohe.aidetect.service.AiDetectService

/**
 * AI 检测助手主界面
 *
 * 主界面(文本/图片双 tab)、历史页、历史详情页均在此内部切换,对外只有一个路由。
 */
@Composable
fun AiDetectScreen(component: AiDetectComponent) {
    val page by component.currentPage.collectAsState()
    val error by component.errorEvent.collectAsState()
    val context = LocalContext.current

    // 检测失败统一 toast
    LaunchedEffect(error) {
        error?.let {
            AppToastHost.showToast(context.getString(it.toStringRes()))
            component.consumeError()
        }
    }

    AnimatedContent(
        targetState = page,
        transitionSpec = {
            fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(200))
        },
        label = "ai_detect_page",
    ) { current ->
        when (current) {
            AiDetectComponent.Page.MAIN -> AiDetectMainContent(component)
            AiDetectComponent.Page.HISTORY -> AiDetectHistoryScreen(component)
            AiDetectComponent.Page.HISTORY_DETAIL -> AiDetectHistoryDetailScreen(component)
        }
    }
}

/**
 * 图片检测开关: 上游(腾讯 EdgeOne Makers 内置模型)免费档仅支持文本检测,
 * 图片检测需企业版专属方案, 未开通前隐藏图片 tab, 底部 tab 栏替换为固定的开始检测按钮;
 * 开通后置 true 即可恢复。
 * AgentTool detect_ai_image 不受影响(上游未开通时会透传 403 错误)。
 */
private const val IS_IMAGE_DETECT_ENABLED = false

@Composable
private fun AiDetectMainContent(component: AiDetectComponent) {
    val currentTab by component.currentTab.collectAsState()
    // 图片检测未开放时, deeplink 直达 ImageDetect 也回落到文本 tab
    val effectiveTab = if (!IS_IMAGE_DETECT_ENABLED &&
        currentTab == Screen.AiDetect.Type.ImageDetect
    ) {
        Screen.AiDetect.Type.TextDetect
    } else {
        currentTab
    }

    BaseScreen(
        title = stringResource(com.shifenmiao.core.R.string.ai_detect_title),
        onGoBack = component::handleBack,
        isShowDefaultActions = true,
        showNavigationBarsPadding = false,
        supportGlassEffect = true,
        actions = {
            IconButton(onClick = component::openHistory) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                    contentDescription = stringResource(R.string.ai_detect_history),
                )
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = effectiveTab,
                    transitionSpec = {
                        val direction = if (targetState.ordinal() > initialState.ordinal()) 1 else -1
                        (fadeIn(animationSpec = tween(250)) +
                            slideInHorizontally(animationSpec = tween(300)) { it / 4 * direction })
                            .togetherWith(
                                fadeOut(animationSpec = tween(200)) +
                                    slideOutHorizontally(animationSpec = tween(300)) { -it / 4 * direction }
                            )
                    },
                    label = "ai_detect_tab_switch",
                ) { tab ->
                    when (tab) {
                        Screen.AiDetect.Type.TextDetect -> TextDetectTab(
                            component = component,
                            showDetectButton = IS_IMAGE_DETECT_ENABLED,
                        )

                        Screen.AiDetect.Type.ImageDetect -> ImageDetectTab(component)
                    }
                }
            }

            if (IS_IMAGE_DETECT_ENABLED) {
                AiDetectBottomBar(
                    currentTab = effectiveTab,
                    onSwitch = component::switchTo,
                )
            } else {
                AiDetectBottomDetectBar(component)
            }
        }
    }
}

/** 图片检测未开放时的底部栏: 固定的开始检测按钮(结果页不显示, 结果卡片自带重新检测) */
@Composable
private fun AiDetectBottomDetectBar(component: AiDetectComponent) {
    val state by component.textState.collectAsState()
    if (state.result != null) return

    GlassButton(
        onClick = component::detectText,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp),
        enabled = state.input.isNotBlank() && !state.isDetecting,
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineManageSearch,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.ai_detect_button))
    }
}

@Composable
private fun AiDetectBottomBar(
    currentTab: Screen.AiDetect.Type,
    onSwitch: (Screen.AiDetect.Type) -> Unit,
) {
    val tabs = buildList {
        add(
            TabInfo(
                label = stringResource(R.string.ai_detect_text_tab),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDescription,
                type = Screen.AiDetect.Type.TextDetect,
            )
        )
        if (IS_IMAGE_DETECT_ENABLED) {
            add(
                TabInfo(
                    label = stringResource(R.string.ai_detect_image_tab),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImageSearch,
                    type = Screen.AiDetect.Type.ImageDetect,
                )
            )
        }
    }

    val items = tabs.mapIndexed { index, tab ->
        BottomNavItem(
            id = index.toString(),
            label = tab.label,
            icon = tab.icon,
            contentDescription = tab.label,
        )
    }

    BottomNavigationBar(
        items = items,
        selectedItemId = currentTab.ordinal().toString(),
        onItemClick = { clicked ->
            val index = clicked.id.toIntOrNull() ?: return@BottomNavigationBar
            tabs.getOrNull(index)?.let { onSwitch(it.type) }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

private data class TabInfo(
    val label: String,
    val icon: ImageVector,
    val type: Screen.AiDetect.Type,
)

/** 用于 AnimatedContent 判断滑动方向的顺序 */
private fun Screen.AiDetect.Type.ordinal(): Int = when (this) {
    Screen.AiDetect.Type.TextDetect -> 0
    Screen.AiDetect.Type.ImageDetect -> 1
}

internal fun AiDetectError.toStringRes(): Int = when (this) {
    AiDetectError.IMAGE_TOO_LARGE -> R.string.ai_detect_error_image_too_large
    AiDetectError.IMAGE_TOO_SMALL -> R.string.ai_detect_error_image_too_small
    AiDetectError.IMAGE_UNREADABLE -> R.string.ai_detect_error_image_unreadable
    AiDetectError.NETWORK -> R.string.ai_detect_error_network
}

/** 判定 key → 文案资源 */
@Composable
internal fun verdictText(verdictKey: String): String = stringResource(
    when (verdictKey) {
        AiDetectService.VERDICT_LIKELY_AI -> R.string.ai_detect_verdict_likely_ai
        AiDetectService.VERDICT_MAYBE_AI -> R.string.ai_detect_verdict_maybe_ai
        else -> R.string.ai_detect_verdict_likely_human
    }
)

/** 判定 key → 环形/文案颜色 */
@Composable
internal fun verdictColor(verdictKey: String) = when (verdictKey) {
    AiDetectService.VERDICT_LIKELY_AI -> MaterialTheme.colorScheme.error
    AiDetectService.VERDICT_MAYBE_AI -> MaterialTheme.colorScheme.onTertiaryContainer
    else -> MaterialTheme.colorScheme.onPrimaryContainer
}
