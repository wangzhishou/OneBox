package com.shifenmiao.demo.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.demo.R
import com.t8rin.imagetoolbox.core.ui.widget.glass.ColoredGlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.ColoredLiquidGlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassFilterChip
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSegmentedButtonRow
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTextFieldContainer
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.resources.icons.Search
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTouchApp

private enum class GlassShowcaseMode(
    @StringRes val titleRes: Int,
    val style: GlassStyle,
) {
    Quiet(R.string.demo_glass_showcase_mode_quiet, GlassStyle.Darker),
    Balanced(R.string.demo_glass_showcase_mode_balanced, GlassStyle.Regular),
    Expressive(R.string.demo_glass_showcase_mode_expressive, GlassStyle.Dense),
}

@Composable
fun GlassComponentShowcaseSheet() {
    val colorScheme = MaterialTheme.colorScheme
    var selectedMode by rememberSaveable { mutableStateOf(GlassShowcaseMode.Balanced) }
    var cardClicks by rememberSaveable { mutableIntStateOf(0) }
    var surfaceClicks by rememberSaveable { mutableIntStateOf(0) }
    var primaryChipSelected by rememberSaveable { mutableStateOf(true) }
    var secondaryChipSelected by rememberSaveable { mutableStateOf(false) }
    var switchChecked by rememberSaveable { mutableStateOf(true) }
    var inputText by rememberSaveable { mutableStateOf("") }
    var isInputFocused by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ShowcaseIntroCard(
                title = stringResource(R.string.demo_glass_showcase_title),
                description = stringResource(R.string.demo_glass_showcase_sheet_subtitle),
                style = selectedMode.style,
            )
        }

        item {
            ShowcaseSectionHeader(
                title = stringResource(R.string.demo_glass_showcase_mode_title),
                description = stringResource(R.string.demo_glass_showcase_mode_desc),
            )
        }

        item {
            GlassSegmentedButtonRow(
                options = GlassShowcaseMode.entries.toList(),
                selectedOption = selectedMode,
                onOptionSelected = { selectedMode = it },
                modifier = Modifier.fillMaxWidth(),
                rowStyle = GlassStyle.Darker,
                selectedStyle = selectedMode.style,
                selectedColor = colorScheme.primaryContainer,
                selectedContentColor = colorScheme.onPrimaryContainer,
                unselectedContentColor = colorScheme.onSurfaceVariant,
                label = { mode ->
                    Text(text = stringResource(mode.titleRes))
                },
                selectedIcon = {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
        }

        item {
            ShowcaseSectionHeader(
                title = stringResource(R.string.demo_glass_showcase_tint_title),
                description = stringResource(R.string.demo_glass_showcase_tint_desc),
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TintValidationSurface(
                    title = stringResource(R.string.demo_glass_showcase_tint_primary),
                    color = colorScheme.primaryContainer,
                    contentColor = colorScheme.onPrimaryContainer,
                    style = selectedMode.style,
                )
                TintValidationSurface(
                    title = stringResource(R.string.demo_glass_showcase_tint_secondary),
                    color = colorScheme.secondaryContainer,
                    contentColor = colorScheme.onSecondaryContainer,
                    style = selectedMode.style,
                )
                TintValidationSurface(
                    title = stringResource(R.string.demo_glass_showcase_tint_error),
                    color = colorScheme.errorContainer,
                    contentColor = colorScheme.onErrorContainer,
                    style = selectedMode.style,
                )
            }
        }

        item {
            ShowcaseSectionHeader(
                title = stringResource(R.string.demo_glass_showcase_colored_material_title),
                description = stringResource(R.string.demo_glass_showcase_colored_material_desc),
            )
        }

        item {
            ColoredGlassComparisonSection(style = selectedMode.style)
        }

        item {
            ShowcaseSectionHeader(
                title = stringResource(R.string.demo_glass_showcase_cards_title),
                description = stringResource(R.string.demo_glass_showcase_cards_desc),
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceContainerHigh,
                        contentColor = colorScheme.onSurface,
                    ),
                    containerAlpha = selectedMode.style.backgroundAlpha,
                    borderWidth = 0.5.dp,
                ) {
                    ShowcaseCardContent(
                        title = stringResource(R.string.demo_glass_showcase_primary_card_title),
                        body = stringResource(R.string.demo_glass_showcase_primary_card_body),
                    )
                }

                GlassCard(
                    onClick = { cardClicks++ },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceContainerHighest,
                        contentColor = colorScheme.onSurface,
                    ),
                    containerAlpha = (selectedMode.style.backgroundAlpha + 0.03f).coerceAtMost(1f),
                    borderWidth = 0.5.dp,
                ) {
                    ShowcaseCardContent(
                        title = stringResource(R.string.demo_glass_showcase_clickable_card_title),
                        body = stringResource(R.string.demo_glass_showcase_clickable_card_body, cardClicks),
                        trailing = {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTouchApp,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    )
                }
            }
        }

        item {
            ShowcaseSectionHeader(
                title = stringResource(R.string.demo_glass_showcase_actions_title),
                description = stringResource(R.string.demo_glass_showcase_actions_desc),
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    GlassButton(
                        onClick = { surfaceClicks++ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        color = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer,
                        containerAlpha = (selectedMode.style.backgroundAlpha + 0.12f).coerceAtMost(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primaryContainer,
                            contentColor = colorScheme.onPrimaryContainer,
                        ),
                    ) {
                        Text(text = stringResource(R.string.demo_glass_showcase_primary_button))
                    }

                    GlassTonalButton(
                        onClick = { surfaceClicks++ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        style = selectedMode.style,
                        color = colorScheme.surfaceContainerHighest,
                        contentColor = colorScheme.onSurface,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = colorScheme.surfaceContainerHighest,
                            contentColor = colorScheme.onSurface,
                        ),
                    ) {
                        Text(text = stringResource(R.string.demo_glass_showcase_secondary_button))
                    }
                }

                GlassSurface(
                    onClick = { surfaceClicks++ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = colorScheme.surfaceContainerHigh,
                    style = selectedMode.style,
                    borderWidth = 0.5.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic,
                            contentDescription = null,
                            tint = colorScheme.primary,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.demo_glass_showcase_surface_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = stringResource(R.string.demo_glass_showcase_surface_body, surfaceClicks),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        item {
            ShowcaseSectionHeader(
                title = stringResource(R.string.demo_glass_showcase_selection_title),
                description = stringResource(R.string.demo_glass_showcase_selection_desc),
            )
        }

        item {
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                style = selectedMode.style,
                color = colorScheme.surfaceContainerHigh,
                borderWidth = 0.5.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.demo_glass_showcase_switch_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = stringResource(
                                    if (switchChecked) {
                                        R.string.demo_glass_showcase_switch_state_on
                                    } else {
                                        R.string.demo_glass_showcase_switch_state_off
                                    }
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                        GlassSwitch(
                            checked = switchChecked,
                            onCheckedChange = { switchChecked = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colorScheme.onPrimary,
                                checkedIconColor = colorScheme.onPrimary,
                                uncheckedThumbColor = colorScheme.onSurface,
                                uncheckedIconColor = colorScheme.onSurface,
                            ),
                            style = selectedMode.style,
                            checkedGlassColor = colorScheme.primaryContainer,
                            uncheckedGlassColor = colorScheme.surfaceContainerHighest,
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassFilterChip(
                            selected = primaryChipSelected,
                            onClick = { primaryChipSelected = !primaryChipSelected },
                            label = {
                                Text(text = stringResource(R.string.demo_glass_showcase_chip_primary))
                            },
                            selectedColor = colorScheme.onSecondaryContainer,
                            glassContainerColor = colorScheme.surfaceContainerHighest,
                            glassSelectedContainerColor = colorScheme.primaryContainer,
                            style = if (primaryChipSelected) GlassStyle.Medium else GlassStyle.Darker,
                        )
                        GlassFilterChip(
                            selected = secondaryChipSelected,
                            onClick = { secondaryChipSelected = !secondaryChipSelected },
                            label = {
                                Text(text = stringResource(R.string.demo_glass_showcase_chip_secondary))
                            },
                            selectedColor = colorScheme.onTertiaryContainer,
                            glassContainerColor = colorScheme.surfaceContainerHighest,
                            glassSelectedContainerColor = colorScheme.secondaryContainer,
                            style = if (secondaryChipSelected) GlassStyle.Medium else GlassStyle.Darker,
                        )
                    }
                }
            }
        }

        item {
            ShowcaseSectionHeader(
                title = stringResource(R.string.demo_glass_showcase_input_title),
                description = stringResource(R.string.demo_glass_showcase_input_desc),
            )
        }

        item {
            GlassTextFieldContainer(
                modifier = Modifier.fillMaxWidth(),
                isFocused = isInputFocused,
                style = selectedMode.style,
                shape = RoundedCornerShape(22.dp),
                color = colorScheme.surfaceContainerHigh,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Search,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(12.dp))
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { isInputFocused = it.isFocused },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = colorScheme.onSurface,
                    ),
                    cursorBrush = SolidColor(colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (inputText.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.demo_glass_showcase_input_placeholder),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colorScheme.onSurfaceVariant,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ColoredGlassComparisonSection(
    style: GlassStyle,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ColoredGlassSurface(
            modifier = Modifier.weight(1f),
            color = colorScheme.primaryContainer,
            style = style,
            shape = RoundedCornerShape(24.dp),
        ) {
            ColoredMaterialContent(
                title = stringResource(R.string.demo_glass_showcase_colored_plain_title),
                body = stringResource(R.string.demo_glass_showcase_colored_plain_body),
                contentColor = colorScheme.onPrimaryContainer,
            )
        }
        ColoredLiquidGlassSurface(
            modifier = Modifier.weight(1f),
            color = colorScheme.tertiaryContainer,
            style = style,
            shape = RoundedCornerShape(24.dp),
        ) {
            ColoredMaterialContent(
                title = stringResource(R.string.demo_glass_showcase_colored_liquid_title),
                body = stringResource(R.string.demo_glass_showcase_colored_liquid_body),
                contentColor = colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun ColoredMaterialContent(
    title: String,
    body: String,
    contentColor: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor.copy(alpha = 0.76f),
        )
    }
}

@Composable
private fun ShowcaseIntroCard(
    title: String,
    description: String,
    style: GlassStyle,
) {
    val colorScheme = MaterialTheme.colorScheme
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = style,
        shape = RoundedCornerShape(28.dp),
        color = colorScheme.surfaceContainerHigh,
        borderWidth = 0.5.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ShowcaseSectionHeader(
    title: String,
    description: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ShowcaseCardContent(
    title: String,
    body: String,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        trailing?.invoke()
    }
}

@Composable
private fun TintValidationSurface(
    title: String,
    color: Color,
    contentColor: Color,
    style: GlassStyle,
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = style,
        shape = RoundedCornerShape(20.dp),
        color = color,
        borderWidth = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
            )
        }
    }
}

