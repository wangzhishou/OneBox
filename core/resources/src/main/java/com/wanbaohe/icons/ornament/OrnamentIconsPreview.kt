package com.wanbaohe.icons.ornament

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons

@Preview(apiLevel = 36)
@Composable
private fun OrnamentIconsPreview() {
    Column(
        modifier = Modifier
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(Icons.Filled.OrnamentSealRed, null, Modifier.size(96.dp, 88.94.dp))
            Image(Icons.Filled.OrnamentSealGreen, null, Modifier.size(96.dp, 89.33.dp))
            Image(Icons.Filled.OrnamentChineseKnot, null, Modifier.size(96.dp))
        }
        Image(Icons.Filled.OrnamentCloudRed, null, Modifier.size(96.dp, 41.04.dp))

        // 回纹角:一个左上资产 + 翻转/旋转复用四角
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(Icons.Filled.OrnamentCornerTlRed, null, Modifier.size(96.dp, 59.07.dp))
            Image(
                Icons.Filled.OrnamentCornerTlRed, null,
                Modifier.size(96.dp, 59.07.dp).graphicsLayer { scaleX = -1f },
            )
            Image(
                Icons.Filled.OrnamentCornerTlRed, null,
                Modifier.size(96.dp, 59.07.dp).graphicsLayer { rotationZ = 180f },
            )
            Image(
                Icons.Filled.OrnamentCornerTlRed, null,
                Modifier.size(96.dp, 59.07.dp).graphicsLayer { scaleY = -1f },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(Icons.Filled.OrnamentCornerTlGray, null, Modifier.size(96.dp, 59.07.dp))
        }

        // 回纹线:233x47 无缝平铺单元,横向 repeat
        Row {
            repeat(6) {
                Image(Icons.Filled.OrnamentMeanderTile, null, Modifier.size(96.dp, 19.36.dp))
            }
        }
        // 竖向:旋转 90 度后 repeat
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(3) {
                Image(
                    Icons.Filled.OrnamentMeanderTile, null,
                    Modifier
                        .width(19.36.dp)
                        .height(96.dp)
                        .graphicsLayer { rotationZ = 90f },
                )
            }
            Box(Modifier.size(8.dp))
            Image(Icons.Filled.OrnamentMeanderTile, null, Modifier.size(96.dp, 19.36.dp))
        }
    }
}
