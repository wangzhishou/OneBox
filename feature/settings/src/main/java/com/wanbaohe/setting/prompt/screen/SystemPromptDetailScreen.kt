package com.wanbaohe.setting.prompt.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.entrypoint.ChannelConfigEntryPoint
import com.shifenmiao.base.ui.MarkdownLazyContent
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.storage.TokenStorage
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTopAppBarType
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.text.EditorUiDefaults
import com.wanbaohe.markdown.edit.webview.WebViewMarkdownEditor
import com.wanbaohe.markdown.edit.webview.rememberWebViewMarkdownEditorState
import com.wanbaohe.setting.prompt.component.SystemPromptDetailComponent
import com.shifenmiao.webview.mermaid.ProvideMermaidRenderer
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import dagger.hilt.android.EntryPointAccessors
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo

@Composable
fun SystemPromptDetailScreen(
    component: SystemPromptDetailComponent,
) {
    val prompt by component.prompt.collectAsState()

    val scope = rememberCoroutineScope()
    val editorState = rememberWebViewMarkdownEditorState()
    var isDirty by remember { mutableStateOf(false) }
    var showVipDialog by remember { mutableStateOf(false) }
    val onNavigate = LocalOnNavigate.current
    val navigator = LocalUrlNavigator.current
    val uriHandler = LocalUriHandler.current

    val vipLevel = remember { TokenStorage.getUserVipLevel() }
    val context = LocalComponentActivity.current
    val channelConfig = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ChannelConfigEntryPoint::class.java
        ).getChannelConfig()
    }
    // 国内渠道 VIP >= 2 才可编辑; 国外(google)渠道全面放开, 不校验 VIP
    val canEdit = channelConfig.enableGms || vipLevel >= 2
    // 支付全关的渠道(google)没有升级入口, VIP 弹窗只展示提示不展示"去升级"按钮
    val enablePayment = channelConfig.enablePayment
    val saveSuccessText = stringResource(R.string.system_prompt_save_success)
    val saveFailedText = stringResource(R.string.system_prompt_save_failed)
    var isReminderVisible by rememberSaveable(component.promptId) { mutableStateOf(true) }

    LaunchedEffect(prompt) {
        prompt?.prompt?.let {
            editorState.setContent(it)
            isDirty = false
        }
    }

    if (canEdit) {
        // VIP >= 2: 可编辑模式（WebView Markdown 编辑器）
        BaseScreen(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            title = prompt?.title ?: "",
            onGoBack = component.onGoBack,
            supportGlassEffect = false,
            actions = {
                if (isDirty) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val content = editorState.getContent()
                                component.save(
                                    content = content,
                                    onSuccess = {
                                        isDirty = false
                                        AppToastHost.showToast(saveSuccessText)
                                    },
                                    onFailure = { error ->
                                        AppToastHost.showToast("$saveFailedText: $error")
                                    }
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                            contentDescription = stringResource(R.string.done_button),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (isReminderVisible) {
                    SystemPromptFriendlyReminder(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onDismiss = { isReminderVisible = false },
                    )
                    Spacer(modifier = Modifier.height(OneBoxDesignSystem.itemSpacing))
                }
                WebViewMarkdownEditor(
                    initialValue = prompt?.prompt ?: "",
                    state = editorState,
                    placeholder = stringResource(R.string.note_placeholder),
                    readOnly = false,
                    textStyle = EditorUiDefaults.contentTextStyle(),
                    storageKey = "system_prompt_${component.promptId}",
                    onContentChanged = {
                        isDirty = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .navigationBarsPadding()
                )
            }
        }
    } else {
        // VIP < 2: 只读模式（Native 渲染，类似 NoteItemScreen）
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        BaseScreen(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            title = {
                Text(
                    text = prompt?.title ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            onGoBack = component.onGoBack,
            supportGlassEffect = false,
            type = EnhancedTopAppBarType.Medium,
            scrollBehavior = scrollBehavior,
            showNavigationBarsPadding = true,
            actions = {
                IconButton(
                    onClick = { showVipDialog = true }
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.edit_prompt),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        ) {
            ProvideMermaidRenderer {
                MarkdownLazyContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    message = prompt?.prompt ?: "",
                    isLoading = prompt == null,
                    onLinkClick = { url ->
                        if (!navigator.navigate(url)) {
                            uriHandler.openUri(url)
                        }
                    },
                    onGoBack = component.onGoBack
                )
            }
        }
    }

    if (showVipDialog) {
        EnhancedAlertDialog(
            visible = showVipDialog,
            onDismissRequest = { showVipDialog = false },
            confirmButton = {
                if (enablePayment) {
                    TextButton(
                        onClick = {
                            showVipDialog = false
                            // google 渠道(Play Billing)支付入口先登录; 国内渠道直接进入
                            if (channelConfig.enablePlayBilling) {
                                ActionUtils.showLogin(source = "SystemPromptVipUpgrade") {
                                    onNavigate(Screen.BuyCoffee())
                                }
                            } else {
                                onNavigate(Screen.BuyCoffee())
                            }
                        }
                    ) {
                        Text(stringResource(R.string.system_prompt_edit_vip_upgrade))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showVipDialog = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            },
            title = {
                Text(stringResource(R.string.system_prompt_edit_vip_required_title))
            },
            text = {
                Text(stringResource(R.string.system_prompt_edit_vip_required_message))
            }
        )
    }
}

@Composable
private fun SystemPromptFriendlyReminder(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
            .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),
            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.system_prompt_friendly_reminder_title),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.system_prompt_friendly_reminder_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

