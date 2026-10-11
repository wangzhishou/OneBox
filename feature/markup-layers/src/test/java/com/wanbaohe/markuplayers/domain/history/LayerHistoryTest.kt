package com.wanbaohe.markuplayers.domain.history

import com.t8rin.imagetoolbox.core.filters.presentation.model.UiColorMatrix3x3Filter
import com.wanbaohe.markuplayers.data.project.MarkupDocumentCodec
import com.wanbaohe.markuplayers.domain.model.BrushType
import com.wanbaohe.markuplayers.domain.model.DrawStroke
import com.wanbaohe.markuplayers.domain.model.LayerEffect
import com.wanbaohe.markuplayers.domain.model.LayerMask
import com.wanbaohe.markuplayers.domain.model.LayerTransform
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.model.MaskStroke
import com.wanbaohe.markuplayers.domain.model.NormalizedRect
import com.wanbaohe.markuplayers.domain.model.StrokePoint
import com.wanbaohe.markuplayers.domain.project.MarkupDocument
import com.wanbaohe.markuplayers.presentation.editor.CanvasBackground
import com.wanbaohe.markuplayers.presentation.export.ExportResolution
import com.wanbaohe.markuplayers.presentation.export.ExportSettings
import com.wanbaohe.markuplayers.presentation.tools.adjust.BaseAdjustments
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LayerHistoryTest {

    @Test
    fun retainsOnlyTheLatestFiftySnapshotsAndBoundsRedo() {
        val history = LayerHistory()
        repeat(75) { history.snapshot(document(it)) }
        var current = document(75)
        val undoVersions = mutableListOf<Int>()
        while (history.canUndo) {
            current = history.undo(current)!!
            undoVersions += current.width
        }
        assertEquals((74 downTo 25).map { 100 + it }, undoVersions)
        assertNull(history.undo(current))
        val redoVersions = mutableListOf<Int>()
        while (history.canRedo) {
            current = history.redo(current)!!
            redoVersions += current.width
        }
        assertEquals((26..75).map { 100 + it }, redoVersions)
        assertNull(history.redo(current))
    }

    @Test
    fun newEditInvalidatesRedoAndClearResetsBothStacks() {
        val history = LayerHistory(2)
        history.snapshot(document(0))
        val previous = history.undo(document(1))!!
        assertTrue(history.canRedo)
        history.snapshot(previous)
        assertFalse(history.canRedo)
        history.clear()
        assertFalse(history.canUndo)
        assertFalse(history.canRedo)
        assertNull(history.undo(document(3)))
        assertNull(history.redo(document(3)))
    }

    @Test
    fun snapshotsIncludeBaseEditsMetadataAndExportPreferences() {
        val before = document(1).copy(
            baseAdjustments = BaseAdjustments(1, 2, 3, 4, 5, 6),
            baseEffects = listOf(LayerEffect(UiColorMatrix3x3Filter(), 0.4f)),
            baseMask = LayerMask(
                rect = NormalizedRect(0.1f, 0.2f, 0.7f, 0.8f),
                strokes = listOf(MaskStroke(listOf(StrokePoint(0.2f, 0.3f)), 0.02f, true)),
                inverted = true, feather = 0.03f
            ),
            exportSettings = ExportSettings(
                resolution = ExportResolution.Custom, customWidth = 512, customHeight = 700,
                shareAfterSave = true, saveCopy = false, maxFileSizeBytes = 102_400
            ),
            originalUri = "/private/original.jpg",
            saveExif = true,
            workspaceBackground = CanvasBackground.Solid(0xff112233.toInt()),
            layers = listOf(MarkupLayer(
                id = "layer", name = "Image", type = LayerType.Image("/private/layer.png"),
                transform = LayerTransform(flipHorizontal = true, flipVertical = true),
                adjustments = BaseAdjustments(exposure = 12, tint = -15),
                groupId = "group", imageHistory = listOf("/private/old.png")
            ))
        )
        val after = document(9)
        val history = LayerHistory()
        history.snapshot(before)
        val restored = history.undo(after)!!
        assertEquals(MarkupDocumentCodec.encode(before), MarkupDocumentCodec.encode(restored))
        assertEquals(after, history.redo(restored))
    }

    @Test
    fun replacingSnapshotCoalescesAnEditWithoutAddingAnUndoStep() {
        val history = LayerHistory()
        assertFalse(history.replaceLastSnapshot(document(0)))
        history.snapshot(document(0))
        assertTrue(history.replaceLastSnapshot(document(1)))
        assertEquals(document(1), history.undo(document(10)))
        assertFalse(history.canUndo)
    }

    @Test
    fun freezesMutableCollectionsFilterVisibilityAndArrays() {
        val points = mutableListOf(StrokePoint(0.1f, 0.2f))
        val filter = UiColorMatrix3x3Filter()
        val effects = mutableListOf(LayerEffect(filter))
        val layers = mutableListOf(MarkupLayer(
            id = "draw",
            type = LayerType.Draw(listOf(DrawStroke(points, -1, 0.02f, BrushType.Pen))),
            effects = effects,
            mask = LayerMask(strokes = listOf(MaskStroke(points, 0.03f)))
        ))
        val history = LayerHistory()
        history.snapshot(document(0).copy(layers = layers, baseEffects = effects))
        points.clear()
        effects.clear()
        layers.clear()
        filter.isVisible = false
        filter.value[0] = 99f
        val restored = history.undo(document(1))!!
        assertEquals(1, restored.layers.size)
        assertEquals(1, (restored.layers.single().type as LayerType.Draw).strokes.single().points.size)
        assertEquals(1, restored.layers.single().mask!!.strokes.single().points.size)
        assertTrue(restored.baseEffects.single().filter.isVisible)
        val restoredFilter = restored.baseEffects.single().filter as UiColorMatrix3x3Filter
        assertNotSame(filter.value, restoredFilter.value)
        assertEquals(1f, restoredFilter.value[0], 0f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsZeroCapacity() {
        LayerHistory(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsCapacityAboveFifty() {
        LayerHistory(51)
    }

    private fun document(version: Int) = MarkupDocument(
        baseUri = "/private/base-$version.jpg", width = 100 + version, height = 200,
        layers = emptyList()
    )
}
