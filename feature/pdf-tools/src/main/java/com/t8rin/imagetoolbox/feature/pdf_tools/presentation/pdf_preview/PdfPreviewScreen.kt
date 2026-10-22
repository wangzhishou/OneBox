package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.pdf_preview

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import com.t8rin.imagetoolbox.core.resources.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Search
import com.wanbaohe.core.ui.utils.content.FileAccessGuidance
import com.wanbaohe.core.ui.utils.content.rememberFileAccessController
import com.wanbaohe.core.ui.utils.content_pickers.rememberFilePicker
import com.wanbaohe.core.ui.utils.helper.ContextUtils.rememberFilename
import com.wanbaohe.core.ui.utils.helper.ImageUtils.rememberPdfPages
import com.wanbaohe.core.ui.utils.helper.isPortraitOrientationAsState
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.feature.pdf_tools.data.utils.canUseNewPdfFully
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.BasePdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.preview.screenLogic.PreviewPdfToolComponent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.root.components.PdfViewer
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSearch

@Composable
fun PdfPreviewScreen(component: PreviewPdfToolComponent) {
    val isPortrait by isPortraitOrientationAsState()

    var isSearching by remember(component.uri) {
        mutableStateOf(false)
    }

    // file:// 直开无读权限时走统一授权引导(PDF 非媒体文件,无媒体权限路径)
    val accessController = rememberFileAccessController(
        uriString = component.initialUri?.toString().orEmpty(),
        isLoadFailed = component.loadFailed,
        mediaReadPermission = null
    )

    // SAF 授权恢复出可读 Uri 后自动重载
    LaunchedEffect(accessController.recoveredUriString) {
        accessController.recoveredUriString?.let { component.setUri(Uri.parse(it)) }
    }

    BasePdfToolContent(
        component = component,
        contentPicker = rememberFilePicker(
            mimeType = MimeType.Pdf,
            onSuccess = component::setUri
        ),
        isPickedAlready = component.initialUri != null,
        canShowScreenData = component.uri != null,
        title = component.uri?.let {
            rememberFilename(it)
        }.orEmpty().ifEmpty { stringResource(R.string.preview_pdf) },
        actions = {},
        forceImagePreviewToMax = component.uri != null,
        placeControlsSeparately = true,
        canShare = true,
        canSave = false,
        topAppBarPersistentActions = {
            if (component.uri != null && canUseNewPdfFully()) {
                EnhancedIconButton(
                    onClick = {
                        isSearching = !isSearching
                    }
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSearch,
                        contentDescription = stringResource(R.string.search_here)
                    )
                }
            }
        },
        drawBottomShadow = !isSearching,
        controls = {
            if (rememberPdfPages(component.uri).value > 0) {
                PdfViewer(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (isPortrait) 104.dp else 0.dp),
                    uri = component.uri,
                    contentPadding = PaddingValues(),
                    isSearching = isSearching
                )
            }
        },
        onFilledPassword = {
            component.setUri(component.uri)
        },
        noDataOverlay = {
            // 授权引导覆盖层:面板是深色背景设计,页面浅色故垫深色容器
            if (accessController.isGuidanceVisible || accessController.isCheckingPersistedGrants) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (accessController.isGuidanceVisible) {
                        FileAccessGuidance(accessController)
                    } else {
                        // 静默检查已持久化授权期间只出 loading,不闪引导面板
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    )
}
