package com.wanbaohe.camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

enum class CameraAspectRatio {
    ORIGINAL,
    FREE,
    RATIO_4_3,
    RATIO_1_1,
    RATIO_16_9,
    RATIO_9_16;

    val value: Float?
        get() = when (this) {
            ORIGINAL, FREE -> null
            RATIO_4_3 -> 4f / 3f
            RATIO_1_1 -> 1f
            RATIO_16_9 -> 16f / 9f
            RATIO_9_16 -> 9f / 16f
        }
}

data class CameraCaptureRequest(
    val outputUri: Uri,
    val initialAspectRatio: CameraAspectRatio = CameraAspectRatio.ORIGINAL,
    val editingEnabled: Boolean = true,
    val jpegQuality: Int = 95
) {
    init {
        require(jpegQuality in 1..100)
    }
}

class CameraCaptureContract : ActivityResultContract<CameraCaptureRequest, Uri?>() {

    override fun createIntent(context: Context, input: CameraCaptureRequest): Intent {
        return Intent(ACTION_CAPTURE).setPackage(context.packageName).apply {
            putExtra(EXTRA_OUTPUT_URI, input.outputUri.toString())
            putExtra(EXTRA_ASPECT_RATIO, input.initialAspectRatio.name)
            putExtra(EXTRA_EDITING_ENABLED, input.editingEnabled)
            putExtra(EXTRA_JPEG_QUALITY, input.jpegQuality)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getStringExtra(EXTRA_RESULT_URI)?.let(Uri::parse)
    }

    companion object {
        private const val ACTION_CAPTURE = "com.wanbaohe.camera.action.CAPTURE"
        private const val EXTRA_OUTPUT_URI = "camera_output_uri"
        private const val EXTRA_ASPECT_RATIO = "camera_aspect_ratio"
        private const val EXTRA_EDITING_ENABLED = "camera_editing_enabled"
        private const val EXTRA_JPEG_QUALITY = "camera_jpeg_quality"
        private const val EXTRA_RESULT_URI = "camera_result_uri"

        fun requestFrom(intent: Intent): CameraCaptureRequest? {
            val outputUri = intent.getStringExtra(EXTRA_OUTPUT_URI)?.let(Uri::parse) ?: return null
            val ratio = intent.getStringExtra(EXTRA_ASPECT_RATIO)
                ?.let { value -> runCatching { CameraAspectRatio.valueOf(value) }.getOrNull() }
                ?: CameraAspectRatio.ORIGINAL
            return CameraCaptureRequest(
                outputUri = outputUri,
                initialAspectRatio = ratio,
                editingEnabled = intent.getBooleanExtra(EXTRA_EDITING_ENABLED, true),
                jpegQuality = intent.getIntExtra(EXTRA_JPEG_QUALITY, 95).coerceIn(1, 100)
            )
        }

        fun resultIntent(outputUri: Uri): Intent {
            return Intent().putExtra(EXTRA_RESULT_URI, outputUri.toString())
        }
    }
}
