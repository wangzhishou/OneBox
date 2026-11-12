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

package com.wanbaohe.camera

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.core.settings.presentation.model.toUiState
import com.wanbaohe.core.ui.utils.provider.ImageToolboxCompositionLocals
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CameraCaptureActivity : ComponentActivity() {

    @Inject
    lateinit var settingsManager: SettingsManager

    private lateinit var processor: PhotoFileProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val request = CameraCaptureContract.requestFrom(intent)
        if (request == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        processor = PhotoFileProcessor(this)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            val settingsState by settingsManager.settingsState.collectAsState()
            ImageToolboxCompositionLocals(
                settingsState = settingsState.toUiState(),
                settingsManager = settingsManager
            ) {
                CameraCaptureFlow(
                    request = request,
                    processor = processor,
                    onCancel = {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    },
                    onComplete = ::completeCapture
                )
            }
        }
    }

    override fun onDestroy() {
        if (isFinishing && ::processor.isInitialized) processor.close()
        super.onDestroy()
    }

    private fun completeCapture(outputUri: Uri) {
        setResult(
            Activity.RESULT_OK,
            CameraCaptureContract.resultIntent(outputUri)
        )
        finish()
    }
}
