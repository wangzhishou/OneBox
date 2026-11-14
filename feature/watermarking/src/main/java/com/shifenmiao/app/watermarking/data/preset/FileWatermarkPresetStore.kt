/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.data.preset

import android.content.Context
import android.net.Uri
import android.util.AtomicFile
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.preset.WatermarkPreset
import com.shifenmiao.app.watermarking.domain.preset.WatermarkPresetStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileWatermarkPresetStore @Inject constructor(
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
) : WatermarkPresetStore, DispatchersHolder by dispatchersHolder {
    private val mutex = Mutex()

    override suspend fun listCustomPresets(): List<WatermarkPreset> = withContext(ioDispatcher) {
        mutex.withLock {
            presetDirectory().listFiles().orEmpty().filter { it.extension == "json" }
                .mapNotNull { file ->
                    runCatching {
                        AtomicFile(file).openRead().use { input ->
                            WatermarkPresetCodec.decode(input.readBytes().toString(Charsets.UTF_8))
                        }
                    }.getOrNull()
                }.sortedByDescending(WatermarkPreset::updatedAt)
        }
    }

    override suspend fun savePreset(preset: WatermarkPreset): WatermarkPreset = withContext(ioDispatcher) {
        mutex.withLock {
            require(preset.id.matches(Regex("[A-Za-z0-9_-]{1,80}"))) { "Invalid preset ID" }
            val durable = materializeAssets(preset).copy(
                isBuiltin = false,
                nameRes = 0,
                updatedAt = System.currentTimeMillis(),
            )
            val file = File(presetDirectory(), "${durable.id}.json")
            val atomic = AtomicFile(file)
            var output: FileOutputStream? = null
            try {
                val stream = atomic.startWrite().also { output = it }
                stream.write(WatermarkPresetCodec.encode(durable).toByteArray(Charsets.UTF_8))
                atomic.finishWrite(stream)
                output = null
            } catch (error: Throwable) {
                output?.let(atomic::failWrite)
                throw error
            }
            durable
        }
    }

    override suspend fun deletePreset(id: String) = withContext(ioDispatcher) {
        mutex.withLock {
            AtomicFile(File(presetDirectory(), "$id.json")).delete()
            assetDirectory().listFiles().orEmpty().filter { it.name.startsWith("$id-") }
                .forEach(File::delete)
        }
    }

    private fun materializeAssets(preset: WatermarkPreset): WatermarkPreset {
        val elements = preset.document.elements.map { element ->
            val content = element.content
            if (content !is WatermarkContent.Image || !content.imageData.startsWith("content://")) {
                return@map element
            }
            val target = File(assetDirectory(), "${preset.id}-${element.id}.img")
            context.contentResolver.openInputStream(Uri.parse(content.imageData))?.use { input ->
                target.outputStream().use(input::copyTo)
            } ?: return@map element
            element.copy(content = content.copy(imageData = Uri.fromFile(target).toString()))
        }
        return preset.copy(document = preset.document.copy(elements = elements))
    }

    private fun presetDirectory(): File = File(context.filesDir, "image-watermark/presets").apply {
        if (!isDirectory) mkdirs()
    }

    private fun assetDirectory(): File = File(context.filesDir, "image-watermark/assets").apply {
        if (!isDirectory) mkdirs()
    }
}
