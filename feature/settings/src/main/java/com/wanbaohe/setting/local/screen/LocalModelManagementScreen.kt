package com.wanbaohe.setting.local.screen

import android.net.Uri
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.ai.EngineFilterChip
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionHeader
import com.wanbaohe.setting.local.component.LocalModelFile
import com.wanbaohe.setting.local.component.LocalModelManagementComponent
import com.wanbaohe.setting.local.component.ModelDownloadUiState
import com.wanbaohe.setting.local.component.RecommendedLocalModel
import com.wanbaohe.settings.R
import java.util.Locale
import kotlin.math.roundToInt
import com.shifenmiao.core.R as CoreR

@Composable
fun LocalModelManagementScreen(
    component: LocalModelManagementComponent,
) {
    val context = LocalContext.current
    val downloadedModels by component.downloadedModels.collectAsState()
    val downloadStates by component.downloadStates.collectAsState()
    val isImporting by component.isImporting.collectAsState()
    val currentEngine by component.currentAIEngine.collectAsState()

    var deletingModel by remember { mutableStateOf<LocalModelFile?>(null) }
    var pendingOverwriteUri by remember { mutableStateOf<Pair<Uri, String>?>(null) }

    val currentChatModelName = currentEngine
        .takeIf { it.requestProtocol == com.shifenmiao.model.ai.AiRequestProtocol.LOCAL_ON_DEVICE }
        ?.model?.name

    fun handleImportResult(result: LocalModelManagementComponent.ImportResult, uri: Uri) {
        when (result) {
            LocalModelManagementComponent.ImportResult.Success ->
                AppToastHost.showToast(R.string.local_models_import_success)

            LocalModelManagementComponent.ImportResult.Failed ->
                AppToastHost.showToast(R.string.local_models_import_failed)

            is LocalModelManagementComponent.ImportResult.AlreadyExists ->
                pendingOverwriteUri = uri to result.fileName
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            component.importModel(uri, overwrite = false) { result ->
                handleImportResult(result, uri)
            }
        }
    }

    BaseScreen(
        title = stringResource(CoreR.string.profile_item_local_models),
        onGoBack = component.onGoBack,
        supportGlassEffect = true,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing),
        ) {
            Spacer(modifier = Modifier.height(OneBoxDesignSystem.microSpacing))

            // ─── 推荐下载 ───
            OneBoxSectionCard {
                OneBoxSectionHeader(
                    title = stringResource(R.string.local_models_recommended_section),
                    supporting = stringResource(R.string.local_models_recommended_supporting),
                )
            }
            component.recommendedModels.forEach { model ->
                RecommendedModelCard(
                    model = model,
                    isDownloaded = downloadedModels.any { it.fileName == model.fileName },
                    downloadState = downloadStates[model.fileName],
                    onDownload = { component.startDownload(model) },
                    onCancel = { component.cancelDownload(model.fileName) },
                )
            }

            // ─── 已下载 ───
            OneBoxSectionCard {
                OneBoxSectionHeader(
                    title = stringResource(R.string.local_models_downloaded_section),
                    supporting = stringResource(R.string.local_models_downloaded_supporting),
                )
            }
            if (downloadedModels.isEmpty()) {
                Text(
                    text = stringResource(R.string.local_models_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            downloadedModels.forEach { model ->
                DownloadedModelCard(
                    model = model,
                    sizeText = Formatter.formatShortFileSize(context, model.sizeBytes),
                    isCurrentChatModel = currentChatModelName.equals(model.modelName, ignoreCase = true),
                    onSetAsChatModel = {
                        component.setAsChatModel(model) { success ->
                            AppToastHost.showToast(
                                if (success) R.string.local_models_set_success
                                else R.string.local_models_set_failed
                            )
                        }
                    },
                    onDelete = { deletingModel = model },
                )
            }

            // ─── 从文件导入 ───
            OneBoxSectionCard {
                OneBoxSectionHeader(
                    title = stringResource(R.string.local_models_import_section),
                    supporting = stringResource(R.string.local_models_import_supporting),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isImporting) {
                        LinearProgressIndicator(modifier = Modifier.weight(1f))
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    EngineFilterChip(
                        text = stringResource(R.string.local_models_import_button),
                        isSelected = false,
                        onClick = {
                            if (!isImporting) {
                                runCatching { importLauncher.launch(arrayOf("*/*")) }
                            }
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(OneBoxDesignSystem.sectionSpacing))
        }
    }

    // 删除确认
    deletingModel?.let { model ->
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { deletingModel = null },
            title = { Text(text = stringResource(R.string.local_models_delete_confirm_title)) },
            text = {
                Text(text = stringResource(R.string.local_models_delete_confirm_message, model.fileName))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        component.deleteModel(model) { success, revertedChatModel ->
                            when {
                                !success -> AppToastHost.showToast(R.string.local_models_delete_failed)
                                revertedChatModel ->
                                    AppToastHost.showToast(R.string.local_models_chat_model_reverted)
                            }
                        }
                        deletingModel = null
                    }
                ) {
                    Text(
                        text = stringResource(CoreR.string.button_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingModel = null }) {
                    Text(text = stringResource(CoreR.string.button_cancel))
                }
            },
        )
    }

    // 导入覆盖确认
    pendingOverwriteUri?.let { (uri, fileName) ->
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { pendingOverwriteUri = null },
            title = { Text(text = stringResource(R.string.local_models_import_overwrite_title)) },
            text = {
                Text(text = stringResource(R.string.local_models_import_overwrite_message, fileName))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        component.importModel(uri, overwrite = true) { result ->
                            handleImportResult(result, uri)
                        }
                        pendingOverwriteUri = null
                    }
                ) {
                    Text(text = stringResource(CoreR.string.button_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingOverwriteUri = null }) {
                    Text(text = stringResource(CoreR.string.button_cancel))
                }
            },
        )
    }
}

@Composable
private fun RecommendedModelCard(
    model: RecommendedLocalModel,
    isDownloaded: Boolean,
    downloadState: ModelDownloadUiState?,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
) {
    OneBoxSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
            ) {
                Text(
                    text = stringResource(model.displayNameRes),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(model.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = model.sizeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            when {
                isDownloaded -> Text(
                    text = stringResource(R.string.local_models_downloaded),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                )

                downloadState?.isDownloading == true -> TextButton(onClick = onCancel) {
                    Text(text = stringResource(R.string.local_models_cancel))
                }

                downloadState?.isFailed == true -> EngineFilterChip(
                    text = stringResource(R.string.local_models_retry),
                    isSelected = false,
                    onClick = onDownload,
                )

                else -> EngineFilterChip(
                    text = stringResource(R.string.local_models_download),
                    isSelected = false,
                    onClick = onDownload,
                )
            }
        }
        if (downloadState?.isDownloading == true) {
            val progress = downloadState.progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (progress != null) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%d%%", (progress * 100).roundToInt()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LinearProgressIndicator(modifier = Modifier.weight(1f))
                }
            }
        }
        if (downloadState?.isFailed == true) {
            Text(
                text = stringResource(R.string.local_models_download_failed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun DownloadedModelCard(
    model: LocalModelFile,
    sizeText: String,
    isCurrentChatModel: Boolean,
    onSetAsChatModel: () -> Unit,
    onDelete: () -> Unit,
) {
    OneBoxSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
            ) {
                Text(
                    text = model.fileName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = sizeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isCurrentChatModel) {
                Text(
                    text = stringResource(R.string.local_models_current_chat_model),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
            } else {
                EngineFilterChip(
                    text = stringResource(R.string.local_models_set_as_chat_model),
                    isSelected = false,
                    onClick = onSetAsChatModel,
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            TextButton(onClick = onDelete) {
                Text(
                    text = stringResource(R.string.local_models_delete),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
