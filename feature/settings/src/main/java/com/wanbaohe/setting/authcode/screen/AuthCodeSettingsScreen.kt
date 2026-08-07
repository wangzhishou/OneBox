package com.wanbaohe.setting.authcode.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Refresh
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.handle.HandleEvent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.rememberImmersiveModeState
import com.shifenmiao.database.item.entity.ItemWithCategories
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.LockScreenActions
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.LockScreenBase
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.LockScreenStatus
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxListItem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxThemedIconBadge
import com.t8rin.imagetoolbox.core.ui.widget.utils.FullscreenPopup
import com.t8rin.imagetoolbox.core.utils.getString
import com.wanbaohe.setting.authcode.component.AuthCodeSettingsComponent
import kotlinx.coroutines.launch
import com.wanbaohe.settings.R as SettingsR
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLock
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUnlock
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandLess
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibilityOff

private const val PIN_LENGTH = 6

/**
 * 内部状态机:
 * - [AuthCodeFlow.Set] 流程: AwaitingFirstCode -> AwaitingConfirmFirst(first) -> 完成
 * - [AuthCodeFlow.Change] 流程: AwaitingOld -> AwaitingFirstCode -> AwaitingConfirmNew(first) -> 完成
 * - [AuthCodeFlow.VerifyOnly] 流程: AwaitingOld -> 完成回调
 */
private sealed interface Step {
    data object AwaitingOld : Step
    data object AwaitingFirstCode : Step
    data class AwaitingConfirmFirst(val first: String) : Step
    data class AwaitingConfirmNew(val first: String) : Step
}

private sealed interface AuthCodeFlow {
    data object Set : AuthCodeFlow
    data object Change : AuthCodeFlow
    data class VerifyOnly(
        val title: String,
        val onVerified: () -> Unit,
    ) : AuthCodeFlow
}

private data class FlowStrings(
    val mismatchError: String,
    val wrongCurrentError: String,
    val saveFailed: String,
)

@Composable
fun AuthCodeSettingsScreen(component: AuthCodeSettingsComponent) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val onNavigate = LocalOnNavigate.current
    val immersiveModeState = rememberImmersiveModeState()

    var loading by remember { mutableStateOf(true) }
    var hasCode by remember { mutableStateOf(false) }
    var flowType by remember { mutableStateOf<AuthCodeFlow?>(null) }
    val protectedItems by component.observeProtectedItems()
        .collectAsState(initial = emptyList())

    val unnamedFallback = stringResource(SettingsR.string.auth_code_settings_unnamed_item)
    val failedText = stringResource(SettingsR.string.auth_code_settings_failed)
    val protectedRemoveTitle = stringResource(
        SettingsR.string.auth_code_settings_protected_verify_title
    )
    val setSuccessText = stringResource(SettingsR.string.auth_code_settings_success_set)
    val changeSuccessText = stringResource(SettingsR.string.auth_code_settings_success_change)
    val removeSuccessText = stringResource(SettingsR.string.auth_code_settings_success_remove)

    LaunchedEffect(Unit) {
        hasCode = component.hasCode()
        loading = false
    }

    // 修改 / 验证 / 关闭 任意流程开启时,自动进入沉浸式,TopAppBar 让位
    LaunchedEffect(flowType != null) {
        if (flowType != null) immersiveModeState.enterImmersive()
    }

    BaseScreen(
        title = {
            Text(
                text = stringResource(SettingsR.string.auth_code_settings_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        onGoBack = component.onGoBack,
        isShowDefaultActions = true,
        showNavigationBarsPadding = false,
        supportGlassEffect = true,
        immersiveModeState = immersiveModeState,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = OneBoxDesignSystem.screenPadding)
        ) {
            item { Spacer(modifier = Modifier.height(OneBoxDesignSystem.screenTopSpacing)) }

            item {
                OneBoxSectionCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(OneBoxDesignSystem.cardPadding)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)) {
                        StatusRow(loading = loading, hasCode = hasCode)

                        if (hasCode) {
                            OneBoxListItem(
                                onClick = { flowType = AuthCodeFlow.Change },
                                headlineContent = {
                                    Text(
                                        text = stringResource(SettingsR.string.auth_code_settings_action_change),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                },
                                leadingContent = {
                                    OneBoxThemedIconBadge(
                                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                                        themeIndex = 0,
                                    )
                                },
                            )
                            // 关闭密码保护:走全局 ActionUtils.showAuthCode,
                            // 已被授权则直接关,未授权则弹全局锁屏;
                            // 关闭时同时清理所有已保护 item,避免残留
                            OneBoxListItem(
                                onClick = {
                                    ActionUtils.showAuthCode(
                                        source = "auth_code_settings_disable_global",
                                        onSuccess = {
                                            scope.launch {
                                                runCatching { component.clearCodeAndAllProtection() }
                                                    .onSuccess {
                                                        hasCode = false
                                                        AppToastHost.showToast(removeSuccessText)
                                                    }
                                                    .onFailure {
                                                        AppToastHost.showToast(failedText)
                                                    }
                                            }
                                        },
                                    )
                                },
                                headlineContent = {
                                    Text(
                                        text = stringResource(SettingsR.string.auth_code_settings_action_remove),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                },
                                leadingContent = {
                                    OneBoxThemedIconBadge(
                                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUnlock,
                                        themeIndex = 1,
                                    )
                                },
                            )
                        } else {
                            OneBoxListItem(
                                onClick = { flowType = AuthCodeFlow.Set },
                                headlineContent = {
                                    Text(
                                        text = stringResource(SettingsR.string.auth_code_settings_action_set),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                },
                                leadingContent = {
                                    OneBoxThemedIconBadge(
                                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLock,
                                        themeIndex = 0,
                                    )
                                },
                            )
                        }
                    }
                }
            }

            // 已开启密码保护的应用列表:仅在已设置密码时有意义
            if (hasCode) {
                item {
                    Spacer(modifier = Modifier.height(OneBoxDesignSystem.blockSpacing))
                    ProtectedItemsSection(
                        items = protectedItems,
                        onRemoveRequested = { item ->
                            flowType = AuthCodeFlow.VerifyOnly(
                                title = protectedRemoveTitle,
                                onVerified = {
                                    scope.launch {
                                        runCatching { component.disableProtection(item.item.id) }
                                            .onSuccess {
                                                AppToastHost.showToast(
                                                    getString(
                                                        SettingsR.string.auth_code_settings_protected_remove_success,
                                                        item.item.title.ifBlank { unnamedFallback }
                                                    )
                                                )
                                            }
                                            .onFailure {
                                                AppToastHost.showToast(failedText)
                                            }
                                    }
                                }
                            )
                        },
                        onItemClick = { item ->
                            scope.launch {
                                val resource = component.loadItemResource(
                                    itemId = item.item.id,
                                    listType = item.item.listType,
                                )
                                // 已在密码设置页内,跳过授权码验证直接导航
                                HandleEvent.handleCardClickDirect(
                                    context = context,
                                    onNavigate = onNavigate,
                                    itemWithRelation = item,
                                    resource = resource,
                                )
                            }
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(OneBoxDesignSystem.blockSpacing)) }
        }
    }

    // 用动画状态机控制锁屏弹窗的出现/消失:
    // flowType 变化时先触发动画,动画完成后再真正移除 composable
    var pendingDismiss by remember { mutableStateOf(false) }
    var dialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(flowType) {
        if (flowType != null) {
            pendingDismiss = false
            dialogVisible = true
        } else if (dialogVisible) {
            // flowType 被置空 -> 触发退出动画,完成后才真正隐藏
            pendingDismiss = true
        }
    }

    if (dialogVisible) {
        val currentType = flowType
        AuthCodeFlowDialog(
            type = currentType,
            visible = !pendingDismiss,
            component = component,
            onDismissed = {
                // 退出动画结束后清理状态
                dialogVisible = false
                pendingDismiss = false
                immersiveModeState.exitImmersive()
            },
            onDismiss = {
                flowType = null
                immersiveModeState.exitImmersive()
            },
            onSuccess = {
                val successText = when (currentType) {
                    AuthCodeFlow.Set -> {
                        hasCode = true
                        setSuccessText
                    }
                    AuthCodeFlow.Change -> {
                        hasCode = true
                        changeSuccessText
                    }
                    is AuthCodeFlow.VerifyOnly -> null
                    null -> null
                }
                flowType = null
                immersiveModeState.exitImmersive()
                if (successText != null) {
                    AppToastHost.showToast(successText)
                }
            }
        )
    }
}

@Composable
private fun StatusRow(loading: Boolean, hasCode: Boolean) {
    val text = when {
        loading -> stringResource(SettingsR.string.auth_code_settings_status_awaiting)
        hasCode -> stringResource(SettingsR.string.auth_code_settings_status_on)
        else -> stringResource(SettingsR.string.auth_code_settings_status_off)
    }
    val color = if (hasCode) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (hasCode) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLock else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUnlock,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = color,
        )
        Text(
            text = stringResource(SettingsR.string.auth_code_settings_title),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ProtectedItemsSection(
    items: List<ItemWithCategories>,
    onRemoveRequested: (ItemWithCategories) -> Unit,
    onItemClick: (ItemWithCategories) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    OneBoxSectionCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(OneBoxDesignSystem.cardPadding)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(OneBoxDesignSystem.smallRadius))
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(SettingsR.string.auth_code_settings_section_protected),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = items.size.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.size(4.dp))
                Icon(
                    imageVector = if (expanded) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandLess else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                if (items.isEmpty()) {
                    Text(
                        text = stringResource(SettingsR.string.auth_code_settings_protected_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items.forEach { item ->
                            ProtectedItemRow(
                                key = item.item.id,
                                item = item,
                                onRemove = { onRemoveRequested(item) },
                                onClick = { onItemClick(item) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProtectedItemRow(
    key: Int,
    item: ItemWithCategories,
    onRemove: () -> Unit,
    onClick: () -> Unit,
) {
    val unnamedText = stringResource(SettingsR.string.auth_code_settings_unnamed_item)
    val displayTitle = item.item.title.ifBlank { unnamedText }
    val closeButtonText = stringResource(SettingsR.string.auth_code_settings_protected_close_button)

    OneBoxListItem(
        onClick = onClick,
        headlineContent = {
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        },
        leadingContent = {
            OneBoxThemedIconBadge(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLock,
                themeIndex = key % 4,
            )
        },
        trailingContent = {
            // 显式「关闭保护」按钮:
            // IconButton 自身会消费点击事件,不会冒泡到行 onClick
            IconButton(
                onClick = onRemove,
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibilityOff,
                    contentDescription = closeButtonText,
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        },
    )
}

@Composable
private fun AuthCodeFlowDialog(
    type: AuthCodeFlow?,
    visible: Boolean,
    component: AuthCodeSettingsComponent,
    onDismissed: () -> Unit,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var step by remember { mutableStateOf<Step>(initialStep(type ?: AuthCodeFlow.Set)) }
    var status by remember { mutableStateOf(LockScreenStatus.Awaiting) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val strings = FlowStrings(
        mismatchError = stringResource(SettingsR.string.auth_code_settings_error_mismatch),
        wrongCurrentError = stringResource(SettingsR.string.auth_code_settings_error_wrong_current),
        saveFailed = stringResource(SettingsR.string.auth_code_settings_failed),
    )
    val awaitingHint = stringResource(SettingsR.string.auth_code_settings_status_awaiting)
    val pinLabel = stringResource(SettingsR.string.auth_code_settings_pin_label)

    val title = when (type) {
        is AuthCodeFlow.VerifyOnly -> type.title
        AuthCodeFlow.Set -> when (step) {
            Step.AwaitingFirstCode -> stringResource(SettingsR.string.auth_code_settings_step_enter_first)
            is Step.AwaitingConfirmFirst -> stringResource(SettingsR.string.auth_code_settings_step_confirm_first)
            else -> stringResource(SettingsR.string.auth_code_settings_step_enter_first)
        }
        AuthCodeFlow.Change -> when (step) {
            Step.AwaitingOld -> stringResource(SettingsR.string.auth_code_settings_step_verify_current)
            Step.AwaitingFirstCode -> stringResource(SettingsR.string.auth_code_settings_step_enter_new)
            is Step.AwaitingConfirmNew -> stringResource(SettingsR.string.auth_code_settings_step_confirm_new)
            else -> stringResource(SettingsR.string.auth_code_settings_step_verify_current)
        }
        null -> ""
    }
    val canConfirm = !isSaving && input.length == PIN_LENGTH

    fun resetInput() {
        input = ""
    }

    /**
     * 修复 Bug:输入发生改变时,如果当前处于 Error 状态,清掉红色高亮,
     * 让新一轮输入以 Awaiting 状态开始,避免 PIN 圆点持续红。
     */
    fun clearErrorOnEdit() {
        if (status == LockScreenStatus.Error) {
            status = LockScreenStatus.Awaiting
            statusText = null
        }
    }

    fun handleComplete() {
        if (input.length != PIN_LENGTH || type == null) return
        when (type) {
            AuthCodeFlow.Set -> {
                val current = step
                if (current is Step.AwaitingFirstCode) {
                    val first = input
                    step = Step.AwaitingConfirmFirst(first)
                    resetInput()
                    status = LockScreenStatus.Awaiting
                    statusText = null
                } else if (current is Step.AwaitingConfirmFirst) {
                    if (current.first != input) {
                        step = Step.AwaitingFirstCode
                        status = LockScreenStatus.Error
                        statusText = strings.mismatchError
                        resetInput()
                    } else {
                        val second = input
                        isSaving = true
                        scope.launch {
                            runCatching { component.setCode(second) }
                                .onSuccess { onSuccess() }
                                .onFailure {
                                    isSaving = false
                                    status = LockScreenStatus.Error
                                    statusText = strings.saveFailed
                                    step = Step.AwaitingFirstCode
                                    resetInput()
                                }
                        }
                    }
                }
            }
            AuthCodeFlow.Change -> {
                when (val current = step) {
                    Step.AwaitingOld -> {
                        val candidate = input
                        resetInput()
                        scope.launch {
                            val ok = runCatching { component.verifyCode(candidate) }.getOrDefault(false)
                            if (ok) {
                                step = Step.AwaitingFirstCode
                                status = LockScreenStatus.Awaiting
                                statusText = null
                            } else {
                                status = LockScreenStatus.Error
                                statusText = strings.wrongCurrentError
                            }
                        }
                    }
                    Step.AwaitingFirstCode -> {
                        val first = input
                        step = Step.AwaitingConfirmNew(first)
                        resetInput()
                        status = LockScreenStatus.Awaiting
                        statusText = null
                    }
                    is Step.AwaitingConfirmNew -> {
                        if (current.first != input) {
                            step = Step.AwaitingFirstCode
                            status = LockScreenStatus.Error
                            statusText = strings.mismatchError
                            resetInput()
                        } else {
                            val second = input
                            isSaving = true
                            scope.launch {
                                runCatching { component.setCode(second) }
                                    .onSuccess { onSuccess() }
                                    .onFailure {
                                        isSaving = false
                                        status = LockScreenStatus.Error
                                        statusText = strings.saveFailed
                                        step = Step.AwaitingFirstCode
                                        resetInput()
                                    }
                            }
                        }
                    }
                    else -> Unit
                }
            }
            is AuthCodeFlow.VerifyOnly -> {
                if (step !is Step.AwaitingOld) return
                val candidate = input
                resetInput()
                scope.launch {
                    val ok = runCatching { component.verifyCode(candidate) }.getOrDefault(false)
                    if (ok) {
                        type.onVerified()
                        onSuccess()
                    } else {
                        status = LockScreenStatus.Error
                        statusText = strings.wrongCurrentError
                    }
                }
            }
        }
    }
    FullscreenPopup(
        onDismiss = { if (!isSaving) onDismiss() },
        placeAboveAll = true,
    ) {
        var animateIn by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { animateIn = true }

        AnimatedVisibility(
            visible = animateIn && visible,
            enter = fadeIn(tween(240)) + scaleIn(
                initialScale = 0.92f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            ),
            exit = fadeOut(tween(200)) + scaleOut(
                targetScale = 0.92f,
                animationSpec = tween(200),
            ),
        ) {
            LockScreenBase(
                pinLength = PIN_LENGTH,
                filledLength = input.length,
                actions = LockScreenActions(
                    onDigit = { digit ->
                        if (!isSaving && input.length < PIN_LENGTH) {
                            clearErrorOnEdit()
                            input += digit
                        }
                    },
                    onBackspace = {
                        if (!isSaving && input.isNotEmpty()) {
                            clearErrorOnEdit()
                            input = input.dropLast(1)
                        }
                    },
                    onClear = {
                        if (!isSaving) {
                            clearErrorOnEdit()
                            resetInput()
                        }
                    },
                    onAction = {
                        if (canConfirm) handleComplete() else if (!isSaving) onDismiss()
                    },
                    onEmergency = {
                        if (!isSaving) onDismiss()
                    },
                ),
                showBiometric = false,
                title = title,
                statusText = statusText ?: awaitingHint,
                status = status,
                pinLabel = pinLabel,
                actionKeyIcon = if (canConfirm) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check else Icons.Outlined.Cancel,
                actionKeyTint = if (canConfirm) MaterialTheme.colorScheme.primary else null,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // 监听 visible 从 true -> false 的退出动画,动画结束后回调上层清理状态
        LaunchedEffect(visible) {
            if (!visible && animateIn) {
                // 等待退出动画时间 (与 exit tween 时长一致) 后再回调
                kotlinx.coroutines.delay(220)
                onDismissed()
            }
        }
    }
}

private fun initialStep(type: AuthCodeFlow): Step = when (type) {
    AuthCodeFlow.Set -> Step.AwaitingFirstCode
    AuthCodeFlow.Change, is AuthCodeFlow.VerifyOnly -> Step.AwaitingOld
}
