package com.wanbaohe.iching.ui.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.icons.TaijiBagua

@Preview(showBackground = true, apiLevel = 36)
@Composable
private fun IChingIconsPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(
            "Qian" to CoinFrontQian,
            "Kun" to CoinFrontKun,
            "Li" to CoinFrontLi,
            "Back" to CoinBackRipple,
            "Taiji" to com.t8rin.imagetoolbox.core.resources.Icons.Outlined.TaijiBagua,
        ).forEach { (label, icon) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(96.dp),
                )
                Text(label)
            }
        }
    }
}
