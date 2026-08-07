package com.wanbaohe.dynamicui.di

import com.wanbaohe.dynamicui.action.ActionHandler
import com.wanbaohe.dynamicui.action.ActionRegistry
import com.wanbaohe.dynamicui.action.handlers.BackHandler
import com.wanbaohe.dynamicui.action.handlers.CopyTextHandler
import com.wanbaohe.dynamicui.action.handlers.DatePickerHandler
import com.wanbaohe.dynamicui.action.handlers.DateRangePickerHandler
import com.wanbaohe.dynamicui.action.handlers.DialogHandler
import com.wanbaohe.dynamicui.action.handlers.HttpHandler
import com.wanbaohe.dynamicui.action.handlers.NavigateHandler
import com.wanbaohe.dynamicui.action.handlers.TimePickerHandler
import com.wanbaohe.dynamicui.action.handlers.TimeRangePickerHandler
import com.wanbaohe.dynamicui.action.handlers.CityPickerHandler
import com.wanbaohe.dynamicui.action.handlers.ColorPickerHandler
import com.wanbaohe.dynamicui.action.handlers.SetStateHandler
import com.wanbaohe.dynamicui.action.handlers.ToggleStateHandler
import com.wanbaohe.dynamicui.action.handlers.ToastHandler
import com.wanbaohe.dynamicui.components.registerLayoutComponents
import com.wanbaohe.dynamicui.components.registerMaterialComponents
import com.wanbaohe.dynamicui.components.registerMediaComponents
import com.wanbaohe.dynamicui.modifier.ModifierPipeline
import com.wanbaohe.dynamicui.modifier.ModifierRegistry
import com.wanbaohe.dynamicui.renderer.ComponentRegistry
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * DynamicUiModule – wires together all DI bindings for the dynamic UI engine.
 *
 * ## Extending the engine with Hilt
 * Feature modules can contribute custom component renderers or action handlers without
 * touching this module:
 *
 * ```kotlin
 * @Module @InstallIn(SingletonComponent::class)
 * object MyFeatureUiModule {
 *     @Provides @IntoMap @StringKey("my_action")
 *     fun provideMyAction(): ActionHandler = MyCustomActionHandler()
 * }
 * ```
 *
 * Note: Custom component renderers are registered imperatively on the [ComponentRegistry]
 * singleton in Application.onCreate() or a feature init class.
 */
@Module
@InstallIn(SingletonComponent::class)
object DynamicUiModule {

    // ── Core singletons ───────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideModifierRegistry(): ModifierRegistry = ModifierRegistry()

    @Provides
    @Singleton
    fun provideModifierPipeline(registry: ModifierRegistry): ModifierPipeline =
        ModifierPipeline(registry)

    @Provides
    @Singleton
    fun provideComponentRegistry(): ComponentRegistry = ComponentRegistry().also { registry ->
        registry.registerLayoutComponents()
        registry.registerMaterialComponents()
        registry.registerMediaComponents()
    }

    // ── Shared OkHttpClient for HttpHandler ────────────────────────────────────

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // ── Built-in action handlers ──────────────────────────────────────────────

    @Provides @IntoMap @StringKey("setState")
    fun provideSetState(h: SetStateHandler): ActionHandler = h

    @Provides @IntoMap @StringKey("toggleState")
    fun provideToggleState(h: ToggleStateHandler): ActionHandler = h

    @Provides @IntoMap @StringKey("navigate")
    fun provideNavigate(h: NavigateHandler): ActionHandler = h

    @Provides @IntoMap @StringKey("push")
    fun providePush(h: NavigateHandler): ActionHandler = h

    @Provides @IntoMap @StringKey("toast")
    fun provideToast(h: ToastHandler): ActionHandler = h

    @Provides @IntoMap @StringKey("back")
    fun provideBack(h: BackHandler): ActionHandler = h

    @Provides @IntoMap @StringKey("pop")
    fun providePop(h: BackHandler): ActionHandler = h

    @Provides @IntoMap @StringKey("http")
    fun provideHttp(h: HttpHandler): ActionHandler = h

    @Provides @IntoMap @StringKey("request")
    fun provideRequest(h: HttpHandler): ActionHandler = h

    @Provides @IntoMap @StringKey("copy")
    fun provideCopy(h: CopyTextHandler): ActionHandler = h

    @Provides @IntoMap @StringKey("copyText")
    fun provideCopyText(h: CopyTextHandler): ActionHandler = h

    @Provides @IntoMap @StringKey("dialog")
    fun provideDialog(h: DialogHandler): ActionHandler = h

    @Provides @IntoMap @StringKey("datePicker")
    fun provideDatePicker(h: DatePickerHandler): ActionHandler = h

    @Provides @IntoMap @StringKey("dateRangePicker")
    fun provideDateRangePicker(h: DateRangePickerHandler): ActionHandler = h

    @Provides @IntoMap @StringKey("timePicker")
    fun provideTimePicker(h: TimePickerHandler): ActionHandler = h

    @Provides @IntoMap @StringKey("timeRangePicker")
    fun provideTimeRangePicker(h: TimeRangePickerHandler): ActionHandler = h

    @Provides @IntoMap @StringKey("cityPicker")
    fun provideCityPicker(h: CityPickerHandler): ActionHandler = h

    @Provides @IntoMap @StringKey("colorPicker")
    fun provideColorPicker(h: ColorPickerHandler): ActionHandler = h

    // ── Hilt Multibinding base (required to compile even if no custom handlers added) ──

    @Provides
    fun provideActionRegistry(
        hiltHandlers: Map<String, @JvmSuppressWildcards ActionHandler>,
    ): ActionRegistry = ActionRegistry(hiltHandlers)
}
