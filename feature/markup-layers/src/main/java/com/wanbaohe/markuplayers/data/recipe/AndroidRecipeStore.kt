package com.wanbaohe.markuplayers.data.recipe

import android.content.Context
import android.util.AtomicFile
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.markuplayers.data.project.MarkupProjectStore
import com.wanbaohe.markuplayers.domain.recipe.MarkupRecipe
import com.wanbaohe.markuplayers.domain.recipe.RecipeStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidRecipeStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val projectStore: MarkupProjectStore,
    dispatchersHolder: DispatchersHolder
) : RecipeStore, DispatchersHolder by dispatchersHolder {

    private val mutex = Mutex()

    override suspend fun listRecipes(): List<MarkupRecipe> = withContext(ioDispatcher) {
        mutex.withLock {
            val root = directory()
            val files = root.listFiles() ?: throw IOException("Could not list recipes.")
            val ids = files.mapNotNull { file ->
                when {
                    file.name.endsWith(".json") -> file.name.removeSuffix(".json")
                    file.name.endsWith(".json.bak") -> file.name.removeSuffix(".json.bak")
                    else -> null
                }
            }.distinct()
            ids.map { id ->
                currentCoroutineContext().ensureActive()
                val file = recipeFile(root, id)
                val recipe = read(file)
                if (recipe.id != id) throw MarkupRecipeFormatException("The recipe ID does not match its file.")
                file.lastModified() to recipe
            }.sortedWith(
                compareByDescending<Pair<Long, MarkupRecipe>> { it.first }.thenBy { it.second.id }
            ).map { it.second }
        }
    }

    override suspend fun saveRecipe(recipe: MarkupRecipe): MarkupRecipe = withContext(ioDispatcher) {
        mutex.withLock {
            recipe.validate()
            currentCoroutineContext().ensureActive()
            val document = projectStore.materializeRecipeAssets(MarkupRecipeCodec.toDocument(recipe))
            val durable = MarkupRecipeCodec.withDurableAssets(recipe, document)
            val bytes = MarkupRecipeCodec.encode(durable).toByteArray(Charsets.UTF_8)
            if (bytes.size > MaxRecipeBytes) throw IOException("Recipe JSON exceeds the size limit.")
            val file = recipeFile(directory(), durable.id)
            write(file, bytes)
            durable
        }
    }

    override suspend fun deleteRecipe(id: String) = withContext(ioDispatcher) {
        mutex.withLock {
            val file = recipeFile(directory(), id)
            currentCoroutineContext().ensureActive()
            AtomicFile(file).delete()
            if (file.exists() || File(file.path + ".bak").exists() || File(file.path + ".new").exists()) {
                throw IOException("Could not delete recipe $id.")
            }
        }
    }

    private fun directory(): File {
        val root = File(context.filesDir, "image-studio/recipes")
        if (!root.isDirectory && !root.mkdirs()) throw IOException("Could not create recipe storage.")
        return root
    }

    private fun recipeFile(root: File, id: String): File {
        require(id.matches(Regex("[A-Za-z0-9_-]{1,80}"))) { "Invalid recipe ID." }
        val file = File(root, "$id.json")
        if (file.canonicalFile.parentFile != root.canonicalFile) {
            throw IOException("The recipe path is outside its storage directory.")
        }
        return file
    }

    private suspend fun read(file: File): MarkupRecipe = AtomicFile(file).openRead().use { input ->
        ByteArrayOutputStream().use { output ->
            val buffer = ByteArray(8192)
            while (true) {
                currentCoroutineContext().ensureActive()
                val count = input.read(buffer)
                if (count < 0) break
                if (output.size().toLong() + count > MaxRecipeBytes) {
                    throw IOException("Recipe JSON exceeds the size limit.")
                }
                output.write(buffer, 0, count)
            }
            MarkupRecipeCodec.decode(output.toString(Charsets.UTF_8.name()))
        }
    }

    private suspend fun write(file: File, bytes: ByteArray) {
        val atomic = AtomicFile(file)
        var output: FileOutputStream? = null
        try {
            currentCoroutineContext().ensureActive()
            val stream = atomic.startWrite().also { output = it }
            var offset = 0
            while (offset < bytes.size) {
                currentCoroutineContext().ensureActive()
                val count = minOf(8192, bytes.size - offset)
                stream.write(bytes, offset, count)
                offset += count
            }
            currentCoroutineContext().ensureActive()
            atomic.finishWrite(stream)
            output = null
            if (!file.isFile || file.length() != bytes.size.toLong()) {
                throw IOException("Could not commit the recipe file.")
            }
        } catch (error: Throwable) {
            output?.let(atomic::failWrite)
            throw error
        }
    }

    companion object {
        private const val MaxRecipeBytes = 8 * 1024 * 1024
    }
}
