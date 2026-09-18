package com.wanbaohe.setting.ai.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.audio.NetworkAudioPlayer
import com.shifenmiao.common.components.GenericScrollableTabRow
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.imagegeneration.service.ImageGenerationManager
import com.shifenmiao.model.channel.FlavorType
import com.shifenmiao.model.tts.TTSConfig
import com.shifenmiao.tts.service.TTSService
import com.wanbaohe.setting.ai.component.AIEngineSettingsComponent
import com.wanbaohe.setting.image.screen.ImageGenerationSettingsContent
import com.wanbaohe.setting.local.component.LocalModelManagementComponent
import com.wanbaohe.setting.local.screen.LocalModelManagementContent
import com.wanbaohe.setting.router.AIServiceHubTab
import com.wanbaohe.settings.R
import com.shifenmiao.core.R as CoreR

private data class HubTab(
    val key: AIServiceHubTab,
    @StringRes val titleRes: Int,
)

// 「服务与模型」聚合页: 文本引擎 / 本地模型 / 多模态(语音合成 + 图片生成与编辑) 三个 Tab
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
    // 本地模型(端侧 LiteRT-LM)仅海外渠道(google / foss)开放,
    // 与原 Profile 入口按 isOverseas 过滤的可见性保持一致
    val tabs = remember {
        buildList {
            add(HubTab(AIServiceHubTab.Text, R.string.ai_service_hub_tab_text))
            if (FlavorType.fromName().isOverseas) {
                add(HubTab(AIServiceHubTab.Local, R.string.ai_service_hub_tab_local))
            }
            add(HubTab(AIServiceHubTab.Multimodal, R.string.ai_service_hub_tab_multimodal))
        }
    }
    val initialPage = remember {
        tabs.indexOfFirst { it.key == initialTab }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(initialPage = initialPage) { tabs.size }
    val coroutineScope = rememberCoroutineScope()

    BaseScreen(
        title = stringResource(CoreR.string.profile_item_ai_service_and_models),
        onGoBack = onGoBack,
        supportGlassEffect = true,
    ) {
        GenericScrollableTabRow(
            pagerState = pagerState,
            items = tabs,
            coroutineScope = coroutineScope,
            indicatorHeight = 34.dp,
            indicatorShape = MaterialTheme.shapes.large,
            trailingContent = {
                // 「新增引擎」按钮只在文本 Tab 显示
                if (tabs.getOrNull(pagerState.currentPage)?.key == AIServiceHubTab.Text) {
                    AIEngineAddEngineAction(onNavigate = engineComponent.onNavigate)
                }
            },
            getTitle = { tab -> stringResource(tab.titleRes) },
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { page ->
            when (tabs[page].key) {
                AIServiceHubTab.Text -> AIEngineSettingsContent(
                    component = engineComponent,
                    modifier = Modifier.fillMaxSize(),
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
}

// 多模态 Tab: 上语音合成、下图片生成与编辑;
// 两块内容各自内部滚动, 用 weight 平分剩余高度(外层不能再套 verticalScroll)
@Composable
private fun MultimodalSettingsContent(
    ttsService: TTSService,
    networkAudioPlayer: NetworkAudioPlayer,
    imageGenerationManager: ImageGenerationManager,
) {
    val ttsConfig by ttsService.observeConfig().collectAsState(initial = TTSConfig())

    Column(modifier = Modifier.fillMaxSize()) {
        MultimodalSectionHeader(title = stringResource(CoreR.string.profile_item_tts_settings))
        Box(modifier = Modifier.weight(1f)) {
            TTSSettingsContent(
                config = ttsConfig,
                ttsService = ttsService,
                networkAudioPlayer = networkAudioPlayer,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        MultimodalSectionHeader(title = stringResource(CoreR.string.profile_item_image_generation_settings))
        Box(modifier = Modifier.weight(1f)) {
            ImageGenerationSettingsContent(
                manager = imageGenerationManager,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MultimodalSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
}
