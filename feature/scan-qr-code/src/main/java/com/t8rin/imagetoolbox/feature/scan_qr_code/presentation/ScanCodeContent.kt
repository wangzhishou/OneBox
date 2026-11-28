/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox.feature.scan_qr_code.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.shifenmiao.base.utils.ActionUtils
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.DeleteSweep
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLink
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQrCode
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQrScanner
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTouchApp
import com.t8rin.imagetoolbox.feature.scan_qr_code.domain.ScanCodeContentType
import com.t8rin.imagetoolbox.feature.scan_qr_code.presentation.model.ScanCodeHistoryItem
import com.t8rin.imagetoolbox.feature.scan_qr_code.presentation.screenLogic.ScanCodeComponent
import com.wanbaohe.base.ui.ClearHistoryConfirmDialog
import com.wanbaohe.base.ui.card.TonalCardPaletteDefaults
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.utils.helper.ProvidesValue
import com.wanbaohe.core.ui.utils.helper.rememberBarcodeScanner
import com.wanbaohe.core.ui.utils.provider.LocalComponentActivity
import com.wanbaohe.core.ui.widget.AdaptiveLayoutScreen
import com.wanbaohe.core.ui.widget.glass.GlassButton
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.preferences.PreferenceItemDefaults
import com.wanbaohe.core.ui.widget.system.OneBoxBottomActionBar
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.text.TitleItem
import java.text.DateFormat
import java.util.Date

@Composable
fun ScanCodeContent(
    component: ScanCodeComponent
) {
    val state = component.state
    val activity = LocalComponentActivity.current
    val scanner = rememberBarcodeScanner(component::onScanned)
    val latest = state.latest
    val canOpenLatest = latest != null && latest.type != ScanCodeContentType.TEXT
    val showClearHistoryDialog = remember { mutableStateOf(false) }

    AdaptiveLayoutScreen(
        title = {
            Text(text = stringResource(R.string.scan_code))
        },
        onGoBack = component.onGoBack,
        shouldDisableBackHandler = false,
        actions = {},
        topAppBarPersistentActions = {
            if (latest != null || state.history.isNotEmpty()) {
                IconButton(onClick = { showClearHistoryDialog.value = true }) {
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
                        onOpenInBrowser = { ActionUtils.openWebBrowser(activity, it.entry.raw) },
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
                            onOpenInBrowser = { ActionUtils.openWebBrowser(activity, item.entry.raw) },
                            onCopy = { Clipboard.copy(item.entry.raw) },
                            onDelete = component::removeHistoryItem
                        )
                    }
                }
            }
        },
        buttons = {
            OneBoxBottomActionBar(
                primaryText = stringResource(R.string.start_scanning),
                onPrimaryClick = { scanner.scan(contextActivity = activity) },
                primaryIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQrScanner,
                secondaryText = if (canOpenLatest) stringResource(R.string.open_latest_result) else null,
                onSecondaryClick = if (canOpenLatest) component::openLatest else null,
            )
        },
        canShowScreenData = true,
        showImagePreviewAsStickyHeader = false,
        noDataControls = {},
    )

    ClearHistoryConfirmDialog(
        showDialog = showClearHistoryDialog,
        onConfirm = component::clearHistory,
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
    onOpenInBrowser: () -> Unit,
    onCopy: () -> Unit,
    onDelete: (ScanCodeHistoryItem) -> Unit,
    isPinned: Boolean = false
) {
    val canOpen = item.type != ScanCodeContentType.TEXT
    val canOpenInSystemBrowser = item.canOpenInSystemBrowser()

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
            ScanHistoryItemHeader(
                item = item,
                isPinned = isPinned,
                onDelete = { onDelete(item) },
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

            FlexBox(
                modifier = Modifier.fillMaxWidth(),
                config = {
                    wrap(FlexWrap.Wrap)
                    columnGap(8.dp)
                    rowGap(8.dp)
                },
            ) {
                var paletteIndex = 0
                if (canOpen) {
                    ScanGlassActionButton(
                        text = stringResource(R.string.open),
                        paletteIndex = paletteIndex++,
                        onClick = { onOpen(item) },
                    )
                }
                if (canOpenInSystemBrowser) {
                    ScanGlassActionButton(
                        text = stringResource(com.shifenmiao.core.R.string.web_browser_title),
                        paletteIndex = paletteIndex++,
                        onClick = onOpenInBrowser,
                    )
                }
                ScanGlassActionButton(
                    text = stringResource(R.string.copy),
                    paletteIndex = paletteIndex,
                    onClick = onCopy,
                )
            }
        }
    }
}

@Composable
private fun ScanHistoryItemHeader(
    item: ScanCodeHistoryItem,
    isPinned: Boolean,
    onDelete: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = item.type.icon(),
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.72f),
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(item.type.labelRes()),
                style = PreferenceItemDefaults.TitleFontStyle,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            ScanHistoryDeleteButton(onClick = onDelete)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = buildString {
                append(formatTime(item.entry.scannedAtMillis))
                if (isPinned) {
                    append(" · ")
                    append(stringResource(R.string.latest_scan_result))
                }
            },
            modifier = Modifier.padding(start = 30.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 14.sp,
            color = LocalContentColor.current.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun ScanHistoryDeleteButton(onClick: () -> Unit) {
    LocalMinimumInteractiveComponentSize.ProvidesValue(Dp.Unspecified) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(28.dp),
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.delete_history_item),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * 和首页功能卡片同一套四色轮换：只取主题色相，浓淡交给玻璃管线。
 */
@Composable
private fun ScanGlassActionButton(
    text: String,
    paletteIndex: Int,
    onClick: () -> Unit,
) {
    val palette = TonalCardPaletteDefaults.forIndex(paletteIndex)
    GlassButton(
        onClick = onClick,
        shape = OneBoxDesignSystem.pillShape,
        color = palette.containerColor,
        contentColor = palette.titleColor,
        borderWidth = 0.9.dp,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
        )
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

private fun ScanCodeHistoryItem.canOpenInSystemBrowser(): Boolean {
    val scheme = entry.raw.trim().toUri().scheme?.lowercase()
    return scheme == "http" || scheme == "https"
}

private fun formatTime(timeMillis: Long): String {
    return runCatching {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(timeMillis))
    }.getOrDefault(timeMillis.toString())
}

