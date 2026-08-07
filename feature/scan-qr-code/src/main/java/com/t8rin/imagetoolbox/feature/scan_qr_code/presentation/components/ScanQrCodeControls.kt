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

package com.t8rin.imagetoolbox.feature.scan_qr_code.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.extendedcolors.util.roundToTwoDigits
import com.t8rin.imagetoolbox.core.domain.model.QrType
import com.t8rin.imagetoolbox.core.domain.model.copy
import com.t8rin.imagetoolbox.core.domain.utils.safeCast
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.MiniEdit
import com.t8rin.imagetoolbox.core.ui.theme.mixedContainer
import com.t8rin.imagetoolbox.core.ui.theme.onMixedContainer
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.DataSelector
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.FontSelector
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.ImageSelector
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedSliderItem
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.hapticsClickable
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.animateShape
import com.t8rin.imagetoolbox.core.ui.widget.other.BarcodeType
import com.t8rin.imagetoolbox.core.ui.widget.other.BoxAnimatedVisibility
import com.t8rin.imagetoolbox.core.ui.widget.other.InfoContainer
import com.t8rin.imagetoolbox.core.ui.widget.other.LinkPreviewList
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.text.RoundedTextField
import com.t8rin.imagetoolbox.feature.scan_qr_code.presentation.screenLogic.ScanQrCodeComponent
import com.shifenmiao.theme.AppTheme
import kotlin.math.roundToInt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineError
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQrCode
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRoundedCorner

@Composable
internal fun ScanQrCodeControls(component: ScanQrCodeComponent) {
    val params by rememberUpdatedState(component.params)

    LinkPreviewList(
        text = params.content.raw,
        externalLinks = remember(params.content) {
            (params.content as? QrType.Url)?.let { listOf(it.url) }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppTheme.dimens.spaceSmall)
    )
    QrTypeInfoItem(
        qrType = params.content,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppTheme.dimens.spaceSmall)
    )

    val noContent = params.content.raw.isEmpty()

    val isNotScannable = !noContent && component.mayBeNotScannable

    AnimatedVisibility(
        visible = isNotScannable,
        modifier = Modifier.fillMaxWidth()
    ) {
        InfoContainer(
            text = stringResource(R.string.code_may_be_not_scannable),
            modifier = Modifier.padding(AppTheme.dimens.spaceSmall),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineError
        )
    }

    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = OneBoxDesignSystem.sectionGlassStyle,
        shape = OneBoxDesignSystem.sectionCardShape,
        borderWidth = 0.dp
    ) {
        Column {
            RoundedTextField(
                modifier = Modifier
                    .padding(
                        top = AppTheme.dimens.spaceSmall,
                        start = AppTheme.dimens.spaceSmall,
                        end = AppTheme.dimens.spaceSmall,
                        bottom = if (noContent) AppTheme.dimens.spaceExtraSmall else AppTheme.dimens.spaceSmall
                    ),
                shape = animateShape(
                    if (noContent) ShapeDefaults.smallTop else ShapeDefaults.small
                ),
                value = params.content.raw,
                onValueChange = {
                    component.updateParams(
                        params.copy(
                            content = params.content.copy(it)
                        )
                    )
                },
                maxSymbols = 2500,
                singleLine = false,
                supportingText = if (!noContent) {
                    {
                        AnimatedContent(
                            targetState = params.content,
                            contentKey = { it::class.simpleName },
                            transitionSpec = { fadeIn() togetherWith fadeOut() }
                        ) { content ->
                            Text(
                                text = stringResource(content.name),
                                color = MaterialTheme.colorScheme.onMixedContainer,
                                modifier = Modifier
                                    .glassBackground(
                                        style = GlassStyle.Thin,
                                        color = MaterialTheme.colorScheme.mixedContainer,
                                        shape = ShapeDefaults.small,
                                        borderWidth = 0.dp,
                                    )
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                } else null,
                label = {
                    Text(stringResource(id = R.string.code_content))
                },
                keyboardOptions = KeyboardOptions()
            )

            var showEditField by rememberSaveable {
                mutableStateOf(false)
            }

            GlassTonalButton(
                onClick = { showEditField = true },
                shape = if (noContent) ShapeDefaults.smallBottom else ShapeDefaults.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = AppTheme.dimens.spaceSmall,
                        end = AppTheme.dimens.spaceSmall,
                        bottom = AppTheme.dimens.spaceSmall
                    ),
                color = MaterialTheme.colorScheme.mixedContainer,
                contentColor = MaterialTheme.colorScheme.onMixedContainer,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.MiniEdit,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(AppTheme.dimens.spaceExtraSmall))
                    Text(
                        text = stringResource(
                            if (params.content is QrType.Complex) R.string.edit_barcode else R.string.create_barcode
                        )
                    )
                }
            }

            QrTypeEditSheet(
                qrType = params.content.safeCast(),
                onSave = { component.updateParams(params.copy(content = it)) },
                onDismiss = { showEditField = false },
                visible = showEditField
            )
        }
    }
    Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))
    InfoContainer(
        text = stringResource(R.string.scan_qr_code_to_replace_content),
        modifier = Modifier.padding(AppTheme.dimens.spaceSmall)
    )
    Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))

    AnimatedVisibility(visible = params.content.raw.isNotEmpty()) {
        Column {
            DataSelector(
                value = params.type,
                onValueChange = {
                    component.updateParams(
                        params.copy(
                            type = it
                        )
                    )
                },
                spanCount = 2,
                entries = BarcodeType.entries,
                title = stringResource(R.string.barcode_type),
                titleIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQrCode,
                itemContentText = {
                    remember {
                        it.name.replace("_", " ")
                    }
                }
            )
            Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))
            BoxAnimatedVisibility(
                visible = !params.type.isSquare || params.type == BarcodeType.DATA_MATRIX,
                modifier = Modifier.fillMaxWidth()
            ) {
                EnhancedSliderItem(
                    value = params.heightRatio,
                    title = stringResource(R.string.height_ratio),
                    valueRange = 1f..4f,
                    onValueChange = {},
                    onValueChangeFinished = {
                        component.updateParams(
                            params.copy(
                                heightRatio = it
                            )
                        )
                    },
                    internalStateTransformation = {
                        it.roundToTwoDigits()
                    },
                    modifier = Modifier.padding(bottom = AppTheme.dimens.spaceSmall)
                )
            }
            Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))
            QrParamsSelector(
                isQrType = params.type == BarcodeType.QR_CODE,
                value = params.qrParams,
                onValueChange = {
                    component.updateParams(
                        params.copy(
                            qrParams = it
                        )
                    )
                }
            )
            Spacer(modifier = Modifier.height(AppTheme.dimens.spaceLarge))
            Row(
                modifier = Modifier.height(intrinsicSize = IntrinsicSize.Max)
            ) {
                ImageSelector(
                    value = params.imageUri,
                    subtitle = stringResource(id = R.string.qr_code_top_image),
                    onValueChange = {
                        component.updateParams(
                            params.copy(
                                imageUri = it
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    shape = ShapeDefaults.extraLarge
                )
                BoxAnimatedVisibility(visible = params.imageUri != null) {
                    val interactionSource = remember { MutableInteractionSource() }

                    GlassSurface(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = AppTheme.dimens.spaceSmall),
                        onClick = {
                            component.updateParams(
                                params.copy(
                                    imageUri = null
                                )
                            )
                        },
                        style = GlassStyle.Thin,
                        shape = ShapeDefaults.default,
                        color = MaterialTheme.colorScheme.errorContainer,
                        borderWidth = 0.dp
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = AppTheme.dimens.spaceSmall),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))
            RoundedTextField(
                modifier = Modifier
                    .padding(horizontal = AppTheme.dimens.spaceSmall)
                    .glassBackground(
                        style = OneBoxDesignSystem.rowGlassStyle,
                        shape = animateShape(
                            if (params.description.isNotEmpty()) ShapeDefaults.top
                            else ShapeDefaults.default
                        ),
                        borderWidth = 0.dp
                    )
                    .padding(AppTheme.dimens.spaceSmall),
                value = params.description,
                onValueChange = {
                    component.updateParams(
                        params.copy(
                            description = it
                        )
                    )
                },
                singleLine = false,
                label = {
                    Text(stringResource(id = R.string.qr_description))
                }
            )
            Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))
            BoxAnimatedVisibility(
                visible = params.description.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                FontSelector(
                    value = params.descriptionFont,
                    onValueChange = {
                        component.updateParams(
                            params.copy(
                                descriptionFont = it
                            )
                        )
                    },
                    containerColor = Color.Unspecified,
                    shape = ShapeDefaults.bottom,
                    modifier = Modifier.padding(bottom = AppTheme.dimens.spaceSmall)
                )
            }
            EnhancedSliderItem(
                value = params.cornersSize,
                title = stringResource(R.string.corners),
                valueRange = 0f..36f,
                onValueChange = {
                    component.updateParams(
                        params.copy(
                            cornersSize = it.toInt()
                        )
                    )
                },
                internalStateTransformation = {
                    it.roundToInt()
                },
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRoundedCorner,
                steps = 22
            )
            Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))
        }
    }
}