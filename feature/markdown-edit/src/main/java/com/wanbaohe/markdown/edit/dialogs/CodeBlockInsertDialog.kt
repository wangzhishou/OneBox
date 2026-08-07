package com.wanbaohe.markdown.edit.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.wanbaohe.markdown.edit.R as MarkdownR

/**
 * 代码块插入对话框
 *
 * @param visible 是否显示
 * @param onConfirm 确认回调，返回 (代码, 语言)
 * @param onDismiss 取消回调
 */
@Composable
fun CodeBlockInsertDialog(
    visible: Boolean,
    onConfirm: (code: String, language: String) -> Unit,
    onDismiss: () -> Unit
) {
    var code by remember(visible) { mutableStateOf("") }
    var selectedLanguage by remember(visible) { mutableStateOf("kotlin") }
    var searchQuery by remember(visible) { mutableStateOf("") }

    val languages = listOf(
        "kotlin", "java", "python", "javascript", "typescript",
        "c", "cpp", "csharp", "go", "rust",
        "swift", "php", "ruby", "sql", "html",
        "css", "json", "xml", "yaml", "markdown",
        "shell", "bash", "powershell", "dockerfile", "text"
    )

    val filteredLanguages = if (searchQuery.isBlank()) {
        languages
    } else {
        languages.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    EnhancedAlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(MarkdownR.string.insert_code_block)) },
        confirmButton = {
            EnhancedButton(
                onClick = {
                    if (code.isNotBlank()) {
                        onConfirm(code, selectedLanguage)
                    }
                },
                enabled = code.isNotBlank()
            ) {
                Text(stringResource(R.string.button_confirm))
            }
        },
        dismissButton = {
            EnhancedButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.button_cancel))
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 代码输入
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text(stringResource(MarkdownR.string.code_label)) },
                    placeholder = { Text(stringResource(MarkdownR.string.enter_code_hint)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    minLines = 5,
                    maxLines = 10
                )

                // 语言选择
                Text(
                    text = stringResource(MarkdownR.string.select_language),
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )

                // 搜索框
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(MarkdownR.string.search_language)) },
                    placeholder = { Text(stringResource(MarkdownR.string.search_language_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 语言列表
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredLanguages) { language ->
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedLanguage = language }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == language,
                                onClick = { selectedLanguage = language }
                            )
                            Text(
                                text = language,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}

