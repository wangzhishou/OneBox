package com.wanbaohe.markdown.edit.export

import androidx.activity.ComponentActivity
import com.shifenmiao.webview.MarkdownRenderHtmlTemplate
import com.wanbaohe.markdown.OneBoxMarkdownHtml
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object NativeMarkdownPdfExporter {
    fun export(
        activity: ComponentActivity,
        markdown: String,
        fileName: String,
        onComplete: (Boolean, String?) -> Unit,
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            runCatching {
                val bodyHtml = withContext(Dispatchers.Default) {
                    OneBoxMarkdownHtml.render(markdown)
                }
                MarkdownRenderHtmlTemplate.wrap(fileName, bodyHtml)
            }.onSuccess { html ->
                NativeMarkdownPrintSession(activity, fileName, onComplete).print(html)
            }.onFailure { error ->
                onComplete(false, error.localizedMessage)
            }
        }
    }
}
