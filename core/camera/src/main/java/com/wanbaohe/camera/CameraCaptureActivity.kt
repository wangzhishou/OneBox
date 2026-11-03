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
