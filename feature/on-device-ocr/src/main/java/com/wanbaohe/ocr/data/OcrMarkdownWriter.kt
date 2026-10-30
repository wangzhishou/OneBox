package com.wanbaohe.ocr.data

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class OcrMarkdownWriter @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun write(uri: Uri, content: String) {
        val output = context.contentResolver.openOutputStream(uri, "wt")
            ?: error("Could not open Markdown destination")
        output.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(content)
        }
    }
}
