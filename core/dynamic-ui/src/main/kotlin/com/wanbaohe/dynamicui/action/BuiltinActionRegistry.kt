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

package com.wanbaohe.dynamicui.action

import com.wanbaohe.dynamicui.action.handlers.BackHandler
import com.wanbaohe.dynamicui.action.handlers.CopyTextHandler
import com.wanbaohe.dynamicui.action.handlers.DialogHandler
import com.wanbaohe.dynamicui.action.handlers.HttpHandler
import com.wanbaohe.dynamicui.action.handlers.HostActionHandler
import com.wanbaohe.dynamicui.action.handlers.NavigateHandler
import com.wanbaohe.dynamicui.action.handlers.DatePickerHandler
import com.wanbaohe.dynamicui.action.handlers.DateRangePickerHandler
import com.wanbaohe.dynamicui.action.handlers.TimePickerHandler
import com.wanbaohe.dynamicui.action.handlers.CityPickerHandler
import com.wanbaohe.dynamicui.action.handlers.ColorPickerHandler
import com.wanbaohe.dynamicui.action.handlers.SetStateHandler
import com.wanbaohe.dynamicui.action.handlers.ToggleStateHandler
import com.wanbaohe.dynamicui.action.handlers.ToastHandler
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Creates an [ActionRegistry] pre-loaded with every built-in [ActionHandler].
 *
 * Used by [com.wanbaohe.dynamicui.DynamicUiEnv.defaultEngine] so that actions
 * (toast, setState, navigate, http, …) work out-of-the-box without Hilt.
 */
fun createBuiltinActionRegistry(): ActionRegistry = ActionRegistry().apply {
    val sharedClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    register(SetStateHandler())
    register(ToggleStateHandler())
    register(NavigateHandler())
    register(HostActionHandler())
    register(BackHandler())
    register(ToastHandler())
    register(HttpHandler(sharedClient))
    register(CopyTextHandler())
    register(DialogHandler())
    register(DatePickerHandler())
    register(DateRangePickerHandler())
    register(TimePickerHandler())
    register(CityPickerHandler())
    register(ColorPickerHandler())
}
