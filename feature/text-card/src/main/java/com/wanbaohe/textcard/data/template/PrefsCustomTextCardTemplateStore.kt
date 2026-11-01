package com.wanbaohe.textcard.data.template

import android.content.Context
import com.wanbaohe.textcard.data.project.TextCardDocumentCodec
import com.wanbaohe.textcard.domain.CustomTextCardTemplateStore
import com.wanbaohe.textcard.domain.StoredTextCardTemplate
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

internal class PrefsCustomTextCardTemplateStore @Inject constructor(
    @ApplicationContext context: Context,
) : CustomTextCardTemplateStore {

    private val prefs = context.getSharedPreferences("text_card_custom_templates", Context.MODE_PRIVATE)
    private val mutex = Mutex()

    override suspend fun list(): List<StoredTextCardTemplate> = withContext(Dispatchers.IO) {
        mutex.withLock {
            read().let { array ->
                buildList {
                    for (index in 0 until array.length()) {
                        val item = array.getJSONObject(index)
                        add(
                            StoredTextCardTemplate(
                                id = item.getString("id"),
                                name = item.getString("name"),
                                document = TextCardDocumentCodec.decodeDocument(
                                    kotlinx.serialization.json.Json.parseToJsonElement(
                                        item.getJSONObject("document").toString()
                                    )
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    override suspend fun save(template: StoredTextCardTemplate) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val current = read()
            val updated = JSONArray()
            for (index in 0 until current.length()) {
                val item = current.getJSONObject(index)
                if (item.getString("id") != template.id) updated.put(item)
            }
            updated.put(
                JSONObject()
                    .put("id", template.id)
                    .put("name", template.name)
                    .put(
                        "document",
                        JSONObject(TextCardDocumentCodec.encodeDocument(template.document).toString())
                    )
            )
            while (updated.length() > MaxTemplates) updated.remove(0)
            write(updated)
        }
    }

    override suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val current = read()
            val updated = JSONArray()
            for (index in 0 until current.length()) {
                val item = current.getJSONObject(index)
                if (item.getString("id") != id) updated.put(item)
            }
            write(updated)
        }
    }

    private fun read(): JSONArray = JSONArray(prefs.getString(KeyTemplates, "[]") ?: "[]")

    private fun write(templates: JSONArray) {
        check(prefs.edit().putString(KeyTemplates, templates.toString()).commit()) {
            throw IOException("Could not save custom text card templates")
        }
    }

    private companion object {
        const val KeyTemplates = "templates"
        const val MaxTemplates = 20
    }
}
