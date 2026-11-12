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
import com.wanbaohe.base.ui.picker.CityPickerState
import com.wanbaohe.base.ui.picker.rememberCityPickerState
import com.shifenmiao.model.ui.picker.SelectedCountryData

private class DomesticLocationPickerState(
    private val delegate: CityPickerState,
) : PlatformLocationPickerState {

    override fun show(
        title: String?,
        initData: SelectedCountryData?,
        initLayer: Int,
        onCancel: () -> Unit,
        onChange: (SelectedCountryData) -> Unit,
    ) {
        delegate.show(
            title = title,
            initData = initData,
            initLayer = initLayer,
            onCancel = onCancel,
            onChange = onChange,
        )
    }

    override fun hide() = delegate.hide()
}

@Composable
fun rememberPlatformLocationPickerState(): PlatformLocationPickerState {
    val cityPicker = rememberCityPickerState()
    return remember(cityPicker) { DomesticLocationPickerState(cityPicker) }
}
