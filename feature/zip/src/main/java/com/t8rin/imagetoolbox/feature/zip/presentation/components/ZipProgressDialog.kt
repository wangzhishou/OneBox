/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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

package com.t8rin.imagetoolbox.feature.zip.presentation.components

import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.wanbaohe.core.ui.widget.dialogs.LoadingDialog
import com.wanbaohe.core.ui.widget.text.AutoSizeText
import com.t8rin.imagetoolbox.feature.zip.R
import com.t8rin.imagetoolbox.feature.zip.domain.ZipProgress
import kotlin.math.roundToInt

/**
 * 压缩/解压进度弹窗：优先显示字节百分比，大文件时进度也能连续走动；
 * 拿不到总量时退回普通转圈。
 */
@Composable
internal fun ZipProgressDialog(
    visible: Boolean,
    progress: ZipProgress,
    onCancelLoading: () -> Unit
) {
    if (progress.totalBytes <= 0L && progress.totalFiles <= 0) {
        LoadingDialog(
            visible = visible,
            onCancelLoading = onCancelLoading
        )
        return
    }

    LoadingDialog(
        visible = visible,
        progress = { progress.fraction },
        onCancelLoading = onCancelLoading,
        additionalContent = { loaderSize ->
            AutoSizeText(
                text = stringResource(
                    R.string.zip_progress_percent,
                    (progress.fraction * 100).roundToInt()
                ),
                modifier = Modifier.width(loaderSize * 0.8f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    )
}
