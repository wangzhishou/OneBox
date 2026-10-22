package com.wanbaohe.imageviewer.components

import android.Manifest
import android.net.Uri
import android.os.Build
import android.content.pm.PackageManager
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImagePainter
import com.wanbaohe.base.ui.page.PageIndicator
import com.shifenmiao.common.ui.ImageViewerBaseScreen
import com.shifenmiao.common.ui.ViewerActionButton
import com.shifenmiao.common.ui.rememberImmersiveModeState
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.ImageEdit
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.glass.GlassTextButton
import com.wanbaohe.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.core.ui.widget.image.Picture
import com.wanbaohe.imageviewer.screenLogic.ImageViewerComponent
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.toggleScale
import net.engawapg.lib.zoomable.zoomable
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineError

private const val MaxZoomScale = 5f
private const val DoubleTapZoomScale = 2.5f

@Composable
fun ImageViewerScreen(
    imageViewerComponent: ImageViewerComponent
) {
    val images = imageViewerComponent.imageViewerInfo?.images
    val initialIndex = imageViewerComponent.imageViewerInfo?.initialIndex ?: 0

    if (images.isNullOrEmpty()) {
        LaunchedEffect(Unit) {
            imageViewerComponent.exit()
        }
        return
    }

    ImageViewer(
        images = images,
        initialIndex = initialIndex,
        onDownload = imageViewerComponent::downloadImage,
        onEdit = imageViewerComponent::editImage,
        onResolveGrantedImage = imageViewerComponent::resolveImageInGrantedTree,
        onResolvePersistedGrants = imageViewerComponent::resolveImageFromPersistedGrants,
        onDismiss = imageViewerComponent::exit
    )
}

@Composable
fun ImageViewer(
    images: List<String>,
    initialIndex: Int = 0,
    onDownload: (String) -> Unit = {},
    onEdit: (String) -> Unit = {},
    onResolveGrantedImage: (Uri, String, (String?) -> Unit) -> Unit = { _, _, _ -> },
    onResolvePersistedGrants: (String, (String?) -> Unit) -> Unit = { _, _ -> },
    onDismiss: () -> Unit
) {
    val immersiveModeState = rememberImmersiveModeState()
    val pagerState = rememberPagerState(initialPage = initialIndex) { images.size }

    ImageViewerBaseScreen(
        onDismiss = onDismiss,
        immersiveModeState = immersiveModeState,
        title = {
            PageIndicator(
                pageCount = images.size,
                currentPage = pagerState.currentPage
            )
        },
        actions = {
            EditButton(
                images = images,
                currentPage = pagerState.currentPage,
                onEdit = onEdit
            )
            DownloadButton(
                images = images,
                currentPage = pagerState.currentPage,
                onDownload = onDownload
            )
        }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { images[it] },
            beyondViewportPageCount = 1
        ) { index ->
            ZoomableImagePage(
                imageUrl = images[index],
                onTap = { immersiveModeState.toggle() },
                onResolveGrantedImage = onResolveGrantedImage,
                onResolvePersistedGrants = onResolvePersistedGrants
            )
        }
    }
}

@Composable
private fun ZoomableImagePage(
    imageUrl: String,
    onTap: () -> Unit,
    onResolveGrantedImage: (Uri, String, (String?) -> Unit) -> Unit,
    onResolvePersistedGrants: (String, (String?) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val zoomState = rememberZoomState(maxScale = MaxZoomScale)
    var loadRetry by remember(imageUrl) { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var grantedImageUri by remember(imageUrl) { mutableStateOf<String?>(null) }
    var persistedGrantChecked by remember(imageUrl) { mutableStateOf(false) }

    // 媒体图片权限覆盖所有公共目录;SAF 文件夹授权留给不想给全量权限的用户(Download 根目录等系统会拒授权)
    val mediaPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
    var mediaPermissionGranted by remember(imageUrl, loadRetry) {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, mediaPermission) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    // file:// 直开没权限时,先用已持久化的 SAF 授权静默解析一遍,兜不住再引导授权
    val needsAccessGuidance = isError && grantedImageUri == null &&
        imageUrl.startsWith("file://") && !mediaPermissionGranted
    LaunchedEffect(needsAccessGuidance) {
        if (needsAccessGuidance && !persistedGrantChecked) {
            onResolvePersistedGrants(imageUrl) { resolved ->
                if (resolved != null) grantedImageUri = resolved
                persistedGrantChecked = true
            }
        }
    }

    val treeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        if (treeUri != null && treeUri != Uri.EMPTY) {
            onResolveGrantedImage(treeUri, imageUrl) { resolved ->
                if (resolved != null) {
                    grantedImageUri = resolved
                } else {
                    AppToastHost.showFailureToast(R.string.image_not_in_granted_folder)
                }
            }
        }
    }

    val mediaPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // 权限生效后 file:// 路径直接可读,原模型重试即可
            mediaPermissionGranted = true
            loadRetry++
        } else {
            AppToastHost.showFailureToast(R.string.grant_permission_manual)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        key(loadRetry) {
            Picture(
                model = grantedImageUri ?: imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                shape = RectangleShape,
                showTransparencyChecker = false,
                shimmerEnabled = false,
                crossfadeEnabled = true,
                modifier = Modifier
                    .fillMaxSize()
                    .zoomable(
                        zoomState = zoomState,
                        onTap = { onTap() },
                        onDoubleTap = { position ->
                            zoomState.toggleScale(
                                targetScale = DoubleTapZoomScale,
                                position = position
                            )
                        }
                    ),
                onState = { state ->
                    isLoading = state is AsyncImagePainter.State.Loading
                    isError = state is AsyncImagePainter.State.Error
                }
            )
        }

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        AnimatedVisibility(
            visible = isError,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            when {
                // 静默检查已持久化的 SAF 授权,期间不闪错误图标
                needsAccessGuidance && !persistedGrantChecked -> Unit
                needsAccessGuidance -> {
                    // 外部 file:// 直开没有读权限:引导授权,媒体权限 / 所在文件夹(SAF)二选一
                    FolderAccessGuidance(
                        onGrantMedia = { mediaPermissionLauncher.launch(mediaPermission) },
                        onGrantFolder = { treeLauncher.launch(initialFolderTreeUri(imageUrl)) }
                    )
                }
                else -> {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineError,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

/**
 * file:// 无权限时的引导面板:媒体图片权限(主)/ 仅授权所在文件夹 SAF(次)。
 * 查看器背景固定为深色,文案直接用白色,不随主题走。
 */
@Composable
private fun FolderAccessGuidance(
    onGrantMedia: () -> Unit,
    onGrantFolder: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineError,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.image_load_no_permission),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        GlassTonalButton(
            onClick = onGrantMedia,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Text(stringResource(R.string.grant_media_access))
        }
        Spacer(Modifier.height(4.dp))
        GlassTextButton(
            onClick = onGrantFolder,
            contentColor = Color.White
        ) {
            Text(stringResource(R.string.grant_folder_access))
        }
    }
}

/**
 * 为 SAF 目录选择器构造图片所在文件夹的初始定位(仅主存储,失败返回 null 由系统默认位置兜底)。
 * EXTRA_INITIAL_URI 必须是 document 形态的 Uri,tree 形态会被 DocumentsUI 忽略回退到上次位置。
 */
private fun initialFolderTreeUri(imageUrl: String): Uri? {
    return runCatching {
        val path = Uri.parse(imageUrl).path ?: return null
        val normalized = path.replaceFirst("^/sdcard/".toRegex(), "/storage/emulated/0/")
        val parent = normalized.substringBeforeLast('/', "")
        if (!parent.startsWith("/storage/emulated/0/")) return null
        DocumentsContract.buildDocumentUri(
            "com.android.externalstorage.documents",
            "primary:${parent.removePrefix("/storage/emulated/0/")}"
        )
    }.getOrNull()
}

private fun isNetworkImage(url: String): Boolean {
    return url.startsWith("http://") || url.startsWith("https://")
}

@Composable
private fun EditButton(
    images: List<String>,
    currentPage: Int,
    onEdit: (String) -> Unit
) {
    val url = images.getOrNull(currentPage) ?: return

    AnimatedVisibility(
        visible = !isNetworkImage(url),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ViewerActionButton(onClick = { onEdit(url) }) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ImageEdit,
                contentDescription = stringResource(R.string.edit)
            )
        }
    }
}

@Composable
private fun DownloadButton(
    images: List<String>,
    currentPage: Int,
    onDownload: (String) -> Unit
) {
    val url = images.getOrNull(currentPage) ?: return

    AnimatedVisibility(
        visible = isNetworkImage(url),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ViewerActionButton(onClick = { onDownload(url) }) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownload,
                contentDescription = "Download"
            )
        }
    }
}
