package com.shifenmiao.ai.ui

import androidx.compose.runtime.Composable
import com.shifenmiao.common.ui.ai.AiEngineTestDialog as CommonAiEngineTestDialog
import com.shifenmiao.model.ai.AiEngine

@Composable
fun AiEngineTestDialog(
    aiEngine: AiEngine? = null,
    onDismiss: () -> Unit,
    onApiTestSuccess: (AiEngine) -> Unit,
    autoTest: Boolean = false,
) {
    CommonAiEngineTestDialog(
        aiEngine = aiEngine,
        onDismiss = onDismiss,
        onApiTestSuccess = onApiTestSuccess,
        autoTest = autoTest,
    )
}