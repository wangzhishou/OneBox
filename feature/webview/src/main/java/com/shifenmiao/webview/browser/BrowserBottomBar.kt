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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.t8rin.imagetoolbox.core.resources.icons.Language
import com.t8rin.imagetoolbox.core.resources.icons.line.LineApp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBookmark
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePdf
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowForwardIos

@Composable
fun BrowserBottomBar(
    canGoBack: Boolean,
    canGoForward: Boolean,
    tabCount: Int,
    isBookmarked: Boolean,
    selectedPage: BrowserState.BrowserPage,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onHome: () -> Unit,
    onTabs: () -> Unit,
    onAddBookmark: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerAlpha = 0.25f,
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
                contentDescription = "后退",
                enabled = canGoBack,
                onClick = onGoBack
            )
            BottomIconButton(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowForwardIos,
                contentDescription = "前进",
                enabled = canGoForward,
                onClick = onGoForward
            )
            BottomIconButton(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineApp,
                contentDescription = "主页",
                selected = selectedPage == BrowserState.BrowserPage.Home,
                onClick = onHome
            )
            BottomIconButton(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Language,
                contentDescription = "标签页",
                selected = selectedPage == BrowserState.BrowserPage.Tabs,
                badgeCount = tabCount,
                onClick = onTabs
            )
            BottomIconButton(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMore,
                contentDescription = "更多",
                onClick = onMoreClick
            )
        }
    }
}

@Composable
fun BrowserMoreMenu(
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onExportPng: () -> Unit,
    onExportPdf: () -> Unit,
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
            modifier = modifier.width(200.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (isBookmarked) "取消书签" else "添加书签",
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
                    text = { Text("导出PNG", style = MaterialTheme.typography.bodyLarge) },
                    leadingIcon = { Icon(imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage, contentDescription = null) },
                    onClick = onExportPng
                )
                DropdownMenuItem(
                    text = { Text("导出PDF", style = MaterialTheme.typography.bodyLarge) },
                    leadingIcon = { Icon(imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePdf, contentDescription = null) },
                    onClick = onExportPdf
                )
                DropdownMenuItem(
                    text = { Text("书签与历史", style = MaterialTheme.typography.bodyLarge) },
                    leadingIcon = { Icon(imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBookmark, contentDescription = null) },
                    onClick = onBookmarks
                )
                DropdownMenuItem(
                    text = { Text("设置", style = MaterialTheme.typography.bodyLarge) },
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
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(44.dp)) {
        if (badgeCount > 0) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
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
