package com.wanbaohe.profile.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.webview.WebViewParams
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.wanbaohe.profile.ui.AboutCardDivider
import com.wanbaohe.profile.ui.AboutGlassCard
import com.wanbaohe.profile.ui.AboutSectionTitle
import com.t8rin.imagetoolbox.core.resources.icons.OpenInNew

@Composable
fun AboutModelScreen(
    appComponent: AppComponent,
    onGoBack: () -> Unit = {},
) {
    BaseScreen(
        title = stringResource(id = R.string.profile_item_about_model),
        onGoBack = onGoBack,
        supportGlassEffect = true,
    ) {
        AboutAIModelContainer()
    }
}

@Composable
fun ShowAboutAIModelsModalSheet(
    isVisible: MutableState<Boolean>
) {
    if (isVisible.value) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )
        ModalBottomSheet(
            modifier = Modifier.navigationBarsPadding().statusBarsPadding(),
            onDismissRequest = {
                isVisible.value = false
            },
            sheetState = sheetState,
            dragHandle = {
                Text(
                    modifier = Modifier.padding(AppTheme.dimens.paddingNormal),
                    text = stringResource(id = R.string.profile_item_about_model),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            containerColor = AppTheme.colors.getContainerSurfaceColor(),
        ) {
            AboutAIModelContainer()
        }
    }
}

@Composable
fun ColumnScope.AboutAIModelContainer(
    modifier: Modifier = Modifier,
) {
    val onNavigate = LocalOnNavigate.current

    LazyColumn(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .weight(1f)
            .fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── 描述文字 ──
        item {
            Text(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                text = stringResource(id = R.string.ai_model_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.15,
            )
        }

        // ── 备案信息区块（仅国内渠道显示，海外渠道无备案监管要求） ──
        if (UrlConstants.SHOW_BEI_AN_ENTRY) {
            // ── 分组标题 ──
            item {
                AboutSectionTitle(
                    text = stringResource(R.string.ai_type_generate),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp),
                )
            }

            // ── 模型列表卡片 ──
            item {
                AboutGlassCard {
                    ModelItem(
                        name = stringResource(R.string.onebox_ai),
                        filingNumber = UrlConstants.BEI_AN_ONEBOX_NUMBER,
                        onClick = {
                            Clipboard.copy(UrlConstants.BEI_AN_ONEBOX_NUMBER)
                            onNavigate(
                                Screen.WebView(
                                    WebViewParams(title = "", url = UrlConstants.BEI_AN_AI_QUERY)
                                )
                            )
                        },
                    )
                    AboutCardDivider()
                    ModelItem(
                        name = stringResource(R.string.baidu_ai),
                        filingNumber = UrlConstants.BEI_AN_BAIDU_NUMBER,
                        onClick = {
                            Clipboard.copy(UrlConstants.BEI_AN_BAIDU_NUMBER)
                            onNavigate(
                                Screen.WebView(
                                    WebViewParams(title = "", url = UrlConstants.BEI_AN_AI_QUERY)
                                )
                            )
                        },
                    )
                    AboutCardDivider()
                    ModelItem(
                        name = stringResource(R.string.ai_tencent),
                        filingNumber = UrlConstants.BEI_AN_TENCENT_NUMBER,
                        onClick = {
                            Clipboard.copy(UrlConstants.BEI_AN_TENCENT_NUMBER)
                            onNavigate(
                                Screen.WebView(
                                    WebViewParams(title = "", url = UrlConstants.BEI_AN_AI_QUERY)
                                )
                            )
                        },
                    )
                    AboutCardDivider()
                    ModelItem(
                        name = stringResource(R.string.aliyun_qwen),
                        filingNumber = UrlConstants.BEI_AN_ALI_NUMBER,
                        onClick = {
                            Clipboard.copy(UrlConstants.BEI_AN_ALI_NUMBER)
                            onNavigate(
                                Screen.WebView(
                                    WebViewParams(title = "", url = UrlConstants.BEI_AN_AI_QUERY)
                                )
                            )
                        },
                    )
                    AboutCardDivider()
                    ModelItem(
                        name = stringResource(R.string.ai_kimi),
                        filingNumber = UrlConstants.BEI_AN_KIMI_NUMBER,
                        onClick = {
                            Clipboard.copy(UrlConstants.BEI_AN_KIMI_NUMBER)
                            onNavigate(
                                Screen.WebView(
                                    WebViewParams(title = "", url = UrlConstants.BEI_AN_AI_QUERY)
                                )
                            )
                        },
                    )
                    AboutCardDivider()
                    ModelItem(
                        name = stringResource(R.string.ai_doubao),
                        filingNumber = UrlConstants.BEI_AN_DOUBAO_NUMBER,
                        onClick = {
                            Clipboard.copy(UrlConstants.BEI_AN_DOUBAO_NUMBER)
                            onNavigate(
                                Screen.WebView(
                                    WebViewParams(title = "", url = UrlConstants.BEI_AN_AI_QUERY)
                                )
                            )
                        },
                    )
                    AboutCardDivider()
                    ModelItem(
                        name = stringResource(R.string.ai_deep_seek),
                        filingNumber = UrlConstants.BEI_AN_DEEPSEEK_NUMBER,
                        onClick = {
                            Clipboard.copy(UrlConstants.BEI_AN_DEEPSEEK_NUMBER)
                            onNavigate(
                                Screen.WebView(
                                    WebViewParams(title = "", url = UrlConstants.BEI_AN_AI_QUERY)
                                )
                            )
                        },
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 两行布局的模型条目：
 * 第一行 — 模型名称（大字）       ↗ 图标
 * 第二行 — 备案号（小字、次要色）
 */
@Composable
private fun ModelItem(
    name: String,
    filingNumber: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = filingNumber,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            modifier = Modifier
                .padding(start = 12.dp)
                .size(16.dp),
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.OpenInNew,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
