package com.t8rin.imagetoolbox.core.data.image

import android.graphics.Bitmap
import android.os.Build
import com.t8rin.gif_converter.GifEncoder
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.GifAnimationEncoder
import com.t8rin.imagetoolbox.core.domain.image.GifAnimationFrame
import com.t8rin.imagetoolbox.core.domain.model.IntegerSize
import com.t8rin.imagetoolbox.core.domain.saving.io.Writeable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import javax.inject.Inject

internal class AndroidGifAnimationEncoder @Inject constructor(
    private val dispatchers: DispatchersHolder,
) : GifAnimationEncoder<Bitmap> {
    override suspend fun encode(
        size: IntegerSize,
        frameCount: Int,
        output: Writeable,
        frameProvider: suspend (index: Int) -> GifAnimationFrame<Bitmap>,
        onProgress: suspend (completedFrames: Int) -> Unit,
    ) = withContext(dispatchers.defaultDispatcher) {
        require(size.width in 1..65535 && size.height in 1..65535)
        require(frameCount > 0)
        currentCoroutineContext().ensureActive()
        val stream = GifOutputStream(output)
        val encoder = GifEncoder().apply {
            setSize(size.width, size.height)
            setRepeat(0)
            setDispose(1)
            setQuality(10)
        }
        if (!encoder.start(stream)) throw stream.failure ?: IOException("Cannot start the GIF encoder")
        repeat(frameCount) { index ->
            currentCoroutineContext().ensureActive()
            val frame = frameProvider(index)
            try {
                require(!frame.image.isRecycled)
                require(frame.image.width == size.width && frame.image.height == size.height)
                require(!frame.image.hasAlpha()) { "GIF replay frames must be opaque" }
                require(Build.VERSION.SDK_INT < 26 || frame.image.config != Bitmap.Config.HARDWARE) {
                    "GIF replay frames must be software bitmaps"
                }
                require(frame.durationMillis in 10..655350 && frame.durationMillis % 10 == 0)
                currentCoroutineContext().ensureActive()
                encoder.setDelay(frame.durationMillis)
                if (!encoder.addFrame(frame.image)) {
                    throw stream.failure ?: IOException("Cannot encode GIF frame ${index + 1}")
                }
            } finally {
                frame.image.recycle()
            }
            currentCoroutineContext().ensureActive()
            onProgress(index + 1)
        }
        currentCoroutineContext().ensureActive()
        if (!encoder.finish()) throw stream.failure ?: IOException("Cannot finish the GIF")
        stream.flush()
    }
}

// The backend writes individual bytes; batch them without retaining the complete animation.
private class GifOutputStream(private val output: Writeable) : OutputStream() {
    private val buffer = ByteArray(64 * 1024)
    private var count = 0
    var failure: IOException? = null
        private set

    override fun write(value: Int) {
        if (count == buffer.size) flush()
        buffer[count++] = value.toByte()
    }

    override fun write(bytes: ByteArray, offset: Int, length: Int) {
        require(offset >= 0 && length >= 0 && offset <= bytes.size - length)
        var remaining = length
        var position = offset
        while (remaining > 0) {
            if (count == buffer.size) flush()
            val next = minOf(remaining, buffer.size - count)
            bytes.copyInto(buffer, count, position, position + next)
            count += next
            position += next
            remaining -= next
        }
    }

    override fun flush() {
        failure?.let { throw it }
        if (count == 0) return
        try {
            output.writeBytes(buffer.copyOf(count))
            count = 0
        } catch (exception: IOException) {
            failure = exception
            throw exception
        }
    }
}
