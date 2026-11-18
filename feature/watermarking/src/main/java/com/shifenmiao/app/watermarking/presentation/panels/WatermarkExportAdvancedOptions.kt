/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shifenmiao.app.watermarking.presentation.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.shifenmiao.app.watermarking.domain.HiddenWatermark
import com.shifenmiao.app.watermarking.domain.model.HiddenWatermarkParams
import com.shifenmiao.app.watermarking.presentation.components.HiddenWatermarkInfo
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.core.ui.widget.export.ImageExportSection
import com.wanbaohe.core.ui.widget.preferences.PreferenceRowSwitch
import androidx.compose.ui.res.stringResource

@Composable
fun WatermarkExportAdvancedOptions(
    currentHiddenWatermark: HiddenWatermark?,
    hiddenWatermark: HiddenWatermarkParams,
    supportsHiddenWatermark: Boolean,
    onHiddenWatermarkChange: (HiddenWatermarkParams) -> Unit,
    onHiddenRejected: () -> Unit,
) {
    Column {
        HiddenWatermarkInfo(currentHiddenWatermark)
        ImageExportSection(title = null) {
            PreferenceRowSwitch(
                title = stringResource(R.string.watermark_hidden_title),
                subtitle = stringResource(R.string.watermark_hidden_subtitle),
                checked = hiddenWatermark.enabled,
                onClick = { enabled ->
                    if (enabled && !supportsHiddenWatermark) {
                        onHiddenRejected()
                    } else {
                        onHiddenWatermarkChange(hiddenWatermark.copy(enabled = enabled))
                    }
                },
                drawContainer = false,
            )
            if (hiddenWatermark.enabled) {
                PreferenceRowSwitch(
                    title = stringResource(R.string.watermark_lsb_title),
                    subtitle = stringResource(R.string.watermark_lsb_subtitle),
                    checked = hiddenWatermark.isLSB,
                    onClick = { isLSB ->
                        onHiddenWatermarkChange(hiddenWatermark.copy(isLSB = isLSB))
                    },
                    drawContainer = false,
                )
            }
        }
    }
}
