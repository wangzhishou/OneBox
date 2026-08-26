package com.wanbaohe.textcard.presentation.selection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.shifenmiao.base.ui.button.PrimaryButton
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowForwardIos
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.CanvasSpec
import com.wanbaohe.textcard.domain.model.ElementLayer
import com.wanbaohe.textcard.domain.model.GradientPresets
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.TextCardRenderState
import com.wanbaohe.textcard.domain.render.CardLayout
import com.wanbaohe.textcard.presentation.editor.CardCanvasPreview
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.Add

/**
 * 选择画布页(设计稿 00):两个平台卡片(带 mini 预览)+ 底部「开始制作」。
 */
@Composable
fun CanvasSelectContent(
    component: TextCardComponent,
) {
    var showCustomSizeDialog by remember { mutableStateOf(false) }

    BaseScreen(
        title = stringResource(R.string.textcard_select_canvas_title),
        onGoBack = component.onGoBack,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.textcard_select_canvas_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 16.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    CanvasSpec.builtIn.forEach { spec ->
                        CanvasCard(
                            spec = spec,
                            selected = component.pendingCanvas == spec,
                            onClick = { component.selectPendingCanvas(spec) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    CustomCanvasCard(
                        component = component,
                        onEditRequest = { showCustomSizeDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            PrimaryButton(
                text = stringResource(R.string.textcard_start_create),
                onClick = component::startEditing,
                icon = {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.width(16.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }
    )

    if (showCustomSizeDialog) {
        CustomSizeDialog(
            initial = component.lastCustomCanvas,
            onConfirm = { width, height ->
                component.selectCustomCanvas(width, height)
                showCustomSizeDialog = false
            },
            onDismiss = { showCustomSizeDialog = false }
        )
    }
}

@Composable
private fun CanvasCard(
    spec: CanvasSpec,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
        Text(
            text = stringResource(spec.titleRes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(spec.descRes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )
        // mini 预览:默认渐变 + 默认文案,与进入编辑后的初始卡片一致;
        // 内层预览自带点按手势,盖一层透明拦截,让整卡(含预览区)统一响应选中
        val previewBlocks = listOf(
            TextBlock(
                content = stringResource(R.string.textcard_default_title),
                baseSizeRatio = CardLayout.TITLE_BASE_SIZE_RATIO,
                baseTopRatio = CardLayout.CONTENT_PADDING_RATIO,
                isBold = true
            ),
            TextBlock(
                content = stringResource(R.string.textcard_default_body),
                baseSizeRatio = CardLayout.BODY_BASE_SIZE_RATIO,
                baseTopRatio = CardLayout.BODY_BASE_TOP_RATIO
            )
        )
        Box {
            CardCanvasPreview(
                state = TextCardRenderState(
                    canvas = spec,
                    background = GradientPresets.default,
                    textBlocks = previewBlocks,
                    layers = previewBlocks.map {
                        ElementLayer(it.id, ElementLayer.Kind.Text)
                    }
                ),
                cornerRadius = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(spec.aspectRatio)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectTapGestures { onClick() }
                    }
            )
        }
        Text(
            text = spec.ratioLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )
        }
    }
}

/** 自定义尺寸卡片:有上次尺寸时按该比例 mini 预览,否则 + 占位;点击弹尺寸输入框 */
@Composable
private fun CustomCanvasCard(
    component: TextCardComponent,
    onEditRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lastCustom = component.lastCustomCanvas
    val selected = component.pendingCanvas is CanvasSpec.Custom
    GlassCard(
        onClick = {
            if (lastCustom != null) {
                component.selectPendingCanvas(lastCustom)
            }
            onEditRequest()
        },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.textcard_canvas_custom),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.textcard_canvas_custom_desc),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            if (lastCustom != null) {
                val previewBlocks = listOf(
                    TextBlock(
                        content = stringResource(R.string.textcard_default_title),
                        baseSizeRatio = CardLayout.TITLE_BASE_SIZE_RATIO,
                        baseTopRatio = CardLayout.CONTENT_PADDING_RATIO,
                        isBold = true
                    ),
                    TextBlock(
                        content = stringResource(R.string.textcard_default_body),
                        baseSizeRatio = CardLayout.BODY_BASE_SIZE_RATIO,
                        baseTopRatio = CardLayout.BODY_BASE_TOP_RATIO
                    )
                )
                Box {
                    CardCanvasPreview(
                        state = TextCardRenderState(
                            canvas = lastCustom,
                            background = GradientPresets.default,
                            textBlocks = previewBlocks,
                            layers = previewBlocks.map {
                                ElementLayer(it.id, ElementLayer.Kind.Text)
                            }
                        ),
                        cornerRadius = 12.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(lastCustom.aspectRatio)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    component.selectPendingCanvas(lastCustom)
                                }
                            }
                    )
                }
                Text(
                    text = lastCustom.ratioLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    Icon(
                        imageVector = MaterialIcons.Outlined.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** 自定义尺寸输入框:两个数字输入,确认时钳制 256..4096(组件侧再钳一次) */
@Composable
private fun CustomSizeDialog(
    initial: CanvasSpec.Custom?,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var widthText by remember(initial) {
        mutableStateOf((initial?.customWidth ?: 1080).toString())
    }
    var heightText by remember(initial) {
        mutableStateOf((initial?.customHeight ?: 1440).toString())
    }

    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.textcard_custom_size_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = widthText,
                    onValueChange = { value ->
                        widthText = value.filter { it.isDigit() }.take(4)
                    },
                    label = { Text(stringResource(R.string.textcard_canvas_width)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = heightText,
                    onValueChange = { value ->
                        heightText = value.filter { it.isDigit() }.take(4)
                    },
                    label = { Text(stringResource(R.string.textcard_canvas_height)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }
        },
        confirmButton = {
            ConfirmButton(
                onClick = {
                    val width = widthText.toIntOrNull() ?: return@ConfirmButton
                    val height = heightText.toIntOrNull() ?: return@ConfirmButton
                    onConfirm(width, height)
                }
            )
        },
        dismissButton = {
            CancelButton(onClick = onDismiss)
        }
    )
}
