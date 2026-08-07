package com.wanbaohe.cloud.storage.data.protocol

import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

object ObjectStorageErrorParser {

    fun parse(responseBody: String?): String? {
        if (responseBody.isNullOrBlank()) return null
        return runCatching {
            val doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(ByteArrayInputStream(responseBody.toByteArray()))
            val code = doc.documentElement.childText("Code")
            val message = doc.documentElement.childText("Message")
            listOfNotNull(code, message).joinToString(": ").ifBlank { null }
        }.getOrNull() ?: responseBody.lineSequence().firstOrNull()?.trim()
    }

    private fun Element.childText(tagName: String): String? {
        val nodes = getElementsByTagName(tagName)
        if (nodes.length == 0) return null
        return nodes.item(0)?.textContent?.trim()?.takeIf { it.isNotBlank() }
    }
}
