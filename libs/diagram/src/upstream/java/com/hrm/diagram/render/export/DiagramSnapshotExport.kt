package com.hrm.diagram.render.export

import com.hrm.diagram.core.DiagramApi
import com.hrm.diagram.core.draw.Color
import com.hrm.diagram.core.draw.Point
import com.hrm.diagram.core.draw.Rect
import com.hrm.diagram.core.draw.Size
import com.hrm.diagram.core.export.ExportBackground
import com.hrm.diagram.core.export.JpegExportOptions
import com.hrm.diagram.core.export.RasterExportOptions
import com.hrm.diagram.core.export.RenderedDiagram
import com.hrm.diagram.core.export.SvgExportOptions
import com.hrm.diagram.core.export.exportJpeg
import com.hrm.diagram.core.export.exportPng
import com.hrm.diagram.core.export.svg.exportSvg
import com.hrm.diagram.render.streaming.DiagramSnapshot

/**
 * Convert a streaming snapshot into the stable export payload consumed by core export backends.
 *
 * Minimal usage:
 * ```kotlin
 * val rendered = snapshot.prepareExport()
 * ```
 */
@DiagramApi
fun DiagramSnapshot.prepareExport(
    background: ExportBackground = ExportBackground.Auto,
): RenderedDiagram = RenderedDiagram(
    bounds = laidOut?.bounds ?: Rect(Point.Zero, Size.Zero),
    drawCommands = drawCommands,
    background = resolveSnapshotBackground(background),
)

/**
 * Convenience SVG export for the current streaming frame.
 *
 * Minimal usage:
 * ```kotlin
 * val svg = snapshot.toSvg()
 * ```
 */
@DiagramApi
fun DiagramSnapshot.toSvg(
    options: SvgExportOptions = SvgExportOptions(),
): String = prepareExport(background = options.background).exportSvg(options).value

@DiagramApi
suspend fun DiagramSnapshot.toPng(
    options: RasterExportOptions = RasterExportOptions(),
): ByteArray = prepareExport(background = options.background).exportPng(options).value

@DiagramApi
suspend fun DiagramSnapshot.toJpeg(
    options: JpegExportOptions = JpegExportOptions(),
): ByteArray = prepareExport(background = options.background).exportJpeg(options).value

private fun DiagramSnapshot.resolveSnapshotBackground(
    background: ExportBackground,
): Color? =
    when (background) {
        ExportBackground.Auto -> null
        ExportBackground.Transparent -> null
        is ExportBackground.Solid -> background.color
    }
