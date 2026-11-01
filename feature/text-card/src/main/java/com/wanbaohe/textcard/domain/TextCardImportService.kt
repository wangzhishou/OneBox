package com.wanbaohe.textcard.domain

interface TextCardImportService {
    suspend fun readText(uri: String): String
}
