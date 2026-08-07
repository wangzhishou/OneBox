package com.halilibo.richtext.markdown

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageScope
import coil3.request.ImageRequest
import coil3.size.Size

private val DEFAULT_IMAGE_SIZE = 64.dp

/**
 * Implementation of RemoteImage by using Coil library for Android.
 */
@Composable
internal fun RemoteImage(
  url: String,
  contentDescription: String?,
  modifier: Modifier,
  contentScale: ContentScale,
  onClick: (() -> Unit)? = null,
  onLongClick: (() -> Unit)? = null,
  loading: (@Composable SubcomposeAsyncImageScope.(AsyncImagePainter.State.Loading) -> Unit)? = null,
  error: (@Composable SubcomposeAsyncImageScope.(AsyncImagePainter.State.Error) -> Unit)? = null
) {
  val context = LocalContext.current
  val request = remember(url, context) {
    ImageRequest.Builder(context)
      .data(data = url)
      // Let Coil decide size with constraints; ORIGINAL can trigger huge decode.
      .build()
  }

  val density = LocalDensity.current

  // Track the final measured size so inline placeholders (if any) can be stable.
  var lastMeasuredSize = remember(url) { IntSize.Zero }

  BoxWithConstraints(
    modifier = modifier.then(
      if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(
          onClick = { onClick?.invoke() },
          onLongClick = { onLongClick?.invoke() }
        )
      } else {
        Modifier
      }
    ),
    contentAlignment = Alignment.Center
  ) {
    val sizeModifier by remember(density, constraints.maxWidth, constraints.maxHeight) {
      derivedStateOf {
        // Give a placeholder size only when width is unknown/zero; otherwise fill available width.
        if (constraints.maxWidth == 0) {
          Modifier.size(DEFAULT_IMAGE_SIZE)
        } else {
          Modifier.fillMaxWidth()
        }
      }
    }

    SubcomposeAsyncImage(
      model = request,
      contentDescription = contentDescription,
      contentScale = contentScale,
      modifier = sizeModifier.onSizeChanged {
        lastMeasuredSize = it
      },
      loading = loading,
      error = error
    )
  }
}
