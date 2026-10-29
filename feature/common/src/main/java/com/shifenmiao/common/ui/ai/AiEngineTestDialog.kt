package com.shifenmiao.common.ui.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.ai.AiEngineConnectivityTester
import com.shifenmiao.common.ai.AiEngineTestFailureKind
import com.shifenmiao.common.ai.AiEngineTestResult
import com.shifenmiao.core.R
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AiEngineTestDialog(
    aiEngine: AiEngine? = null,
    onDismiss: () -> Unit,
    onApiTestSuccess: (AiEngine) -> Unit,
    autoTest: Boolean = false,
) {
    if (aiEngine == null) {
        ActionUtils.showError("AI Engine is null")
        return
    }
    var isLoading by remember { mutableStateOf(autoTest) }
    var testResult by remember { mutableStateOf<AiEngineTestResult?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (autoTest) {
            coroutineScope.launch(Dispatchers.IO) {
                val result = AiEngineConnectivityTester.test(aiEngine)
                testResult = result
                isLoading = false
                if (result.success) {
                    onApiTestSuccess(aiEngine)
                    onDismiss()
                }
            }
        }
    }

    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading
        ),
        title = {
            Text(
                text = stringResource(R.string.test_ai_engine_connection),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "${stringResource(R.string.provider)}: ${aiEngine.title}")
                Text(text = "${stringResource(R.string.model)}: ${aiEngine.model.name}")

                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                AnimatedVisibility(visible = testResult != null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (testResult?.success == true) {
                                stringResource(R.string.connection_successful)
                            } else {
                                stringResource(R.string.connection_failed)
                            },
                            color = if (testResult?.success == true) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            },
                            fontWeight = FontWeight.Bold
                        )

                        // 失败归因: 按连接/鉴权/限流/服务商故障分类给可操作的排查提示,
                        // 详细 HTTP 日志仍保留在下方
                        testResult
                            ?.takeIf { !it.success }
                            ?.failureKind
                            ?.let { kind ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = failureKindText(kind),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Medium,
                                )
                            }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (testResult?.httpLogs?.isNotEmpty() == true) {
                            Text(
                                text = "HTTP Request/Response Logs:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                shape = AppTheme.shapes.getTextFieldShape()
                            ) {
                                SelectionContainer {
                                    Text(
                                        text = testResult?.httpLogs ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (testResult?.errorMessage != null) {
                            Text(
                                text = "Error Details:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = AppTheme.shapes.getMediumShape()
                            ) {
                                SelectionContainer {
                                    Text(
                                        text = testResult?.errorMessage ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (testResult?.success == true) {
                        onApiTestSuccess(aiEngine)
                        onDismiss()
                    } else {
                        testResult = null
                        isLoading = true
                        coroutineScope.launch(Dispatchers.IO) {
                            testResult = AiEngineConnectivityTester.test(aiEngine)
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading
            ) {
                Text(
                    if (testResult?.success == true) {
                        stringResource(R.string.button_confirm)
                    } else {
                        stringResource(R.string.test_connection)
                    }
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun failureKindText(kind: AiEngineTestFailureKind): String {
    return stringResource(
        when (kind) {
            AiEngineTestFailureKind.CONNECT_FAILED -> R.string.ai_test_failure_connect_failed
            AiEngineTestFailureKind.RESPONSE_TIMEOUT -> R.string.ai_test_failure_response_timeout
            AiEngineTestFailureKind.AUTH_INVALID -> R.string.ai_test_failure_auth_invalid
            AiEngineTestFailureKind.MODEL_NOT_FOUND -> R.string.ai_test_failure_model_not_found
            AiEngineTestFailureKind.RATE_LIMITED -> R.string.ai_test_failure_rate_limited
            AiEngineTestFailureKind.SERVER_ERROR -> R.string.ai_test_failure_server_error
            AiEngineTestFailureKind.UNKNOWN -> R.string.ai_test_failure_unknown
        }
    )
}
