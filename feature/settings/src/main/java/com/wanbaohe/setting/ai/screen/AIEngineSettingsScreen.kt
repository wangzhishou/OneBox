package com.wanbaohe.setting.ai.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.shifenmiao.common.ui.ai.providerAccentColor
import com.shifenmiao.common.ui.ai.providerBrandIcon
import com.shifenmiao.model.ai.AiEngine
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
    BaseScreen(
        title = stringResource(CoreR.string.profile_item_ai_service_and_models),
        onGoBack = component.onGoBack,
        supportGlassEffect = true,
        actions = {
            AIEngineAddEngineAction(onNavigate = component.onNavigate)
        }
    ) {
        AIEngineSettingsContent(
            component = component,
            modifier = Modifier.weight(1f),
        )
    }
}

// 「新增引擎」入口按钮: 渠道能力(如 Google 全量放开)或国内管理员可见;
// 独立页面放在顶栏 actions, 聚合页放在 TabRow 尾部
@Composable
fun AIEngineAddEngineAction(
    onNavigate: (Screen) -> Unit,
) {
    val capabilities = remember { AiEngineConfig.getCapabilities() }
    if (capabilities.canAddEngine || LoginUtils.isAdmin()) {
        IconButton(onClick = {
            onNavigate(
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

@Composable
fun AIEngineSettingsContent(
    component: AIEngineSettingsComponent,
    modifier: Modifier = Modifier,
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

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
        ) {
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

        // 目录刷新状态悬浮底部轻提示(刷新中/失败), 不占列表位
        AnimatedVisibility(
            visible = isRefreshing || !lastRefreshError.isNullOrBlank(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
        ) {
            RefreshStatusPill(isRefreshing = isRefreshing, error = lastRefreshError)
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
                    val brandIcon = providerBrandIcon(engine.name)
                    if (brandIcon != null) {
                        val accent = providerAccentColor(engine.name)
                            ?: MaterialTheme.colorScheme.primary
                        OneBoxLeadingIconBadge(
                            icon = brandIcon,
                            iconTint = accent,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        )
                    } else {
                        val engineIcon = remember(engine.iconName) {
                            IconRegistry.resolve(engine.iconName) ?: com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettingsSuggest
                        }
                        OneBoxLeadingIconBadge(
                            icon = engineIcon,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        )
                    }
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
                        // 副标题展示当前选中模型名(品牌名 + 模型名, 不再展示引擎简介)
                        Text(
                            text = engine.model.title.ifBlank { engine.model.name },
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 链路状态:可用 / 不可用(不可用红色警示)
                    val routeAvailable = engine.hasAvailableChatRoute()
                    EngineBadge(
                        text = stringResource(
                            if (routeAvailable) R.string.ai_engine_status_available
                            else R.string.ai_engine_status_unavailable
                        ),
                        isError = !routeAvailable,
                    )
                    if (isFast) {
                        EngineBadge(text = stringResource(R.string.ai_engine_role_fast))
                    }
                    if (engine.hasDirectConnectionReady()) {
                        EngineBadge(text = stringResource(R.string.ai_engine_role_verified))
                    }
                    // 代理中转链路按模型倍率扣积分,展示当前选中模型的倍率
                    if (engine.usesProxyRoute()) {
                        EngineBadge(text = engine.model.pointsMultiplierText())
                    }
                    // 链路标签按真实路由显示: 走 App 中转=代理(主色), 可直连官方=直连; 与"不可用"天然互斥
                    when {
                        engine.usesProxyRoute() -> EngineBadge(
                            text = stringResource(R.string.ai_engine_remote_badge),
                            isPrimary = true,
                        )
                        engine.canChatDirectly() -> EngineBadge(
                            text = stringResource(R.string.ai_engine_local_badge),
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = stringResource(R.string.ai_engine_edit_action),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
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
    isError: Boolean = false,
    isPrimary: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val contentColor = when {
        isError -> MaterialTheme.colorScheme.error
        isPrimary -> MaterialTheme.colorScheme.primary
        selected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .then(if (onClick != null) Modifier.combinedClickable(onClick = onClick) else Modifier)
            .glassBackground(
                style = if (selected) GlassStyle.Thin else GlassStyle.Regular,
                color = when {
                    isError -> MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
                    isPrimary -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    selected -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(50),
                borderWidth = 0.dp,
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}

// 目录刷新状态: 悬浮底部 pill(刷新中带小圈, 失败红字)
@Composable
private fun RefreshStatusPill(
    isRefreshing: Boolean,
    error: String?,
) {
    Row(
        modifier = Modifier
            .glassBackground(
                style = GlassStyle.Thin,
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f),
                shape = RoundedCornerShape(50),
                borderWidth = 0.dp,
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (isRefreshing) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp,
            )
            Text(
                text = stringResource(R.string.ai_engine_auto_refreshing),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (!error.isNullOrBlank()) {
            Text(
                text = stringResource(R.string.ai_engine_refresh_failed, error),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
