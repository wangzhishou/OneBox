package com.shifenmiao.demo.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.core.R
import com.shifenmiao.demo.screenLogic.DemoComponent
import com.shifenmiao.model.HomeTabKey
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton

private data class DeeplinkExample(
    val label: String,
    val url: String,
    val openScreen: Screen? = null
)

@Composable
fun NavigationDeeplinkHelpSheet(
    demoComponent: DemoComponent,
    onNavigate: (Screen) -> Unit,
) {
    val state by demoComponent.state.collectAsState()

    val navigator = LocalUrlNavigator.current

    val screenExamples = listOf(
        DeeplinkExample(
            label = stringResource(R.string.demo_navigation_deeplink_example_theme_settings),
            url = "onebox://screen/theme_settings",
            openScreen = Screen.ThemeSettings
        ),
        DeeplinkExample(
            label = stringResource(R.string.demo_navigation_deeplink_example_feedback),
            url = "onebox://screen/feedback",
            openScreen = Screen.Feedback()
        ),
        DeeplinkExample(
            label = stringResource(R.string.demo_navigation_deeplink_example_create_feedback),
            url = "onebox://screen/create_feedback",
            openScreen = Screen.CreateFeedback()
        )
    )

    val actionExamples = listOf(
        DeeplinkExample(
            label = stringResource(R.string.demo_navigation_deeplink_example_open_file_picker),
            url = "onebox://action/open_file_picker"
        ),
        DeeplinkExample(
            label = stringResource(R.string.demo_navigation_deeplink_example_pick_image),
            url = "onebox://action/open_file_picker?mime_types=image/*"
        ),
        DeeplinkExample(
            label = stringResource(R.string.demo_navigation_deeplink_example_pick_multiple_files),
            url = "onebox://action/open_file_picker?mime_types=image/*|application/pdf&multiple=true"
        )
    )

    val homeTabExamples = listOf(
        DeeplinkExample(
            label = stringResource(R.string.demo_navigation_deeplink_example_home_default),
            url = "onebox://screen/home",
            openScreen = Screen.NewApp()
        ),
        DeeplinkExample(
            label = stringResource(R.string.demo_navigation_deeplink_example_home_text),
            url = "onebox://screen/home/text",
            openScreen = Screen.NewApp(initialTab = HomeTabKey.TEXT)
        ),
        DeeplinkExample(
            label = stringResource(R.string.demo_navigation_deeplink_example_home_app),
            url = "onebox://screen/home/app",
            openScreen = Screen.NewApp(initialTab = HomeTabKey.APP)
        ),
        DeeplinkExample(
            label = stringResource(R.string.demo_navigation_deeplink_example_home_agent),
            url = "onebox://screen/home/agent",
            openScreen = Screen.NewApp(initialTab = HomeTabKey.AGENT)
        ),
        DeeplinkExample(
            label = stringResource(R.string.demo_navigation_deeplink_example_home_prompt),
            url = "onebox://screen/home/prompt",
            openScreen = Screen.NewApp(initialTab = HomeTabKey.PROMPT)
        ),
        DeeplinkExample(
            label = stringResource(R.string.demo_navigation_deeplink_example_home_web),
            url = "onebox://screen/home/web",
            openScreen = Screen.NewApp(initialTab = HomeTabKey.WEB)
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.demo_navigation_deeplink_tool_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.demo_navigation_deeplink_tool_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            DeeplinkSectionTitle(title = stringResource(R.string.demo_navigation_deeplink_section_screen))
        }
        items(screenExamples, key = { it.url }) { example ->
            DeeplinkExampleCard(
                label = example.label,
                url = example.url,
                onCopy = { Clipboard.copy(example.url) },
                onOpen = {
                    example.openScreen?.let(onNavigate) ?: navigator.navigate(example.url)
                }
            )
        }

        item {
            DeeplinkSectionTitle(title = stringResource(R.string.demo_navigation_deeplink_section_home_tab))
        }
        items(homeTabExamples, key = { it.url }) { example ->
            DeeplinkExampleCard(
                label = example.label,
                url = example.url,
                onCopy = { Clipboard.copy(example.url) },
                onOpen = {
                    example.openScreen?.let(onNavigate) ?: navigator.navigate(example.url)
                }
            )
        }

        item {
            DeeplinkSectionTitle(title = stringResource(R.string.demo_navigation_deeplink_section_action))
        }
        items(actionExamples, key = { it.url }) { example ->
            DeeplinkExampleCard(
                label = example.label,
                url = example.url,
                onCopy = { Clipboard.copy(example.url) },
                onOpen = { navigator.navigate(example.url) }
            )
        }

        item {
            DeeplinkSectionTitle(title = stringResource(R.string.demo_navigation_deeplink_section_item))
        }
        if (state.itemDeeplinkExamples.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.demo_navigation_deeplink_no_item_examples),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            items(state.itemDeeplinkExamples, key = { it.itemId }) { example ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DeeplinkExampleCard(
                        label = "${example.title} · ${example.typeLabel}",
                        url = example.openDeeplink,
                        onCopy = { Clipboard.copy(example.openDeeplink) },
                        onOpen = { navigator.navigate(example.openDeeplink) }
                    )
                    example.editDeeplink?.let { editUrl ->
                        DeeplinkExampleCard(
                            label = stringResource(
                                R.string.demo_navigation_deeplink_item_edit_format,
                                example.title
                            ),
                            url = editUrl,
                            onCopy = { Clipboard.copy(editUrl) },
                            onOpen = { navigator.navigate(editUrl) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeeplinkSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun DeeplinkExampleCard(
    label: String,
    url: String,
    onCopy: () -> Unit,
    onOpen: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        SelectionContainer {
            Text(
                text = url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassTonalButton(
                onClick = onCopy,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(R.string.demo_navigation_deeplink_copy))
            }
            GlassTonalButton(
                onClick = onOpen,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(R.string.demo_navigation_deeplink_open))
            }
        }
    }
}


