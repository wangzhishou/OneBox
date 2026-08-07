package com.wanbaohe.dynamicui
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.wanbaohe.dynamicui.action.ActionContext
import com.wanbaohe.dynamicui.action.ActionEngine
import com.wanbaohe.dynamicui.action.DynamicUiInternalState
import com.wanbaohe.dynamicui.action.ActionRegistry
import com.wanbaohe.dynamicui.action.NavigationCallback
import com.wanbaohe.dynamicui.action.createBuiltinActionRegistry
import com.wanbaohe.dynamicui.components.registerLayoutComponents
import com.wanbaohe.dynamicui.components.registerMaterialComponents
import com.wanbaohe.dynamicui.components.registerMediaComponents
import com.wanbaohe.dynamicui.ir.ActionSpec
import com.wanbaohe.dynamicui.ir.UiNode
import com.wanbaohe.dynamicui.modifier.ModifierPipeline
import com.wanbaohe.dynamicui.modifier.ModifierRegistry
import com.wanbaohe.dynamicui.parser.DualFormatParser
import com.wanbaohe.dynamicui.renderer.ComponentRegistry
import com.wanbaohe.dynamicui.renderer.NodeRenderer
import com.wanbaohe.dynamicui.renderer.RenderContext
import com.wanbaohe.dynamicui.renderer.UiNodeRenderer
import com.wanbaohe.dynamicui.state.UiStateScope
import com.shifenmiao.base.ui.picker.ChineseDatePickerDialog
import com.shifenmiao.base.ui.picker.ChineseDateRangePickerDialog
import com.shifenmiao.base.ui.picker.ChineseTimeRangePickerDialog
import com.shifenmiao.base.ui.picker.rememberCityPickerState
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorPickerSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTimePickerDialog
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

import androidx.compose.runtime.rememberUpdatedState

// ─────────────────────────────────────────────────────────────────────────────
// Public API
// ─────────────────────────────────────────────────────────────────────────────

/**
 * # DynamicUi
 *
 * Top-level entry-point for rendering a JSON-driven dynamic screen.
 *
 * ## Quick start
 * ```kotlin
 * DynamicUi(json = myJsonString, modifier = Modifier.fillMaxSize())
 * ```
 *
 * ## With navigation + custom actions
 * ```kotlin
 * DynamicUi(
 *     json = screenConfig,
 *     onNavigate = { screen, params -> component.onNavigate(screen, params) },
 *     initialState = mapOf("userId" to userId),
 * )
 * ```
 *
 * @param json            JSON config string
 * @param modifier        Modifier applied to the root container
 * @param onNavigate      Navigation callback; called when a navigate action fires
 * @param onBack          Back navigation callback
 * @param initialState    Merged over the dataContext declared in the JSON
 * @param onUnhandledAction Callback for actions that no handler consumed
 * @param loadingContent  Custom loading placeholder
 * @param errorContent    Custom error placeholder
 */
@Composable
fun DynamicUi(
    json: String,
    modifier: Modifier = Modifier,
    onNavigate: NavigationCallback? = null,
    onHostAction: ((actionName: String, params: Map<String, String>, stateScope: UiStateScope) -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    initialState: Map<String, Any?> = emptyMap(),
    onUnhandledAction: ((ActionSpec) -> Unit)? = null,
    onUnhandledActionWithState: ((ActionSpec, UiStateScope) -> Unit)? = null,
    loadingContent: @Composable () -> Unit = { LoadingPlaceholder() },
    errorContent: @Composable (String) -> Unit = { ErrorPlaceholder(it) },
) {
    val androidContext = LocalContext.current
    val scope = rememberCoroutineScope()

    // ── Parse (async, cached) ─────────────────────────────────────────────────
    var parseResult by remember(json) {
        mutableStateOf<DualFormatParser.ParseResult?>(null)
    }
    var parseError by remember(json) { mutableStateOf<String?>(null) }

    LaunchedEffect(json) {
        runCatching {
            DynamicUiEnv.parser.parseJsonAsync(json)
        }.onSuccess { result ->
            parseResult = result
            parseError = null
        }.onFailure { e ->
            parseError = e.message ?: "Parse error"
        }
    }

    // ── State scope ───────────────────────────────────────────────────────────
    val stateScope = remember(parseResult) {
        val merged = (parseResult?.initialState ?: emptyMap()) + initialState
        UiStateScope(merged)
    }
    LaunchedEffect(initialState) { stateScope.patch(initialState) }

    // ── Action dispatch ───────────────────────────────────────────────────────
    val currentOnNavigate by rememberUpdatedState(onNavigate)
    val currentOnHostAction by rememberUpdatedState(onHostAction)
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnUnhandledAction by rememberUpdatedState(onUnhandledAction)
    val currentOnUnhandledActionWithState by rememberUpdatedState(onUnhandledActionWithState)
    val currentAndroidContext by rememberUpdatedState(androidContext)

    val dispatch: (ActionSpec, UiStateScope, RenderContext, Map<String, Any?>) -> Unit = remember {
        { action, activeScope, ctx, itemCtx ->
            val actionCtx = ActionContext(
                androidContext = currentAndroidContext,
                onNavigate = currentOnNavigate,
                onHostAction = currentOnHostAction,
                onBack = currentOnBack,
                stateScope = activeScope,
                itemContext = itemCtx,
            )
            scope.launch {
                val result = DynamicUiEnv.actionEngine.dispatchSuspending(action, actionCtx)
                if (!result.handled) {
                    currentOnUnhandledAction?.invoke(result.action)
                    currentOnUnhandledActionWithState?.invoke(result.action, activeScope)
                }
            }
        }
    }

    val renderCtx = remember(dispatch) {
        RenderContext(
            registry = DynamicUiEnv.components,
            modifierPipeline = DynamicUiEnv.modifierPipeline,
            actionDispatcher = dispatch,
        )
    }

    // ── Render ────────────────────────────────────────────────────────────────
    Box(modifier = modifier.imePadding()) {
        when {
            parseError != null -> errorContent(parseError!!)
            parseResult == null -> loadingContent()
            else -> UiNodeRenderer.Render(
                node = parseResult!!.root,
                scope = stateScope,
                context = renderCtx,
            )
        }
        PickerOverlayHost(stateScope)
    }
}

// ─── Overload: pre-parsed node ────────────────────────────────────────────────

@Composable
fun DynamicUi(
    root: UiNode,
    modifier: Modifier = Modifier,
    onNavigate: NavigationCallback? = null,
    onHostAction: ((actionName: String, params: Map<String, String>, stateScope: UiStateScope) -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    initialState: Map<String, Any?> = emptyMap(),
    stateScope: UiStateScope? = null,
    onUnhandledAction: ((ActionSpec) -> Unit)? = null,
    onUnhandledActionWithState: ((ActionSpec, UiStateScope) -> Unit)? = null,
) {
    val androidContext = LocalContext.current
    val scope = rememberCoroutineScope()
    val resolvedStateScope = stateScope ?: remember(root) { UiStateScope(initialState) }
    LaunchedEffect(initialState, stateScope) {
        if (stateScope == null) {
            resolvedStateScope.patch(initialState)
        }
    }

    val currentOnNavigate by rememberUpdatedState(onNavigate)
    val currentOnHostAction by rememberUpdatedState(onHostAction)
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnUnhandledAction by rememberUpdatedState(onUnhandledAction)
    val currentOnUnhandledActionWithState by rememberUpdatedState(onUnhandledActionWithState)
    val currentAndroidContext by rememberUpdatedState(androidContext)

    val dispatch: (ActionSpec, UiStateScope, RenderContext, Map<String, Any?>) -> Unit = remember {
        { action, activeScope, ctx, itemCtx ->
            val actionCtx = ActionContext(
                androidContext = currentAndroidContext,
                onNavigate = currentOnNavigate,
                onHostAction = currentOnHostAction,
                onBack = currentOnBack,
                stateScope = activeScope,
                itemContext = itemCtx,
            )
            scope.launch {
                val result = DynamicUiEnv.actionEngine.dispatchSuspending(action, actionCtx)
                if (!result.handled) {
                    currentOnUnhandledAction?.invoke(result.action)
                    currentOnUnhandledActionWithState?.invoke(result.action, activeScope)
                }
            }
        }
    }
    val renderCtx = remember(dispatch) {
        RenderContext(
            registry = DynamicUiEnv.components,
            modifierPipeline = DynamicUiEnv.modifierPipeline,
            actionDispatcher = dispatch,
        )
    }

    Box(modifier = modifier) {
        UiNodeRenderer.Render(root, resolvedStateScope, renderCtx)
        PickerOverlayHost(resolvedStateScope)
    }
}

// ─── Loading / Error placeholders ────────────────────────────────────────────

@Composable
private fun LoadingPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorPlaceholder(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "⚠ $message",
            color = Color.Red,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickerOverlayHost(scope: UiStateScope) {
    val pickerVisible = scope.getByPath(DynamicUiInternalState.PICKER_VISIBLE) == true
    if (!pickerVisible) return

    val pickerType = scope.getByPath(DynamicUiInternalState.PICKER_TYPE)?.toString().orEmpty()
    val pickerParams = (scope.getByPath(DynamicUiInternalState.PICKER_PARAMS) as? Map<*, *>)
        ?.mapNotNull { (k, v) -> k?.toString()?.let { it to (v?.toString() ?: "") } }
        ?.toMap()
        .orEmpty()

    fun closePicker() {
        scope.setByPath(DynamicUiInternalState.PICKER_VISIBLE, false)
        scope.setByPath(DynamicUiInternalState.PICKER_TYPE, "")
        scope.setByPath(DynamicUiInternalState.PICKER_PARAMS, emptyMap<String, String>())
    }

    when (pickerType) {
        "datePicker" -> {
            val format = pickerParams["format"].orEmpty().ifBlank { "yyyy-MM-dd" }
            val target = pickerParams.resolveTargetKey()
            val initialDateMillis = pickerParams["initialDate"]?.toLongOrNull()
                ?: target?.let { scope.getByPath(it)?.toString()?.toLongOrNull() }
            ChineseDatePickerDialog(
                initialDateMillis = initialDateMillis,
                onDateSelected = { millis ->
                    if (target != null) {
                        scope.setByPath(target, formatDate(millis, format))
                    }
                    closePicker()
                },
                onDismiss = ::closePicker,
            )
        }

        "dateRangePicker" -> {
            val format = pickerParams["format"].orEmpty().ifBlank { "yyyy-MM-dd" }
            val key = pickerParams.resolveTargetKey()
            val startKey = pickerParams["startKey"]?.let(::resolveKeyPath)
            val endKey = pickerParams["endKey"]?.let(::resolveKeyPath)
            val separator = pickerParams["separator"].orEmpty().ifBlank { " ~ " }

            val initialStart = startKey?.let { parseDate(scope.getByPath(it)?.toString(), format) }
            val initialEnd = endKey?.let { parseDate(scope.getByPath(it)?.toString(), format) }

            ChineseDateRangePickerDialog(
                initialStartDateMillis = initialStart,
                initialEndDateMillis = initialEnd,
                onDateRangeSelected = { start, end ->
                    val startValue = formatDate(start, format)
                    val endValue = formatDate(end, format)
                    startKey?.let { scope.setByPath(it, startValue) }
                    endKey?.let { scope.setByPath(it, endValue) }
                    key?.let { scope.setByPath(it, "$startValue$separator$endValue") }
                    closePicker()
                },
                onDismiss = ::closePicker,
            )
        }

        "timePicker" -> {
            val timeState = rememberTimePickerState(is24Hour = true)
            val format = pickerParams["format"].orEmpty().ifBlank { "HH:mm" }
            val target = pickerParams.resolveTargetKey()
            EnhancedTimePickerDialog(
                visible = true,
                onDismissRequest = ::closePicker,
                state = timeState,
                onTimePicked = { hour, minute ->
                    if (target != null) {
                        scope.setByPath(target, formatTime(hour, minute, format))
                    }
                    closePicker()
                },
            )
        }

        "timeRangePicker" -> {
            val format = pickerParams["format"].orEmpty().ifBlank { "HH:mm" }
            val key = pickerParams.resolveTargetKey()
            val startKey = pickerParams["startKey"]?.let(::resolveKeyPath)
            val endKey = pickerParams["endKey"]?.let(::resolveKeyPath)
            val separator = pickerParams["separator"].orEmpty().ifBlank { " ~ " }
            val title = pickerParams["title"]?.ifBlank { null }
            val initialStartHour = pickerParams["initialStartHour"]?.toIntOrNull() ?: 9
            val initialStartMinute = pickerParams["initialStartMinute"]?.toIntOrNull() ?: 0
            val initialEndHour = pickerParams["initialEndHour"]?.toIntOrNull() ?: 18
            val initialEndMinute = pickerParams["initialEndMinute"]?.toIntOrNull() ?: 0
            ChineseTimeRangePickerDialog(
                initialStartHour = initialStartHour,
                initialStartMinute = initialStartMinute,
                initialEndHour = initialEndHour,
                initialEndMinute = initialEndMinute,
                title = title,
                onTimeRangeSelected = { startHour, startMinute, endHour, endMinute ->
                    val startValue = formatTime(startHour, startMinute, format)
                    val endValue = formatTime(endHour, endMinute, format)
                    startKey?.let { scope.setByPath(it, startValue) }
                    endKey?.let { scope.setByPath(it, endValue) }
                    key?.let { scope.setByPath(it, "$startValue$separator$endValue") }
                    closePicker()
                },
                onDismiss = ::closePicker,
            )
        }

        "cityPicker" -> {
            val cityPicker = rememberCityPickerState()
            val title = pickerParams["title"]
            val layer = pickerParams["layer"]?.toIntOrNull()?.coerceIn(1, 3) ?: 3
            val key = pickerParams.resolveTargetKey()
            val provinceKey = pickerParams["provinceKey"]?.ifBlank { null }
            val cityKey = pickerParams["cityKey"]?.ifBlank { null }
            val districtKey = pickerParams["districtKey"]?.ifBlank { null }
            val separator = pickerParams["separator"].orEmpty().ifBlank { " " }

            LaunchedEffect(pickerType, pickerParams.hashCode(), pickerVisible) {
                cityPicker.show(
                    title = title,
                    initData = null,
                    initLayer = layer,
                    onCancel = {
                        cityPicker.hide()
                        closePicker()
                    },
                ) { selected ->
                    cityPicker.hide()
                    provinceKey?.let { selected.province?.let { value -> scope.setByPath(it, value) } }
                    cityKey?.let { selected.city?.let { value -> scope.setByPath(it, value) } }
                    districtKey?.let { selected.district?.let { value -> scope.setByPath(it, value) } }
                    key?.let {
                        scope.setByPath(
                            it,
                            listOfNotNull(selected.province, selected.city, selected.district)
                                .filter { value -> value.isNotBlank() }
                                .joinToString(separator),
                        )
                    }
                    closePicker()
                }
            }
        }

        "colorPicker" -> {
            val target = pickerParams.resolveTargetKey()
            val allowAlpha = pickerParams["allowAlpha"]?.toBooleanStrictOrNull() ?: true
            val initialRaw = pickerParams["initialColor"]
                ?: target?.let(scope::getByPath)?.toString()
            val initialColor = parseHexColor(initialRaw) ?: Color.Black
            ColorPickerSheet(
                visible = true,
                onDismiss = ::closePicker,
                color = initialColor,
                allowAlpha = allowAlpha,
                onColorSelected = { picked ->
                    target?.let {
                        scope.setByPath(it, picked.toHex(includeAlpha = allowAlpha))
                    }
                    closePicker()
                },
            )
        }
    }
}

private fun Map<String, String>.resolveTargetKey(): String? {
    val raw = this["key"]
        ?: this["target"]
        ?: this["binding"]
        ?: return null
    return resolveKeyPath(raw)
}

private fun resolveKeyPath(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    return when {
        raw.startsWith("\${state.") && raw.endsWith("}") ->
            raw.removePrefix("\${state.").removeSuffix("}")
        raw.startsWith("state.") -> raw.removePrefix("state.")
        else -> raw
    }
}

private fun formatDate(millis: Long, pattern: String): String {
    val localDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    return runCatching {
        localDate.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    }.getOrDefault(localDate.toString())
}

private fun parseDate(dateStr: String?, pattern: String): Long? {
    if (dateStr.isNullOrBlank() || dateStr == "--") return null
    return runCatching {
        val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        val localDate = java.time.LocalDate.parse(dateStr, formatter)
        localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }.getOrNull()
}

private fun formatTime(hour: Int, minute: Int, pattern: String): String {
    val localTime = LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
    return runCatching {
        localTime.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    }.getOrDefault("%02d:%02d".format(hour, minute))
}

private fun parseHexColor(raw: String?): Color? {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return null
    return runCatching {
        val normalized = if (value.startsWith("#")) value else "#$value"
        Color(android.graphics.Color.parseColor(normalized))
    }.getOrNull()
}

private fun Color.toHex(includeAlpha: Boolean): String {
    val argb = toArgb()
    return if (includeAlpha) {
        String.format("#%08X", argb)
    } else {
        String.format("#%06X", argb and 0x00FFFFFF)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DynamicUiEnv – global singleton environment, self-initializing with defaults
// ─────────────────────────────────────────────────────────────────────────────

/**
 * DynamicUiEnv holds the shared singletons used by [DynamicUi].
 *
 * ## Zero-config usage (no Hilt required)
 * [DynamicUi] works out-of-the-box with no Application setup. On first access
 * the engine self-initializes with default instances via `by lazy`.
 *
 * ## Hilt usage
 * Call [initWithHilt] from [DynamicUiInitializer] in `Application.onCreate()`
 * **before** any [DynamicUi] composable is first rendered to inject Hilt-managed
 * singletons (with full `@IntoMap` action-handler support).
 */
object DynamicUiEnv {

    // ── Internal engine bundle (avoids 4 separate @Volatile fields) ───────────

    private data class Engine(
        val components: ComponentRegistry,
        val modifierPipeline: ModifierPipeline,
        val actionEngine: ActionEngine,
        val parser: DualFormatParser,
    )

    /**
     * Lazily-initialized default engine.
     * `by lazy` uses `LazyThreadSafetyMode.SYNCHRONIZED` internally –
     * the safest JVM pattern for one-time initialization.
     */
    private val defaultEngine: Engine by lazy {
        val modRegistry = ModifierRegistry()
        val comp = ComponentRegistry()
        // Component registration must stay static; per-instance callbacks live in RenderContext.
        comp.registerLayoutComponents()
        comp.registerMaterialComponents()
        comp.registerMediaComponents()
        Engine(
            components = comp,
            modifierPipeline = ModifierPipeline(modRegistry),
            actionEngine = ActionEngine(createBuiltinActionRegistry()),
            parser = DualFormatParser(),
        )
    }

    /** Overridden by [initWithHilt]; null means use [defaultEngine]. */
    @Volatile private var hiltEngine: Engine? = null

    private val engine: Engine get() = hiltEngine ?: defaultEngine

    val components: ComponentRegistry   get() = engine.components
    val modifierPipeline: ModifierPipeline get() = engine.modifierPipeline
    val actionEngine: ActionEngine      get() = engine.actionEngine
    val parser: DualFormatParser        get() = engine.parser

    val isInitialized: Boolean get() = hiltEngine != null || defaultEngine.let { true }

    // ── Initialization API ────────────────────────────────────────────────────

    /**
     * Called by [DynamicUiInitializer] when Hilt is active.
     * Must be called before the first [DynamicUi] composable renders.
     */
    fun initWithHilt(
        components: ComponentRegistry,
        modifierPipeline: ModifierPipeline,
        actionEngine: ActionEngine,
        parser: DualFormatParser,
    ) {
        hiltEngine = Engine(components, modifierPipeline, actionEngine, parser)
    }

    /** Manual initialization without Hilt (optional — engine auto-inits if omitted). */
    fun init(
        components: ComponentRegistry = ComponentRegistry(),
        modifiers: ModifierRegistry = ModifierRegistry(),
        actionHandlers: ActionRegistry = ActionRegistry(),
    ) {
        hiltEngine = Engine(
            components = components.also {
                it.registerLayoutComponents()
                it.registerMaterialComponents()
                it.registerMediaComponents()
            },
            modifierPipeline = ModifierPipeline(modifiers),
            actionEngine = ActionEngine(actionHandlers),
            parser = DualFormatParser(),
        )
    }

    /** Register a custom component renderer at any time. */
    fun registerComponent(type: String, renderer: NodeRenderer) {
        engine.components.register(type, renderer)
    }
}
