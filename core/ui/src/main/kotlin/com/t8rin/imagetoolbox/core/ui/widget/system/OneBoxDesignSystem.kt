package com.t8rin.imagetoolbox.core.ui.widget.system

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTextFieldVisualPreset
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassMedium

@Immutable
object OneBoxDesignSystem {
    val screenPadding: Dp = 16.dp
    val screenTopSpacing: Dp = 12.dp
    val sectionSpacing: Dp = 20.dp
    val blockSpacing: Dp = 16.dp
    val itemSpacing: Dp = 12.dp
    val compactSpacing: Dp = 8.dp
    val microSpacing: Dp = 4.dp

    val cardPadding: Dp = 16.dp
    val listItemHorizontalPadding: Dp = 12.dp
    val listItemVerticalPadding: Dp = 16.dp

    val smallRadius: Dp = 12.dp
    val mediumRadius: Dp = 16.dp
    val largeRadius: Dp = 20.dp
    val pillRadius: Dp = 50.dp

    val sectionCardShape: Shape = RoundedCornerShape(largeRadius)
    val listRowShape: Shape = RoundedCornerShape(mediumRadius)
    val compactBadgeShape: Shape = RoundedCornerShape(smallRadius)
    val pillShape: Shape = RoundedCornerShape(pillRadius)

    val toolbarGlassStyle: GlassStyle = GlassStyle.Medium
    val navigationGlassStyle: GlassStyle = GlassStyle.Regular
    val drawerGlassStyle: GlassStyle = GlassStyle.Medium
    val sectionGlassStyle: GlassStyle = GlassStyle.Regular
    val rowGlassStyle: GlassStyle = GlassStyle.Thin
}

@Composable
fun OneBoxSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (!supporting.isNullOrBlank()) {
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun OneBoxSectionCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(OneBoxDesignSystem.cardPadding),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = OneBoxDesignSystem.sectionCardShape,
        containerAlpha = 0.22f,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}

@Composable
fun OneBoxLeadingIconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 40.dp, minWidth = 40.dp)
            .glassMedium(
                color = containerColor,
                shape = OneBoxDesignSystem.compactBadgeShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(20.dp),
            tint = iconTint,
        )
    }
}

@Composable
fun OneBoxThemedIconBadge(
    icon: ImageVector,
    themeIndex: Int,
    modifier: Modifier = Modifier,
) {
    val (containerColor, iconTint) = when (themeIndex % 4) {
        0 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        1 -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        2 -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainer to MaterialTheme.colorScheme.onSurfaceVariant
    }
    OneBoxLeadingIconBadge(
        icon = icon,
        modifier = modifier,
        containerColor = containerColor,
        iconTint = iconTint
    )
}

@Composable
fun OneBoxListItem(
    headlineContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    colors: ListItemColors = ListItemDefaults.colors(),
    subtitle: (@Composable ColumnScope.() -> Unit)? = null,
    supportingContent: (@Composable ColumnScope.() -> Unit)? = null,
    leadingContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    contained: Boolean = false,
    selected: Boolean = false,
    shape: Shape = OneBoxDesignSystem.listRowShape,
) {
    val contentModifier = Modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = 64.dp)
        .padding(
            horizontal = OneBoxDesignSystem.listItemHorizontalPadding,
            vertical = OneBoxDesignSystem.listItemVerticalPadding,
        )

    val content: @Composable RowScope.() -> Unit = {
        if (leadingContent != null) {
            Box(content = leadingContent)
        }
        if (leadingContent != null) {
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.size(OneBoxDesignSystem.compactSpacing)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
        ) {
            headlineContent()
            subtitle?.invoke(this)
            supportingContent?.invoke(this)
        }
        if (trailingContent != null) {
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.size(OneBoxDesignSystem.compactSpacing)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
                verticalAlignment = Alignment.CenterVertically,
                content = trailingContent,
            )
        }
    }

    if (contained) {
        GlassSurface(
            modifier = modifier.fillMaxWidth(),
            onClick = onClick ?: {},
            enabled = onClick != null,
            style = if (selected) GlassStyle.Regular else OneBoxDesignSystem.rowGlassStyle,
            shape = shape,
            color = if (selected) {
                colors.selectedContentColor
            } else {
                MaterialTheme.colorScheme.surfaceContainerLowest
                colors.containerColor
            },
            borderWidth = 0.dp,
        ) {
            Row(
                modifier = contentModifier,
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClick,
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(
                    horizontal = OneBoxDesignSystem.listItemHorizontalPadding,
                    vertical = OneBoxDesignSystem.listItemVerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
fun OneBoxGroupDivider(
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    )
}

@Composable
fun OneBoxOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    readOnly: Boolean = false,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    textStyle: androidx.compose.ui.text.TextStyle = androidx.compose.material3.LocalTextStyle.current,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions =
        androidx.compose.foundation.text.KeyboardOptions.Default,
) {
    GlassOutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        supportingText = supportingText,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        readOnly = readOnly,
        singleLine = singleLine,
        textStyle = textStyle,
        maxLines = maxLines,
        minLines = minLines,
        colors = AppTheme.colors.getOutlinedTextFieldColors(),
        visualPreset = GlassTextFieldVisualPreset.Quiet,
        shape = OneBoxDesignSystem.listRowShape,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
    )
}

@Composable
fun OnePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    EnhancedButton(
        onClick = onClick,
        modifier = modifier.widthIn(min = 96.dp),
        enabled = enabled,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = OneBoxDesignSystem.listRowShape,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.size(OneBoxDesignSystem.compactSpacing)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun OneSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    EnhancedButton(
        onClick = onClick,
        modifier = modifier.widthIn(min = 96.dp),
        enabled = enabled,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = OneBoxDesignSystem.listRowShape,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.size(OneBoxDesignSystem.compactSpacing)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun OneBoxDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    EnhancedButton(
        onClick = onClick,
        modifier = modifier.widthIn(min = 96.dp),
        enabled = enabled,
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = OneBoxDesignSystem.listRowShape,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.size(OneBoxDesignSystem.compactSpacing)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun OneBoxBottomActionBar(
    primaryText: String,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryText: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    primaryEnabled: Boolean = true,
    secondaryEnabled: Boolean = true,
    extraActions: (@Composable RowScope.() -> Unit)? = null,
) {
    GlassSurface(
        modifier = modifier
            .fillMaxWidth(),
        style = OneBoxDesignSystem.navigationGlassStyle,
        shape = RoundedCornerShape(
            topStart = OneBoxDesignSystem.largeRadius,
            topEnd = OneBoxDesignSystem.largeRadius,
        ),
        color = MaterialTheme.colorScheme.surface,
        borderWidth = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = OneBoxDesignSystem.screenPadding,
                    vertical = OneBoxDesignSystem.compactSpacing,
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                extraActions?.invoke(this)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!secondaryText.isNullOrBlank() && onSecondaryClick != null) {
                    OneSecondaryButton(
                        text = secondaryText,
                        onClick = onSecondaryClick,
                        enabled = secondaryEnabled,
                    )
                }
                OnePrimaryButton(
                    text = primaryText,
                    onClick = onPrimaryClick,
                    enabled = primaryEnabled,
                )
            }
        }
    }
}
