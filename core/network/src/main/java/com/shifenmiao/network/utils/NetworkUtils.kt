/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.network.utils

import com.shifenmiao.model.ModelProvider.AppJson
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.DataValue
import com.shifenmiao.model.StrapiErrorResponse
import com.shifenmiao.network.R
import com.t8rin.logger.makeLog
import retrofit2.Response

object NetworkUtils {

    suspend fun <T> safeApiCall(
        onError: (String) -> Unit = {},
        apiCall: suspend () -> Response<T>
    ): Response<T>? {
        return try {
            apiCall()
        } catch (e: kotlinx.coroutines.CancellationException) {
            // CancellationException must not be swallowed so coroutines can be cancelled cleanly.
            throw e
        } catch (e: Exception) {
            val friendlyMessage = getFriendlyErrorMessage(e)
            makeLog {
                friendlyMessage + "\n"
                "safeApiCall" + e.printStackTrace()
            }
            onError(friendlyMessage)
            null
        }
    }

    /**
     * 将异常转换为友好的错误提示信息
     * @param context Android Context
     * @param exception 捕获的异常
     * @return 友好的中文错误提示
     */
    fun getFriendlyErrorMessage(exception: Exception): String {
        val context = AppContext.getContext()
        return when (exception) {
            is java.net.UnknownHostException -> context.getString(R.string.error_network_connection_failed)
            is java.net.SocketTimeoutException -> context.getString(R.string.error_request_timeout)
            is java.net.ConnectException -> context.getString(R.string.error_cannot_connect_server)
            is javax.net.ssl.SSLException -> context.getString(R.string.error_ssl_failed)
            is java.io.IOException -> context.getString(R.string.error_network_exception)
            is kotlinx.coroutines.CancellationException -> context.getString(R.string.error_request_cancelled)
            is kotlinx.serialization.SerializationException -> context.getString(R.string.error_data_parse_failed)
            is retrofit2.HttpException -> {
                when (exception.code()) {
                    400 -> context.getString(R.string.error_bad_request)
                    401 -> context.getString(R.string.error_unauthorized)
                    403 -> context.getString(R.string.error_forbidden)
                    404 -> context.getString(R.string.error_not_found)
                    500 -> context.getString(R.string.error_server_internal)
                    502, 503 -> context.getString(R.string.error_server_maintenance)
                    else -> context.getString(R.string.error_server_code, exception.code())
                }
            }

            else -> context.getString(
                R.string.error_request_failed_with_message,
                exception.message ?: context.getString(R.string.error_unknown)
            )
        }
    }


    // Data class to hold parsed error information
    data class ErrorResult(
        val message: String,
        val details: String = "",
        val statusCode: Int = 0,
        val requestInfo: String = "",
        val responseInfo: String = ""
    )

    fun handleErrorResponse(
        response: Response<*>,
        onFriendlyErrorTip: (String) -> Unit = {},
        onFail: (String) -> Unit = {}
    ) {
        val errorResult = parseErrorResponse(response)

        // 记录详细的原始错误日志
        logDetailedError(errorResult)
        onFriendlyErrorTip(getFriendlyErrorMessageFromResult(errorResult))
        onFail(logDetailedError(errorResult))
    }

    /**
     * 根据错误结果生成友好的错误提示信息
     */
    private fun getFriendlyErrorMessageFromResult(errorResult: ErrorResult): String {
        val context = AppContext
        // 优先使用解析出的错误消息（如果是有效的）
        if (isValidErrorMessage(errorResult.message) &&
            !errorResult.message.startsWith("Error:") &&
            !errorResult.message.startsWith("Failed to parse")
        ) {
            return errorResult.message
        }

        // 如果有有效的详情信息，使用详情
        if (isValidErrorMessage(errorResult.details)) {
            return errorResult.details
        }

        // 根据状态码返回友好提示
        return when (errorResult.statusCode) {
            400 -> context.getString(R.string.error_bad_request)
            401 -> context.getString(R.string.error_unauthorized)
            403 -> context.getString(R.string.error_forbidden)
            404 -> context.getString(R.string.error_not_found)
            500 -> context.getString(R.string.error_server_internal)
            502, 503 -> context.getString(R.string.error_server_maintenance)
            in 400..499 -> context.getString(R.string.error_client_error)
            in 500..599 -> context.getString(R.string.error_server_error)
            else -> context.getString(R.string.error_request_failed)
        }
    }

    private fun parseErrorResponse(response: Response<*>): ErrorResult {
        val errorBody = response.errorBody()
        val statusCode = response.code()

        return try {
            if (errorBody != null) {
                try {
                    // Try to parse as Strapi error
                    parseStrapiError(errorBody.string())
                } catch (e: Exception) {
                    // Fall back to generic error parsing
                    makeLog { "Failed to parse as Strapi error: ${e.message}" }
                    parseGenericError(response, errorBody)
                }
            } else {
                ErrorResult(
                    message = "Error: ${response.message()}",
                    statusCode = statusCode,
                    requestInfo = getRequestInfo(response),
                    responseInfo = getResponseInfo(response)
                )
            }
        } catch (e: Exception) {
            makeLog { e.printStackTrace() }
            ErrorResult(
                message = "Failed to parse error response: ${e.message}",
                statusCode = statusCode,
                requestInfo = getRequestInfo(response),
                responseInfo = getResponseInfo(response)
            )
        }
    }

    private fun parseStrapiError(errorString: String): ErrorResult {
        val errorResponse: StrapiErrorResponse =
            AppJson.decodeFromString<StrapiErrorResponse>(errorString)

        // 合法 JSON 但非 Strapi 形状(如 nginx/上游透传的 {"message":"..."})也会
        // 因字段默认值"解码成功"且 message 为空,按退化形状抛出让调用方回退 generic 解析
        if (errorResponse.error.message.isBlank() && errorResponse.error.status == 0) {
            throw kotlinx.serialization.SerializationException("Not a Strapi-shaped error body")
        }

        val detailsMessage = when (val details = errorResponse.error.details) {
            is DataValue.StringValue -> details.value
            is DataValue.StringListValue -> details.value.joinToString(", ")
            is DataValue.IntValue -> details.value.toString()
            is DataValue.BooleanValue -> details.value.toString()
            is DataValue.MapValue -> details.value.toString()
        }

        return ErrorResult(
            message = errorResponse.error.message,
            details = detailsMessage
        )
    }

    private fun parseGenericError(
        response: Response<*>,
        errorBody: okhttp3.ResponseBody?
    ): ErrorResult {
        val statusCode = response.code()
        val errorBodyString = errorBody?.string() ?: "No error body"

        val requestInfo = getRequestInfo(response)
        val responseInfo = getResponseInfo(response)

        val errorMessage = try {
            val jsonObject = AppJson.parseToJsonElement(errorBodyString) as? JsonObject
            fun JsonObject.str(key: String) = (this[key] as? JsonPrimitive)?.contentOrNull
            when {
                jsonObject?.str("message") != null -> jsonObject.str("message")!!
                (jsonObject?.get("error") as? JsonObject) != null -> {
                    val errorObj = jsonObject.get("error") as JsonObject
                    errorObj.str("message") ?: errorBodyString
                }

                else -> errorBodyString
            }
        } catch (_: Exception) {
            errorBodyString
        }

        return ErrorResult(
            message = errorMessage,
            statusCode = statusCode,
            requestInfo = requestInfo,
            responseInfo = responseInfo
        )
    }

    private fun getRequestInfo(response: Response<*>): String {
        val request = response.raw().request
        val requestHeaders = request.headers

        return StringBuilder()
            .append("URL: ${request.url}\n")
            .append("Method: ${request.method}\n")
            .append("Headers:\n")
            .apply {
                for (i in 0 until requestHeaders.size) {
                    val name = requestHeaders.name(i)
                    val value = if (name.equals("Authorization", ignoreCase = true)) {
                        "Bearer ********"
                    } else {
                        requestHeaders.value(i)
                    }
                    append("  $name: $value\n")
                }
            }
            .toString()
    }

    private fun getResponseInfo(response: Response<*>): String {
        val responseHeaders = response.headers()

        return StringBuilder()
            .append("Code: ${response.code()}\n")
            .append("Message: ${response.message()}\n")
            .append("Headers:\n")
            .apply {
                for (i in 0 until responseHeaders.size) {
                    val name = responseHeaders.name(i)
                    val value = if (name.equals("Authorization", ignoreCase = true)) {
                        "Bearer ********"
                    } else {
                        responseHeaders.value(i)
                    }
                    append("  $name: $value\n")
                }
            }
            .toString()
    }

    private fun isValidErrorMessage(message: String): Boolean {
        return message.isNotEmpty()
                && message.isNotBlank()
                && message != "null"
                && message != "Unknown details format"
                && message != "[]"
                && message != "{}"
    }

    private fun logDetailedError(errorResult: ErrorResult): String {
        val detailedError = """
========== 网络请求错误详情 ==========
HTTP Status: ${errorResult.statusCode}

=== Request Info ===
${errorResult.requestInfo}

=== Response Info ===
${errorResult.responseInfo}

=== Error Message ===
${errorResult.message}

=== Error Details ===
${errorResult.details}
=====================================
        """.trimIndent()

        makeLog { detailedError }
        return detailedError
    }
}
