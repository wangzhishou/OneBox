package com.wanbaohe.boardgame.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.widget.enhanced.EnhancedDropdownMenu
import com.wanbaohe.core.ui.widget.glass.GlassTonalIconButton
import com.wanbaohe.boardgame.R

@Composable
fun AnalysisActionsMenu(
    returnLabel: String,
    practiceLabel: String,
    exportLabel: String,
    onReturnToGame: () -> Unit,
    onPractice: (() -> Unit)?,
    onExport: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    practiceEnabled: Boolean = enabled,
    sourceLabel: String = "",
    onShowSource: (() -> Unit)? = null,
    onGenerateGif: (() -> Unit)? = null,
    gifEnabled: Boolean = enabled,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        GlassTonalIconButton(onClick = { expanded = true }, enabled = enabled) {
            Icon(Icons.Outlined.MoreVert, stringResource(R.string.boardgame_more), Modifier.size(18.dp))
        }
        EnhancedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(returnLabel) },
                leadingIcon = { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, Modifier.size(18.dp)) },
                enabled = enabled,
                onClick = { expanded = false; onReturnToGame() },
            )
            if (onPractice != null) {
                DropdownMenuItem(
                    text = { Text(practiceLabel) },
                    leadingIcon = { Icon(Icons.Outlined.Edit, null, Modifier.size(18.dp)) },
                    enabled = enabled && practiceEnabled,
                    onClick = { expanded = false; onPractice() },
                )
            }
            if (onShowSource != null) {
                DropdownMenuItem(
                    text = { Text(sourceLabel) },
                    leadingIcon = { Icon(Icons.Outlined.Info, null, Modifier.size(18.dp)) },
                    enabled = enabled,
                    onClick = { expanded = false; onShowSource() },
                )
            }
            DropdownMenuItem(
                text = { Text(exportLabel) },
                leadingIcon = { Icon(Icons.Outlined.FileDownload, null, Modifier.size(18.dp)) },
                enabled = enabled,
                onClick = { expanded = false; onExport() },
            )
            if (onGenerateGif != null) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.boardgame_export_gif)) },
                    leadingIcon = { Icon(Icons.Outlined.FileDownload, null, Modifier.size(18.dp)) },
                    enabled = enabled && gifEnabled,
                    onClick = { expanded = false; onGenerateGif() },
                )
            }
        }
    }
}
