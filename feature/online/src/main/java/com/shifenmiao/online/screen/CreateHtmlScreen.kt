package com.shifenmiao.online.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.icon.BuildCustomIcon
import com.wanbaohe.base.ui.icon.IconPickerSheet
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.online.component.CreateHtmlComponent
import com.shifenmiao.online.ui.HtmlCategorySelection
import com.shifenmiao.online.ui.ItemTextField
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.system.OneBoxBottomActionBar
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneBoxSectionHeader
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLink
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSingleEdit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCodeEditor
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownloadForOffline

@Composable
fun CreateHtmlScreen(
    createHtmlComponent: CreateHtmlComponent,
    appComponent: AppComponent,
) {
    val uiState by createHtmlComponent.uiState.collectAsState()
    val showExitDialog = rememberSaveable { mutableStateOf(false) }
    val showIconPicker = rememberSaveable { mutableStateOf(false) }
    val navigator = LocalUrlNavigator.current
    val scope = rememberCoroutineScope()

    val onSave = {
        createHtmlComponent.saveItem(
            onSuccess = { savedItemId ->
                AppToastHost.showToast(AppContext.getString(R.string.save_success))
                if (uiState.isEditing) {
                    // 从收藏查看器进来的编辑：退回查看器即可，它在 onStart 会重载最新快照
                    appComponent.onGoBack()
                } else {
                    // 新建：替换当前页直接看内容，返回即回收藏列表
                    appComponent.onNavigateReplacingCurrent(
                        Screen.WebBrowser(bookmarkItemId = savedItemId)
                    )
                }
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
                            // leadIcon 可点：整块热区打开全局图标选择浮动层
                            val hasIcon = uiState.iconName.isNotBlank()
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(OneBoxDesignSystem.smallRadius))
                                    .clickable { showIconPicker.value = true },
                                contentAlignment = Alignment.Center
                            ) {
                                BuildCustomIcon(
                                    iconName = uiState.iconName.ifBlank { null },
                                    fallbackName = uiState.title,
                                    modifier = Modifier.size(22.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                                if (!hasIcon) {
                                    // 空态提示：未选图标时显示小铅笔角标，暗示这里可以点
                                    Icon(
                                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSingleEdit,
                                        contentDescription = stringResource(R.string.html_title_icon_pick),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(12.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        },
                        trailingIcon = {
                            IconButton(onClick = { showIconPicker.value = true }) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSingleEdit,
                                    contentDescription = stringResource(R.string.html_title_icon_pick),
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
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
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                        },
                    )
                }
            }

            // ── CATEGORY（可选，横向滚动，紧接 URL） ───────────────
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
            primaryText = stringResource(R.string.save_bookmark),
            onPrimaryClick = onSave,
            primaryEnabled = !uiState.isSaving,
        )
    }

    BackHandler(onBack = onBack)

    // 全局图标选择浮动层：标题 leadIcon 与卡片列表共用一个注册表 key
    IconPickerSheet(
        visible = showIconPicker.value,
        onDismiss = { showIconPicker.value = false },
        onIconSelected = createHtmlComponent::onIconChange,
        selectedIconName = uiState.iconName.takeIf { it.isNotBlank() },
    )

    ExitWithoutSavingDialog(
        onExit = { appComponent.onGoBack() },
        onDismiss = { showExitDialog.value = false },
        visible = showExitDialog.value,
    )
}
