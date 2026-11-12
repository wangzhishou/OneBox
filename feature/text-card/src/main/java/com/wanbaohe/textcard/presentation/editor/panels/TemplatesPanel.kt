/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
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

package com.wanbaohe.textcard.presentation.editor.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.button.CancelButton
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import com.wanbaohe.textcard.presentation.selection.TemplateCard

@Composable
fun TemplatesPanel(component: TextCardComponent) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var templateName by remember { mutableStateOf("") }
    Column {
        component.allTemplates.chunked(2).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                row.forEach { template ->
                    TemplateCard(
                        template = template,
                        selected = component.document.templateId == template.id,
                        onClick = { component.applyTemplate(template) },
                        canvas = component.document.canvas,
                        document = component.document,
                        modifier = Modifier.weight(1f),
                        onDelete = if (template.id.startsWith("custom_")) {
                            { component.deleteCustomTemplate(template) }
                        } else null
                    )
                }
            }
        }
        TextButton(
            onClick = {
                templateName = ""
                showSaveDialog = true
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.textcard_save_as_template))
        }
    }
    if (showSaveDialog) {
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { showSaveDialog = false },
            title = { Text(stringResource(R.string.textcard_save_as_template)) },
            text = {
                GlassOutlinedTextField(
                    value = templateName,
                    onValueChange = { templateName = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.textcard_template_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                ConfirmButton(
                    enabled = templateName.isNotBlank(),
                    onClick = {
                        component.saveCurrentTemplate(templateName)
                        showSaveDialog = false
                    }
                )
            },
            dismissButton = { CancelButton(onClick = { showSaveDialog = false }) }
        )
    }
}
