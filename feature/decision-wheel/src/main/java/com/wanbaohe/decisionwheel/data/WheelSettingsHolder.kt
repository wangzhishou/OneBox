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

package com.wanbaohe.decisionwheel.data

import com.wanbaohe.decisionwheel.component.WheelSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 转盘设置的进程内单一真相。
 *
 * 设置页改完要立刻反映到 Spin 页（旋转时长、抽后移除等），所以不能各自从 MMKV 各读各的。
 */
@Singleton
class WheelSettingsHolder @Inject constructor() {

    private val _settings = MutableStateFlow(WheelSettingsStorage.load())
    val settings: StateFlow<WheelSettings> = _settings.asStateFlow()

    fun update(settings: WheelSettings) {
        WheelSettingsStorage.save(settings)
        _settings.value = WheelSettingsStorage.load()
    }

    fun current(): WheelSettings = _settings.value
}
