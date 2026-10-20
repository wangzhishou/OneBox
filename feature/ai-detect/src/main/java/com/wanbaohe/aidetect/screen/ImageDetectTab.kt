package com.wanbaohe.aidetect.screen

import androidx.compose.foundation.clickable
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.wanbaohe.core.ui.utils.content_pickers.rememberImagePicker
import com.wanbaohe.core.ui.widget.glass.GlassButton
import com.wanbaohe.core.ui.widget.glass.GlassCircularProgressIndicator
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedButton
import com.wanbaohe.aidetect.R
import com.wanbaohe.aidetect.component.AiDetectComponent

/** 图片检测 tab:虚线边框上传区 + 预览 + 检测按钮 + 结果卡片 */
@Composable
fun ImageDetectTab(component: AiDetectComponent) {
    val state by component.imageState.collectAsState()

    val imagePicker = rememberImagePicker(
        onSuccess = { uri: Uri -> component.onImageSelected(uri) },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        val result = state.result
        if (result == null) {
            val selectedUri = state.selectedUri
            if (selectedUri == null) {
                ImageUploadArea(onClick = { imagePicker.pickImage() })
            } else {
                AsyncImage(
                    model = selectedUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isDetecting) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GlassCircularProgressIndicator()
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.ai_detect_detecting),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    GlassOutlinedButton(
                        onClick = { imagePicker.pickImage() },
                        enabled = !state.isDetecting,
                    ) {
                        Text(stringResource(R.string.ai_detect_change_image))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    GlassButton(
                        onClick = component::detectImage,
                        modifier = Modifier.weight(1f),
                        enabled = !state.isDetecting,
                    ) {
                        Text(stringResource(R.string.ai_detect_button))
                    }
                }
            }
        } else {
            DetectResultCard(
                probability = result.confidence,
                isDetailExpanded = state.isDetailExpanded,
                onRecheck = component::resetImageDetect,
                onToggleDetail = component::toggleImageDetail,
            ) {
                Text(
                    text = stringResource(
                        R.string.ai_detect_image_confidence_detail,
                        (result.confidence * 100).toInt(),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

/** 虚线边框的图片上传区 */
@Composable
private fun ImageUploadArea(onClick: () -> Unit) {
    val borderColor = MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f)),
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                )
            }
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.ai_detect_upload_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.ai_detect_upload_format),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }
    }
}
