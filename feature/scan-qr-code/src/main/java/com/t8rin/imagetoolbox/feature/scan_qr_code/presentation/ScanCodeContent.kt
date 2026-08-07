package com.t8rin.imagetoolbox.feature.scan_qr_code.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.utils.helper.rememberBarcodeScanner
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity
import com.t8rin.imagetoolbox.core.ui.widget.AdaptiveLayoutScreen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.text.TitleItem
import com.t8rin.imagetoolbox.feature.scan_qr_code.domain.ScanCodeContentType
import com.t8rin.imagetoolbox.feature.scan_qr_code.presentation.model.ScanCodeHistoryItem
import com.t8rin.imagetoolbox.feature.scan_qr_code.presentation.screenLogic.ScanCodeComponent
import java.text.DateFormat
import java.util.Date
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.DeleteSweep
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLink
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTouchApp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQrCode
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQrScanner

@Composable
fun ScanCodeContent(
    component: ScanCodeComponent
) {
    val state = component.state
    val activity = LocalComponentActivity.current
    val scanner = rememberBarcodeScanner(component::onScanned)
    val latest = state.latest
    val canOpenLatest = latest != null && latest.type != ScanCodeContentType.TEXT

    AdaptiveLayoutScreen(
        title = {
            Text(text = stringResource(R.string.scan_code))
        },
        onGoBack = component.onGoBack,
        shouldDisableBackHandler = false,
        actions = {},
        topAppBarPersistentActions = {
            if (latest != null || state.history.isNotEmpty()) {
                IconButton(onClick = component::clearHistory) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.DeleteSweep,
                        contentDescription = stringResource(R.string.clear_scan_history)
                    )
                }
            }
        },
        imagePreview = {},
        controls = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScanCodeInfoCard()

                latest?.let {
                    ScanHistorySectionTitle(
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                        title = stringResource(R.string.latest_scan_result)
                    )
                    ScanHistoryItemCard(
                        item = it,
                        onOpen = component::openHistoryItem,
                        onCopy = { Clipboard.copy(it.entry.raw) },
                        onDelete = component::removeHistoryItem,
                        isPinned = true
                    )
                }

                ScanHistorySectionTitle(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQrCode,
                    title = stringResource(R.string.scan_history)
                )

                if (state.history.isEmpty()) {
                    EmptyHistoryCard()
                } else {
                    state.history.forEach { item ->
                        ScanHistoryItemCard(
                            item = item,
                            onOpen = component::openHistoryItem,
                            onCopy = { Clipboard.copy(item.entry.raw) },
                            onDelete = component::removeHistoryItem
                        )
                    }
                }
            }
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth().navigationBarsPadding()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (canOpenLatest) {
                    FilledTonalButton(
                        onClick = component::openLatest,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.open_latest_result))
                    }
                }
                Button(
                    onClick = {
                        scanner.scan(contextActivity = activity)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQrScanner,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.start_scanning))
                }
            }
        },
        canShowScreenData = true,
        showImagePreviewAsStickyHeader = false,
        noDataControls = {},
    )
}

@Composable
private fun ScanCodeInfoCard() {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = OneBoxDesignSystem.sectionGlassStyle,
        shape = OneBoxDesignSystem.sectionCardShape,
        borderWidth = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TitleItem(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQrScanner,
                text = stringResource(R.string.scan_code),
                subtitle = stringResource(R.string.scan_code_subtitle),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(R.string.scan_code_auto_open_internal_deeplink_only),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = OneBoxDesignSystem.sectionGlassStyle,
        shape = OneBoxDesignSystem.sectionCardShape,
        borderWidth = 0.dp
    ) {
        Text(
            text = stringResource(R.string.no_scan_history),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ScanHistorySectionTitle(
    icon: ImageVector,
    title: String
) {
    TitleItem(
        icon = icon,
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    )
}

@Composable
private fun ScanHistoryItemCard(
    item: ScanCodeHistoryItem,
    onOpen: (ScanCodeHistoryItem) -> Unit,
    onCopy: () -> Unit,
    onDelete: (ScanCodeHistoryItem) -> Unit,
    isPinned: Boolean = false
) {
    val canOpen = item.type != ScanCodeContentType.TEXT

    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = OneBoxDesignSystem.sectionGlassStyle,
        shape = OneBoxDesignSystem.sectionCardShape,
        borderWidth = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TitleItem(
                icon = item.type.icon(),
                text = stringResource(item.type.labelRes()),
                subtitle = buildString {
                    append(formatTime(item.entry.scannedAtMillis))
                    if (isPinned) {
                        append(" · ")
                        append(stringResource(R.string.latest_scan_result))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            SelectionContainer {
                Text(
                    text = item.entry.raw,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (canOpen) {
                    FilledTonalButton(onClick = { onOpen(item) }) {
                        Text(text = stringResource(item.type.openActionRes()))
                    }
                }

                OutlinedButton(onClick = onCopy) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.copy_content))
                }

                IconButton(onClick = { onDelete(item) }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.DeleteSweep,
                        contentDescription = stringResource(R.string.delete_history_item)
                    )
                }
            }
        }
    }
}

private fun ScanCodeContentType.icon(): ImageVector = when (this) {
    ScanCodeContentType.INTERNAL_DEEPLINK -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTouchApp
    ScanCodeContentType.IMAGE -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage
    ScanCodeContentType.WEB -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLink
    ScanCodeContentType.TEXT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineText
}

private fun ScanCodeContentType.labelRes(): Int = when (this) {
    ScanCodeContentType.INTERNAL_DEEPLINK -> R.string.scan_content_type_internal_deeplink
    ScanCodeContentType.IMAGE -> R.string.scan_content_type_image
    ScanCodeContentType.WEB -> R.string.scan_content_type_web
    ScanCodeContentType.TEXT -> R.string.scan_content_type_text
}

private fun ScanCodeContentType.openActionRes(): Int = when (this) {
    ScanCodeContentType.INTERNAL_DEEPLINK -> R.string.open_internal_deeplink
    ScanCodeContentType.IMAGE -> R.string.preview_image
    ScanCodeContentType.WEB -> R.string.open_in_browser
    ScanCodeContentType.TEXT -> R.string.copy_content
}

private fun formatTime(timeMillis: Long): String {
    return runCatching {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(timeMillis))
    }.getOrDefault(timeMillis.toString())
}

