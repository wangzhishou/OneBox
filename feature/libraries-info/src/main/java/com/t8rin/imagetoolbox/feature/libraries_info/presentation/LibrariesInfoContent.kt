/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file has been modified from the original ImageToolbox source.
 *
 * Origin: ImageToolbox derivative, modified
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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.LibraryColors
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.m3.chipColors
import com.mikepenz.aboutlibraries.ui.compose.m3.libraryColors
import com.mikepenz.aboutlibraries.ui.compose.util.htmlReadyLicenseContent
import com.mikepenz.aboutlibraries.util.withContext
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.feature.libraries_info.presentation.components.LibrariesContainer
import com.t8rin.imagetoolbox.feature.libraries_info.presentation.screenLogic.LibrariesInfoComponent
import com.wanbaohe.core.ui.widget.glass.glassMedium
import kotlinx.collections.immutable.toPersistentList

private const val UPSTREAM_PROJECT_URL = "https://github.com/T8RIN/ImageToolbox"


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

        val libraryColors = LibraryDefaults.libraryColors(
            versionChipColors = LibraryDefaults.chipColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.5f),
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        )

        LibrariesContainer(
            libraries = libraries,
            modifier = Modifier.weight(1f),
            contentPadding = contentPadding,
            dimensions = LibraryDefaults.libraryDimensions(
                itemSpacing = 8.dp
            ),
            colors = libraryColors,
            header = {
                item {
                    UpstreamCreditItem(
                        colors = libraryColors,
                        onClick = {
                            runCatching {
                                linkHandler.openUri(UPSTREAM_PROJECT_URL)
                            }.onFailure(AppToastHost::showFailureToast)
                        }
                    )
                }
            },
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

@Composable
private fun UpstreamCreditItem(
    colors: LibraryColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassMedium(color = MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.upstream_credit_title),
                style = MaterialTheme.typography.titleLarge,
                color = colors.libraryContentColor,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.upstream_credit_license),
                style = MaterialTheme.typography.labelMedium,
                color = colors.libraryContentColor
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.upstream_credit_description),
            style = MaterialTheme.typography.bodySmall,
            color = colors.libraryContentColor
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.upstream_credit_author),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.libraryContentColor,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
    }
}