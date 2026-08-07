package com.wanbaohe.setting.ai.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.icon.IconRegistry
import com.shifenmiao.base.utils.LoginUtils
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.remote.AiEngineConfig
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDangerButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxLeadingIconBadge
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneSecondaryButton
import com.t8rin.imagetoolbox.core.utils.getString
import com.wanbaohe.setting.ai.component.AIEngineSettingsComponent
import com.wanbaohe.settings.R
import kotlinx.coroutines.launch
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettingsSuggest

@Composable
fun AIEngineSettingsScreen(
    component: AIEngineSettingsComponent,
) {
    val allEngines by component.allEngines.collectAsState()
    val currentAIEngine by component.currentAIEngine.collectAsState()
    val fastAIEngine by component.fastAIEngine.collectAsState()
    val isRefreshing by component.isRefreshing.collectAsState()
    val lastRefreshError by component.lastRefreshError.collectAsState()
    val localOwnedEngineKeys by component.localOwnedEngineKeys.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    var pendingDeleteEngine by remember { mutableStateOf<AiEngine?>(null) }

    LaunchedEffect(Unit) {
        component.ensureCatalogRefreshed()
    }

    BaseScreen(
        title = stringResource(CoreR.string.profile_item_ai_service_and_models),
        onGoBack = component.onGoBack,
        supportGlassEffect = true,
        actions = {
            // 渠道能力(如 Google 全量放开)或国内管理员可新增引擎
            val capabilities = remember { AiEngineConfig.getCapabilities() }
            if (capabilities.canAddEngine || LoginUtils.isAdmin()) {
                IconButton(onClick = {
                    component.onNavigate(
                        Screen.AISettings(Screen.AISettings.Type.AddEngine)
                    )
                }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.ai_engine_add_engine),
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
        ) {
            Spacer(modifier = Modifier.height(OneBoxDesignSystem.microSpacing))

            OneBoxSectionCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                ) {
                    Text(
                        text = stringResource(R.string.ai_engine_list_heading),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.ai_engine_list_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (isRefreshing) {
                        Text(
                            text = stringResource(R.string.ai_engine_auto_refreshing),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else if (!lastRefreshError.isNullOrBlank()) {
                        Text(
                            text = stringResource(
                                R.string.ai_engine_refresh_failed,
                                lastRefreshError.orEmpty()
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            if (allEngines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettingsSuggest,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(R.string.ai_engine_list_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                allEngines.forEach { engine ->
                    EngineListCard(
                        engine = engine,
                        isDefault = currentAIEngine.identityKey() == engine.identityKey(),
                        isFast = fastAIEngine.identityKey() == engine.identityKey(),
                        isLocalOwned = localOwnedEngineKeys.contains(engine.identityKey()),
                        onClick = {
                            component.onNavigate(
                                Screen.AISettings(
                                    Screen.AISettings.Type.EngineDetail(
                                        engineName = engine.name,
                                        requestProtocol = engine.requestProtocol.name,
                                    )
                                )
                            )
                        },
                        onDelete = { pendingDeleteEngine = engine },
                    )
                }
            }
        }
    }

    pendingDeleteEngine?.let { engine ->
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { pendingDeleteEngine = null },
            title = { Text(stringResource(R.string.ai_engine_delete_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.ai_engine_delete_confirm_message,
                        engine.title.ifBlank { engine.name })
                )
            },
            confirmButton = {
                OneBoxDangerButton(
                    text = stringResource(R.string.ai_engine_delete_action),
                    onClick = {
                        component.deleteLocalEngine(engine) { success ->
                            coroutineScope.launch {
                                if (success) {
                                    AppToastHost.showToast(
                                        getString(
                                            R.string.ai_engine_delete_success,
                                            engine.title.ifBlank { engine.name },
                                        )
                                    )
                                    pendingDeleteEngine = null
                                } else {
                                    AppToastHost.showFailureToast(
                                        getString(R.string.ai_engine_delete_failed)
                                    )
                                }
                            }
                        }
                    },
                )
            },
            dismissButton = {
                OneSecondaryButton(
                    text = stringResource(CoreR.string.button_cancel),
                    onClick = { pendingDeleteEngine = null },
                )
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EngineListCard(
    engine: AiEngine,
    isDefault: Boolean,
    isFast: Boolean,
    isLocalOwned: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    var showContextMenu by remember { mutableStateOf(false) }

    Box {
        OneBoxSectionCard(
            modifier = Modifier.clip(OneBoxDesignSystem.sectionCardShape)
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = if (isLocalOwned) {
                        { showContextMenu = true }
                    } else null,
                ),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    val engineIcon = remember(engine.iconName) {
                        IconRegistry.resolve(engine.iconName) ?: com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettingsSuggest
                    }
                    OneBoxLeadingIconBadge(
                        icon = engineIcon
                    )
                    Spacer(modifier = Modifier.size(OneBoxDesignSystem.itemSpacing))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = engine.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = engine.description.ifBlank { engine.model.title },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (isDefault) {
                        EngineBadge(text = stringResource(R.string.ai_engine_role_default))
                    }
                }

                InfoLabel(
                    title = stringResource(R.string.ai_engine_request_protocol_label),
                    value = when (engine.requestProtocol) {
                        AiRequestProtocol.OPENAI_COMPATIBLE -> stringResource(R.string.ai_engine_protocol_openai)
                        AiRequestProtocol.RESPONSES_COMPATIBLE -> stringResource(R.string.ai_engine_protocol_responses)
                        AiRequestProtocol.ANTHROPIC_COMPATIBLE -> stringResource(R.string.ai_engine_protocol_anthropic)
                        AiRequestProtocol.OWN_PROXY -> stringResource(R.string.ai_engine_protocol_proxy)
                        // 仅云端协议出现在本选择器；
                        // LOCAL_ON_DEVICE 由独立的"本地模型管理"页处理（Phase 2）。
                        AiRequestProtocol.LOCAL_ON_DEVICE -> stringResource(R.string.ai_engine_protocol_local_on_device)
                    }
                )

                InfoLabel(
                    title = stringResource(R.string.ai_engine_route_label),
                    value = if (engine.canChatDirectly()) {
                        stringResource(R.string.ai_engine_protocol_effective_direct)
                    } else if (engine.hasProxyRouteConfigured()) {
                        stringResource(R.string.ai_engine_protocol_effective_proxy)
                    } else {
                        stringResource(R.string.ai_engine_protocol_effective_unavailable)
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isFast) {
                        EngineBadge(text = stringResource(R.string.ai_engine_role_fast))
                    }
                    if (engine.hasDirectConnectionReady()) {
                        EngineBadge(text = stringResource(R.string.ai_engine_role_verified))
                    }
                    Spacer(modifier = Modifier.size(4.dp))
                    EngineBadge(
                        text = stringResource(
                            if (isLocalOwned) R.string.ai_engine_local_badge else R.string.ai_engine_remote_badge
                        ),
                        selected = isLocalOwned,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = stringResource(R.string.ai_engine_edit_action),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false }
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.ai_engine_delete_action),
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                onClick = {
                    showContextMenu = false
                    onDelete()
                },
            )
        }
    }
}

@Composable
private fun EngineBadge(
    text: String,
    selected: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .then(if (onClick != null) Modifier.combinedClickable(onClick = onClick) else Modifier)
            .glassBackground(
                style = if (selected) GlassStyle.Thin else GlassStyle.Regular,
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50),
                borderWidth = 0.dp,
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InfoLabel(title: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
