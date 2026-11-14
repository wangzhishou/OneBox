/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.data.export

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.Metadata
import com.t8rin.imagetoolbox.core.domain.image.get
import com.t8rin.imagetoolbox.core.domain.image.model.MetadataTag
import com.shifenmiao.app.watermarking.domain.export.WatermarkMetadataWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AndroidWatermarkMetadataWriter @Inject constructor(
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
) : WatermarkMetadataWriter, DispatchersHolder by dispatchersHolder {
    override suspend fun writeMetadata(outputUri: String, metadata: Metadata): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching {
                context.contentResolver.openFileDescriptor(Uri.parse(outputUri), "rw")?.use { descriptor ->
                    val exif = ExifInterface(descriptor.fileDescriptor)
                    MetadataTag.entries.forEach { tag ->
                        metadata[tag]?.let { value -> exif.setAttribute(tag.key, value) }
                    }
                    exif.saveAttributes()
                } ?: error("Cannot open exported image metadata")
            }
        }
}
