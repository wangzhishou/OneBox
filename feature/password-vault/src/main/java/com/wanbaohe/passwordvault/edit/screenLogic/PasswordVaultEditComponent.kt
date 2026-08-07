package com.wanbaohe.passwordvault.edit.screenLogic

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.base.auth.AuthorizationCodeStateHolder
import com.shifenmiao.base.utils.ActionUtils
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.passwordvault.R
import com.wanbaohe.passwordvault.model.PasswordVaultCategoryEvent
import com.wanbaohe.passwordvault.model.PasswordVaultCategoryUi
import com.wanbaohe.passwordvault.model.PasswordVaultEditEvent
import com.wanbaohe.passwordvault.model.PasswordVaultEditState
import com.wanbaohe.passwordvault.model.SecretField
import com.wanbaohe.passwordvault.model.VaultEntry
import com.wanbaohe.passwordvault.service.PasswordVaultService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class PasswordVaultEditComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val entryId: String?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigateToType: (Screen.PasswordVault.Type) -> Unit,
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
    private val service: PasswordVaultService,
    private val authCodeStateHolder: AuthorizationCodeStateHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(PasswordVaultEditState())
    val uiState: StateFlow<PasswordVaultEditState> = _uiState.asStateFlow()

    init {
        componentScope.launch { service.ensureDefaultCategories() }
        observeAuth()
        observeCategories()
        if (authCodeStateHolder.isAuthorized) {
            entryId?.let { loadEntry(it) }
        } else {
            requestInitialAuth()
        }
    }

    fun handleEvent(event: PasswordVaultEditEvent) {
        when (event) {
            is PasswordVaultEditEvent.TitleChanged -> _uiState.value = _uiState.value.copy(
                title = event.value
            )
            is PasswordVaultEditEvent.CategoryChanged -> _uiState.value = _uiState.value.copy(
                categoryId = event.categoryId
            )
            is PasswordVaultEditEvent.AccountChanged -> _uiState.value = _uiState.value.copy(
                account = event.value
            )
            is PasswordVaultEditEvent.PasswordChanged -> _uiState.value = _uiState.value.copy(
                password = event.value
            )
            is PasswordVaultEditEvent.NoteChanged -> _uiState.value = _uiState.value.copy(
                note = event.value
            )
            is PasswordVaultEditEvent.TogglePasswordVisibility -> _uiState.value = _uiState.value.copy(
                isPasswordVisible = event.visible
            )
            is PasswordVaultEditEvent.AddSecretField -> addSecretField(event.label, event.value)
            is PasswordVaultEditEvent.UpdateSecretField -> updateSecretField(
                event.fieldId,
                event.label,
                event.value
            )
            is PasswordVaultEditEvent.RemoveSecretField -> removeSecretField(event.fieldId)
            PasswordVaultEditEvent.Save -> save()
            PasswordVaultEditEvent.Unlock -> requestInitialAuth()
        }
    }

    fun handleCategoryEvent(event: PasswordVaultCategoryEvent) {
        when (event) {
            is PasswordVaultCategoryEvent.Add -> componentScope.launch {
                service.addCategory(event.name)
            }
            is PasswordVaultCategoryEvent.Rename -> componentScope.launch {
                service.renameCategory(event.categoryId, event.newName)
            }
            is PasswordVaultCategoryEvent.Delete -> componentScope.launch {
                service.deleteCategory(event.categoryId)
                if (_uiState.value.categoryId == event.categoryId) {
                    _uiState.value = _uiState.value.copy(categoryId = null)
                }
            }
            is PasswordVaultCategoryEvent.Reorder -> componentScope.launch {
                service.reorderCategories(event.orderedIds)
            }
        }
    }

    private fun observeAuth() {
        componentScope.launch {
            authCodeStateHolder.authCode.collect { code ->
                val wasAuthorized = _uiState.value.isAuthorized
                val isAuthorized = !code.isNullOrEmpty()
                _uiState.value = _uiState.value.copy(isAuthorized = isAuthorized)
                if (isAuthorized && !wasAuthorized && entryId != null) {
                    loadEntry(entryId)
                }
            }
        }
    }

    private fun observeCategories() {
        componentScope.launch {
            service.observeCategories().collect { categories: List<PasswordVaultCategoryUi> ->
                val current = _uiState.value
                val resolvedCategoryId = current.categoryId
                    ?: categories.firstOrNull { it.isDefault }?.id
                    ?: categories.firstOrNull()?.id
                _uiState.value = current.copy(
                    availableCategories = categories,
                    categoryId = resolvedCategoryId,
                )
            }
        }
    }

    private fun requestInitialAuth() {
        ActionUtils.showAuthCode(
            source = "vault_edit_initial",
        )
    }

    private fun loadEntry(entryId: String) {
        componentScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val code = authCodeStateHolder.authCode.value
            val entry = code?.let { service.getEntry(entryId, it) }
            if (entry != null) {
                _uiState.value = _uiState.value.copy(
                    entryId = entry.id,
                    title = entry.title,
                    categoryId = entry.categoryId,
                    account = entry.account ?: "",
                    password = entry.password,
                    secretFields = entry.secretFields,
                    note = entry.note,
                    createdAt = entry.createdAt,
                    isAuthorized = authCodeStateHolder.isAuthorized,
                    isLoading = false,
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = context.getString(R.string.password_vault_error_entry_not_found),
                )
            }
        }
    }

    private fun addSecretField(label: String, value: String) {
        if (label.isBlank()) return
        val fields = _uiState.value.secretFields + SecretField(
            id = UUID.randomUUID().toString(),
            label = label.trim(),
            value = value
        )
        _uiState.value = _uiState.value.copy(secretFields = fields)
    }

    private fun updateSecretField(fieldId: String, label: String, value: String) {
        val fields = _uiState.value.secretFields.map {
            if (it.id == fieldId) it.copy(label = label, value = value) else it
        }
        _uiState.value = _uiState.value.copy(secretFields = fields)
    }

    private fun removeSecretField(fieldId: String) {
        val fields = _uiState.value.secretFields.filter { it.id != fieldId }
        _uiState.value = _uiState.value.copy(secretFields = fields)
    }

    private fun save() {
        val code = authCodeStateHolder.authCode.value
        if (code.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = context.getString(R.string.password_vault_error_not_unlocked)
            )
            requestInitialAuth()
            return
        }
        val state = _uiState.value
        if (!state.isValid || state.isLoading) return

        componentScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            val now = System.currentTimeMillis()
            val newEntryId = state.entryId ?: UUID.randomUUID().toString()
            val createdAt = state.createdAt.takeIf { it > 0 } ?: now

            service.saveEntry(
                masterPassword = code,
                entry = VaultEntry(
                    id = newEntryId,
                    title = state.title.trim(),
                    categoryId = state.categoryId.orEmpty(),
                    categoryName = state.availableCategories
                        .firstOrNull { it.id == state.categoryId }
                        ?.name
                        .orEmpty(),
                    account = state.account.trim().takeIf { it.isNotBlank() },
                    password = state.password,
                    secretFields = state.secretFields,
                    note = state.note.trim(),
                    createdAt = createdAt,
                    updatedAt = now,
                )
            )
            onNavigateToType(Screen.PasswordVault.Type.Detail(newEntryId))
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            entryId: String?,
            onGoBack: () -> Unit,
            onNavigateToType: (Screen.PasswordVault.Type) -> Unit,
        ): PasswordVaultEditComponent
    }
}
