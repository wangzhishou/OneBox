package com.wanbaohe.passwordvault.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDangerButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.system.OneSecondaryButton
import com.wanbaohe.passwordvault.R
import com.wanbaohe.passwordvault.detail.screenLogic.PasswordVaultDetailComponent
import com.wanbaohe.passwordvault.model.PasswordVaultDetailEvent
import com.wanbaohe.passwordvault.model.SecretField
import com.wanbaohe.passwordvault.model.VaultEntry
import com.wanbaohe.passwordvault.util.localizedCategoryName
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLock
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibility
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibilityOff

/** 掩码固定长度,不泄露真实密码长度 */
private const val PASSWORD_MASK = "••••••••"

@Composable
fun PasswordVaultDetailScreen(
    component: PasswordVaultDetailComponent
) {
    val uiState by component.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        BaseScreen(
            title = uiState.entry?.title ?: stringResource(R.string.password_vault_title),
            onGoBack = component.onGoBack,
            actions = {
                uiState.entry?.let {
                    IconButton(onClick = { component.handleEvent(PasswordVaultDetailEvent.Edit) }) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.password_vault_edit_entry)
                        )
                    }
                    IconButton(onClick = { component.handleEvent(PasswordVaultDetailEvent.Delete) }) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.password_vault_delete_entry)
                        )
                    }
                }
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                    uiState.errorMessage != null -> ErrorPlaceholder(
                        message = uiState.errorMessage.orEmpty(),
                        modifier = Modifier.align(Alignment.Center)
                    )
                    uiState.entry != null -> EntryDetailContent(
                        entry = uiState.entry!!,
                        isPasswordRevealed = uiState.isPasswordRevealed,
                        revealedSecretFieldIds = uiState.revealedSecretFieldIds,
                        isAuthorized = uiState.isAuthorized,
                        onTogglePasswordReveal = {
                            component.handleEvent(PasswordVaultDetailEvent.RequestPasswordReveal)
                        },
                        onCopyPassword = {
                            component.handleEvent(PasswordVaultDetailEvent.RequestPasswordCopy)
                        },
                        onCopyAccount = {
                            component.handleEvent(PasswordVaultDetailEvent.RequestAccountCopy)
                        },
                        onToggleSecretFieldReveal = { fieldId ->
                            component.handleEvent(
                                PasswordVaultDetailEvent.RequestSecretFieldReveal(fieldId)
                            )
                        },
                        onCopySecretField = { fieldId ->
                            component.handleEvent(
                                PasswordVaultDetailEvent.RequestSecretFieldCopy(fieldId)
                            )
                        },
                        onUnlock = {
                            component.handleEvent(PasswordVaultDetailEvent.Unlock)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        if (uiState.showDeleteConfirm) {
            DeleteConfirmDialog(
                entryTitle = uiState.entry?.title.orEmpty(),
                onConfirm = { component.handleEvent(PasswordVaultDetailEvent.ConfirmDelete) },
                onDismiss = { component.handleEvent(PasswordVaultDetailEvent.DismissDelete) }
            )
        }
    }
}

@Composable
private fun EntryDetailContent(
    entry: VaultEntry,
    isPasswordRevealed: Boolean,
    revealedSecretFieldIds: Set<String>,
    isAuthorized: Boolean,
    onTogglePasswordReveal: () -> Unit,
    onCopyPassword: () -> Unit,
    onCopyAccount: () -> Unit,
    onToggleSecretFieldReveal: (String) -> Unit,
    onCopySecretField: (String) -> Unit,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(OneBoxDesignSystem.screenPadding),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
    ) {
        if (!isAuthorized) {
            LockedNoticeCard(onUnlock = onUnlock)
        }
        ReadOnlyField(
            label = stringResource(R.string.password_vault_entry_title),
            value = entry.title
        )
        ReadOnlyField(
            label = stringResource(R.string.password_vault_entry_category),
            value = localizedCategoryName(entry.categoryId, entry.categoryName)
                .ifBlank { stringResource(R.string.password_vault_default_category) }
        )
        entry.account?.takeIf { it.isNotBlank() }?.let { account ->
            ReadOnlyField(
                label = stringResource(R.string.password_vault_entry_account),
                value = account,
                copyContentDescription = stringResource(R.string.password_vault_copy_account),
                onCopy = onCopyAccount
            )
        }
        PasswordField(
            label = stringResource(R.string.password_vault_entry_password),
            password = entry.password,
            isRevealed = isPasswordRevealed,
            isAuthorized = isAuthorized,
            onToggleReveal = onTogglePasswordReveal,
            onCopy = onCopyPassword
        )
        if (entry.note.isNotBlank()) {
            ReadOnlyField(
                label = stringResource(R.string.password_vault_entry_note),
                value = entry.note
            )
        }
        if (entry.secretFields.isNotEmpty()) {
            Text(
                text = stringResource(R.string.password_vault_secret_fields),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = OneBoxDesignSystem.compactSpacing)
            )
            entry.secretFields.forEach { field ->
                SecretFieldCard(
                    field = field,
                    isRevealed = field.id in revealedSecretFieldIds,
                    isAuthorized = isAuthorized,
                    onToggleReveal = { onToggleSecretFieldReveal(field.id) },
                    onCopy = { onCopySecretField(field.id) }
                )
            }
        }
    }
}

@Composable
private fun LockedNoticeCard(
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(OneBoxDesignSystem.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = stringResource(R.string.password_vault_locked_notice),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.password_vault_locked_notice_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            EnhancedButton(
                onClick = onUnlock,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(top = OneBoxDesignSystem.compactSpacing)
            ) {
                Text(stringResource(R.string.password_vault_unlock_now))
            }
        }
    }
}

@Composable
private fun ReadOnlyField(
    label: String,
    value: String,
    onCopy: (() -> Unit)? = null,
    copyContentDescription: String = stringResource(R.string.password_vault_copy_password),
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(OneBoxDesignSystem.screenPadding)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (onCopy != null) {
                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                            contentDescription = copyContentDescription
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordField(
    label: String,
    password: String,
    isRevealed: Boolean,
    isAuthorized: Boolean,
    onToggleReveal: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayValue = if (isAuthorized && isRevealed) password else PASSWORD_MASK

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(OneBoxDesignSystem.screenPadding)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OneBoxOutlinedTextField(
                value = displayValue,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    Row {
                        IconButton(onClick = onToggleReveal) {
                            Icon(
                                imageVector = if (isRevealed) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibilityOff else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibility,
                                contentDescription = stringResource(
                                    if (isRevealed) R.string.password_vault_hide_password
                                    else R.string.password_vault_show_password
                                )
                            )
                        }
                        IconButton(onClick = onCopy) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                                contentDescription = stringResource(R.string.password_vault_copy_password)
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun SecretFieldCard(
    field: SecretField,
    isRevealed: Boolean,
    isAuthorized: Boolean,
    onToggleReveal: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayValue = if (isAuthorized && isRevealed) field.value else PASSWORD_MASK

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(OneBoxDesignSystem.screenPadding)) {
            Text(
                text = field.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = onToggleReveal) {
                        Icon(
                            imageVector = if (isRevealed) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibilityOff else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibility,
                            contentDescription = stringResource(
                                if (isRevealed) R.string.password_vault_hide_password
                                else R.string.password_vault_show_password
                            )
                        )
                    }
                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                            contentDescription = stringResource(R.string.password_vault_copy_password)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorPlaceholder(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(OneBoxDesignSystem.screenPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun DeleteConfirmDialog(
    entryTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                contentDescription = null
            )
        },
        title = { Text(stringResource(R.string.password_vault_delete_title)) },
        text = {
            Text(
                text = stringResource(R.string.password_vault_delete_message, entryTitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            OneBoxDangerButton(
                text = stringResource(R.string.password_vault_delete),
                onClick = onConfirm
            )
        },
        dismissButton = {
            OneSecondaryButton(
                text = stringResource(R.string.password_vault_cancel),
                onClick = onDismiss
            )
        }
    )
}
