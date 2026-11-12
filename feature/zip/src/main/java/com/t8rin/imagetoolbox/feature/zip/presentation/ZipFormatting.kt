/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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
