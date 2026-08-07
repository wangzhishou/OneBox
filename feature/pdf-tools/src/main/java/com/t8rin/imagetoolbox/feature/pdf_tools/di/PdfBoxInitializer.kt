package com.t8rin.imagetoolbox.feature.pdf_tools.di

import android.content.Context
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfBoxInitializer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val initialized = AtomicBoolean(false)

    fun init() {
        if (initialized.compareAndSet(false, true)) {
            PDFBoxResourceLoader.init(context)
        }
    }
}
