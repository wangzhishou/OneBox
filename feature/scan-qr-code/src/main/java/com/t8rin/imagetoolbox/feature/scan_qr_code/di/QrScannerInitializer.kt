package com.t8rin.imagetoolbox.feature.scan_qr_code.di

import android.graphics.Bitmap
import com.t8rin.imagetoolbox.core.ui.utils.helper.ImageUtils.applyPadding
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
