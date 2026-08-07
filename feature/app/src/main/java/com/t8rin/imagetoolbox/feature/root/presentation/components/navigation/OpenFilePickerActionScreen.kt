package com.t8rin.imagetoolbox.feature.root.presentation.components.navigation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.shifenmiao.common.logic.AppComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.navigation.ScreenCallbackResult

@Composable
fun OpenFilePickerActionScreen(
    screen: Screen.OpenFilePicker,
    appComponent: AppComponent
) {
    var completed by remember { mutableStateOf(false) }

    fun finish(result: ScreenCallbackResult) {
        if (completed) return
        completed = true
        screen.onResult?.invoke(result)
        appComponent.onGoBack()
    }

    val singleFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        finish(
            if (uri != null) {
                ScreenCallbackResult.opened(
                    url = uri.toString(),
                    extra = mapOf(
                        "uris" to listOf(uri.toString()),
                        "multiple" to false,
                        "mimeTypes" to screen.mimeTypes
                    )
                )
            } else {
                ScreenCallbackResult.cancelled()
            }
        )
    }

    val multipleFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        finish(
            if (uris.isNotEmpty()) {
                val uriStrings = uris.map { it.toString() }
                ScreenCallbackResult.opened(
                    url = uriStrings.firstOrNull(),
                    data = uriStrings.joinToString(separator = "\n"),
                    extra = mapOf(
                        "uris" to uriStrings,
                        "multiple" to true,
                        "mimeTypes" to screen.mimeTypes
                    )
                )
            } else {
                ScreenCallbackResult.cancelled()
            }
        )
    }

    LaunchedEffect(screen.mimeTypes, screen.allowMultiple) {
        if (completed) return@LaunchedEffect
        val mimeTypes = screen.mimeTypes.ifEmpty { listOf("*/*") }.toTypedArray()
        if (screen.allowMultiple) {
            multipleFileLauncher.launch(mimeTypes)
        } else {
            singleFileLauncher.launch(mimeTypes)
        }
    }

    BackHandler(enabled = !completed) {
        finish(ScreenCallbackResult.cancelled())
    }

    Box(modifier = Modifier.fillMaxSize())
}

