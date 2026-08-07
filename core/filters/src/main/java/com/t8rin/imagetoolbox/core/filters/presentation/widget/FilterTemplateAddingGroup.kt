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

package com.t8rin.imagetoolbox.core.filters.presentation.widget

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFilePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.rememberBarcodeScanner
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.utils.getString
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoFix
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFileOpen
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQrScanner

@SuppressLint("StringFormatInvalid")
@Composable
internal fun FilterTemplateAddingGroup(
    component: FilterTemplateCreationSheetComponent,
    onAddTemplateFilterFromString: (
        string: String,
        onSuccess: suspend (filterName: String, filtersCount: Int) -> Unit,
        onFailure: suspend () -> Unit
    ) -> Unit,
    onAddTemplateFilterFromUri: (
        uri: String,
        onSuccess: suspend (filterName: String, filtersCount: Int) -> Unit,
        onFailure: suspend () -> Unit
    ) -> Unit
) {
    fun addTemplateFilterFromString(
        string: String,
        onSuccess: suspend (filterName: String, filtersCount: Int) -> Unit,
        onFailure: suspend () -> Unit
    ) = onAddTemplateFilterFromString(string, onSuccess, onFailure)

    fun addTemplateFilterFromUri(
        uri: String,
        onSuccess: suspend (filterName: String, filtersCount: Int) -> Unit,
        onFailure: suspend () -> Unit
    ) = onAddTemplateFilterFromUri(uri, onSuccess, onFailure)

    val scanner = rememberBarcodeScanner {
        addTemplateFilterFromString(
            string = it.raw,
            onSuccess = { filterName, filtersCount ->
                AppToastHost.showToast(
                    message = getString(
                        R.string.added_filter_template,
                        filterName,
                        filtersCount
                    ),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoFix
                )
            },
            onFailure = {
                AppToastHost.showToast(
                    message = getString(R.string.scanned_qr_code_isnt_filter_template),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQrScanner
                )
            }
        )
    }

    val importFromFileLauncher = rememberFilePicker { uri: Uri ->
        addTemplateFilterFromUri(
            uri = uri.toString(),
            onSuccess = { filterName, filtersCount ->
                AppToastHost.showToast(
                    message = getString(
                        R.string.added_filter_template,
                        filterName,
                        filtersCount
                    ),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoFix
                )
            },
            onFailure = {
                AppToastHost.showToast(
                    message = getString(R.string.opened_file_have_no_filter_template),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoFix
                )
            }
        )
    }

    var showFilterTemplateCreationSheet by rememberSaveable {
        mutableStateOf(false)
    }

    val localActivity = LocalComponentActivity.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
    ) {
        EnhancedIconButton(
            onClick = {
                scanner.scan(contextActivity = localActivity)
            },
            containerColor = MaterialTheme.colorScheme.tertiary
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQrScanner,
                contentDescription = "Scan QR"
            )
        }
        EnhancedIconButton(
            onClick = importFromFileLauncher::pickFile,
            containerColor = MaterialTheme.colorScheme.secondary
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFileOpen,
                contentDescription = "Import From File"
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        EnhancedButton(
            onClick = { showFilterTemplateCreationSheet = true },
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Text(stringResource(R.string.create_new))
        }
    }

    FilterTemplateCreationSheet(
        visible = showFilterTemplateCreationSheet,
        onDismiss = { showFilterTemplateCreationSheet = false },
        component = component
    )
}