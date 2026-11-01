package com.wanbaohe.textcard.presentation.editor.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.glassBackground
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.TextCardRole

@Composable
fun ContentPanel(
    states: Map<TextCardRole, TextFieldState>,
    selectedRole: TextCardRole?,
    onFocusRole: (TextCardRole) -> Unit,
) {
    val role = TextCardRole.Body
    val state = states.getValue(role)
    var lastSelection by remember(state) { mutableStateOf(state.selection) }
    LaunchedEffect(state) {
        snapshotFlow { state.selection }.collect { lastSelection = it }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.textcard_content_hint),
            style = MaterialTheme.typography.bodySmall,
            color = if (role == selectedRole) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else MaterialTheme.colorScheme.onSurfaceVariant
        )
        BasicTextField(
            state = state,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focus ->
                    if (focus.hasFocus) onFocusRole(role)
                }
                .glassBackground(
                    style = GlassStyle.Thin,
                    shape = ShapeDefaults.default
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.textcard_character_count, state.text.length),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = {
                    val start = lastSelection.min.coerceIn(0, state.text.length)
                    val end = lastSelection.max.coerceIn(0, state.text.length)
                    state.edit { replace(start, end, "\u000C") }
                }
            ) {
                Text(stringResource(R.string.textcard_insert_page_break))
            }
        }
    }
}
