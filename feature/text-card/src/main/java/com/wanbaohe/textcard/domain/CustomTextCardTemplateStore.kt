package com.wanbaohe.textcard.domain

import com.wanbaohe.textcard.domain.model.TextCardDocument

data class StoredTextCardTemplate(
    val id: String,
    val name: String,
    val document: TextCardDocument,
)

interface CustomTextCardTemplateStore {
    suspend fun list(): List<StoredTextCardTemplate>
    suspend fun save(template: StoredTextCardTemplate)
    suspend fun delete(id: String)
}
