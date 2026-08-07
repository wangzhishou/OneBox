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
import com.shifenmiao.core.R
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AuthType
import com.shifenmiao.network.AiRequestUrlResolver
import com.shifenmiao.network.AiRequestUrlResolver.normalizeBaseUrl
import com.shifenmiao.network.NetworkBuilder
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

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
    var testResult by remember { mutableStateOf<TestResult?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (autoTest) {
            coroutineScope.launch(Dispatchers.IO) {
                val result = testAiEngine(aiEngine)
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
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            fontWeight = FontWeight.Bold
                        )

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
                                color = MaterialTheme.colorScheme.surfaceVariant,
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
                            testResult = testAiEngine(aiEngine)
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

data class TestResult(
    val success: Boolean,
    val errorMessage: String? = null,
    val httpLogs: String? = null,
)

private fun testAiEngine(aiEngine: AiEngine): TestResult {
    return try {
        val logBuffer = StringBuilder()

        val loggingInterceptor = HttpLoggingInterceptor { message ->
            logBuffer.append(message).append("\n")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(150, TimeUnit.SECONDS)
            .readTimeout(150, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()

        val url = AiRequestUrlResolver.joinUrl(
            baseUrl = normalizeBaseUrl(
                baseUrl = aiEngine.requestUrl,
                fallbackBaseUrl = NetworkBuilder.ensureValidBaseUrl(aiEngine)
            ),
            path = aiEngine.requestPath
        )

        val jsonBody = if (aiEngine.requestProtocol == AiRequestProtocol.RESPONSES_COMPATIBLE) {
            JSONObject().apply {
                put("model", aiEngine.model.name)
                put("input", JSONArray().apply {
                    put(JSONObject().apply {
                        put("type", "message")
                        put("role", "user")
                        put("content", JSONArray().apply {
                            put(JSONObject().apply {
                                put("type", "input_text")
                                put("text", "Say this is a test!")
                            })
                        })
                    })
                })
                put("stream", true)
            }
        } else {
            JSONObject().apply {
                put("model", aiEngine.model.name)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", "Say this is a test!")
                    })
                })
                put("stream", true)
            }
        }

        val requestBuilder = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")

        if (aiEngine.authorizationCode.isNotBlank()) {
            when {
                aiEngine.requestProtocol == AiRequestProtocol.ANTHROPIC_COMPATIBLE -> {
                    requestBuilder.addHeader("x-api-key", aiEngine.authorizationCode)
                }
                aiEngine.authType == AuthType.API_KEY -> {
                    requestBuilder.addHeader("api-key", aiEngine.authorizationCode)
                }
                aiEngine.requestProtocol != AiRequestProtocol.OWN_PROXY -> {
                    requestBuilder.addHeader("Authorization", "Bearer ${aiEngine.authorizationCode}")
                }
            }
        }

        val request = requestBuilder
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            if (response.looksLikeStreamingResponse()) {
                TestResult(success = true, httpLogs = logBuffer.toString())
            } else {
                TestResult(
                    success = false,
                    errorMessage = buildString {
                        append("Response received but does not appear to be a stream response")
                        response.body.contentType().toString().takeIf { it.isNotBlank() }?.let {
                            append(" (content-type=")
                            append(it)
                            append(')')
                        }
                    },
                    httpLogs = logBuffer.toString()
                )
            }
        } else {
            TestResult(
                success = false,
                errorMessage = "HTTP ${response.code}: ${response.message}",
                httpLogs = logBuffer.toString()
            )
        }
    } catch (e: Exception) {
        TestResult(
            success = false,
            errorMessage = e.message ?: "Unknown error",
            httpLogs = null
        )
    }
}

private fun Response.looksLikeStreamingResponse(): Boolean {
    val responseBody = body
    val contentType = responseBody.contentType().toString()
    if (contentType.contains("text/event-stream", ignoreCase = true)) {
        return true
    }

    if (header("x-dashscope-finished")?.equals("false", ignoreCase = true) == true) {
        return true
    }

    val bodyString = runCatching { responseBody.string() }.getOrDefault("")

    return bodyString.contains("data: ") ||
        bodyString.contains("\"delta\"") ||
        bodyString.contains("\"chunk\"") ||
        bodyString.contains("\"response.output_text.delta\"")
}

