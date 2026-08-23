package com.shifenmiao.base.ui.select

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shifenmiao.theme.AppTheme

/**
 * 选择类型对话框
 */
@Composable
fun SelectBaseTypeDialog(
    title: String = stringResource(id = com.shifenmiao.core.R.string.select),
    values: Array<String>,
    onDismiss: () -> Unit,
    onClick: (String, Int) -> Unit,
) {
    // Compose 1.13 窗口模糊:blurBehind 糊整个背景,backgroundBlur 让卡片本身呈磨砂
    // (均 API 31+ 生效,低版本忽略);卡片半透明才能透出 backgroundBlur
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            blurBehindRadius = 10.dp,
            backgroundBlurRadius = 28.dp,
            scrimAlpha = 0.32f,
        ),
    ) {
        Surface(
            shape = AppTheme.shapes.getMediumShape(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        ) {
            Column(
                modifier = Modifier.padding(AppTheme.dimens.paddingNormal),
            ) {
                SelectBaseTypeHead(title)
                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingSmall))
                SelectBaseTypeBody(
                    onClick = onClick,
                    values = values
                )
            }
        }
    }
}

/**
 * 对话框的 head 部分
 */
@Composable
private fun SelectBaseTypeHead(
    text: String = "选择"
) {
    Column {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        HorizontalDivider(
            modifier = Modifier.padding(
                top = AppTheme.dimens.paddingSmall,
            ),
            color = MaterialTheme.colorScheme.surfaceContainer
        )
    }
}

/**
 * 对话框的 body 部分
 */
@Composable
private fun SelectBaseTypeBody(
    onClick: (String, Int) -> Unit,
    values: Array<String>,
) {
    Column {
        values.forEachIndexed { index, s ->
            Text(
                modifier = Modifier
                    .clickable {
                        onClick(s, index)
                    }
                    .padding(
                        vertical = 10.dp
                    )
                    .fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                text = s,
            )
        }
    }
}