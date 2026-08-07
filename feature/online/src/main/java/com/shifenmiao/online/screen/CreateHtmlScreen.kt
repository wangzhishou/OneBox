package com.shifenmiao.online.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.HomeTabKey
import com.shifenmiao.online.component.CreateHtmlComponent
import com.shifenmiao.online.ui.HtmlCategorySelection
import com.shifenmiao.online.ui.ItemTextField
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxBottomActionBar
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionHeader
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLink
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCodeEditor
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownloadForOffline
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText

@Composable
fun CreateHtmlScreen(
    createHtmlComponent: CreateHtmlComponent,
    appComponent: AppComponent,
) {
    val uiState by createHtmlComponent.uiState.collectAsState()
    val showExitDialog = rememberSaveable { mutableStateOf(false) }
    val navigator = LocalUrlNavigator.current
    val scope = rememberCoroutineScope()

    val onSave = {
        createHtmlComponent.saveItem(
            onSuccess = {
                AppToastHost.showToast(AppContext.getString(R.string.save_success))
                appComponent.onNavigateReplacingCurrent(
                    Screen.NewApp(initialTab = HomeTabKey.WEB)
                )
            },
            onFailure = { errorMsg ->
                AppToastHost.showToast(errorMsg)
            },
        )
    }

    val onBack = {
        if (uiState.isDirty) {
            showExitDialog.value = true
        } else {
            appComponent.onGoBack()
        }
    }

    val screenTitle = if (uiState.isEditing) {
        stringResource(R.string.edit_html)
    } else {
        stringResource(R.string.new_html)
    }

    BaseScreen(
        title = screenTitle,
        onGoBack = onBack,
        supportGlassEffect = true,
        showNavigationBarsPadding = false,
        actions = {
            IconButton(
                onClick = onSave,
                enabled = !uiState.isSaving,
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.save_to_collection),
                    tint = if (uiState.isSaving) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .imePadding(),
            contentPadding = PaddingValues(
                horizontal = OneBoxDesignSystem.screenPadding,
                vertical = OneBoxDesignSystem.screenTopSpacing,
            ),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.sectionSpacing),
        ) {
            // ── TITLE ─────────────────────────────────────────────
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                ) {
                    OneBoxSectionHeader(title = stringResource(R.string.create_ai_common_title_label))
                    ItemTextField(
                        value = uiState.title,
                        onValueChange = createHtmlComponent::onTitleChange,
                        placeholder = { Text(stringResource(R.string.create_ai_common_title_label)) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineText,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                    )
                }
            }

            // ── URL ──────────────────────────────────────────────
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                ) {
                    OneBoxSectionHeader(title = stringResource(R.string.url_section_label))
                    ItemTextField(
                        value = uiState.url,
                        onValueChange = createHtmlComponent::onUrlChange,
                        placeholder = { Text("https://...") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLink,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    createHtmlComponent.loadHtmlDataFromUrl(
                                        onSuccess = {
                                            AppToastHost.showToast(
                                                AppContext.getString(R.string.download_successful)
                                            )
                                        },
                                        onFailure = { message ->
                                            AppToastHost.showToast(message)
                                        },
                                    )
                                },
                                enabled = !uiState.isDownloading,
                            ) {
                                if (uiState.isDownloading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Icon(
                                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownloadForOffline,
                                        contentDescription = stringResource(R.string.download_successful),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        },
                    )
                }
            }

            // ── CATEGORY（横向滚动，紧接 URL） ────────────────────
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                ) {
                    OneBoxSectionHeader(title = stringResource(R.string.category_section_label))
                    HtmlCategorySelection(
                        modifier = Modifier.fillMaxWidth(),
                        createHtmlComponent = createHtmlComponent,
                        uiState = uiState,
                    )
                }
            }

            // ── SCRAPED CONTENT（固定高度，内部可滚动） ────────────
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OneBoxSectionHeader(title = stringResource(R.string.scraped_content_section_label))
                        val codeEditorTitle = stringResource(R.string.code_editor_title)
                        TextButton(
                            onClick = {
                                scope.launch {
                                    val codeDraftId = createHtmlComponent.prepareCodeEditorDraft()
                                    navigator.navigate(
                                        Screen.CodeEditor(
                                            editDraftId = codeDraftId,
                                            editTitle = uiState.title.ifBlank { codeEditorTitle }
                                        )
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCodeEditor,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(text = stringResource(R.string.code_editor_title))
                        }
                    }
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = OneBoxDesignSystem.sectionCardShape,
                        containerAlpha = 0.15f,
                    ) {
                        val htmlContent = uiState.data
                        val isBlank = htmlContent.isBlank()
                        Text(
                            text = if (isBlank) {
                                stringResource(R.string.html_data_placeholder)
                            } else {
                                htmlContent
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                            color = if (isBlank) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 300.dp, max = 600.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(OneBoxDesignSystem.cardPadding),
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(OneBoxDesignSystem.blockSpacing))
            }
        }

        // ── Save to Collection ────────────────────────────────────
        OneBoxBottomActionBar(
            primaryText = stringResource(R.string.save_to_collection),
            onPrimaryClick = onSave,
            primaryEnabled = !uiState.isSaving,
        )
    }

    BackHandler(onBack = onBack)

    ExitWithoutSavingDialog(
        onExit = { appComponent.onGoBack() },
        onDismiss = { showExitDialog.value = false },
        visible = showExitDialog.value,
    )
}
