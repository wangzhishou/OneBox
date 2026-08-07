package com.wanbaohe.bookkeeping.model

import org.json.JSONArray
import org.json.JSONObject

data class BookkeepingBackupPayload(
    val version: Int = 1,
    val exportedAt: Long,
    val categories: List<BookkeepingBackupCategory>,
    val records: List<BookkeepingBackupRecord>,
)

data class BookkeepingBackupCategory(
    val id: String,
    val name: String,
    val type: Int,
    val iconKey: String,
    val sortOrder: Int,
    val isDefault: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

data class BookkeepingBackupRecord(
    val id: String,
    val categoryId: String?,
    val type: Int,
    val amountCents: Long,
    val note: String?,
    val happenedAt: Long,
    val excludeFromStats: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

data class BookkeepingRestoreResult(
    val categoryCount: Int,
    val recordCount: Int,
)

object BookkeepingBackupCodec {
    fun encode(payload: BookkeepingBackupPayload): String {
        val root = JSONObject()
            .put("version", payload.version)
            .put("exportedAt", payload.exportedAt)
            .put("categories", JSONArray().apply {
                payload.categories.forEach { item ->
                    put(
                        JSONObject()
                            .put("id", item.id)
                            .put("name", item.name)
                            .put("type", item.type)
                            .put("iconKey", item.iconKey)
                            .put("sortOrder", item.sortOrder)
                            .put("isDefault", item.isDefault)
                            .put("createdAt", item.createdAt)
                            .put("updatedAt", item.updatedAt)
                    )
                }
            })
            .put("records", JSONArray().apply {
                payload.records.forEach { item ->
                    put(
                        JSONObject()
                            .put("id", item.id)
                            .put("categoryId", item.categoryId)
                            .put("type", item.type)
                            .put("amountCents", item.amountCents)
                            .put("note", item.note)
                            .put("happenedAt", item.happenedAt)
                            .put("excludeFromStats", item.excludeFromStats)
                            .put("createdAt", item.createdAt)
                            .put("updatedAt", item.updatedAt)
                    )
                }
            })
        return root.toString(2)
    }

    fun decode(json: String): BookkeepingBackupPayload {
        val root = JSONObject(json)
        val categories = root.optJSONArray("categories").toCategoryList()
        val records = root.optJSONArray("records").toRecordList()
        return BookkeepingBackupPayload(
            version = root.optInt("version", 1),
            exportedAt = root.optLong("exportedAt", System.currentTimeMillis()),
            categories = categories,
            records = records,
        )
    }

    private fun JSONArray?.toCategoryList(): List<BookkeepingBackupCategory> {
        if (this == null) return emptyList()
        return buildList(length()) {
            for (index in 0 until length()) {
                val item = optJSONObject(index) ?: continue
                add(
                    BookkeepingBackupCategory(
                        id = item.optString("id"),
                        name = item.optString("name"),
                        type = item.optInt("type"),
                        iconKey = item.optString("iconKey"),
                        sortOrder = item.optInt("sortOrder"),
                        isDefault = item.optBoolean("isDefault"),
                        createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = item.optLong("updatedAt", System.currentTimeMillis()),
                    )
                )
            }
        }
    }

    private fun JSONArray?.toRecordList(): List<BookkeepingBackupRecord> {
        if (this == null) return emptyList()
        return buildList(length()) {
            for (index in 0 until length()) {
                val item = optJSONObject(index) ?: continue
                add(
                    BookkeepingBackupRecord(
                        id = item.optString("id"),
                        categoryId = item.optString("categoryId").ifBlank { null },
                        type = item.optInt("type"),
                        amountCents = item.optLong("amountCents"),
                        note = item.optString("note").ifBlank { null },
                        happenedAt = item.optLong("happenedAt"),
                        excludeFromStats = item.optBoolean("excludeFromStats"),
                        createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = item.optLong("updatedAt", System.currentTimeMillis()),
                    )
                )
            }
        }
    }
}
