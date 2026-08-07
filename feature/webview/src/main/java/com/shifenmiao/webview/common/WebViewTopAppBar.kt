package com.shifenmiao.webview.common

import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.widget.other.TopAppBarEmoji
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePdf
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePrint
import com.t8rin.imagetoolbox.core.resources.icons.line.LineOpenInBrowser

/**
 * 通用 WebView 顶部栏
 */
@Composable
fun WebViewTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable () -> Unit = {},
    showEmojiAction: Boolean = false
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                modifier = Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE,
                    spacing = MarqueeSpacing(30.dp),
                    velocity = 30.dp,
                    repeatDelayMillis = 1000
                ),
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            actions()
            if (showEmojiAction) {
                TopAppBarEmoji()
            }
        },
        scrollBehavior = scrollBehavior,
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * 带有导出功能的 WebView 顶部栏
 */
@Composable
fun WebViewTopAppBarWithExport(
    title: String,
    onBackClick: () -> Unit,
    onShareImage: () -> Unit,
    onExportPng: () -> Unit,
    onPrintPdf: () -> Unit,
    onExportImagePdf: () -> Unit = {}, // 新增图片PDF导出回调
    onCopyContent: () -> Unit = {}, // 新增复制内容回调
    onOpenInBrowser: () -> Unit = {}, // 系统浏览器打开回调
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showEmojiAction: Boolean = false,
    showExportOptions: Boolean = true // 是否显示截图导出相关选项
) {
    var showMenu by remember { mutableStateOf(false) }

    WebViewTopAppBar(
        title = title,
        onBackClick = onBackClick,
        scrollBehavior = scrollBehavior,
        showEmojiAction = showEmojiAction,
        actions = {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMore,
                    contentDescription = "Export options"
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("复制内容") },
                    leadingIcon = {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onCopyContent()
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("在浏览器中打开") },
                    leadingIcon = {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineOpenInBrowser,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onOpenInBrowser()
                        showMenu = false
                    }
                )
                if (showExportOptions) {
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(R.string.share_image))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShare,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            onShareImage()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.button_export_image)) },
                        leadingIcon = {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            onExportPng()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.button_export_image_pdf)) },
                        leadingIcon = {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePdf,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            onExportImagePdf()
                            showMenu = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.button_export_pdf)) },
                    leadingIcon = {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePrint,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onPrintPdf()
                        showMenu = false
                    }
                )
            }
        }
    )
}

