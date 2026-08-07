package com.shifenmiao.model.ai

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class ListOrStringContentTypeAdapter : JsonDeserializer<ListOrStringContent>, JsonSerializer<ListOrStringContent> {
    private val gson = Gson()
    
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ListOrStringContent? {
        if (json == null || context == null) {
            throw JsonParseException("Null json or context in ListOrStringContentTypeAdapter")
        }
        
        return when {
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                // 处理字符串内容
                val content = json.asString
                ListOrStringContent.StringContent(content)
            }
            json.isJsonArray -> {
                // 处理数组内容
                val contentItems = mutableListOf<ContentItem>()
                val jsonArray = json.asJsonArray
                
                jsonArray.forEach { element ->
                    if (element.isJsonObject) {
                        val obj = element.asJsonObject
                        val type = obj.get("type")?.asString
                        
                        when (type) {
                            "text" -> {
                                val text = obj.get("text")?.asString ?: ""
                                contentItems.add(ContentItem.TextContent(text))
                            }
                            "image_url" -> {
                                val imageUrlObj = obj.getAsJsonObject("image_url")
                                val url = imageUrlObj.get("url")?.asString ?: ""
                                contentItems.add(ContentItem.ImageContent(ImageUrl(url)))
                            }
                        }
                    }
                }
                
                ListOrStringContent.ListContent(contentItems)
            }
            else -> {
                throw JsonParseException("Unexpected JSON format for ListOrStringContent")
            }
        }
    }

    override fun serialize(
        src: ListOrStringContent?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        if (src == null || context == null) {
            throw JsonParseException("Null source or context in ListOrStringContentTypeAdapter")
        }
        
        return when (src) {
            is ListOrStringContent.StringContent -> {
                // 直接序列化为字符串
                JsonPrimitive(src.content)
            }
            is ListOrStringContent.ListContent -> {
                // 序列化为内容项数组
                val jsonArray = JsonArray()
                
                src.items.forEach { item ->
                    when (item) {
                        is ContentItem.TextContent -> {
                            val itemObj = JsonObject()
                            itemObj.addProperty("type", item.type)
                            itemObj.addProperty("text", item.text)
                            jsonArray.add(itemObj)
                        }
                        is ContentItem.ImageContent -> {
                            val itemObj = JsonObject()
                            itemObj.addProperty("type", item.type)
                            
                            val imageUrlObj = JsonObject()
                            imageUrlObj.addProperty("url", item.imageUrl.url)
                            
                            itemObj.add("image_url", imageUrlObj)
                            jsonArray.add(itemObj)
                        }
                    }
                }
                
                jsonArray
            }
        }
    }
}
