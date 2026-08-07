package com.wanbaohe.visual.automation.service

import android.app.Activity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 提供当前 Activity 给 UI 自动化使用。
 *
 * 由使用方(通常是 Chat 组件所在的 Activity)在生命周期内注入当前实例;
 * AgentTool 调用 [VisualAutomationService] 时,通过该 Provider 获取 Activity,
 * 严禁在 Tool 中直接 `LocalContext.current as Activity`。
 */
@Singleton
class CurrentActivityProvider @Inject constructor() {

    @Volatile
    private var currentActivity: Activity? = null

    fun setCurrentActivity(activity: Activity?) {
        currentActivity = activity
    }

    fun getCurrentActivity(): Activity? = currentActivity

    fun requireActivity(): Activity =
        currentActivity ?: error(
            "CurrentActivityProvider has no Activity. " +
                "Make sure the host Activity is registered via setCurrentActivity()."
        )
}