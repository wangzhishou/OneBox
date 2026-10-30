package com.wanbaohe.mahjong.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.mahjong.R
import com.wanbaohe.mahjong.domain.model.TileSuit
import com.wanbaohe.mahjong.domain.model.Tiles

private val TileFace = Color(0xFFFFFDF6)
private val TileEdge = Color(0xFFCFC5B2)

@Composable
fun MahjongTile(
    tileId: Int,
    width: Dp,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val kind = Tiles.kindOf(tileId)
    val face = tileFace(kind)
    val description = tileDescription(kind)
    val shape = RoundedCornerShape((width.value * 0.14f).dp)
    val interaction = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Box(
        modifier = modifier
            .offset(y = if (selected) (-7).dp else 0.dp)
            .width(width)
            .height(width * 1.34f)
            .semantics { contentDescription = description }
            .then(interaction)
            .clip(shape)
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else TileFace)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else TileEdge,
                shape = shape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (face.secondary == null) {
            Text(
                text = face.primary,
                color = face.color,
                fontSize = (width.value * 0.54f).sp,
                fontWeight = FontWeight.Bold,
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = face.primary,
                    color = face.color,
                    fontSize = (width.value * 0.52f).sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = (width.value * 0.54f).sp,
                )
                Text(
                    text = face.secondary,
                    color = face.color,
                    fontSize = (width.value * 0.27f).sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = (width.value * 0.29f).sp,
                )
            }
        }
    }
}

@Composable
fun MahjongTileBack(width: Dp, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape((width.value * 0.14f).dp)
    Box(
        modifier = modifier
            .width(width)
            .height(width * 1.34f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.onPrimaryContainer, shape),
    )
}

@Composable
fun tileDescription(kind: Int): String = when (kind) {
    in 0..26 -> stringResource(
        R.string.mahjong_tile_number,
        Tiles.rankOfKind(kind),
        stringResource(
            when (Tiles.suitOfKind(kind)) {
                TileSuit.CHARACTERS -> R.string.mahjong_suit_characters
                TileSuit.BAMBOO -> R.string.mahjong_suit_bamboo
                TileSuit.DOTS -> R.string.mahjong_suit_dots
                else -> R.string.mahjong_suit_characters
            }
        ),
    )
    27 -> stringResource(R.string.mahjong_wind_east)
    28 -> stringResource(R.string.mahjong_wind_south)
    29 -> stringResource(R.string.mahjong_wind_west)
    30 -> stringResource(R.string.mahjong_wind_north)
    31 -> stringResource(R.string.mahjong_dragon_red)
    32 -> stringResource(R.string.mahjong_dragon_green)
    else -> stringResource(R.string.mahjong_dragon_white)
}

fun tileSequenceText(start: Int): String =
    (start..start + 2).joinToString(" ") { tileFace(it).primary }

private data class TileFaceParts(
    val primary: String,
    val secondary: String?,
    val color: Color,
)

private fun tileFace(kind: Int): TileFaceParts {
    if (kind in 0..26) {
        val secondary = when (Tiles.suitOfKind(kind)) {
            TileSuit.CHARACTERS -> "萬"
            TileSuit.BAMBOO -> "條"
            else -> "筒"
        }
        val color = when (Tiles.suitOfKind(kind)) {
            TileSuit.CHARACTERS -> Color(0xFF8E3038)
            TileSuit.BAMBOO -> Color(0xFF237052)
            else -> Color(0xFF285B8D)
        }
        return TileFaceParts(Tiles.rankOfKind(kind).toString(), secondary, color)
    }
    return when (kind) {
        27 -> TileFaceParts("東", null, Color(0xFF30343B))
        28 -> TileFaceParts("南", null, Color(0xFF30343B))
        29 -> TileFaceParts("西", null, Color(0xFF30343B))
        30 -> TileFaceParts("北", null, Color(0xFF30343B))
        31 -> TileFaceParts("中", null, Color(0xFFC3333D))
        32 -> TileFaceParts("發", null, Color(0xFF237052))
        else -> TileFaceParts("白", null, Color(0xFF54728C))
    }
}
