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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.math.max

internal class PhotoFileProcessor(context: Context) {

    private val appContext = context.applicationContext
    private val sessionFiles = mutableSetOf<File>()

    private val sessionDirectory: File
        get() = File(appContext.cacheDir, "camera").apply { mkdirs() }

    fun createCaptureFile(): File {
        return File(sessionDirectory, "capture-${UUID.randomUUID()}.jpg").also(sessionFiles::add)
    }

    fun restoreSession(originalUri: Uri?, workingUri: Uri?) {
        originalUri?.toSessionFile()?.let(sessionFiles::add)
        workingUri?.toSessionFile()?.let(sessionFiles::add)
    }

    fun discard(originalUri: Uri?, workingUri: Uri?) {
        setOfNotNull(originalUri, workingUri).forEach { uri ->
            uri.toSessionFile()?.let { file ->
                file.delete()
                sessionFiles.remove(file)
            }
        }
    }

    fun discardWorkingCopy(originalUri: Uri, workingUri: Uri) {
        if (originalUri != workingUri) discard(null, workingUri)
    }

    fun close() {
        sessionFiles.forEach(File::delete)
        sessionFiles.clear()
    }

    suspend fun loadBitmap(uri: Uri): Bitmap = withContext(Dispatchers.IO) {
        decodeBitmap(uri)
    }

    suspend fun transform(
        sourceUri: Uri,
        rotationDegrees: Int = 0,
        flipHorizontal: Boolean = false
    ): Uri = withContext(Dispatchers.IO) {
        val source = decodeBitmap(sourceUri)
        val matrix = Matrix().apply {
            if (rotationDegrees != 0) postRotate(rotationDegrees.toFloat())
            if (flipHorizontal) postScale(-1f, 1f)
        }
        val transformed = Bitmap.createBitmap(
            source,
            0,
            0,
            source.width,
            source.height,
            matrix,
            true
        )
        val target = File(sessionDirectory, "edit-${UUID.randomUUID()}.jpg").also(sessionFiles::add)
        target.outputStream().buffered().use { output ->
            check(transformed.compress(Bitmap.CompressFormat.JPEG, 95, output))
        }
        Uri.fromFile(target)
    }

    suspend fun export(
        sourceUri: Uri,
        outputUri: Uri,
        aspectRatio: CameraAspectRatio,
        quality: Int
    ) = withContext(Dispatchers.IO) {
        val ratio = aspectRatio.value
        if (ratio == null) {
            appContext.contentResolver.openInputStream(sourceUri).use { input ->
                requireNotNull(input)
                appContext.contentResolver.openOutputStream(outputUri, "wt").use { output ->
                    requireNotNull(output)
                    input.copyTo(output)
                    output.flush()
                }
            }
            return@withContext
        }

        val source = decodeBitmap(sourceUri)
        val cropped = centerCrop(source, ratio)
        writeBitmap(cropped, outputUri, quality)
    }

    suspend fun export(
        bitmap: Bitmap,
        outputUri: Uri,
        quality: Int
    ) = withContext(Dispatchers.IO) {
        writeBitmap(bitmap, outputUri, quality)
    }

    private fun writeBitmap(bitmap: Bitmap, outputUri: Uri, quality: Int) {
        appContext.contentResolver.openOutputStream(outputUri, "wt").use { output ->
            requireNotNull(output)
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output))
            output.flush()
        }
    }

    private fun decodeBitmap(uri: Uri): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        appContext.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input)
            BitmapFactory.decodeStream(input, null, bounds)
        }
        check(bounds.outWidth > 0 && bounds.outHeight > 0)

        var sampleSize = 1
        while (max(bounds.outWidth, bounds.outHeight) / sampleSize > MAX_IMAGE_EDGE) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = appContext.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input)
            BitmapFactory.decodeStream(input, null, options)
        }
        requireNotNull(decoded)

        val orientation = appContext.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input)
            ExifInterface(input).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate(180f)
                matrix.postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
        }
        if (matrix.isIdentity) return decoded
        return Bitmap.createBitmap(
            decoded,
            0,
            0,
            decoded.width,
            decoded.height,
            matrix,
            true
        )
    }

    private fun centerCrop(source: Bitmap, targetRatio: Float): Bitmap {
        val sourceRatio = source.width.toFloat() / source.height.toFloat()
        val cropWidth: Int
        val cropHeight: Int
        if (sourceRatio > targetRatio) {
            cropHeight = source.height
            cropWidth = (cropHeight * targetRatio).toInt().coerceIn(1, source.width)
        } else {
            cropWidth = source.width
            cropHeight = (cropWidth / targetRatio).toInt().coerceIn(1, source.height)
        }
        return Bitmap.createBitmap(
            source,
            (source.width - cropWidth) / 2,
            (source.height - cropHeight) / 2,
            cropWidth,
            cropHeight
        )
    }

    private fun Uri.toSessionFile(): File? {
        if (scheme != "file") return null
        val file = path?.let(::File) ?: return null
        return file.takeIf { it.parentFile == sessionDirectory }
    }

    private companion object {
        const val MAX_IMAGE_EDGE = 4096
    }
}
