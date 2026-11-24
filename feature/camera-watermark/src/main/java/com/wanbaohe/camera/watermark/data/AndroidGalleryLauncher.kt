/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.camera.watermark.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import com.wanbaohe.camera.watermark.domain.GalleryLauncher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidGalleryLauncher @Inject constructor(
    @ApplicationContext private val context: Context,
) : GalleryLauncher {
    override fun open(uri: Uri?): Boolean = runCatching {
        val intent = if (uri == null) {
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        } else {
            val mimeType = context.contentResolver.getType(uri) ?: "image/*"
            Intent(Intent.ACTION_VIEW).setDataAndType(uri, mimeType).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (uri == null) {
            context.startActivity(intent)
        } else {
            val chooser = Intent.createChooser(intent, null).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putParcelableArrayListExtra(
                    Intent.EXTRA_EXCLUDE_COMPONENTS,
                    arrayListOf(
                        ComponentName("com.shifenmiao.app", "com.shifenmiao.app.AppActivity"),
                        ComponentName("com.shifenmiao.app.debug", "com.shifenmiao.app.AppActivity"),
                    ),
                )
            }
            context.startActivity(chooser)
        }
        true
    }.getOrDefault(false)
}
