package com.wanbaohe.markuplayers.data.recipe

import com.wanbaohe.markuplayers.data.project.MarkupDocumentCodec
import com.wanbaohe.markuplayers.domain.model.NormalizedRect
import com.wanbaohe.markuplayers.domain.project.MarkupDocument
import com.wanbaohe.markuplayers.domain.recipe.MarkupRecipe
import com.wanbaohe.markuplayers.presentation.editor.CanvasBackground
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import java.io.IOException

class MarkupRecipeFormatException(message: String, cause: Throwable? = null) :
    IOException(message, cause)

internal object MarkupRecipeCodec {
    private const val SchemaVersion = 1
    private const val RecipeBaseUri = "recipe://base"

    fun encode(recipe: MarkupRecipe): String {
        recipe.validate()
        return JsonObject(
            mapOf(
                "schemaVersion" to JsonPrimitive(SchemaVersion),
                "id" to JsonPrimitive(recipe.id),
                "name" to JsonPrimitive(recipe.name),
                "cropRect" to (recipe.cropRect?.let { crop ->
                    JsonObject(
                        mapOf(
                            "left" to JsonPrimitive(crop.left),
                            "top" to JsonPrimitive(crop.top),
                            "right" to JsonPrimitive(crop.right),
                            "bottom" to JsonPrimitive(crop.bottom)
                        )
                    )
                } ?: JsonNull),
                "document" to Json.parseToJsonElement(MarkupDocumentCodec.encode(toDocument(recipe)))
            )
        ).toString()
    }

    fun decode(source: String): MarkupRecipe = try {
        val root = fields(
            Json.parseToJsonElement(source), "schemaVersion", "id", "name", "cropRect", "document"
        )
        val version = root.getValue("schemaVersion") as? JsonPrimitive
        if (version == null || version.isString || version.intOrNull != SchemaVersion) {
            invalid("Unsupported recipe schema version.")
        }
        val document = MarkupDocumentCodec.decode(root.getValue("document").toString())
        if (
            document.baseUri != RecipeBaseUri || document.width != 1 || document.height != 1 ||
            document.originalUri != null || document.sourceUri != null || document.saveExif ||
            document.baseTransforms.isNotEmpty() || document.baseImageHistory.isNotEmpty() ||
            document.workspaceBackground != CanvasBackground.Default ||
            document.layers.any { it.imageHistory.isNotEmpty() } ||
            !document.exportSettings.saveCopy || document.exportSettings.shareAfterSave
        ) {
            invalid("A recipe cannot contain a base photo, original reference, history, or overwrite action.")
        }
        val crop = root.getValue("cropRect").takeUnless { it == JsonNull }?.let {
            val value = fields(it, "left", "top", "right", "bottom")
            NormalizedRect(
                number(value, "left"), number(value, "top"),
                number(value, "right"), number(value, "bottom")
            )
        }
        MarkupRecipe(
            id = string(root, "id"),
            name = string(root, "name"),
            adjustments = document.baseAdjustments,
            effects = document.baseEffects,
            mask = document.baseMask,
            cropRect = crop,
            layers = document.layers,
            exportSettings = document.exportSettings
        ).also { it.validate() }
    } catch (error: MarkupRecipeFormatException) {
        throw error
    } catch (error: Exception) {
        throw MarkupRecipeFormatException("Invalid recipe: ${error.message}", error)
    }

    fun toDocument(recipe: MarkupRecipe): MarkupDocument = MarkupDocument(
        baseUri = RecipeBaseUri,
        width = 1,
        height = 1,
        layers = recipe.layers.map { it.copy(imageHistory = emptyList()) },
        baseAdjustments = recipe.adjustments,
        baseEffects = recipe.effects,
        baseMask = recipe.mask,
        exportSettings = recipe.exportSettings.copy(saveCopy = true, shareAfterSave = false),
        originalUri = null,
        saveExif = false,
        workspaceBackground = CanvasBackground.Default
    )

    fun withDurableAssets(recipe: MarkupRecipe, document: MarkupDocument): MarkupRecipe = recipe.copy(
        name = recipe.name.trim(),
        adjustments = document.baseAdjustments,
        effects = document.baseEffects,
        mask = document.baseMask,
        layers = document.layers.map { it.copy(imageHistory = emptyList()) },
        exportSettings = document.exportSettings.copy(saveCopy = true, shareAfterSave = false)
    )

    private fun fields(element: JsonElement, vararg keys: String): JsonObject {
        val value = element as? JsonObject ?: invalid("Expected a JSON object.")
        if (value.keys != keys.toSet()) invalid("Missing or unknown recipe fields.")
        return value
    }

    private fun string(value: JsonObject, key: String): String {
        val primitive = value.getValue(key) as? JsonPrimitive
            ?: invalid("Expected text for $key.")
        if (!primitive.isString) invalid("Expected text for $key.")
        return primitive.content
    }

    private fun number(value: JsonObject, key: String): Float {
        val primitive = value.getValue(key) as? JsonPrimitive
            ?: invalid("Expected a number for $key.")
        if (primitive.isString) invalid("Expected a number for $key.")
        return primitive.floatOrNull?.takeIf(Float::isFinite)
            ?: invalid("Invalid recipe coordinate $key.")
    }

    private fun invalid(message: String): Nothing = throw MarkupRecipeFormatException(message)
}
