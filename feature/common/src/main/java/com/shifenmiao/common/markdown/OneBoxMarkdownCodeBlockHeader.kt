package com.shifenmiao.common.markdown

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.R
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.widget.glass.GlassButton
import com.wanbaohe.markdown.OneBoxMarkdownCodeActions
import com.wanbaohe.markdown.OneBoxMarkdownCodeBlockInfo

@Composable
internal fun OneBoxMarkdownCodeBlockHeader(
    info: OneBoxMarkdownCodeBlockInfo,
    actions: OneBoxMarkdownCodeActions?,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (actions != null && actions.canRun(info.language)) {
            CodeActionButton(
                label = stringResource(R.string.markdown_create_widget),
                icon = Icons.Outlined.Add,
                containerColor = colorScheme.secondaryContainer,
                contentColor = colorScheme.onSecondaryContainer,
                onClick = { actions.createWidget(info.language, info.code) },
            )
            CodeActionButton(
                label = stringResource(R.string.markdown_run_code),
                icon = Icons.Outlined.PlayCircle,
                containerColor = colorScheme.tertiaryContainer,
                contentColor = colorScheme.onTertiaryContainer,
                onClick = { actions.run(info.language, info.code) },
            )
        }
        if (actions != null && actions.canSave(info.language)) {
            CodeActionButton(
                label = stringResource(R.string.markdown_save_code),
                icon = Icons.Outlined.LineSave,
                containerColor = colorScheme.tertiaryContainer,
                contentColor = colorScheme.onTertiaryContainer,
                onClick = { actions.save(info.language, info.code) },
            )
        }
        CodeActionButton(
            label = stringResource(com.shifenmiao.core.R.string.copy),
            icon = Icons.Outlined.ContentCopy,
            containerColor = colorScheme.tertiaryContainer,
            contentColor = colorScheme.onTertiaryContainer,
            onClick = {
                if (actions == null) Clipboard.copy(info.code) else actions.copy(info.code)
            },
        )
    }
}

@Composable
private fun CodeActionButton(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    GlassButton(
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        color = containerColor,
        contentColor = contentColor,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp),
            tint = contentColor,
        )
        Spacer(Modifier.width(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1,
        )
    }
}
