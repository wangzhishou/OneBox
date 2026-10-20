package com.t8rin.imagetoolbox.feature.zip.presentation

import android.content.Context
import android.text.format.Formatter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.t8rin.imagetoolbox.feature.zip.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun rememberFormattedSize(size: Long): String {
    val context = LocalContext.current
    return remember(context, size) { formatZipSize(context, size) }
}

internal fun formatZipSize(context: Context, size: Long): String =
    if (size < 0) {
        context.getString(R.string.zip_unknown_size)
    } else {
        Formatter.formatFileSize(context, size)
    }

internal fun formatZipTimestamp(timestamp: Long): String =
    if (timestamp <= 0L) {
        ""
    } else {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
