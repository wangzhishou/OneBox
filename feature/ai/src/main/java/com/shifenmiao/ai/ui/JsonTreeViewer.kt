package com.shifenmiao.ai.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandLess
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandMore

/**
 * JSON 树形查看器，支持折叠/展开和节点值编辑。
 *
 * @param jsonString 要展示的 JSON 字符串
 * @param onEditValue 值编辑回调，参数为 (路径, 新值)
 * @param modifier Modifier
 */
@Composable
fun JsonTreeViewer(
    jsonString: String,
    onEditValue: (path: List<String>, value: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val element = remember(jsonString) {
        try {
            JsonParser.parseString(jsonString)
        } catch (_: Exception) {
            null
        }
    }

    if (element == null) {
        Text(
            text = stringResource(R.string.code_editor_parse_error),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(16.dp),
        )
        return
    }

    GlassSurface(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(
                    12.dp
                )
        ) {
            // 根节点如果是对象或数组，直接渲染其内部（避免多一层外壳）
            when {
                element.isJsonObject -> {
                    val obj = element.asJsonObject
                    obj.entrySet().forEach { (k, v) ->
                        JsonNodeItem(
                            key = k,
                            element = v,
                            path = listOf(k),
                            depth = 0,
                            onEditValue = onEditValue,
                        )
                    }
                }

                element.isJsonArray -> {
                    val arr = element.asJsonArray
                    arr.forEachIndexed { index, item ->
                        JsonNodeItem(
                            key = index.toString(),
                            element = item,
                            path = listOf(index.toString()),
                            depth = 0,
                            onEditValue = onEditValue,
                        )
                    }
                }

                else -> {
                    JsonLeafNode(
                        key = null,
                        element = element,
                        path = emptyList(),
                        depth = 0,
                        onEditValue = onEditValue,
                    )
                }
            }
        }
    }
}

@Composable
private fun JsonNodeItem(
    key: String?,
    element: JsonElement,
    path: List<String>,
    depth: Int,
    onEditValue: (path: List<String>, value: String) -> Unit,
) {
    val indentDp = (depth * 16).dp

    when {
        element.isJsonObject -> {
            var expanded by remember { mutableStateOf(depth < 2) }
            val obj = element.asJsonObject
            val size = obj.size()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = indentDp)
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (expanded) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandLess else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                JsonKeyText(key = key)
                Text(
                    text = "{${if (size == 0) "" else " $size "}}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column {
                    obj.entrySet().forEach { (k, v) ->
                        JsonNodeItem(
                            key = k,
                            element = v,
                            path = path + k,
                            depth = depth + 1,
                            onEditValue = onEditValue,
                        )
                    }
                    Text(
                        text = "}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = indentDp),
                    )
                }
            }
        }

        element.isJsonArray -> {
            var expanded by remember { mutableStateOf(depth < 2) }
            val arr = element.asJsonArray
            val size = arr.size()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = indentDp)
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (expanded) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandLess else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                JsonKeyText(key = key)
                Text(
                    text = "[${if (size == 0) "" else " $size "}]",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column {
                    arr.forEachIndexed { index, item ->
                        JsonNodeItem(
                            key = index.toString(),
                            element = item,
                            path = path + index.toString(),
                            depth = depth + 1,
                            onEditValue = onEditValue,
                        )
                    }
                    Text(
                        text = "]",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = indentDp),
                    )
                }
            }
        }

        else -> {
            JsonLeafNode(
                key = key,
                element = element,
                path = path,
                depth = depth,
                onEditValue = onEditValue,
            )
        }
    }
}

@Composable
private fun JsonLeafNode(
    key: String?,
    element: JsonElement,
    path: List<String>,
    depth: Int,
    onEditValue: (path: List<String>, value: String) -> Unit,
) {
    val (valueColor, displayValue, rawValue) = when {
        element.isJsonNull -> Triple(
            Color(0xFF757575),
            "null",
            "null",
        )

        element.isJsonPrimitive -> {
            val primitive = element.asJsonPrimitive
            when {
                primitive.isString -> Triple(
                    Color(0xFF4CAF50),
                    "\"${primitive.asString}\"",
                    primitive.asString,
                )

                primitive.isNumber -> Triple(
                    Color(0xFF2196F3),
                    primitive.asNumber.toString(),
                    primitive.asNumber.toString(),
                )

                primitive.isBoolean -> Triple(
                    Color(0xFF9C27B0),
                    primitive.asBoolean.toString(),
                    primitive.asBoolean.toString(),
                )

                else -> Triple(
                    MaterialTheme.colorScheme.onSurface,
                    primitive.asString,
                    primitive.asString,
                )
            }
        }

        else -> Triple(
            MaterialTheme.colorScheme.onSurface,
            element.toString(),
            element.toString(),
        )
    }

    var showEditDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp)
            .clickable(enabled = !element.isJsonNull) { showEditDialog = true },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        JsonKeyText(key = key)
        Text(
            text = displayValue,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }

    if (showEditDialog) {
        var editText by remember { mutableStateOf(rawValue) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.code_editor_edit_value),
                    style = MaterialTheme.typography.titleSmall,
                )
            },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEditValue(path, editText)
                        showEditDialog = false
                    }
                ) {
                    Text(stringResource(R.string.code_editor_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            },
        )
    }
}

@Composable
private fun JsonKeyText(key: String?) {
    if (key == null) return
    val isArrayIndex = key.all { it.isDigit() }
    Text(
        text = if (isArrayIndex) "$key: " else "\"$key\": ",
        style = MaterialTheme.typography.bodyMedium,
        color = if (isArrayIndex) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onSurface
        },
    )
}
