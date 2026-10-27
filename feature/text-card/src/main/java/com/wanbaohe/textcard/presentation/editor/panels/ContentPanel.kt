package com.wanbaohe.textcard.presentation.editor.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextCardRole.entries.forEach { role ->
            val state = states.getValue(role)
            Column {
                Text(
                    text = stringResource(role.labelRes()),
                    style = MaterialTheme.typography.labelMedium,
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
                if (role == TextCardRole.Body) {
                    Text(
                        text = stringResource(R.string.textcard_character_count, state.text.length),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
