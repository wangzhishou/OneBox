package com.shifenmiao.base.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.shifenmiao.base.ui.painter.TextPainter
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.StrapiImage
import java.io.ByteArrayOutputStream

object ImageUtils {

    fun resourceToBitmap(resourceId: Int): Bitmap {
        val context = AppContext.getContext()
        val drawable: Drawable? = ContextCompat.getDrawable(context, resourceId)

        return if (drawable != null) {
            // Convert Drawable to Bitmap
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } else {
            // Fallback to decodeResource for image resources
            BitmapFactory.decodeResource(context.resources, resourceId)
        }
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        ByteArrayOutputStream().use { outputStream ->
            val compressed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val bytes = outputStream.toByteArray()
            if (!compressed || bytes.isEmpty()) {
                throw IllegalStateException("Bitmap compress failed or produced 0 bytes (w=${bitmap.width}, h=${bitmap.height}, recycled=${bitmap.isRecycled})")
            }
            return bytes
        }
    }

    fun getImageThumbnailPath(strapiImage: StrapiImage?): String {
        if (strapiImage == null) {
            return ""
        }
        return if (strapiImage.formats?.small?.url?.isNotEmpty() == true) {
            strapiImage.formats?.small?.url!!
        } else if (strapiImage.formats?.thumbnail?.url?.isNotEmpty() == true) {
            strapiImage.formats?.thumbnail?.url!!
        } else {
            strapiImage.url
        }
    }

    @Composable
    fun getDefaultTextImage(avatarTextString: String): TextPainter {
        val avatarText = TextPainter(
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            textMeasurer = rememberTextMeasurer(),
            text = avatarTextString,
            textColor = MaterialTheme.colorScheme.onTertiaryContainer,
            size = Size(48.dp.value, 48.dp.value)
        )
        return avatarText
    }
}