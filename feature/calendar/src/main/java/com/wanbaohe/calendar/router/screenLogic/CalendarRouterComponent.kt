package com.wanbaohe.calendar.router.screenLogic

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.calendar.bazi.screenLogic.BaZiComponent
import com.wanbaohe.calendar.calendar_view.screenLogic.CalendarViewComponent
import com.wanbaohe.calendar.auspicious.screenLogic.AuspiciousComponent
import com.wanbaohe.calendar.convert.screenLogic.ConvertComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 万年历路由组件
 *
 * 管理 4 个子页面的内部状态切换（日历 / 八字 / 择日 / 转换）。
 * Tab 切换时更新内部 [currentType]，不走外部路由，配合 [CalendarRouterScreen]
 * 的 AnimatedContent 实现平滑过渡动画。
 */
class CalendarRouterComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted initialType: Screen.Calendar.Type?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val calendarViewFactory: CalendarViewComponent.Factory,
    private val baZiFactory: BaZiComponent.Factory,
    private val auspiciousFactory: AuspiciousComponent.Factory,
    private val convertFactory: ConvertComponent.Factory,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _currentType = MutableStateFlow(
        initialType ?: Screen.Calendar.Type.CalendarView()
    )
    val currentType = _currentType.asStateFlow()

    /** 切换当前子页面（内部状态，不走外部路由） */
    fun switchTo(type: Screen.Calendar.Type) {
        if (type::class != _currentType.value::class) {
            _currentType.value = type
        }
    }

    // ─── 子组件懒加载 ────────────────────────────────────────────────

    private val calendarViewCtx = componentContext.childContext("calendar_view")
    private val baZiCtx = componentContext.childContext("bazi")
    private val auspiciousCtx = componentContext.childContext("auspicious")
    private val convertCtx = componentContext.childContext("convert")

    private val calendarViewInitial = when (val t = initialType) {
        is Screen.Calendar.Type.CalendarView -> t
        else -> Screen.Calendar.Type.CalendarView()
    }
    private val baZiInitial = when (val t = initialType) {
        is Screen.Calendar.Type.BaZi -> t
        else -> Screen.Calendar.Type.BaZi()
    }
    private val auspiciousInitial = when (val t = initialType) {
        is Screen.Calendar.Type.Auspicious -> t
        else -> Screen.Calendar.Type.Auspicious()
    }
    private val convertInitial = when (val t = initialType) {
        is Screen.Calendar.Type.Convert -> t
        else -> Screen.Calendar.Type.Convert()
    }

    val calendarViewComponent: CalendarViewComponent by lazy {
        calendarViewFactory(
            componentContext = calendarViewCtx,
            initialYear = calendarViewInitial.year,
            initialMonth = calendarViewInitial.month,
            initialDay = calendarViewInitial.day,
            onGoBack = onGoBack,
            onNavigate = onNavigate,
        )
    }

    val baZiComponent: BaZiComponent by lazy {
        baZiFactory(
            componentContext = baZiCtx,
            initialYear = baZiInitial.year,
            initialMonth = baZiInitial.month,
            initialDay = baZiInitial.day,
            initialHour = baZiInitial.hour,
            onGoBack = onGoBack,
            onNavigate = onNavigate,
        )
    }

    val auspiciousComponent: AuspiciousComponent by lazy {
        auspiciousFactory(
            componentContext = auspiciousCtx,
            isAvoidMode = auspiciousInitial.isAvoidMode,
            onGoBack = onGoBack,
            onNavigate = onNavigate,
        )
    }

    val convertComponent: ConvertComponent by lazy {
        convertFactory(
            componentContext = convertCtx,
            isSolarToLunar = convertInitial.isSolarToLunar,
            onGoBack = onGoBack,
            onNavigate = onNavigate,
        )
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialType: Screen.Calendar.Type?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): CalendarRouterComponent
    }
}
