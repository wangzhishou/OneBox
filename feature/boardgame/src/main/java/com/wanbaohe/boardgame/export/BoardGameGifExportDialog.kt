package com.wanbaohe.boardgame.export

import android.content.ActivityNotFoundException
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGpsFixed
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePlay
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSpeedTest
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTune
import com.t8rin.imagetoolbox.core.resources.icons.line.LineZoomIn
import com.t8rin.imagetoolbox.core.ui.utils.capturable.rememberCaptureController
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCustomRangeSlider
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCustomSlider
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassLinearProgressIndicator
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTextButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.sheets.ZoomModalSheet
import com.t8rin.logger.makeLog
import com.wanbaohe.boardgame.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.IOException
import java.text.NumberFormat
import kotlin.math.roundToInt

private enum class BoardGifRangeMode { Whole, Nearby, Custom }

fun <Position> prepareBoardReplayExport(
    snapshot: () -> BoardReplaySnapshot<Position>,
): BoardReplaySnapshot<Position>? = try {
    snapshot()
} catch (exception: IllegalArgumentException) {
    exception.makeLog("BoardGameGifSnapshot")
    AppToastHost.showFailureToast(R.string.boardgame_export_invalid_replay)
    null
} catch (exception: IllegalStateException) {
    exception.makeLog("BoardGameGifSnapshot")
    AppToastHost.showFailureToast(R.string.boardgame_export_invalid_replay)
    null
}

@Composable
fun <Position> BoardGameGifExportDialog(
    snapshot: BoardReplaySnapshot<Position>,
    exporter: BoardGameGifExporter,
    filenamePrefix: String,
    boardAspectRatio: Float = 1f,
    onDismiss: () -> Unit,
    boardContent: @Composable (BoardGifFrame<Position>, Modifier) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val controller = rememberCaptureController()
    val optionsScrollState = rememberScrollState()
    var rangeMode by remember(snapshot) {
        mutableStateOf(
            if (snapshot.moves.size > BoardReplaySnapshot.LONG_GAME_MOVE_COUNT) {
                BoardGifRangeMode.Nearby
            } else BoardGifRangeMode.Whole,
        )
    }
    var range by remember(snapshot) {
        mutableStateOf(
            if (rangeMode == BoardGifRangeMode.Nearby) snapshot.rangeNearCurrent() else snapshot.wholeRange,
        )
    }
    var firstText by remember { mutableStateOf(snapshot.moves.getOrNull(range.firstMoveIndex)?.ply?.toString().orEmpty()) }
    var lastText by remember { mutableStateOf(snapshot.moves.getOrNull(range.lastMoveIndex)?.ply?.toString().orEmpty()) }
    var speed by remember { mutableStateOf(BoardGifSpeed.Standard) }
    var resolution by remember { mutableStateOf(BoardGifResolution.Standard) }
    val plan = remember(snapshot, range, speed, resolution, boardAspectRatio) {
        BoardGifPlan(snapshot, range, speed, resolution, boardAspectRatio)
    }
    var previewIndex by remember(plan) { mutableIntStateOf(0) }
    var request by remember { mutableStateOf<BoardGifRenderRequest<Position>?>(null) }
    var result by remember { mutableStateOf<BoardGameExportFile?>(null) }
    var pendingSave by remember { mutableStateOf<BoardGameExportFile?>(null) }
    var generationJob by remember { mutableStateOf<Job?>(null) }
    var generating by remember { mutableStateOf(false) }
    var cancelling by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var completedFrames by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var previewReady by remember(result) { mutableStateOf(false) }
    var showOriginalPreview by remember(result) { mutableStateOf(false) }
    val previewRequest = remember(context, result) {
        result?.let { file ->
            ImageRequest.Builder(context)
                .data(file.uri)
                .size(Size.ORIGINAL)
                .crossfade(false)
                .build()
        }
    }
    val busy = generating || saving
    val firstCustomIndex = snapshot.moves.indexOfFirst { it.ply == firstText.toIntOrNull() }
    val lastCustomIndex = snapshot.moves.indexOfFirst { it.ply == lastText.toIntOrNull() }
    val customRangeValid = firstCustomIndex >= 0 && lastCustomIndex >= firstCustomIndex
    val selectionValid = plan.isStatic || rangeMode != BoardGifRangeMode.Custom || customRangeValid
    val numberFormat = remember { NumberFormat.getNumberInstance().apply { maximumFractionDigits = 1 } }
    val owner = LocalLifecycleOwner.current

    fun reportFailure(exception: Throwable, messageResource: Int) {
        exception.makeLog("BoardGameGifExport")
        errorMessage = context.getString(messageResource)
    }

    fun updateCustomRange() {
        val first = snapshot.moves.indexOfFirst { it.ply == firstText.toIntOrNull() }
        val last = snapshot.moves.indexOfFirst { it.ply == lastText.toIntOrNull() }
        if (first >= 0 && last >= first) range = BoardGifRange(first, last)
    }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(result?.mimeType ?: "image/gif"),
    ) { uri ->
        val file = pendingSave
        pendingSave = null
        if (uri != null && file != null) {
            saving = true
            errorMessage = null
            scope.launch {
                try {
                    exporter.save(file, uri)
                    AppToastHost.showToast(R.string.boardgame_export_saved)
                } catch (exception: IOException) {
                    reportFailure(exception, R.string.boardgame_export_save_failed)
                } catch (exception: SecurityException) {
                    reportFailure(exception, R.string.boardgame_export_save_failed)
                } catch (exception: IllegalArgumentException) {
                    reportFailure(exception, R.string.boardgame_export_save_failed)
                } finally {
                    saving = false
                }
            }
        }
    }

    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) generationJob?.cancel()
        }
        owner.lifecycle.addObserver(observer)
        onDispose {
            generationJob?.cancel()
            owner.lifecycle.removeObserver(observer)
        }
    }

    fun generate() {
        generating = true
        cancelling = false
        completedFrames = 0
        errorMessage = null
        generationJob = scope.launch {
            try {
                optionsScrollState.scrollTo(0)
                result = exporter.generate(
                    plan = plan,
                    filenamePrefix = filenamePrefix,
                    frameProvider = { index ->
                        captureBoardFrame(plan.frameAt(index), plan.size, controller) { request = it }
                    },
                    onProgress = { count ->
                        withContext(Dispatchers.Main.immediate) { completedFrames = count }
                    },
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IOException) {
                reportFailure(exception, R.string.boardgame_export_failed)
            } catch (exception: IllegalStateException) {
                reportFailure(exception, R.string.boardgame_export_failed)
            } catch (exception: IllegalArgumentException) {
                reportFailure(exception, R.string.boardgame_export_invalid_replay)
            } catch (exception: SecurityException) {
                reportFailure(exception, R.string.boardgame_export_failed)
            } finally {
                request = null
                generating = false
                cancelling = false
                generationJob = null
            }
        }
    }

    fun share(file: BoardGameExportFile) {
        scope.launch {
            try {
                exporter.share(file)
            } catch (exception: ActivityNotFoundException) {
                reportFailure(exception, R.string.boardgame_export_share_failed)
            } catch (exception: SecurityException) {
                reportFailure(exception, R.string.boardgame_export_share_failed)
            }
        }
    }

    fun save(file: BoardGameExportFile) {
        pendingSave = file
        try {
            saveLauncher.launch(file.filename)
        } catch (exception: ActivityNotFoundException) {
            pendingSave = null
            reportFailure(exception, R.string.boardgame_export_save_failed)
        }
    }

    EnhancedModalBottomSheet(
        visible = true,
        cancelable = !saving && !showOriginalPreview,
        onDismiss = { if (!it && !saving && !showOriginalPreview) onDismiss() },
        enableBackHandler = !showOriginalPreview,
        enableGlass = true,
        enableBottomContentWeight = true,
        nestedScrollEnabled = true,
        title = { Text(stringResource(R.string.boardgame_export_gif)) },
        confirmButton = {
            GlassTextButton(onClick = onDismiss, enabled = !saving) {
                Text(stringResource(R.string.boardgame_export_close))
            }
        },
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val previewHeight = minOf(180.dp, (LocalConfiguration.current.screenHeightDp * 0.25f).dp)
            val file = result
            BoardGifExportSection(
                title = stringResource(R.string.boardgame_export_board_preview),
                icon = Icons.Outlined.LineImage,
                trailing = {
                    if (file != null) {
                        GlassTonalIconButton(
                            onClick = { showOriginalPreview = true },
                            enabled = previewReady,
                        ) {
                            Icon(
                                Icons.Outlined.LineZoomIn,
                                stringResource(R.string.boardgame_export_original_preview),
                                Modifier.size(20.dp),
                            )
                        }
                    }
                },
            ) {
                val previewModifier = Modifier.fillMaxWidth().height(previewHeight)
                if (file == null) {
                    BoardGifCaptureHost(
                        plan = plan,
                        frame = request?.frame ?: plan.frameAt(previewIndex),
                        request = request,
                        controller = controller,
                        modifier = previewModifier,
                        boardContent = boardContent,
                    )
                } else {
                    key(file.uri) {
                        Picture(
                            model = previewRequest,
                            modifier = previewModifier,
                            contentDescription = stringResource(R.string.boardgame_export_preview),
                            contentScale = ContentScale.Fit,
                            filterQuality = FilterQuality.High,
                            crossfadeEnabled = false,
                            showTransparencyChecker = false,
                            onSuccess = { previewReady = true },
                            onError = {
                                reportFailure(it.result.throwable, R.string.boardgame_export_preview_failed)
                            },
                            error = { Text(stringResource(R.string.boardgame_export_preview_failed)) },
                        )
                    }
                }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f, false)
                    .verticalScroll(optionsScrollState, enabled = !generating),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (generating) {
                    BoardGifExportSection(
                        title = stringResource(R.string.boardgame_export_gif),
                        icon = Icons.Outlined.LinePlay,
                    ) {
                        GlassLinearProgressIndicator(
                            progress = { completedFrames.toFloat() / plan.frameCount },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                stringResource(
                                    if (cancelling) R.string.boardgame_export_cancelling else R.string.boardgame_export_progress,
                                    completedFrames,
                                    plan.frameCount,
                                ),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            GlassTextButton(
                                onClick = { cancelling = true; generationJob?.cancel() },
                                enabled = !cancelling,
                            ) { Text(stringResource(R.string.boardgame_export_cancel)) }
                        }
                    }
                }
                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (file != null) {
                    BoardGifExportSection(
                        title = stringResource(R.string.boardgame_export_size_title),
                        icon = Icons.Outlined.LineImage,
                    ) {
                        Text(
                            if (file.durationMillis == 0L) stringResource(
                                R.string.boardgame_export_image_info,
                                file.width,
                                file.height,
                                Formatter.formatShortFileSize(context, file.byteCount),
                            ) else stringResource(
                                R.string.boardgame_export_file_info,
                                file.width,
                                file.height,
                                Formatter.formatShortFileSize(context, file.byteCount),
                                numberFormat.format(file.durationMillis / 1000.0),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        GlassTextButton(
                            onClick = { showOriginalPreview = true },
                            enabled = previewReady,
                        ) {
                            Text(stringResource(R.string.boardgame_export_original_preview))
                        }
                        if (file.mimeType == "image/gif") Text(
                            stringResource(R.string.boardgame_export_share_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        GlassTextButton(
                            onClick = { result = null; errorMessage = null },
                            enabled = !busy && pendingSave == null,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(stringResource(R.string.boardgame_export_edit)) }
                    }
                } else if (!generating) {
                    if (plan.isStatic) {
                        Text(stringResource(R.string.boardgame_export_empty_hint))
                    } else {
                        BoardGifExportSection(
                            title = stringResource(R.string.boardgame_export_range_title),
                            icon = Icons.Outlined.LineGpsFixed,
                        ) {
                            BoardGifExportChoices(
                                options = BoardGifRangeMode.entries,
                                selectedOption = rangeMode,
                                onOptionSelected = { mode ->
                                    rangeMode = mode
                                    range = if (mode == BoardGifRangeMode.Whole) {
                                        snapshot.wholeRange
                                    } else if (mode == BoardGifRangeMode.Nearby) {
                                        snapshot.rangeNearCurrent()
                                    } else range
                                    firstText = snapshot.moves[range.firstMoveIndex].ply.toString()
                                    lastText = snapshot.moves[range.lastMoveIndex].ply.toString()
                                },
                                label = { mode ->
                                    stringResource(when (mode) {
                                        BoardGifRangeMode.Whole -> R.string.boardgame_export_whole
                                        BoardGifRangeMode.Nearby -> R.string.boardgame_export_nearby
                                        BoardGifRangeMode.Custom -> R.string.boardgame_export_custom
                                    })
                                },
                            )
                            if (rangeMode == BoardGifRangeMode.Custom) {
                                if (snapshot.moves.size > 1) {
                                    GlassCustomRangeSlider(
                                        modifier = Modifier.fillMaxWidth().systemGestureExclusion(),
                                        value = range.firstMoveIndex.toFloat()..range.lastMoveIndex.toFloat(),
                                        onValueChange = {
                                            range = BoardGifRange(it.start.roundToInt(), it.endInclusive.roundToInt())
                                            firstText = snapshot.moves[range.firstMoveIndex].ply.toString()
                                            lastText = snapshot.moves[range.lastMoveIndex].ply.toString()
                                        },
                                        valueRange = 0f..snapshot.moves.lastIndex.toFloat(),
                                        steps = (snapshot.moves.size - 2).coerceAtLeast(0),
                                        trackHeight = 6.dp,
                                        thumbSize = DpSize(24.dp, 24.dp),
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    GlassOutlinedTextField(
                                        value = firstText,
                                        onValueChange = { firstText = it; updateCustomRange() },
                                        label = { Text(stringResource(R.string.boardgame_export_first_move)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        isError = !customRangeValid,
                                        modifier = Modifier.weight(1f),
                                    )
                                    GlassOutlinedTextField(
                                        value = lastText,
                                        onValueChange = { lastText = it; updateCustomRange() },
                                        label = { Text(stringResource(R.string.boardgame_export_last_move)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        isError = !customRangeValid,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                if (!customRangeValid) {
                                    Text(stringResource(R.string.boardgame_export_invalid_range), color = MaterialTheme.colorScheme.error)
                                }
                            }
                            Text(
                                stringResource(
                                    R.string.boardgame_export_selection,
                                    snapshot.moves[range.firstMoveIndex].ply,
                                    snapshot.moves[range.lastMoveIndex].ply,
                                    range.moveCount,
                                    numberFormat.format(plan.durationMillis / 1000.0),
                                ),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            if (snapshot.moves.size > BoardReplaySnapshot.LONG_GAME_MOVE_COUNT) {
                                Text(
                                    stringResource(R.string.boardgame_export_long_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        BoardGifExportSection(
                            title = stringResource(R.string.boardgame_export_speed_title),
                            icon = Icons.Outlined.LineSpeedTest,
                        ) {
                            BoardGifExportChoices(
                                options = BoardGifSpeed.entries,
                                selectedOption = speed,
                                onOptionSelected = { speed = it },
                                label = { option ->
                                    stringResource(when (option) {
                                        BoardGifSpeed.Fast -> R.string.boardgame_export_fast
                                        BoardGifSpeed.Standard -> R.string.boardgame_export_standard
                                        BoardGifSpeed.Slow -> R.string.boardgame_export_slow
                                    })
                                },
                            )
                        }
                        BoardGifExportSection(
                            title = stringResource(R.string.boardgame_export_scrub_title),
                            icon = Icons.Outlined.LineTune,
                            trailing = {
                                Text(
                                    stringResource(R.string.boardgame_export_scrub_status, previewIndex, range.moveCount),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                        ) {
                            GlassCustomSlider(
                                modifier = Modifier.fillMaxWidth().systemGestureExclusion(),
                                value = previewIndex.toFloat(),
                                onValueChange = { previewIndex = it.roundToInt() },
                                valueRange = 0f..(plan.frameCount - 1).toFloat(),
                                steps = (plan.frameCount - 2).coerceAtLeast(0),
                                trackHeight = 6.dp,
                                thumbSize = DpSize(24.dp, 24.dp),
                            )
                        }
                    }
                    BoardGifExportSection(
                        title = stringResource(R.string.boardgame_export_size_title),
                        icon = Icons.Outlined.LineImage,
                    ) {
                        BoardGifExportChoices(
                            options = BoardGifResolution.entries,
                            selectedOption = resolution,
                            onOptionSelected = { resolution = it },
                            label = { option ->
                                stringResource(
                                    if (option == BoardGifResolution.Compact) R.string.boardgame_export_compact else R.string.boardgame_export_clear,
                                    option.widthPixels,
                                )
                            },
                        )
                        Text(
                            stringResource(R.string.boardgame_export_pixel_dimensions, plan.size.width, plan.size.height),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            stringResource(R.string.boardgame_export_resolution_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (file != null) {
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GlassTonalButton(
                        modifier = Modifier.weight(1f),
                        enabled = !busy && previewReady && pendingSave == null,
                        onClick = { share(file) },
                    ) { Text(stringResource(R.string.boardgame_export_share)) }
                    GlassTonalButton(
                        modifier = Modifier.weight(1f),
                        enabled = !busy && previewReady && pendingSave == null,
                        onClick = { save(file) },
                    ) {
                        Text(stringResource(if (saving) R.string.boardgame_export_saving else R.string.boardgame_export_save))
                    }
                }
            } else if (!generating) {
                GlassButton(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    enabled = selectionValid,
                    onClick = ::generate,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Icon(Icons.Outlined.LinePlay, null, Modifier.padding(end = 8.dp).size(20.dp))
                    Text(stringResource(if (plan.isStatic) R.string.boardgame_export_generate_image else R.string.boardgame_export_generate))
                }
            }
        }
    }
    ZoomModalSheet(
        data = previewRequest,
        visible = showOriginalPreview,
        onDismiss = { showOriginalPreview = false },
    )
}
