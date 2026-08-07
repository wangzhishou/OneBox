package com.wanbaohe.passwordvault.detail.screenLogic

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.base.auth.AuthorizationCodeStateHolder
import com.shifenmiao.base.utils.ActionUtils
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.passwordvault.R
import com.wanbaohe.passwordvault.model.PasswordVaultDetailEvent
import com.wanbaohe.passwordvault.model.PasswordVaultDetailUiState
import com.wanbaohe.passwordvault.model.PasswordVaultPendingAction
import com.wanbaohe.passwordvault.service.PasswordVaultService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PasswordVaultDetailComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val entryId: String,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigateToType: (Screen.PasswordVault.Type) -> Unit,
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
    private val service: PasswordVaultService,
    private val authCodeStateHolder: AuthorizationCodeStateHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(PasswordVaultDetailUiState(isLoading = true))
    val uiState: StateFlow<PasswordVaultDetailUiState> = _uiState.asStateFlow()

    init {
        observeAuth()
        observeEntryUpdates()
        loadEntry()
    }

    fun handleEvent(event: PasswordVaultDetailEvent) {
        when (event) {
            PasswordVaultDetailEvent.Edit -> onNavigateToType(
                Screen.PasswordVault.Type.Edit(entryId)
            )
            PasswordVaultDetailEvent.Delete -> onRequestDelete()
            PasswordVaultDetailEvent.ConfirmDelete -> deleteEntry()
            PasswordVaultDetailEvent.DismissDelete -> _uiState.value = _uiState.value.copy(
                showDeleteConfirm = false
            )
            PasswordVaultDetailEvent.RequestPasswordReveal -> onRequestPasswordReveal()
            PasswordVaultDetailEvent.RequestPasswordCopy -> onRequestCopy(
                PasswordVaultPendingAction.CopyPassword
            )
            PasswordVaultDetailEvent.RequestAccountCopy -> onCopyAccount()
            is PasswordVaultDetailEvent.RequestSecretFieldReveal -> onRequestSecretFieldReveal(
                event.fieldId
            )
            is PasswordVaultDetailEvent.RequestSecretFieldCopy -> onRequestCopy(
                PasswordVaultPendingAction.CopySecretField(event.fieldId)
            )
            PasswordVaultDetailEvent.Unlock -> requestAuth()
        }
    }

    private fun observeAuth() {
        componentScope.launch {
            authCodeStateHolder.authCode.collect { code ->
                val wasAuthorized = _uiState.value.isAuthorized
                val isAuthorized = !code.isNullOrEmpty()
                _uiState.value = _uiState.value.copy(isAuthorized = isAuthorized)
                if (isAuthorized && !wasAuthorized) {
                    // 授权通过后先加载完整记录,再执行挂起动作,保证复制到的是解密后的真实值
                    loadEntry(onLoaded = ::executePendingAction)
                }
            }
        }
    }

    /**
     * 监听数据库变化:编辑保存返回本页、或其他入口修改了该记录时自动刷新,
     * 避免组件被路由缓存复用时展示过期数据。
     */
    private fun observeEntryUpdates() {
        componentScope.launch {
            service.observeEntrySummaries()
                .map { summaries -> summaries.firstOrNull { it.id == entryId }?.updatedAt }
                .distinctUntilChanged()
                .drop(1)
                .collect { updatedAt ->
                    if (updatedAt != null) loadEntry()
                }
        }
    }

    private fun loadEntry(onLoaded: (() -> Unit)? = null) {
        componentScope.launch {
            // 已有内容时刷新不闪 loading,仅首次进入展示加载态
            _uiState.value = _uiState.value.copy(
                isLoading = _uiState.value.entry == null,
                errorMessage = null
            )
            val summary = service.getEntrySummary(entryId)
            if (summary == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = context.getString(R.string.password_vault_error_entry_not_found)
                )
                return@launch
            }
            val code = authCodeStateHolder.authCode.value
            val fullEntry = code?.let { service.getEntry(entryId, it) }
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                entry = fullEntry ?: summary,
                errorMessage = null
            )
            onLoaded?.invoke()
        }
    }

    private fun onRequestPasswordReveal() {
        if (_uiState.value.isAuthorized) {
            _uiState.value = _uiState.value.copy(
                isPasswordRevealed = !_uiState.value.isPasswordRevealed
            )
        } else {
            _uiState.value = _uiState.value.copy(
                pendingAction = PasswordVaultPendingAction.RevealPassword
            )
            requestAuth()
        }
    }

    private fun onRequestSecretFieldReveal(fieldId: String) {
        if (_uiState.value.isAuthorized) {
            toggleSecretFieldReveal(fieldId)
        } else {
            _uiState.value = _uiState.value.copy(
                pendingAction = PasswordVaultPendingAction.RevealSecretField(fieldId)
            )
            requestAuth()
        }
    }

    private fun toggleSecretFieldReveal(fieldId: String) {
        val current = _uiState.value.revealedSecretFieldIds
        _uiState.value = _uiState.value.copy(
            revealedSecretFieldIds = if (fieldId in current) current - fieldId else current + fieldId
        )
    }

    private fun onRequestCopy(action: PasswordVaultPendingAction) {
        if (_uiState.value.isAuthorized) {
            performCopy(action)
        } else {
            _uiState.value = _uiState.value.copy(pendingAction = action)
            requestAuth()
        }
    }

    private fun onCopyAccount() {
        // 账号为明文存储且已展示在页面上,复制无需额外授权
        val account = _uiState.value.entry?.account
        if (!account.isNullOrBlank()) {
            copyToClipboard(account)
        }
    }

    private fun onRequestDelete() {
        if (_uiState.value.isAuthorized) {
            _uiState.value = _uiState.value.copy(showDeleteConfirm = true)
        } else {
            _uiState.value = _uiState.value.copy(
                pendingAction = PasswordVaultPendingAction.Delete
            )
            requestAuth()
        }
    }

    private fun executePendingAction() {
        when (val action = _uiState.value.pendingAction) {
            PasswordVaultPendingAction.RevealPassword -> {
                _uiState.value = _uiState.value.copy(isPasswordRevealed = true)
            }
            is PasswordVaultPendingAction.RevealSecretField -> {
                toggleSecretFieldReveal(action.fieldId)
            }
            PasswordVaultPendingAction.Delete -> {
                _uiState.value = _uiState.value.copy(showDeleteConfirm = true)
            }
            is PasswordVaultPendingAction.CopySecretField,
            PasswordVaultPendingAction.CopyPassword,
            PasswordVaultPendingAction.CopyAccount -> {
                performCopy(action)
            }
            null -> Unit
        }
        if (_uiState.value.pendingAction != null) {
            _uiState.value = _uiState.value.copy(pendingAction = null)
        }
    }

    private fun performCopy(action: PasswordVaultPendingAction) {
        val entry = _uiState.value.entry ?: return
        val value = when (action) {
            PasswordVaultPendingAction.CopyPassword -> entry.password
            PasswordVaultPendingAction.CopyAccount -> entry.account
            is PasswordVaultPendingAction.CopySecretField -> {
                entry.secretFields.firstOrNull { it.id == action.fieldId }?.value
            }
            else -> null
        }
        if (!value.isNullOrEmpty()) {
            copyToClipboard(value)
        }
    }

    private fun copyToClipboard(value: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return
        clipboard.setPrimaryClip(ClipData.newPlainText("password_vault", value))
        Toast.makeText(context, R.string.password_vault_copied, Toast.LENGTH_SHORT).show()
    }

    private fun requestAuth() {
        ActionUtils.showAuthCode(
            source = "vault_detail_reveal",
        )
    }

    private fun deleteEntry() {
        componentScope.launch {
            service.deleteEntry(entryId)
            onGoBack()
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            entryId: String,
            onGoBack: () -> Unit,
            onNavigateToType: (Screen.PasswordVault.Type) -> Unit,
        ): PasswordVaultDetailComponent
    }
}
