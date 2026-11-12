/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.textcard.presentation.screenLogic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shifenmiao.model.editor.LayerTransform
import com.shifenmiao.model.editor.ShapeSpec
import com.wanbaohe.core.ui.widget.editor.EditorDrawSessionState
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.TextCardAnchorAffinity
import com.wanbaohe.textcard.domain.model.TextCardAnnotation
import com.wanbaohe.textcard.domain.model.TextCardAnnotationType
import com.wanbaohe.textcard.domain.model.TextCardDocument
import com.wanbaohe.textcard.domain.model.TextCardRole
import com.wanbaohe.textcard.domain.model.TextCardSourceAnchor
import com.wanbaohe.textcard.domain.model.TextStyleSpan
import com.wanbaohe.textcard.domain.model.annotationsForPage
import com.wanbaohe.textcard.domain.model.needsReview
import com.wanbaohe.textcard.domain.model.textBlockOrNull
import com.wanbaohe.textcard.domain.render.TextCardLayout
import java.util.UUID

enum class TextCardAnnotationPanel {
    Shape, Layers
}

internal class TextCardAnnotationEditor(
    private val document: () -> TextCardDocument,
    private val layout: () -> TextCardLayout?,
    private val currentPage: () -> Int,
    private val defaultText: () -> String,
    private val closeOtherEditing: () -> Unit,
    private val snapshot: () -> Unit,
    private val mutate: (Boolean, (TextCardDocument) -> TextCardDocument) -> Unit,
    private val beginContinuous: () -> Unit,
    private val endContinuous: () -> Unit,
) {
    var isActive by mutableStateOf(false)
        private set
    var panel by mutableStateOf<TextCardAnnotationPanel?>(null)
        private set
    var selectedId by mutableStateOf<String?>(null)
        private set
    var drawSession by mutableStateOf<EditorDrawSessionState?>(null)
        private set
    var textEditId by mutableStateOf<String?>(null)
        private set
    var shapeSpec by mutableStateOf(ShapeSpec.default(com.shifenmiao.model.editor.ShapeKind.Rectangle))
        private set

    private var textSnapshotTaken = false

    val selectedAnnotation: TextCardAnnotation?
        get() = document().annotations.firstOrNull { it.id == selectedId }
    val currentAnnotations: List<TextCardAnnotation>
        get() = layout()?.let { document().annotationsForPage(it, currentPage()) }.orEmpty()
    val reviewCount: Int
        get() = layout()?.let { current -> document().annotations.count { current.needsReview(it) } } ?: 0
    val selectedTextBlock: TextBlock?
        get() = selectedAnnotation?.textBlockOrNull()

    fun toggle() = if (isActive) finish() else start()

    fun start() {
        closeOtherEditing()
        isActive = true
    }

    fun finish() {
        finishDrawing()
        endTextEdit()
        isActive = false
        panel = null
        selectedId = null
    }

    fun reset() {
        drawSession = null
        textEditId = null
        textSnapshotTaken = false
        isActive = false
        panel = null
        selectedId = null
    }

    fun dismissPanel() {
        panel = null
    }

    fun openShapePanel() {
        endTextEdit()
        drawSession = null
        panel = TextCardAnnotationPanel.Shape
    }

    fun openLayers() {
        endTextEdit()
        drawSession = null
        panel = TextCardAnnotationPanel.Layers
    }

    fun select(id: String?) {
        if (id != selectedId) endTextEdit()
        selectedId = id
    }

    fun addText(): String {
        val id = UUID.randomUUID().toString()
        val block = TextBlock(
            id = id,
            content = defaultText(),
            baseSizeRatio = 0.04f,
            widthRatio = 0.62f,
            widthManuallySet = true,
            lineSpacingMultiplier = 1.2f,
        )
        val annotation = TextCardAnnotation(
            id = id,
            pageAnchor = pageStartAnchor(),
            pageEndAnchor = pageEndAnchor(),
            type = TextCardAnnotationType.Text(block),
        )
        mutate(true) { it.copy(annotations = it.annotations + annotation) }
        selectedId = id
        panel = null
        drawSession = null
        beginTextEdit(id)
        return id
    }

    fun addShape(spec: ShapeSpec) {
        shapeSpec = spec
        val annotation = TextCardAnnotation(
            pageAnchor = pageStartAnchor(),
            pageEndAnchor = pageEndAnchor(),
            type = TextCardAnnotationType.Shape(spec),
        )
        mutate(true) { it.copy(annotations = it.annotations + annotation) }
        selectedId = annotation.id
        panel = null
        drawSession = null
    }

    fun updateShapeSpec(spec: ShapeSpec) {
        shapeSpec = spec
        val selected = selectedAnnotation ?: return
        val current = (selected.type as? TextCardAnnotationType.Shape)?.spec ?: return
        if (current == spec) return
        updateAnnotation(selected.id) {
            it.copy(type = TextCardAnnotationType.Shape(spec))
        }
    }

    fun startDrawing() {
        endTextEdit()
        selectedId = null
        panel = null
        drawSession = EditorDrawSessionState()
    }

    fun finishDrawing() {
        val session = drawSession ?: return
        val strokes = session.strokes
        drawSession = null
        if (strokes.isEmpty()) return
        val annotation = TextCardAnnotation(
            pageAnchor = pageStartAnchor(),
            pageEndAnchor = pageEndAnchor(),
            type = TextCardAnnotationType.Draw(strokes),
        )
        mutate(true) { it.copy(annotations = it.annotations + annotation) }
        selectedId = annotation.id
    }

    fun cancelDrawing() {
        drawSession = null
    }

    fun beginTextEdit(id: String) {
        if (document().annotations.none { it.id == id && it.type is TextCardAnnotationType.Text }) return
        selectedId = id
        textEditId = id
        textSnapshotTaken = false
        panel = null
        drawSession = null
    }

    fun syncTextEdit(id: String, content: String, spans: List<TextStyleSpan>) {
        val annotation = document().annotations.firstOrNull { it.id == id } ?: return
        val block = annotation.textBlockOrNull() ?: return
        if (block.content == content && block.styleSpans == spans) return
        if (!textSnapshotTaken) {
            snapshot()
            textSnapshotTaken = true
        }
        mutate(false) { current ->
            current.copy(
                annotations = current.annotations.map { item ->
                    if (item.id != id) item else item.copy(
                        type = TextCardAnnotationType.Text(
                            block.copy(content = content, styleSpans = spans)
                        )
                    )
                }
            )
        }
    }

    fun endTextEdit() {
        textEditId = null
        textSnapshotTaken = false
    }

    fun updateTextBlock(id: String, transform: (TextBlock) -> TextBlock): Boolean {
        val annotation = document().annotations.firstOrNull { it.textBlockOrNull()?.id == id } ?: return false
        updateAnnotation(annotation.id) { item ->
            val block = item.textBlockOrNull() ?: return@updateAnnotation item
            item.copy(type = TextCardAnnotationType.Text(transform(block)))
        }
        return true
    }

    fun beginTransform() = beginContinuous()

    fun endTransform() = endContinuous()

    fun updateTransform(id: String, transform: LayerTransform) {
        updateAnnotation(id) { it.copy(transform = transform) }
    }

    fun toggleVisible(id: String) = updateAnnotation(id) {
        it.copy(transform = it.transform.copy(visible = !it.transform.visible))
    }

    fun toggleLocked(id: String) = updateAnnotation(id) {
        it.copy(transform = it.transform.copy(locked = !it.transform.locked))
    }

    fun delete(id: String) {
        if (textEditId == id) endTextEdit()
        if (selectedId == id) selectedId = null
        mutate(true) { current ->
            current.copy(annotations = current.annotations.filterNot { it.id == id })
        }
    }

    fun moveAnnotation(id: String, targetIndex: Int) {
        mutate(true) { current ->
            val sourceIndex = current.annotations.indexOfFirst { it.id == id }
            if (sourceIndex < 0) return@mutate current
            val updated = current.annotations.toMutableList()
            val annotation = updated.removeAt(sourceIndex)
            updated.add(targetIndex.coerceIn(0, updated.size), annotation)
            current.copy(annotations = updated)
        }
    }

    fun moveToPage(id: String, pageIndex: Int) {
        val currentLayout = layout() ?: return
        val page = currentLayout.pages.getOrNull(pageIndex) ?: return
        val start = page.segments.firstOrNull()?.let {
            TextCardSourceAnchor(it.role, it.sourceStart)
        } ?: TextCardSourceAnchor()
        val end = page.segments.lastOrNull()?.let {
            TextCardSourceAnchor(it.role, it.sourceEnd, TextCardAnchorAffinity.Previous)
        } ?: TextCardSourceAnchor(affinity = TextCardAnchorAffinity.Previous)
        updateAnnotation(id) { it.copy(pageAnchor = start, pageEndAnchor = end) }
    }

    private fun updateAnnotation(
        id: String,
        transform: (TextCardAnnotation) -> TextCardAnnotation,
    ) {
        mutate(true) { current ->
            current.copy(
                annotations = current.annotations.map { if (it.id == id) transform(it) else it }
            )
        }
    }

    private fun pageStartAnchor(): TextCardSourceAnchor {
        val page = layout()?.pages?.getOrNull(currentPage()) ?: return TextCardSourceAnchor()
        return page.segments.firstOrNull()?.let {
            TextCardSourceAnchor(it.role, it.sourceStart)
        } ?: TextCardSourceAnchor()
    }

    private fun pageEndAnchor(): TextCardSourceAnchor {
        val page = layout()?.pages?.getOrNull(currentPage()) ?: return TextCardSourceAnchor(
            affinity = TextCardAnchorAffinity.Previous
        )
        return page.segments.lastOrNull()?.let {
            TextCardSourceAnchor(it.role, it.sourceEnd, TextCardAnchorAffinity.Previous)
        } ?: TextCardSourceAnchor(affinity = TextCardAnchorAffinity.Previous)
    }
}
