package com.wanbaohe.passwordvault.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
data class VaultEntry(
    val id: String,
    val title: String,
    val categoryId: String,
    val categoryName: String,
    val account: String? = null,
    val password: String = "",
    val secretFields: List<SecretField> = emptyList(),
    val note: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

@Serializable
@Immutable
data class SecretField(
    val id: String,
    val label: String,
    val value: String,
)

@Immutable
data class PasswordVaultCategoryUi(
    val id: String,
    val name: String,
    val sortOrder: Int = 0,
    val isDefault: Boolean = false,
)

@Immutable
data class PasswordVaultListUiState(
    val isLoading: Boolean = false,
    val entries: List<VaultEntry> = emptyList(),
    val categories: List<PasswordVaultCategoryUi> = emptyList(),
    val selectedCategoryId: String? = null,
    val query: String = "",
    val errorMessage: String? = null,
)

sealed interface PasswordVaultListEvent {
    data class OpenEntry(val entryId: String) : PasswordVaultListEvent
    data object CreateEntry : PasswordVaultListEvent
    data class SelectCategory(val categoryId: String?) : PasswordVaultListEvent
    data class QueryChanged(val query: String) : PasswordVaultListEvent
    data object ClearFilters : PasswordVaultListEvent
}

@Immutable
data class PasswordVaultDetailUiState(
    val entry: VaultEntry? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showDeleteConfirm: Boolean = false,
    val isAuthorized: Boolean = false,
    val isPasswordRevealed: Boolean = false,
    val revealedSecretFieldIds: Set<String> = emptySet(),
    val pendingAction: PasswordVaultPendingAction? = null,
)

/**
 * 授权成功后才允许继续执行的动作。
 * 未授权时先挂起，授权码校验通过、完整记录加载完成后再执行。
 */
@Immutable
sealed interface PasswordVaultPendingAction {
    data object RevealPassword : PasswordVaultPendingAction
    data class RevealSecretField(val fieldId: String) : PasswordVaultPendingAction
    data object CopyPassword : PasswordVaultPendingAction
    data object CopyAccount : PasswordVaultPendingAction
    data class CopySecretField(val fieldId: String) : PasswordVaultPendingAction
    data object Delete : PasswordVaultPendingAction
}

sealed interface PasswordVaultDetailEvent {
    data object Edit : PasswordVaultDetailEvent
    data object Delete : PasswordVaultDetailEvent
    data object ConfirmDelete : PasswordVaultDetailEvent
    data object DismissDelete : PasswordVaultDetailEvent
    data object RequestPasswordReveal : PasswordVaultDetailEvent
    data object RequestPasswordCopy : PasswordVaultDetailEvent
    data object RequestAccountCopy : PasswordVaultDetailEvent
    data class RequestSecretFieldReveal(val fieldId: String) : PasswordVaultDetailEvent
    data class RequestSecretFieldCopy(val fieldId: String) : PasswordVaultDetailEvent
    data object Unlock : PasswordVaultDetailEvent
}

@Immutable
data class PasswordVaultEditState(
    val entryId: String? = null,
    val title: String = "",
    val categoryId: String? = null,
    val account: String = "",
    val password: String = "",
    val secretFields: List<SecretField> = emptyList(),
    val note: String = "",
    val createdAt: Long = 0L,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthorized: Boolean = false,
    val availableCategories: List<PasswordVaultCategoryUi> = emptyList(),
) {
    val isValid: Boolean
        get() = title.isNotBlank() && password.isNotBlank() && categoryId != null

    val isNewEntry: Boolean
        get() = entryId == null
}

sealed interface PasswordVaultEditEvent {
    data class TitleChanged(val value: String) : PasswordVaultEditEvent
    data class CategoryChanged(val categoryId: String) : PasswordVaultEditEvent
    data class AccountChanged(val value: String) : PasswordVaultEditEvent
    data class PasswordChanged(val value: String) : PasswordVaultEditEvent
    data class NoteChanged(val value: String) : PasswordVaultEditEvent
    data class TogglePasswordVisibility(val visible: Boolean) : PasswordVaultEditEvent
    data class AddSecretField(val label: String, val value: String) : PasswordVaultEditEvent
    data class UpdateSecretField(
        val fieldId: String,
        val label: String,
        val value: String,
    ) : PasswordVaultEditEvent
    data class RemoveSecretField(val fieldId: String) : PasswordVaultEditEvent
    data object Save : PasswordVaultEditEvent
    data object Unlock : PasswordVaultEditEvent
}

sealed interface PasswordVaultCategoryEvent {
    data class Add(val name: String) : PasswordVaultCategoryEvent
    data class Rename(val categoryId: String, val newName: String) : PasswordVaultCategoryEvent
    data class Delete(val categoryId: String) : PasswordVaultCategoryEvent
    data class Reorder(val orderedIds: List<String>) : PasswordVaultCategoryEvent
}
