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

package com.wanbaohe.a2ui.catalog.builtin.input

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.shifenmiao.model.ui.picker.SelectedCountryData

/**
 * foss(F-Droid)实现:与 Google 渠道同一逻辑——位置输入以可编辑 TextField 为主,
 * 尾部图标唤起的平台选择器在海外不可用(foss 无 GMS/Places),
 * 因此 show() 直接 onCancel,行为等同 Google 渠道 API key 缺失时的降级:
 * 图标为空操作,用户手输地点。不再向海外用户展示中国行政区 CityPicker。
 */
private class FossLocationPickerState : PlatformLocationPickerState {

    override fun show(
        title: String?,
        initData: SelectedCountryData?,
        initLayer: Int,
        onCancel: () -> Unit,
        onChange: (SelectedCountryData) -> Unit,
    ) = onCancel()

    override fun hide() = Unit
}

@Composable
fun rememberPlatformLocationPickerState(): PlatformLocationPickerState {
    return remember { FossLocationPickerState() }
}
