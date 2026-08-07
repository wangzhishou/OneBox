package com.wanbaohe.passwordvault.edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.button.ConfirmButton
import com.shifenmiao.common.components.category.CategoryManagementDialog
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.model.Source
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassIconButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxOutlinedTextField
import com.wanbaohe.passwordvault.R
import com.wanbaohe.passwordvault.components.CategoryPickerField
import com.wanbaohe.passwordvault.components.PasswordGeneratorDialog
import com.wanbaohe.passwordvault.components.PasswordStrengthMeter
import com.wanbaohe.passwordvault.edit.screenLogic.PasswordVaultEditComponent
import com.wanbaohe.passwordvault.model.PasswordVaultCategoryEvent
import com.wanbaohe.passwordvault.model.PasswordVaultCategoryUi
import com.wanbaohe.passwordvault.model.PasswordVaultEditEvent
import com.wanbaohe.passwordvault.model.PasswordVaultEditState
import com.wanbaohe.passwordvault.util.localizedCategoryName
import com.wanbaohe.passwordvault.model.SecretField
import com.wanbaohe.passwordvault.util.PasswordGenerator
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLock
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCasino
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibility
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibilityOff
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMinus

@Composable
fun PasswordVaultEditScreen(
    component: PasswordVaultEditComponent
) {
    val uiState by component.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        BaseScreen(
            title = stringResource(
                if (uiState.isNewEntry) R.string.password_vault_new_entry
                else R.string.password_vault_edit_entry
            ),
            onGoBack = component.onGoBack,
        ) {
            if (uiState.isAuthorized) {
                EditFormContent(
                    uiState = uiState,
                    onEvent = component::handleEvent,
                    onCategoryEvent = component::handleCategoryEvent
                )
            } else {
                LockedPlaceholder(
                    onUnlock = { component.handleEvent(PasswordVaultEditEvent.Unlock) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(OneBoxDesignSystem.screenPadding)
                )
            }
        }
    }
}

@Composable
private fun LockedPlaceholder(
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = stringResource(R.string.password_vault_locked_notice),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.password_vault_locked_notice_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            EnhancedButton(
                onClick = onUnlock,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(top = OneBoxDesignSystem.blockSpacing)
            ) {
                Text(stringResource(R.string.password_vault_unlock_now))
            }
        }
    }
}

@Composable
private fun EditFormContent(
    uiState: PasswordVaultEditState,
    onEvent: (PasswordVaultEditEvent) -> Unit,
    onCategoryEvent: (PasswordVaultCategoryEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showGenerator by remember { mutableStateOf(false) }
    var showCategoryManager by remember { mutableStateOf(false) }
    val generator = remember { PasswordGenerator() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(OneBoxDesignSystem.screenPadding)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
    ) {
        OneBoxOutlinedTextField(
            value = uiState.title,
            onValueChange = { onEvent(PasswordVaultEditEvent.TitleChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.password_vault_entry_title)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        CategoryPickerField(
            selectedCategoryId = uiState.categoryId,
            categories = uiState.availableCategories,
            onSelect = { id -> onEvent(PasswordVaultEditEvent.CategoryChanged(id)) },
            onManage = { showCategoryManager = true },
        )

        OneBoxOutlinedTextField(
            value = uiState.account,
            onValueChange = { onEvent(PasswordVaultEditEvent.AccountChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.password_vault_entry_account)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
            PasswordTextField(
                value = uiState.password,
                onValueChange = { onEvent(PasswordVaultEditEvent.PasswordChanged(it)) },
                label = stringResource(R.string.password_vault_entry_password),
                modifier = Modifier.fillMaxWidth(),
                isVisible = uiState.isPasswordVisible,
                onToggleVisibility = {
                    onEvent(PasswordVaultEditEvent.TogglePasswordVisibility(it))
                }
            )
            PasswordStrengthMeter(password = uiState.password)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                PasswordGeneratorButton(onClick = { showGenerator = true })
            }
        }

        OneBoxOutlinedTextField(
            value = uiState.note,
            onValueChange = { onEvent(PasswordVaultEditEvent.NoteChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.password_vault_entry_note)) },
            maxLines = 4
        )

        var showNewFieldForm by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = OneBoxDesignSystem.compactSpacing),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.password_vault_secret_fields),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            GlassIconButton(
                onClick = { showNewFieldForm = !showNewFieldForm },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = if (showNewFieldForm) {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMinus
                    } else {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add
                    },
                    contentDescription = stringResource(R.string.password_vault_add_field),
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        uiState.secretFields.forEach { field ->
            SecretFieldRow(
                field = field,
                onUpdate = { label, value ->
                    onEvent(PasswordVaultEditEvent.UpdateSecretField(field.id, label, value))
                },
                onRemove = {
                    onEvent(PasswordVaultEditEvent.RemoveSecretField(field.id))
                }
            )
        }

        var newFieldLabel by remember { mutableStateOf("") }
        var newFieldValue by remember { mutableStateOf("") }
        var newFieldVisible by remember { mutableStateOf(false) }

        AnimatedVisibility(visible = showNewFieldForm) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
            ) {
                OneBoxOutlinedTextField(
                    value = newFieldLabel,
                    onValueChange = { newFieldLabel = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.password_vault_new_field_label)) },
                    singleLine = true
                )
                PasswordTextField(
                    value = newFieldValue,
                    onValueChange = { newFieldValue = it },
                    label = stringResource(R.string.password_vault_new_field_value),
                    modifier = Modifier.fillMaxWidth(),
                    isVisible = newFieldVisible,
                    onToggleVisibility = { newFieldVisible = it }
                )
                EnhancedButton(
                    onClick = {
                        if (newFieldLabel.isNotBlank()) {
                            onEvent(
                                PasswordVaultEditEvent.AddSecretField(
                                    label = newFieldLabel.trim(),
                                    value = newFieldValue
                                )
                            )
                            newFieldLabel = ""
                            newFieldValue = ""
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add, contentDescription = null)
                    Text(stringResource(R.string.password_vault_add_field))
                }
            }
        }

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        ConfirmButton(
            text = stringResource(R.string.password_vault_save),
            onClick = { onEvent(PasswordVaultEditEvent.Save) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isValid && !uiState.isLoading,
        )
    }

    if (showGenerator) {
        PasswordGeneratorDialog(
            generator = generator,
            onDismiss = { showGenerator = false },
            onConfirm = { generated ->
                onEvent(PasswordVaultEditEvent.PasswordChanged(generated))
                showGenerator = false
            },
        )
    }

    if (showCategoryManager) {
        CategoryManagementDialog(
            items = uiState.availableCategories.map {
                it.toManageable(localizedCategoryName(it.id, it.name))
            },
            title = stringResource(R.string.password_vault_category_manage),
            onDismiss = { showCategoryManager = false },
            onAdd = { name -> onCategoryEvent(PasswordVaultCategoryEvent.Add(name)) },
            onDelete = { item -> onCategoryEvent(PasswordVaultCategoryEvent.Delete(item.categoryId)) },
            onRename = { item, newName ->
                onCategoryEvent(PasswordVaultCategoryEvent.Rename(item.categoryId, newName))
            },
            onReorder = { orderedItems ->
                onCategoryEvent(
                    PasswordVaultCategoryEvent.Reorder(orderedItems.map { it.categoryId })
                )
            },
        )
    }
}

private fun PasswordVaultCategoryUi.toManageable(displayName: String): VaultCategoryManageableItem =
    VaultCategoryManageableItem(this, displayName, if (isDefault) Source.SYSTEM else Source.LOCAL)

/**
 * 密码生成器入口:骰子图标 + 文字,比纯图标语义更明确。
 */
@Composable
private fun PasswordGeneratorButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCasino,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = stringResource(R.string.password_vault_generate_password),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

private data class VaultCategoryManageableItem(
    private val categoryUi: PasswordVaultCategoryUi,
    override val name: String,
    override val source: Source,
) : com.shifenmiao.common.components.category.ManageableItem {
    val categoryId: String get() = categoryUi.id
    override val id: Int get() = categoryUi.id.hashCode()
    override val order: Int get() = categoryUi.sortOrder
    override val canEdit: Boolean? get() = !categoryUi.isDefault
}

@Composable
private fun SecretFieldRow(
    field: SecretField,
    onUpdate: (String, String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember(field.id) { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
    ) {
        OneBoxOutlinedTextField(
            value = field.label,
            onValueChange = { newLabel -> onUpdate(newLabel, field.value) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.password_vault_field_label)) },
            singleLine = true
        )
        PasswordTextField(
            value = field.value,
            onValueChange = { newValue -> onUpdate(field.label, newValue) },
            label = stringResource(R.string.password_vault_field_value),
            modifier = Modifier.fillMaxWidth(),
            isVisible = isVisible,
            onToggleVisibility = { isVisible = it },
            trailingIcon = {
                Row {
                    IconButton(onClick = { clipboard.setText(AnnotatedString(field.value)) }) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                            contentDescription = stringResource(R.string.password_vault_copy_password)
                        )
                    }
                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.password_vault_remove_field)
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onToggleVisibility: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OneBoxOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Password
        ),
        trailingIcon = {
            Row {
                IconButton(onClick = { onToggleVisibility(!isVisible) }) {
                    Icon(
                        imageVector = if (isVisible) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibilityOff else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibility,
                        contentDescription = null
                    )
                }
                trailingIcon?.invoke()
            }
        }
    )
}
