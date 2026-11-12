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
