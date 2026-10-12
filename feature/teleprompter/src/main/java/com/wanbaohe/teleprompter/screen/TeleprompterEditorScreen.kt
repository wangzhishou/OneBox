package com.wanbaohe.teleprompter.screen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.t8rin.imagetoolbox.core.utils.getString
import com.wanbaohe.teleprompter.R
import com.wanbaohe.teleprompter.component.TeleprompterComponent
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave

/**
 * 脚本编辑页 (Script Editor)
 */
@Composable
fun TeleprompterEditorScreen(
    component: TeleprompterComponent,
) {
    val state by component.editorState.collectAsState()

    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    val onBack: () -> Unit = {
        if (state.isDirty) {
            showExitDialog = true
        } else {
            component.onEditorBack()
        }
    }

    BaseScreen(
        title = if (state.isNew)
            stringResource(R.string.teleprompter_new_script)
        else
            stringResource(R.string.teleprompter_edit_script),
        onGoBack = onBack,
        actions = {
            // 保存按钮
            IconButton(
                onClick = {
                    if (component.onSaveScript()) {
                        AppToastHost.showToast(
                            message = getString(R.string.teleprompter_saved),
                        )
                    } else {
                        AppToastHost.showToast(
                            message = getString(R.string.teleprompter_title_required),
                        )
                    }
                },
                colors = AppTheme.colors.iconButtonColors()
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                    contentDescription = stringResource(R.string.teleprompter_save),
                )
            }

            Spacer(Modifier.width(4.dp))

            // 播放按钮
            IconButton(
                onClick = { component.onPlayFromEditor() },
                colors = AppTheme.colors.iconButtonColors()
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle,
                    contentDescription = stringResource(R.string.teleprompter_play),
                )
            }

            Spacer(Modifier.width(8.dp))
        }
    ) {
        Spacer(Modifier.height(8.dp))

        // 标题输入
        GlassOutlinedTextField(
            value = state.title,
            onValueChange = { component.onTitleChange(it) },
            placeholder = {
                Text(
                    text = stringResource(R.string.teleprompter_title_hint),
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        GlassOutlinedTextField(
            value = state.content,
            onValueChange = { component.onContentChange(it) },
            placeholder = {
                Text(
                    text = stringResource(R.string.teleprompter_content_hint),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 8.dp)
        )
    }

    // 退出未保存提示
    ExitWithoutSavingDialog(
        onExit = { component.onEditorBack() },
        onDismiss = { showExitDialog = false },
        visible = showExitDialog,
    )
}
