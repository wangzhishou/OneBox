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