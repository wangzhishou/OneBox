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

import androidx.compose.runtime.Stable
import com.shifenmiao.model.ui.picker.SelectedCountryData

/**
 * 平台位置选择器,按渠道 flavor 隔离实现(同名工厂函数 `rememberPlatformLocationPickerState`,
 * sourceSet 隔离,同 core/r、core/ui 的做法):
 * - 国内渠道(src/domestic): 行政区滚轮 CityPicker,选省/市/区
 * - Google 渠道(src/google): Google Places Autocomplete,全球地点搜索
 */
@Stable
interface PlatformLocationPickerState {

    fun show(
        title: String? = null,
        initData: SelectedCountryData? = null,
        initLayer: Int = 2,
        onCancel: () -> Unit = {},
        onChange: (SelectedCountryData) -> Unit = {},
    )

    fun hide()
}
