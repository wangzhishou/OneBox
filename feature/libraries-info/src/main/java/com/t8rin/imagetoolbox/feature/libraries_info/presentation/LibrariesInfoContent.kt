/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.feature.libraries_info.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.union
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.m3.chipColors
import com.mikepenz.aboutlibraries.ui.compose.m3.libraryColors
import com.mikepenz.aboutlibraries.ui.compose.util.htmlReadyLicenseContent
import com.mikepenz.aboutlibraries.util.withContext
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.feature.libraries_info.presentation.components.LibrariesContainer
import com.t8rin.imagetoolbox.feature.libraries_info.presentation.screenLogic.LibrariesInfoComponent
import kotlinx.collections.immutable.toPersistentList


@Composable
fun LibrariesInfoContent(
    component: LibrariesInfoComponent
) {


    BaseScreen(
        title = stringResource(id = R.string.open_source_licenses),
        onGoBack = component.onGoBack,
        supportGlassEffect = true,
    ) {
        val linkHandler = LocalUriHandler.current
        val context = LocalContext.current
        val libraries = remember {
            Libs.Builder()
                .withContext(context)
                .build().let { libs ->
                    libs.copy(
                        libraries = libs.libraries.distinctBy {
                            it.name
                        }.filter { it.licenses.isNotEmpty() }.sortedWith(
                            compareBy(
                                { !it.name.contains("T8RIN", true) },
                                { it.name }
                            ),
                        ).toPersistentList()
                    )
                }
        }

        val contentPadding = WindowInsets
            .navigationBars
            .only(WindowInsetsSides.Bottom)
            .union(WindowInsets.ime)
            .union(
                WindowInsets.displayCutout
                    .only(WindowInsetsSides.Horizontal)
            )
            .union(
                WindowInsets(
                    left = 16.dp,
                    top = 12.dp,
                    right = 16.dp,
                    bottom = 12.dp
                )
            )
            .asPaddingValues()

        LibrariesContainer(
            libraries = libraries,
            modifier = Modifier.weight(1f),
            contentPadding = contentPadding,
            dimensions = LibraryDefaults.libraryDimensions(
                itemSpacing = 8.dp
            ),
            colors = LibraryDefaults.libraryColors(
                versionChipColors = LibraryDefaults.chipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.5f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ),
            onLibraryClick = { library ->
                val license = library.licenses.firstOrNull()
                if (!license?.htmlReadyLicenseContent.isNullOrBlank()) {
                    component.selectLibrary(library)
                } else if (!license?.url.isNullOrBlank()) {
                    license.url?.also {
                        runCatching {
                            linkHandler.openUri(it)
                        }.onFailure(AppToastHost::showFailureToast)
                    }
                }
            }
        )
    }
}