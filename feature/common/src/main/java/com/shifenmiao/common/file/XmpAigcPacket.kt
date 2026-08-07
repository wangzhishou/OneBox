package com.shifenmiao.common.file

import androidx.annotation.Keep

/**
 * GB 45438-2025 6.2 节规定的 XMP AIGC packet 工具。
 *
 * ## 标准格式
 *
 * 1) 注册 TC260 自定义命名空间(已有内容时按属性更新,不整体覆盖):
 *    ```
 *    xmlns:TC260="http://www.tc260.org.cn/ns/AIGC/1.0/"
 *    ```
 *
 * 2) 在 TC260 空间下 AIGC 键值中填入 GB 45438-2025 附录 E 规定的 7 要素 JSON:
 *    ```
 *    <TC260:AIGC>{"Label":"value1","ContentProducer":"value2",...}</TC260:AIGC>
 *    ```
 *
 * 3) 整段 XMP packet 写入:
 *    - PNG: iTXt chunk,keyword = `XML:com.adobe.xmp`;
 *    - JPEG / JPG: APP1 segment,header 标识 `XMP\0XMP\0...`。
 *
 * ## 实现策略
 *
 * - 生成:把 TC260 namespace + AIGC 元素嵌入标准 XMP packet 框架。
 * - 解析:用正则找 `<TC260:AIGC>...</TC260:AIGC>` 元素并做 XML 反转义。
 *   只在两个标准 AIGC 标签之间挖内容,不做完整 XML 解析,降低依赖。
 */
@Keep
internal object XmpAigcPacket {

    private const val TC260_NAMESPACE = "http://www.tc260.org.cn/ns/AIGC/1.0/"
    private const val XMP_HEADER =
        "<?xpacket begin=\"\uFEFF\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>"
    private const val XMP_FOOTER = "<?xpacket end=\"w\"?>"

    /**
     * 构建完整 XMP packet,内含 TC260 namespace 与 `<TC260:AIGC>` 元素。
     * @param aigcJson 7 要素 JSON 字符串(已包含 `Label/ContentProducer/...` 7 个字段)
     */
    fun build(aigcJson: String): String {
        // XMP / XML 实体转义(顺序敏感:& 必须第一个,避免 &amp; 被二次转义)
        val escapedJson = aigcJson
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
        return buildString {
            append(XMP_HEADER)
            append("\n<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n")
            append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n")
            append("<rdf:Description rdf:about=\"\"\n")
            append("  xmlns:TC260=\"").append(TC260_NAMESPACE).append("\"\n")
            append("  TC260:AIGC=\"").append(escapedJson).append("\"/>\n")
            append("</rdf:RDF>\n")
            append("</x:xmpmeta>\n")
            append(XMP_FOOTER)
        }
    }

    /**
     * 从 XMP packet 中提取 TC260:AIGC 字段的 JSON 内容(已做 XML 反转义)。
     * 找不到时返回 null。
     *
     * 匹配两种写法(按实际工程经验均可能出现):
     * 1. **属性写法**(当前 [build] 使用):
     *    ```xml
     *    <rdf:Description rdf:about=""
     *      xmlns:TC260="http://www.tc260.org.cn/ns/AIGC/1.0/"
     *      TC260:AIGC="&quot;Label&quot;:&quot;1&quot;,..."/>
     *    ```
     * 2. **元素写法**(早期/三方工具可能生成):
     *    ```xml
     *    <TC260:AIGC>{...}</TC260:AIGC>
     *    ```
     *
     * 实际内容都需做 XML 反转义(`&quot;` → `"` 等)。
     */
    fun extractAigcJson(xmpPacket: String): String? {
        // 1) 优先属性写法:TC260:AIGC="..." 或 TC260:AIGC='...'
        //    属性值中不会含未转义的引号(JSON 中的 " 都被 build() 转义为 &quot;),
        //    所以 `[^"']*` 是安全的简单匹配。
        AIGC_ATTRIBUTE.find(xmpPacket)?.let { match ->
            val raw = match.groupValues[1].ifEmpty { match.groupValues[2] }
            if (raw.isNotEmpty()) return unescapeXml(raw)
        }
        // 2) 回退元素写法:<TC260:AIGC ...>...</TC260:AIGC>
        AIGC_ELEMENT.find(xmpPacket)?.let { match ->
            val raw = match.groupValues[1]
            if (raw.isNotEmpty()) return unescapeXml(raw)
        }
        return null
    }

    private val AIGC_ATTRIBUTE = Regex(
        """TC260:AIGC\s*=\s*"([^"]*)"|TC260:AIGC\s*=\s*'([^']*)'""",
        RegexOption.DOT_MATCHES_ALL
    )

    private val AIGC_ELEMENT = Regex(
        "<TC260:AIGC(?:\\s[^>]*)?>(.*?)</TC260:AIGC>",
        RegexOption.DOT_MATCHES_ALL
    )

    private fun unescapeXml(input: String): String {
        return input
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&amp;", "&")
    }
}
