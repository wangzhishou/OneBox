package com.wanbaohe.dynamicui.action.handlers

import android.widget.Toast
import com.wanbaohe.dynamicui.action.ActionContext
import com.wanbaohe.dynamicui.action.ActionHandler
import com.wanbaohe.dynamicui.action.DynamicUiInternalState
import com.wanbaohe.dynamicui.action.ActionResult
import com.wanbaohe.dynamicui.ir.ActionSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

// ─── SetStateHandler ──────────────────────────────────────────────────────────

@Singleton
class SetStateHandler @Inject constructor() : ActionHandler {
    override val supportedTypes = setOf("setState")
    override suspend fun handle(action: ActionSpec, context: ActionContext): ActionResult {
        val key = action.params["key"] ?: return ActionResult.Unhandled
        val value: Any? = when (val raw = action.params["value"]) {
            null -> null
            "true" -> true
            "false" -> false
            else -> raw.toDoubleOrNull() ?: raw
        }
        context.stateScope.setByPath(key, value)
        return ActionResult.Success
    }
}

// ─── ToggleStateHandler ───────────────────────────────────────────────────────

@Singleton
class ToggleStateHandler @Inject constructor() : ActionHandler {
    override val supportedTypes = setOf("toggleState")
    override suspend fun handle(action: ActionSpec, context: ActionContext): ActionResult {
        val key = action.params["key"] ?: return ActionResult.Unhandled
        val current = context.stateScope.getByPath(key)
        context.stateScope.setByPath(key, !(current as? Boolean ?: false))
        return ActionResult.Success
    }
}

// ─── NavigateHandler ─────────────────────────────────────────────────────────

@Singleton
class NavigateHandler @Inject constructor() : ActionHandler {
    override val supportedTypes = setOf("navigate", "push", "pop", "back")

    override suspend fun handle(action: ActionSpec, context: ActionContext): ActionResult {
        return withContext(Dispatchers.Main) {
            when (action.type) {
                "back", "pop" -> {
                    context.onBack?.invoke()
                        ?: run {
                            context.stateScope.setByPath(DynamicUiInternalState.BACK, true)
                            context.stateScope.setByPath(DynamicUiInternalState.LEGACY_BACK, true)
                        }
                    ActionResult.Success
                }
                else -> {
                    val screen = action.params["screen"] ?: return@withContext ActionResult.Unhandled
                    val navParams = action.params.filter { it.key != "screen" }
                    context.onNavigate?.invoke(screen, navParams)
                        ?: run {
                            // Fallback: write to state so host can react
                            context.stateScope.setByPath(DynamicUiInternalState.NAVIGATE, screen)
                            context.stateScope.setByPath(DynamicUiInternalState.NAVIGATE_ARGS, navParams)
                            context.stateScope.setByPath(DynamicUiInternalState.LEGACY_NAVIGATE, screen)
                            context.stateScope.setByPath(DynamicUiInternalState.LEGACY_NAVIGATE_ARGS, navParams)
                        }
                    ActionResult.Success
                }
            }
        }
    }
}

@Singleton
class HostActionHandler @Inject constructor() : ActionHandler {
    override val supportedTypes = setOf("hostAction")

    override suspend fun handle(action: ActionSpec, context: ActionContext): ActionResult {
        val actionName = action.params["name"] ?: return ActionResult.Unhandled
        context.onHostAction?.invoke(
            actionName,
            action.params.filterKeys { it != "name" },
            context.stateScope
        ) ?: return ActionResult.Error("Host action handler is not available")
        return ActionResult.Success
    }
}

// ─── ToastHandler ────────────────────────────────────────────────────────────

@Singleton
class ToastHandler @Inject constructor() : ActionHandler {
    override val supportedTypes = setOf("toast")
    override suspend fun handle(action: ActionSpec, context: ActionContext): ActionResult {
        val message = action.params["message"] ?: return ActionResult.Unhandled
        val duration = if (action.params["long"] == "true") Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        withContext(Dispatchers.Main) {
            Toast.makeText(context.androidContext, message, duration).show()
        }
        return ActionResult.Success
    }
}

// ─── BackHandler ─────────────────────────────────────────────────────────────

@Singleton
class BackHandler @Inject constructor() : ActionHandler {
    override val supportedTypes = setOf("back")
    override suspend fun handle(action: ActionSpec, context: ActionContext): ActionResult {
        withContext(Dispatchers.Main) {
            context.onBack?.invoke() ?: run {
                context.stateScope.setByPath(DynamicUiInternalState.BACK, true)
                context.stateScope.setByPath(DynamicUiInternalState.LEGACY_BACK, true)
            }
        }
        return ActionResult.Success
    }
}

// ─── CopyTextHandler ─────────────────────────────────────────────────────────

@Singleton
class CopyTextHandler @Inject constructor() : ActionHandler {
    override val supportedTypes = setOf("copy", "copyText")
    override suspend fun handle(action: ActionSpec, context: ActionContext): ActionResult {
        val text = action.params["text"] ?: return ActionResult.Unhandled
        withContext(Dispatchers.Main) {
            val cm = context.androidContext
                .getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                    as android.content.ClipboardManager
            cm.setPrimaryClip(android.content.ClipData.newPlainText("DynamicUI", text))
        }
        return ActionResult.Success
    }
}

// ─── HttpHandler ─────────────────────────────────────────────────────────────

@Singleton
class HttpHandler @Inject constructor(
    private val client: OkHttpClient,
) : ActionHandler {

    override val supportedTypes = setOf("http", "request")

    override suspend fun handle(action: ActionSpec, context: ActionContext): ActionResult {
        val url = action.params["url"] ?: return ActionResult.Unhandled
        val method = action.params["method"]?.uppercase() ?: "GET"
        val responseKey = action.params["responseKey"] ?: "response"

        context.stateScope.setByPath(DynamicUiInternalState.LOADING, true)
        context.stateScope.setByPath(DynamicUiInternalState.LEGACY_LOADING, true)

        val result = withContext(Dispatchers.IO) {
            runCatching {
                val bodyParams = action.params
                    .filterKeys { it.startsWith("body.") }
                    .mapKeys { it.key.removePrefix("body.") }

                val requestBuilder = Request.Builder().url(url)
                    .addHeader("Content-Type", "application/json")

                val request = if (method == "GET" || bodyParams.isEmpty()) {
                    requestBuilder.get().build()
                } else {
                    val json = JSONObject(bodyParams).toString()
                    requestBuilder.method(
                        method,
                        json.toRequestBody("application/json".toMediaType())
                    ).build()
                }
                client.newCall(request).execute().use { response ->
                    Pair(response.isSuccessful, response.body.string())
                }
            }
        }

        context.stateScope.setByPath(DynamicUiInternalState.LOADING, false)
        context.stateScope.setByPath(DynamicUiInternalState.LEGACY_LOADING, false)

        result.onSuccess { (success, body) ->
            if (success) {
                runCatching {
                    val jsonObj = JSONObject(body)
                    val payload = buildMap<String, Any?> {
                        jsonObj.keys().forEach { key -> put(key, jsonValueToState(jsonObj.get(key))) }
                    }
                    context.stateScope.setByPath(responseKey, payload)
                    context.stateScope.setByPath("${responseKey}Raw", body)
                }.onFailure {
                    context.stateScope.setByPath(responseKey, body)
                }
            } else {
                val message = "HTTP error: $body"
                context.stateScope.setByPath(DynamicUiInternalState.ERROR, message)
                context.stateScope.setByPath(DynamicUiInternalState.LEGACY_ERROR, message)
                return ActionResult.Error(message)
            }
        }.onFailure { e ->
            val message = e.message ?: "Network error"
            context.stateScope.setByPath(DynamicUiInternalState.ERROR, message)
            context.stateScope.setByPath(DynamicUiInternalState.LEGACY_ERROR, message)
            return ActionResult.Error(message)
        }
        return ActionResult.Success
    }
}

// ─── DialogHandler ───────────────────────────────────────────────────────────

@Singleton
class DialogHandler @Inject constructor() : ActionHandler {
    override val supportedTypes = setOf("dialog")
    override suspend fun handle(action: ActionSpec, context: ActionContext): ActionResult {
        context.stateScope.setByPath(DynamicUiInternalState.DIALOG_VISIBLE, true)
        context.stateScope.setByPath(DynamicUiInternalState.LEGACY_DIALOG_VISIBLE, true)

        val title = action.params["title"] ?: ""
        val message = action.params["message"] ?: ""
        val confirm = action.params["confirm"] ?: "OK"
        val dismiss = action.params["dismiss"] ?: "Cancel"

        context.stateScope.setByPath(DynamicUiInternalState.DIALOG_TITLE, title)
        context.stateScope.setByPath(DynamicUiInternalState.DIALOG_MESSAGE, message)
        context.stateScope.setByPath(DynamicUiInternalState.DIALOG_CONFIRM, confirm)
        context.stateScope.setByPath(DynamicUiInternalState.DIALOG_DISMISS, dismiss)
        context.stateScope.setByPath(DynamicUiInternalState.LEGACY_DIALOG_TITLE, title)
        context.stateScope.setByPath(DynamicUiInternalState.LEGACY_DIALOG_MESSAGE, message)
        context.stateScope.setByPath(DynamicUiInternalState.LEGACY_DIALOG_CONFIRM, confirm)
        context.stateScope.setByPath(DynamicUiInternalState.LEGACY_DIALOG_DISMISS, dismiss)

        action.onConfirm?.let {
            context.stateScope.setByPath(DynamicUiInternalState.DIALOG_ON_CONFIRM_TYPE, it.type)
            context.stateScope.setByPath(DynamicUiInternalState.LEGACY_DIALOG_ON_CONFIRM_TYPE, it.type)
        }
        return ActionResult.Success
    }
}

// ─── Picker Handlers ─────────────────────────────────────────────────────────

@Singleton
class DatePickerHandler @Inject constructor() : ActionHandler {
    override val supportedTypes = setOf("datePicker")
    override suspend fun handle(action: ActionSpec, context: ActionContext): ActionResult {
        openPickerRequest(context, "datePicker", action.params)
        return ActionResult.Success
    }
}

@Singleton
class DateRangePickerHandler @Inject constructor() : ActionHandler {
    override val supportedTypes = setOf("dateRangePicker")
    override suspend fun handle(action: ActionSpec, context: ActionContext): ActionResult {
        openPickerRequest(context, "dateRangePicker", action.params)
        return ActionResult.Success
    }
}

@Singleton
class TimePickerHandler @Inject constructor() : ActionHandler {
    override val supportedTypes = setOf("timePicker")
    override suspend fun handle(action: ActionSpec, context: ActionContext): ActionResult {
        openPickerRequest(context, "timePicker", action.params)
        return ActionResult.Success
    }
}

@Singleton
class TimeRangePickerHandler @Inject constructor() : ActionHandler {
    override val supportedTypes = setOf("timeRangePicker")
    override suspend fun handle(action: ActionSpec, context: ActionContext): ActionResult {
        openPickerRequest(context, "timeRangePicker", action.params)
        return ActionResult.Success
    }
}

@Singleton
class CityPickerHandler @Inject constructor() : ActionHandler {
    override val supportedTypes = setOf("cityPicker")
    override suspend fun handle(action: ActionSpec, context: ActionContext): ActionResult {
        openPickerRequest(context, "cityPicker", action.params)
        return ActionResult.Success
    }
}

@Singleton
class ColorPickerHandler @Inject constructor() : ActionHandler {
    override val supportedTypes = setOf("colorPicker")
    override suspend fun handle(action: ActionSpec, context: ActionContext): ActionResult {
        openPickerRequest(context, "colorPicker", action.params)
        return ActionResult.Success
    }
}

private fun jsonValueToState(value: Any?): Any? = when (value) {
    is JSONObject -> buildMap<String, Any?> {
        value.keys().forEach { key -> put(key, jsonValueToState(value.get(key))) }
    }
    is JSONArray -> List(value.length()) { index -> jsonValueToState(value.get(index)) }
    org.json.JSONObject.NULL -> null
    else -> value
}

private fun openPickerRequest(
    context: ActionContext,
    pickerType: String,
    params: Map<String, String>,
) {
    context.stateScope.setByPath(DynamicUiInternalState.PICKER_TYPE, pickerType)
    context.stateScope.setByPath(DynamicUiInternalState.PICKER_PARAMS, params)
    context.stateScope.setByPath(DynamicUiInternalState.PICKER_VISIBLE, true)
}
