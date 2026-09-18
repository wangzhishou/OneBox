package com.wanbaohe.setting.ai.screen

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavItem
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.setting.memory.component.MemoryManagementComponent
import com.wanbaohe.setting.memory.screen.MemoryManagementContent
import com.wanbaohe.setting.prompt.component.SystemPromptManagementComponent
import com.wanbaohe.setting.prompt.screen.SystemPromptManagementContent
import com.wanbaohe.setting.skill.component.SkillManagementComponent
import com.wanbaohe.setting.skill.screen.SkillManagementContent
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMemory
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePrompt
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePsychology

private enum class PersonalizationTab {
    Prompt,
    Memory,
    Skill,
}

private data class PersonalizationTabInfo(
    val key: PersonalizationTab,
    @StringRes val titleRes: Int,
    val icon: ImageVector,
)

// 「提示词与个性化」聚合页: 底部导航切换 系统提示词 / AI 记忆 / AI 技能,
// 每个 tab 内嵌对应独立页面的完整内容(独立页面与深链路由保留不动)
@Composable
fun AIPersonalizationScreen(
    promptComponent: SystemPromptManagementComponent,
    memoryComponent: MemoryManagementComponent,
    skillComponent: SkillManagementComponent,
    onGoBack: () -> Unit,
) {
    val tabs = remember {
        listOf(
            PersonalizationTabInfo(
                key = PersonalizationTab.Prompt,
                titleRes = CoreR.string.profile_item_ai_reply_style,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePrompt,
            ),
            PersonalizationTabInfo(
                key = PersonalizationTab.Memory,
                titleRes = CoreR.string.profile_item_ai_memory,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMemory,
            ),
            PersonalizationTabInfo(
                key = PersonalizationTab.Skill,
                titleRes = CoreR.string.profile_item_ai_skill,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePsychology,
            ),
        )
    }
    var selectedTab by remember { mutableStateOf(PersonalizationTab.Prompt) }

    BaseScreen(
        title = stringResource(CoreR.string.profile_item_ai_personalization),
        onGoBack = onGoBack,
        showNavigationBarsPadding = false,
        supportGlassEffect = true,
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
                        val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                        (fadeIn(animationSpec = tween(250)) +
                            slideInHorizontally(animationSpec = tween(300)) { it / 4 * direction })
                            .togetherWith(
                                fadeOut(animationSpec = tween(200)) +
                                    slideOutHorizontally(animationSpec = tween(300)) { -it / 4 * direction }
                            )
                    },
                    label = "ai_personalization_tab_switch",
                ) { tab ->
                    when (tab) {
                        PersonalizationTab.Prompt -> SystemPromptManagementContent(
                            component = promptComponent,
                            modifier = Modifier.fillMaxSize(),
                        )

                        PersonalizationTab.Memory -> MemoryManagementContent(
                            component = memoryComponent,
                            modifier = Modifier.fillMaxSize(),
                        )

                        PersonalizationTab.Skill -> SkillManagementContent(
                            component = skillComponent,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            PersonalizationBottomBar(
                tabs = tabs,
                selectedTab = selectedTab,
                onSelect = { selectedTab = it },
            )
        }
    }
}

@Composable
private fun PersonalizationBottomBar(
    tabs: List<PersonalizationTabInfo>,
    selectedTab: PersonalizationTab,
    onSelect: (PersonalizationTab) -> Unit,
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
        selectedItemId = selectedTab.ordinal.toString(),
        onItemClick = { clicked ->
            val index = clicked.id.toIntOrNull() ?: return@BottomNavigationBar
            tabs.getOrNull(index)?.let { onSelect(it.key) }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}
