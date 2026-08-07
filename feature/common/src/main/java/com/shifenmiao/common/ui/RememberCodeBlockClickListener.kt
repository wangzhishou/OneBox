package com.shifenmiao.common.ui

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.shifenmiao.base.provider.LocalDataDraftHelper
import com.shifenmiao.common.file.TempBitmapFileUtils
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.image.ImageViewerInfo
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import io.noties.markwon.plugins.codeblock.CodeBlockClickListener
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun rememberCodeBlockClickListener(
    appComponent: AppComponent,
    onA2uiSubmit: ((String) -> Unit)? = null
): CodeBlockClickListener {
    val pendingSaveRequest = remember { mutableStateOf<PendingCodeBlockSave?>(null) }
    val localNavigation = LocalUrlNavigator.current
    val dataDraftHelper = LocalDataDraftHelper.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val appComponentState = rememberUpdatedState(appComponent)
    val localNavigationState = rememberUpdatedState(localNavigation)
    val dataDraftHelperState = rememberUpdatedState(dataDraftHelper)
    val contextState = rememberUpdatedState(context)
    val onSubmitState = rememberUpdatedState(onA2uiSubmit)

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        val request = pendingSaveRequest.value
        if (uri != null && request != null) {
            when (request) {
                is PendingCodeBlockSave.Code -> {
                    appComponentState.value.saveCodeFile(
                        uri = uri,
                        onResult = { result -> appComponent.parseFileSaveResult(result) },
                        codeString = request.code,
                    )
                }

                is PendingCodeBlockSave.MermaidBitmap -> {
                    appComponentState.value.saveMermaidBitmapFile(
                        uri = uri,
                        onResult = { result -> appComponent.parseFileSaveResult(result) },
                        bitmap = request.bitmap,
                    )
                }

                is PendingCodeBlockSave.MermaidFile -> {
                    appComponentState.value.saveMermaidFile(
                        uri = uri,
                        onResult = { result -> appComponent.parseFileSaveResult(result) },
                        fileUri = request.file.toURI().toString().let(android.net.Uri::parse),
                    )
                }
            }
        }
        pendingSaveRequest.value = null
    }

    return remember(scope, createDocumentLauncher) {
        object : CodeBlockClickListener {
            private var _isHighlighted: Boolean = false

            override var isHighlighted: Boolean
                get() = _isHighlighted
                set(value) {
                    _isHighlighted = value
                }

            override fun onWidgetButtonClicked(language: String, code: String): Boolean {
                scope.launch {
                    val draftId = dataDraftHelperState.value.createDraft(
                        draftType = ListItemType.HTML.id,
                        data = code
                    )
                    localNavigationState.value.navigate(Screen.CreateHtml(draftId = draftId))
                }
                return true
            }

            override fun onCopyButtonClicked(code: String): Boolean {
                Clipboard.copy(code)
                return true
            }

            override fun onRunButtonClicked(language: String, code: String): Boolean {
                appComponentState.value.runCode(language, code)
                return true
            }

            override fun onSaveButtonClicked(language: String, code: String): Boolean {
                pendingSaveRequest.value = PendingCodeBlockSave.Code(
                    code = code
                )
                val mimeType = MimeType.All.fromLanguage(language)
                val extension = mimeType.toFileExtension()
                createDocumentLauncher.launch("code_${System.currentTimeMillis()}.$extension")
                return true
            }

            override fun onMermaidSaveClicked(code: String, bitmap: Bitmap): Boolean {
                pendingSaveRequest.value = PendingCodeBlockSave.MermaidBitmap(
                    bitmap = bitmap,
                )
                createDocumentLauncher.launch("mermaid_${System.currentTimeMillis()}.png")
                return true
            }

            override fun onMermaidFullscreenClicked(code: String, bitmap: Bitmap): Boolean {
                openMermaidBitmap(bitmap = bitmap)
                return true
            }

            override fun onMermaidSaveFile(code: String, file: File): Boolean {
                val extension = file.extension.ifBlank { "svg" }
                pendingSaveRequest.value = PendingCodeBlockSave.MermaidFile(
                    file = file,
                )
                createDocumentLauncher.launch("mermaid_${System.currentTimeMillis()}.$extension")
                return true
            }

            override fun onMermaidFullscreenFile(code: String, file: File): Boolean {
                openImageViewer(file.toURI().toString())
                return true
            }

            override fun onA2uiSubmit(formData: String): Boolean {
                onSubmitState.value?.invoke(formData)
                return true
            }

            private fun openMermaidBitmap(bitmap: Bitmap) {
                scope.launch {
                    try {
                        openImageViewer(
                            TempBitmapFileUtils.saveBitmapToTempFile(
                                context = contextState.value,
                                bitmap = bitmap
                            )
                        )
                    } catch (e: Exception) {
                        AppToastHost.showToast("打开失败: ${e.message}")
                    }
                }
            }

            private fun openImageViewer(imageUri: String) {
                localNavigationState.value.navigate(
                    Screen.ImageViewer(
                        imageViewerInfo = ImageViewerInfo(
                            images = listOf(imageUri),
                            initialIndex = 0
                        )
                    )
                )
            }
        }
    }
}

private sealed interface PendingCodeBlockSave {
    data class Code(
        val code: String,
    ) : PendingCodeBlockSave

    data class MermaidBitmap(
        val bitmap: Bitmap,
    ) : PendingCodeBlockSave

    data class MermaidFile(
        val file: File,
    ) : PendingCodeBlockSave
}

