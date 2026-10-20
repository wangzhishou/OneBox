package com.shifenmiao.webview.browser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.shifenmiao.webview.R
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.t8rin.imagetoolbox.core.resources.icons.Language
import com.t8rin.imagetoolbox.core.resources.icons.line.LineApp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBookmark
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePdf
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTab
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTravelExplore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowForwardIos

@Composable
fun BrowserBottomBar(
    canGoBack: Boolean,
    canGoForward: Boolean,
    tabCount: Int,
    isBookmarked: Boolean,
    selectedPage: BrowserState.BrowserPage,
    isBookmarkViewer: Boolean,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onHome: () -> Unit,
    onEditBookmark: () -> Unit,
    onTabs: () -> Unit,
    onAddBookmark: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        borderWidth = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomIconButton(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.browser_back),
                enabled = canGoBack,
                onClick = onGoBack
            )
            BottomIconButton(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowForwardIos,
                contentDescription = stringResource(R.string.browser_forward),
                enabled = canGoForward,
                onClick = onGoForward
            )
            // 从收藏进来时，「主页」槽位换成编辑入口 —— 这是查看收藏时最常用的动作
            if (isBookmarkViewer) {
                BottomIconButton(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.browser_edit_bookmark),
                    selected = true,
                    onClick = onEditBookmark
                )
            } else {
                BottomIconButton(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineApp,
                    contentDescription = stringResource(R.string.browser_home),
                    selected = selectedPage == BrowserState.BrowserPage.Home,
                    onClick = onHome
                )
            }
            BottomIconButton(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTab,
                contentDescription = stringResource(R.string.browser_tabs),
                selected = selectedPage == BrowserState.BrowserPage.Tabs,
                badgeCount = tabCount,
                onClick = onTabs
            )
            BottomIconButton(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMore,
                contentDescription = stringResource(R.string.browser_more),
                onClick = onMoreClick
            )
        }
    }
}

@Composable
fun BrowserMoreMenu(
    isBookmarked: Boolean,
    isBookmarkViewer: Boolean,
    isShowingSnapshot: Boolean,
    canToggleSnapshot: Boolean,
    onBookmarkToggle: () -> Unit,
    onExportPng: () -> Unit,
    onExportPdf: () -> Unit,
    onToggleSnapshot: () -> Unit,
    onOpenExternal: () -> Unit,
    onSharePage: () -> Unit,
    onBookmarks: () -> Unit,
    onSettings: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    Popup(
        alignment = Alignment.BottomEnd,
        onDismissRequest = onDismiss,
        offset = with(density) { IntOffset(x = (-8).dp.roundToPx(), y = (-64).dp.roundToPx()) },
        properties = PopupProperties(focusable = true)
    ) {
        Card(
            modifier = modifier.width(220.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                if (isBookmarkViewer && canToggleSnapshot) {
                    // 只对「带快照的收藏」有意义：离线快照 ⇄ 线上原页
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(
                                    if (isShowingSnapshot) R.string.browser_show_original
                                    else R.string.browser_show_snapshot
                                ),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTravelExplore,
                                contentDescription = null
                            )
                        },
                        onClick = onToggleSnapshot
                    )
                }
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(
                                if (isBookmarked) R.string.browser_remove_bookmark
                                else R.string.browser_add_bookmark
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isBookmarked) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = null
                        )
                    },
                    onClick = onBookmarkToggle
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.browser_share_page), style = MaterialTheme.typography.bodyLarge) },
                    leadingIcon = { Icon(imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShare, contentDescription = null) },
                    onClick = onSharePage
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.browser_open_external), style = MaterialTheme.typography.bodyLarge) },
                    leadingIcon = { Icon(imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTravelExplore, contentDescription = null) },
                    onClick = onOpenExternal
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.browser_export_png), style = MaterialTheme.typography.bodyLarge) },
                    leadingIcon = { Icon(imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage, contentDescription = null) },
                    onClick = onExportPng
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.browser_export_pdf), style = MaterialTheme.typography.bodyLarge) },
                    leadingIcon = { Icon(imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePdf, contentDescription = null) },
                    onClick = onExportPdf
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.browser_bookmarks_history), style = MaterialTheme.typography.bodyLarge) },
                    leadingIcon = { Icon(imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBookmark, contentDescription = null) },
                    onClick = onBookmarks
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.browser_settings), style = MaterialTheme.typography.bodyLarge) },
                    leadingIcon = { Icon(imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings, contentDescription = null) },
                    onClick = onSettings
                )
            }
        }
    }
}

@Composable
private fun BottomIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean = true,
    selected: Boolean = false,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    val tint = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        selected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(44.dp)) {
        if (badgeCount > 0) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Text(text = badgeCount.toString(), style = MaterialTheme.typography.labelSmall)
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(24.dp),
                    tint = tint
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
                tint = tint
            )
        }
    }
}
