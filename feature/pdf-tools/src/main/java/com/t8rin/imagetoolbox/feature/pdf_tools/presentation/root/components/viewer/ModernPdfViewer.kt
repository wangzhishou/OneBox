/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.root.components.viewer

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.compose.AndroidFragment
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedLoadingIndicator
import com.t8rin.imagetoolbox.core.ui.widget.modifier.animateContentSizeNoClip
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@RequiresApi(Build.VERSION_CODES.P)
@Composable
internal fun ModernPdfViewer(
    uri: Uri,
    isSearching: Boolean,
    modifier: Modifier = Modifier,
    onError: () -> Unit = {}
) {
    var fragmentReference by remember {
        mutableStateOf<ModernPdfViewerDelegate?>(null)
    }
    var didRequestFallback by remember(uri) {
        mutableStateOf(false)
    }
    val isLoading = fragmentReference?.isLoading?.collectAsState()?.value ?: true
    val colorScheme = MaterialTheme.colorScheme
    val fragmentArguments = remember(uri) {
        Bundle().apply {
            putParcelable("documentUri", uri)
        }
    }

    LaunchedEffect(fragmentReference, uri) {
        fragmentReference?.loadErrors?.collect { _ ->
            if (!didRequestFallback) {
                didRequestFallback = true
                onError()
            }
        }
    }

    LaunchedEffect(fragmentReference, uri, isLoading) {
        if (fragmentReference == null || didRequestFallback || !isLoading) return@LaunchedEffect

        delay(5.seconds)
        if (fragmentReference?.isLoading?.value == true && !didRequestFallback) {
            didRequestFallback = true
            onError()
        }
    }

    LaunchedEffect(colorScheme, fragmentReference) {
        fragmentReference?.setScheme(colorScheme)
    }

    LaunchedEffect(fragmentReference, isSearching) {
        fragmentReference?.isTextSearchActive = isSearching
    }

    key(uri) {
        AndroidFragment<ModernPdfViewerDelegate>(
            arguments = fragmentArguments,
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            onUpdate = {
                fragmentReference = it
            }
        )
    }

    if (isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .animateContentSizeNoClip(),
            contentAlignment = Alignment.Center
        ) {
            EnhancedLoadingIndicator()
        }
    }
}