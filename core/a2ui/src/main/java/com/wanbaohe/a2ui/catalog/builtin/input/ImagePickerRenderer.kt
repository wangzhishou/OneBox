package com.wanbaohe.a2ui.catalog.builtin.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.t8rin.imagetoolbox.core.data.utils.SafUriUtils
import com.wanbaohe.core.ui.utils.content_pickers.Picker
import com.wanbaohe.core.ui.utils.content_pickers.rememberImagePicker
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import com.wanbaohe.a2ui.domain.model.DynamicValue
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage

/**
 * 图片选择器：点击右侧图标唤起系统图片选择器，
 * 选中的 content URI 经 [SafUriUtils.toFileUri] 转写为 file:// 本地路径写入数据模型，
 * 避免 SAF 权限过期问题。输入框也可手动编辑路径。
 */
class ImagePickerRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "ImagePicker"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val valueDynamic = component.properties["value"]
        val currentValue = context.resolveString(valueDynamic) ?: ""
        val pointerPath = (valueDynamic as? DynamicValue.Pointer)?.path
        val label = context.resolveString(component.properties["label"]) ?: ""
        val enabled = context.resolveBoolean(component.properties["enabled"]) ?: true
        val multiple = context.resolveBoolean(component.properties["multiple"]) ?: false

        val androidContext = LocalContext.current

        fun updateValue(value: String) {
            pointerPath?.let {
                context.updateDataModel(it, JsonPrimitive(value))
            }
        }

        val imagePicker = rememberImagePicker(
            picker = if (multiple) Picker.Multiple else Picker.Single,
        ) { uris ->
            val picked = uris.mapNotNull { uri ->
                SafUriUtils.toFileUri(androidContext, uri)?.toString() ?: uri.toString()
            }
            if (picked.isNotEmpty()) {
                updateValue(picked.joinToString(","))
            }
        }

        GlassOutlinedTextField(
            value = currentValue,
            onValueChange = { newValue ->
                if (enabled) {
                    updateValue(newValue)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            label = label.takeIf { it.isNotBlank() }?.let { { Text(it) } },
            trailingIcon = {
                IconButton(onClick = {
                    if (enabled) {
                        imagePicker.pickImage()
                    }
                }) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage, contentDescription = null)
                }
            },
        )
    }
}
