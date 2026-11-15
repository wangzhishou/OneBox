package com.shifenmiao.common.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.shifenmiao.base.provider.LocalDataDraftHelper
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.model.ListItemType
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.markdown.OneBoxMarkdownCodeActions
import kotlinx.coroutines.launch

@Composable
fun rememberMarkdownCodeActions(appComponent: AppComponent): OneBoxMarkdownCodeActions {
    val pendingCode = remember { mutableStateOf<String?>(null) }
    val localNavigation = LocalUrlNavigator.current
    val dataDraftHelper = LocalDataDraftHelper.current
    val scope = rememberCoroutineScope()
    val appComponentState = rememberUpdatedState(appComponent)
    val localNavigationState = rememberUpdatedState(localNavigation)
    val dataDraftHelperState = rememberUpdatedState(dataDraftHelper)
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        val code = pendingCode.value
        if (uri != null && code != null) {
            appComponentState.value.saveCodeFile(
                uri = uri,
                onResult = { result -> appComponent.parseFileSaveResult(result) },
                codeString = code,
            )
        }
        pendingCode.value = null
    }

    return remember(scope, createDocumentLauncher) {
        object : OneBoxMarkdownCodeActions {
            override fun copy(code: String) {
                Clipboard.copy(code)
            }

            override fun createWidget(language: String, code: String) {
                scope.launch {
                    val draftId = dataDraftHelperState.value.createDraft(
                        draftType = ListItemType.HTML.id,
                        data = code,
                    )
                    localNavigationState.value.navigate(Screen.CreateHtml(draftId = draftId))
                }
            }

            override fun run(language: String, code: String) {
                appComponentState.value.runCode(language, code)
            }

            override fun save(language: String, code: String) {
                pendingCode.value = code
                val extension = MimeType.All.fromLanguage(language).toFileExtension()
                createDocumentLauncher.launch("code_${System.currentTimeMillis()}.$extension")
            }
        }
    }
}
