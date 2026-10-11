package com.wanbaohe.boardgame.export

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.domain.model.IntegerSize
import com.t8rin.imagetoolbox.core.ui.utils.capturable.CaptureController
import com.t8rin.imagetoolbox.core.ui.utils.capturable.capturable
import com.wanbaohe.boardgame.R
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import kotlin.math.roundToInt

internal class BoardGifRenderRequest<Position>(
    val frame: BoardGifFrame<Position>,
    val rendered: CompletableDeferred<Unit> = CompletableDeferred(),
)

internal suspend fun <Position> captureBoardFrame(
    frame: BoardGifFrame<Position>,
    outputSize: IntegerSize,
    controller: CaptureController,
    requestRender: (BoardGifRenderRequest<Position>) -> Unit,
): Bitmap {
    var softwareBitmap: Bitmap? = null
    var transferred = false
    try {
        withContext(Dispatchers.Main.immediate) {
            val request = BoardGifRenderRequest(frame)
            requestRender(request)
            try {
                withTimeout(15_000) {
                    // Await the draw for this exact frame, not just the next composition/frame clock.
                    request.rendered.await()
                    val captured = controller.bitmap()
                    try {
                        if (captured.width != outputSize.width || captured.height != outputSize.height) {
                            throw IOException("The captured board does not match the requested pixel dimensions")
                        }
                        softwareBitmap = captured.copy(Bitmap.Config.ARGB_8888, false)
                            ?: throw IOException("Cannot copy the rendered board")
                        softwareBitmap?.setHasAlpha(false)
                    } finally {
                        captured.recycle()
                    }
                }
            } catch (exception: TimeoutCancellationException) {
                throw IOException("The board export canvas did not render in time", exception)
            }
        }
        currentCoroutineContext().ensureActive()
        return requireNotNull(softwareBitmap).also { transferred = true }
    } finally {
        if (!transferred) softwareBitmap?.recycle()
    }
}

@Composable
internal fun <Position> BoardGifCaptureHost(
    plan: BoardGifPlan<Position>,
    frame: BoardGifFrame<Position>,
    request: BoardGifRenderRequest<Position>?,
    controller: CaptureController,
    modifier: Modifier,
    boardContent: @Composable (BoardGifFrame<Position>, Modifier) -> Unit,
) {
    val settings = LocalSettingsState.current
    val exportSettings = remember(settings) {
        settings.copy(
            isGlassAlphaEnabled = false,
            isLiquidGlassEnabled = false,
            isMeshGradientBackgroundEnabled = false,
            customBackgroundImageUri = null,
            glassBaseAlpha = 1f,
            drawContainerShadows = false,
        )
    }
    Layout(
        modifier = modifier,
        content = {
            CompositionLocalProvider(
                LocalDensity provides Density(plan.resolution.widthPixels / BoardGifPlan.LOGICAL_WIDTH, 1f),
                LocalLayoutDirection provides LayoutDirection.Ltr,
                LocalSettingsState provides exportSettings,
            ) {
                MaterialTheme(
                    colorScheme = ExportColors,
                    typography = ExportTypography,
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                drawContent()
                                request?.rendered?.complete(Unit)
                            }
                            .capturable(controller)
                            .background(ExportColors.background),
                    ) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .height(BoardGifPlan.HEADER_HEIGHT.dp)
                                .padding(horizontal = 14.dp, vertical = 5.dp),
                        ) {
                            Text(
                                text = plan.snapshot.title,
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = if (plan.isStatic) stringResource(R.string.boardgame_export_position) else {
                                    stringResource(
                                        R.string.boardgame_export_move_range,
                                        plan.snapshot.moves[plan.range.firstMoveIndex].ply,
                                        plan.snapshot.moves[plan.range.lastMoveIndex].ply,
                                    )
                                },
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(((BoardGifPlan.LOGICAL_WIDTH - BoardGifPlan.BOARD_PADDING * 2) / plan.boardAspectRatio).dp)
                                .padding(horizontal = BoardGifPlan.BOARD_PADDING.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            boardContent(frame, Modifier.fillMaxSize())
                        }
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .height(BoardGifPlan.FOOTER_HEIGHT.dp)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = frame.move?.let {
                                    stringResource(R.string.boardgame_export_move_label, it.ply, it.moverLabel, it.notation)
                                } ?: stringResource(R.string.boardgame_export_initial_position),
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (frame.isLast && !plan.isStatic) {
                                Text(
                                    text = if (!plan.isWholeGame) {
                                        stringResource(R.string.boardgame_export_clip_end)
                                    } else plan.snapshot.resultText.ifBlank {
                                        stringResource(R.string.boardgame_export_replay_end)
                                    },
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { measurables, constraints ->
        val child = measurables.single().measure(Constraints.fixed(plan.size.width, plan.size.height))
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val scale = minOf(width.toFloat() / child.width, height.toFloat() / child.height)
        layout(width, height) {
            // Preview scaling is outside the captured layer; exported pixels do not depend on screen size.
            child.placeWithLayer(
                x = ((width - child.width * scale) / 2).roundToInt(),
                y = ((height - child.height * scale) / 2).roundToInt(),
            ) {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
        }
    }
}

private val ExportTypography = Typography()
private val ExportColors = lightColorScheme(
    primary = Color(0xFF12656D),
    primaryContainer = Color(0xFFCDEAE8),
    onPrimaryContainer = Color(0xFF0B4F56),
    tertiary = Color(0xFF12656D),
    background = Color(0xFFFFFCF5),
    surface = Color(0xFFFFFCF5),
    surfaceContainerLow = Color(0xFFF3E8CE),
    onSurface = Color(0xFF222222),
    onSurfaceVariant = Color(0xFF515151),
    outlineVariant = Color(0xFF9D9179),
    error = Color(0xFFB3261E),
)
