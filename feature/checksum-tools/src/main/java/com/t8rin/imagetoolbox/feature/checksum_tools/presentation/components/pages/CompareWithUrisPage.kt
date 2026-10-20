/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.feature.checksum_tools.presentation.components.pages

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.core.ui.utils.content_pickers.FileType
import com.wanbaohe.core.ui.utils.content_pickers.rememberFilePicker
import com.wanbaohe.core.ui.utils.content_pickers.rememberFolderPicker
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.widget.buttons.PagerScrollPanel
import com.wanbaohe.core.ui.widget.enhanced.EnhancedLoadingIndicator
import com.wanbaohe.core.ui.widget.enhanced.hapticsClickable
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.modifier.container
import com.wanbaohe.core.ui.widget.modifier.fadingEdges
import com.wanbaohe.core.ui.widget.modifier.negativePadding
import com.wanbaohe.core.ui.widget.other.InfoContainer
import com.t8rin.imagetoolbox.feature.checksum_tools.presentation.components.ChecksumEnterField
import com.t8rin.imagetoolbox.feature.checksum_tools.presentation.components.ChecksumResultCard
import com.t8rin.imagetoolbox.feature.checksum_tools.presentation.components.UriWithHashItem
import com.t8rin.imagetoolbox.feature.checksum_tools.presentation.screenLogic.ChecksumToolsComponent
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.FolderOpened

@Composable
internal fun ColumnScope.CompareWithUrisPage(
    component: ChecksumToolsComponent
) {
    val onCopyText: (String) -> Unit = Clipboard::copy

    var previousFolder by rememberSaveable {
        mutableStateOf<Uri?>(null)
    }

    val isFilesLoading = component.filesLoadingProgress >= 0

    val openDirectoryLauncher = rememberFolderPicker(
        onSuccess = { uri ->
            previousFolder = uri
            component.setDataForBatchComparisonFromTree(uri)
        }
    )

    val filePicker = rememberFilePicker(
        type = FileType.Multiple,
        onSuccess = component::setDataForBatchComparison
    )

    val page = component.compareWithUrisPage

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(IntrinsicSize.Max)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .container(
                    shape = ShapeDefaults.start,
                    resultPadding = 0.dp
                )
                .hapticsClickable(onClick = filePicker::pickFile)
                .padding(vertical = 16.dp)
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.pick_files),
                style = MaterialTheme.typography.titleSmall
            )
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .container(
                    shape = ShapeDefaults.end,
                    resultPadding = 0.dp
                )
                .hapticsClickable(
                    onClick = {
                        openDirectoryLauncher.pickFolder(previousFolder)
                    }
                )
                .padding(vertical = 16.dp)
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.FolderOpened,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.pick_directory),
                style = MaterialTheme.typography.titleSmall
            )
        }
    }

    val nestedPagerState = rememberPagerState { page.uris.size }

    AnimatedContent(
        targetState = page.uris.isNotEmpty() to isFilesLoading,
        modifier = Modifier.padding(vertical = 4.dp)
    ) { (isNotEmpty, isLoading) ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                EnhancedLoadingIndicator(
                    progress = component.filesLoadingProgress
                )
            }
        } else if (isNotEmpty) {
            HorizontalPager(
                state = nestedPagerState,
                pageSpacing = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .negativePadding(horizontal = 16.dp)
                    .fadingEdges(nestedPagerState),
                contentPadding = PaddingValues(horizontal = 16.dp),
                beyondViewportPageCount = 10
            ) { nestedPage ->
                UriWithHashItem(
                    uriWithHash = page.uris[nestedPage],
                    onCopyText = onCopyText
                )
            }
        } else {
            InfoContainer(
                text = stringResource(R.string.pick_files_to_checksum),
            )
        }
    }

    AnimatedVisibility(page.uris.isNotEmpty()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (page.uris.size > 1) {
                PagerScrollPanel(nestedPagerState)
            }

            ChecksumEnterField(
                value = page.targetChecksum,
                onValueChange = {
                    component.setDataForBatchComparison(
                        targetChecksum = it
                    )
                }
            )
        }
    }

    AnimatedVisibility(page.targetChecksum.isNotEmpty() && page.uris.isNotEmpty()) {
        val isCorrect =
            page.targetChecksum == page.uris[nestedPagerState.currentPage].checksum

        ChecksumResultCard(isCorrect)
    }
}