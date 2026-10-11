package com.wanbaohe.markuplayers.domain.recipe

import com.wanbaohe.markuplayers.domain.model.LayerEffect
import com.wanbaohe.markuplayers.domain.model.LayerMask
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.model.NormalizedRect
import com.wanbaohe.markuplayers.domain.project.MarkupDocument
import com.wanbaohe.markuplayers.presentation.export.ExportSettings
import com.wanbaohe.markuplayers.presentation.tools.adjust.BaseAdjustments
import java.util.UUID

/** Overlay coordinates and masks refer to the canvas after [cropRect] is applied. */
data class MarkupRecipe(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val adjustments: BaseAdjustments = BaseAdjustments(),
    val effects: List<LayerEffect> = emptyList(),
    val mask: LayerMask? = null,
    val cropRect: NormalizedRect? = null,
    val layers: List<MarkupLayer> = emptyList(),
    val exportSettings: ExportSettings = ExportSettings()
) {
    fun validate() {
        require(id.matches(Regex("[A-Za-z0-9_-]{1,80}"))) { "Invalid recipe ID." }
        require(name.isNotBlank() && name.length <= MAX_NAME_LENGTH) {
            "Enter a recipe name with 1 to $MAX_NAME_LENGTH characters."
        }
        cropRect?.let { crop ->
            require(
                crop.left.isFinite() && crop.top.isFinite() &&
                    crop.right.isFinite() && crop.bottom.isFinite() &&
                    crop.left >= 0f && crop.top >= 0f &&
                    crop.right <= 1f && crop.bottom <= 1f &&
                    crop.width > 0f && crop.height > 0f
            ) { "The recipe crop must be a positive rectangle inside the source image." }
        }
        require(exportSettings.validationError(1, 1) == null) {
            exportSettings.validationError(1, 1)?.message.orEmpty()
        }
        layers.forEach { layer ->
            val transform = layer.transform
            require(
                transform.centerX.isFinite() && transform.centerY.isFinite() &&
                    transform.rotation.isFinite() && transform.scale.isFinite() &&
                    transform.scale > 0f && transform.alpha.isFinite() &&
                    transform.alpha in 0f..1f
            ) { "Recipe layer geometry is invalid." }
        }
    }

    fun instantiateLayers(): List<MarkupLayer> {
        val groupIds = mutableMapOf<String, String>()
        return layers.map { layer ->
            layer.copy(
                id = UUID.randomUUID().toString(),
                imageHistory = emptyList(),
                groupId = layer.groupId?.let {
                    groupIds.getOrPut(it) { UUID.randomUUID().toString() }
                }
            )
        }
    }

    companion object {
        const val MAX_NAME_LENGTH = 80

        /** No base image, original reference, EXIF, or content-version history enters a recipe. */
        fun fromDocument(
            name: String,
            document: MarkupDocument,
            cropRect: NormalizedRect? = null
        ): MarkupRecipe {
            val originals = listOfNotNull(document.baseUri, document.originalUri).toSet()
            return MarkupRecipe(
                name = name.trim(),
                adjustments = document.baseAdjustments,
                effects = document.baseEffects,
                mask = document.baseMask,
                cropRect = cropRect,
                layers = document.layers.filterNot { layer ->
                    val type = layer.type
                    type is LayerType.Image && type.imageData.toString() in originals
                }.map { it.copy(imageHistory = emptyList()) },
                exportSettings = document.exportSettings.copy(
                    saveCopy = true,
                    shareAfterSave = false
                )
            ).also { it.validate() }
        }
    }
}
