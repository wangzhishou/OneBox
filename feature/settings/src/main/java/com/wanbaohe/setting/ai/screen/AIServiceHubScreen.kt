package com.wanbaohe.setting.ai.screen

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.audio.NetworkAudioPlayer
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.ai.EngineFilterChip
import com.shifenmiao.imagegeneration.service.ImageGenerationManager
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.channel.FlavorType
import com.shifenmiao.model.tts.TTSConfig
import com.shifenmiao.tts.service.TTSService
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavItem
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.setting.ai.component.AIEngineSettingsComponent
import com.wanbaohe.setting.image.screen.ImageGenerationSettingsContent
import com.wanbaohe.setting.local.component.LocalModelManagementComponent
import com.wanbaohe.setting.local.screen.LocalModelManagementContent
import com.wanbaohe.setting.router.AIServiceHubTab
import com.wanbaohe.settings.R
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFeatures
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePsychology
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStorage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText

private data class HubTab(
    val key: AIServiceHubTab,
    @StringRes val titleRes: Int,
    val icon: ImageVector,
)

// 「服务与模型」聚合页: 底部导航切换 文本引擎 / 本地模型 / 多模态(语音合成 + 图片生成与编辑)
@Composable
fun AIServiceHubScreen(
    initialTab: AIServiceHubTab,
    engineComponent: AIEngineSettingsComponent,
    localModelComponent: LocalModelManagementComponent,
    ttsService: TTSService,
    networkAudioPlayer: NetworkAudioPlayer,
    imageGenerationManager: ImageGenerationManager,
    onGoBack: () -> Unit,
) {
    // 「服务与模型」聚合页: 底部导航切换 文本引擎 / Jev / 本地模型 / 多模态(语音合成 + 图片生成与编辑)
    // 本地模型(端侧 LiteRT-LM)仅海外渠道(google / foss)开放,
    // 与原 Profile 入口按 isOverseas 过滤的可见性保持一致
    val tabs = remember {
        buildList {
            add(
                HubTab(
                    key = AIServiceHubTab.Text,
                    titleRes = R.string.ai_service_hub_tab_text,
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineText,
                )
            )
            add(
                HubTab(
                    key = AIServiceHubTab.Jev,
                    titleRes = R.string.ai_service_hub_tab_jev,
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePsychology,
                )
            )
            if (FlavorType.fromName().isOverseas) {
                add(
                    HubTab(
                        key = AIServiceHubTab.Local,
                        titleRes = R.string.ai_service_hub_tab_local,
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStorage,
                    )
                )
            }
            add(
                HubTab(
                    key = AIServiceHubTab.Multimodal,
                    titleRes = R.string.ai_service_hub_tab_multimodal,
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFeatures,
                )
            )
        }
    }
    var selectedTab by remember {
        mutableStateOf(
            tabs.firstOrNull { it.key == initialTab }?.key ?: AIServiceHubTab.Text
        )
    }

    BaseScreen(
        title = stringResource(CoreR.string.profile_item_ai_service_and_models),
        onGoBack = onGoBack,
        showNavigationBarsPadding = false,
        supportGlassEffect = true,
        actions = {
            // 「新增引擎」按钮只在引擎列表类 Tab 显示, Jev tab 新增时预选 JEV 协议
            when (selectedTab) {
                AIServiceHubTab.Text -> AIEngineAddEngineAction(onNavigate = engineComponent.onNavigate)
                AIServiceHubTab.Jev -> AIEngineAddEngineAction(
                    onNavigate = engineComponent.onNavigate,
                    initialProtocol = AiRequestProtocol.JEV.name,
                )
                else -> Unit
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
                    targetState = selectedTab,
                    transitionSpec = {
                        val direction = if (
                            tabs.indexOfFirst { it.key == targetState } >
                            tabs.indexOfFirst { it.key == initialState }
                        ) 1 else -1
                        (fadeIn(animationSpec = tween(250)) +
                            slideInHorizontally(animationSpec = tween(300)) { it / 4 * direction })
                            .togetherWith(
                                fadeOut(animationSpec = tween(200)) +
                                    slideOutHorizontally(animationSpec = tween(300)) { -it / 4 * direction }
                            )
                    },
                    label = "ai_service_hub_tab_switch",
                ) { tab ->
                    when (tab) {
                        AIServiceHubTab.Text -> AIEngineSettingsContent(
                            component = engineComponent,
                            modifier = Modifier.fillMaxSize(),
                            // Jev/Pikafish 是独立协议的专用引擎, 在专属 tab 管理, 不混进普通引擎列表
                            engineFilter = { !it.requestProtocol.isNonChat },
                        )

                        AIServiceHubTab.Jev -> AIEngineSettingsContent(
                            component = engineComponent,
                            modifier = Modifier.fillMaxSize(),
                            engineFilter = { it.requestProtocol.isNonChat },
                            emptyMessageRes = R.string.ai_engine_jev_list_empty,
                        )

                        AIServiceHubTab.Local -> LocalModelManagementContent(
                            component = localModelComponent,
                            modifier = Modifier.fillMaxSize(),
                        )

                        AIServiceHubTab.Multimodal -> MultimodalSettingsContent(
                            ttsService = ttsService,
                            networkAudioPlayer = networkAudioPlayer,
                            imageGenerationManager = imageGenerationManager,
                        )
                    }
                }
            }

            HubBottomBar(
                tabs = tabs,
                selectedTab = selectedTab,
                onSelect = { selectedTab = it },
            )
        }
    }
}

@Composable
private fun HubBottomBar(
    tabs: List<HubTab>,
    selectedTab: AIServiceHubTab,
    onSelect: (AIServiceHubTab) -> Unit,
) {
    val items = tabs.mapIndexed { index, tab ->
        BottomNavItem(
            id = index.toString(),
            label = stringResource(tab.titleRes),
            icon = tab.icon,
            contentDescription = stringResource(tab.titleRes),
        )
    }
    BottomNavigationBar(
        items = items,
        selectedItemId = tabs.indexOfFirst { it.key == selectedTab }.toString(),
        onItemClick = { clicked ->
            val index = clicked.id.toIntOrNull() ?: return@BottomNavigationBar
            tabs.getOrNull(index)?.let { onSelect(it.key) }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

private enum class MultimodalSection {
    TTS,
    Image,
}

// 多模态 Tab: 顶部「语音合成 / 图片生成与编辑」筛选切换,
// 每个选项展示完整原详情页内容(占满剩余高度), 默认语音合成
@Composable
private fun MultimodalSettingsContent(
    ttsService: TTSService,
    networkAudioPlayer: NetworkAudioPlayer,
    imageGenerationManager: ImageGenerationManager,
) {
    val ttsConfig by ttsService.observeConfig().collectAsState(initial = TTSConfig())
    var section by remember { mutableStateOf(MultimodalSection.TTS) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EngineFilterChip(
                text = stringResource(CoreR.string.profile_item_tts_settings),
                isSelected = section == MultimodalSection.TTS,
                onClick = { section = MultimodalSection.TTS },
                leadingIcon = multimodalCheckIcon(selected = section == MultimodalSection.TTS),
            )
            EngineFilterChip(
                text = stringResource(CoreR.string.profile_item_image_generation_settings),
                isSelected = section == MultimodalSection.Image,
                onClick = { section = MultimodalSection.Image },
                leadingIcon = multimodalCheckIcon(selected = section == MultimodalSection.Image),
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (section) {
                MultimodalSection.TTS -> TTSSettingsContent(
                    config = ttsConfig,
                    ttsService = ttsService,
                    networkAudioPlayer = networkAudioPlayer,
                    modifier = Modifier.fillMaxSize(),
                )

                MultimodalSection.Image -> ImageGenerationSettingsContent(
                    manager = imageGenerationManager,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

// 多模态筛选 chip 的选中对勾;未选中返回 null 不占位
@Composable
private fun multimodalCheckIcon(selected: Boolean): (@Composable () -> Unit)? {
    if (!selected) return null
    return {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
    }
}
