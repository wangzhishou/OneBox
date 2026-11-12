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

package com.t8rin.imagetoolbox.feature.scan_qr_code.di

import android.graphics.Bitmap
import com.wanbaohe.core.ui.utils.helper.ImageUtils.applyPadding
import com.t8rin.opencv_tools.qr_prepare.QrPrepareHelper
import io.github.g00fy2.quickie.extensions.QrProcessor
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QrScannerInitializer @Inject constructor() {
    private val initialized = AtomicBoolean(false)

    fun init() {
        if (initialized.compareAndSet(false, true)) {
            QrProcessor.setProcessor(::prepareBitmap)
        }
    }

    private fun prepareBitmap(bitmap: Bitmap): Bitmap =
        QrPrepareHelper.prepareQrForDecode(bitmap.applyPadding(100))
}
