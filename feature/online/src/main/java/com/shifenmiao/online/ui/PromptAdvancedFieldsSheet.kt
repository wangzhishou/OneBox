/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shifenmiao.online.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R as CoreR
import com.shifenmiao.online.R
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton

@Composable
internal fun PromptAdvancedFieldsSheet(
    placeholder: String,
    templates: String,
    onPlaceholderChange: (String) -> Unit,
    onTemplatesChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { if (!it) onDismiss() },
        title = stringResource(R.string.prompt_input_settings),
        modifier = Modifier.imePadding(),
        nestedScrollEnabled = true,
        endConfirmButtonPadding = 0.dp,
        confirmButton = {
            OnePrimaryButton(stringResource(CoreR.string.done), onClick = onDismiss)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTheme.dimens.pageHorizontal),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GlassOutlinedTextField(
                value = placeholder,
                onValueChange = onPlaceholderChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(CoreR.string.create_ai_chat_prompt_placeholder_label)) },
                minLines = 2,
                maxLines = 4,
            )
            GlassOutlinedTextField(
                value = templates,
                onValueChange = onTemplatesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.prompt_templates_label)) },
                minLines = 4,
                maxLines = 8,
            )
        }
    }
}
