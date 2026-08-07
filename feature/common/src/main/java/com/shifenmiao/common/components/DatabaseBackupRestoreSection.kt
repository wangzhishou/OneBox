package com.shifenmiao.common.components

import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.shifenmiao.common.logic.CommonComponent
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.FileType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFileCreator
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFilePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.other.ToastDuration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDoneOutline

private const val BACKUP_FILENAME_SUFFIX = "_one_box_backup.zip"

/**
 * 数据库「备份 / 恢复」入口组件。
 *
 * 负责文件选择器 / 文件保存器的创建、Toast 提示与文件名生成，业务流程委派给
 * [CommonComponent.exportDatabaseToUri] / [CommonComponent.importDatabaseFromUri]。
 * 抽屉和「我的」页都直接复用本组件。
 *
 * @param onAction 用户点击「备份」或「恢复」时回调，可用于先关闭抽屉等附加动作。
 */
@Composable
fun DatabaseBackupRestoreSection(
    commonComponent: CommonComponent,
    modifier: Modifier = Modifier,
    onAction: () -> Unit = {},
) {
    val context = LocalContext.current
    val fileNamePrefix = remember {
        SimpleDateFormat("yyyyMMdd_msys", Locale.getDefault()).format(Date())
    }

    val saveBackupLauncher = rememberFileCreator(
        mimeType = MimeType.Zip,
        onSuccess = { uri ->
            commonComponent.exportDatabaseToUri(
                context = context,
                fileUri = uri,
                onResult = commonComponent::parseFileSaveResult,
                onExportFailure = { msg ->
                    AppToastHost.showToast(
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        message = msg
                    )
                }
            )
        }
    )

    val pickBackupLauncher = rememberFilePicker(
        type = FileType.Single,
        mimeType = MimeType.Zip,
        onFailure = remember(context) {
            {
                AppToastHost.showToast(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                    message = AppContext.getString(R.string.cancel)
                )
            }
        }
    ) { files ->
        val uri = files.firstOrNull() ?: return@rememberFilePicker
        commonComponent.importDatabaseFromUri(
            context = context,
            fileUri = uri,
            onImportFailure = { msg ->
                AppToastHost.showToast(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                    message = msg
                )
            },
            onImportSuccess = {
                AppToastHost.showToast(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDoneOutline,
                    message = AppContext.getString(R.string.data_restore_success),
                    duration = ToastDuration.Long
                )
            }
        )
    }

    DatabaseSettingItem(
        modifier = modifier,
        onClicked = { index ->
            onAction()
            when (index) {
                0 -> saveBackupLauncher.make("$fileNamePrefix$BACKUP_FILENAME_SUFFIX")
                1 -> pickBackupLauncher.pickFile()
            }
        }
    )
}
