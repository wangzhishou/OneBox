package com.wanbaohe.richtext.editor.clipboard

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.wanbaohe.richtext.editor.RichTextEditorState
import com.wanbaohe.richtext.editor.RichTextSnapshot
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme

interface RichTextClipboard {
    fun copySelection(state: RichTextEditorState): Boolean
    fun cutSelection(state: RichTextEditorState): Boolean
    fun paste(state: RichTextEditorState): Boolean
    fun hasRichContent(): Boolean
}

class AndroidRichTextClipboard(
    context: Context,
    scheme: RichTextStyleScheme,
) : RichTextClipboard {
    private val manager = context.applicationContext.getSystemService(ClipboardManager::class.java)
    private val codec = RichTextClipboardCodec(scheme)

    override fun copySelection(state: RichTextEditorState): Boolean {
        val snapshot = state.selectionSnapshot()
        if (snapshot.text.isEmpty()) return false
        val description = ClipDescription(
            "Rich text",
            arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN, RICH_TEXT_MIME_TYPE),
        )
        val item = ClipData.Item(snapshot.text, codec.encode(snapshot))
        manager.setPrimaryClip(ClipData(description, item))
        return true
    }

    override fun cutSelection(state: RichTextEditorState): Boolean {
        if (state.selection.collapsed || !copySelection(state)) return false
        state.replaceSelectionWithSnapshot(RichTextSnapshot.Empty)
        return true
    }

    override fun paste(state: RichTextEditorState): Boolean {
        val snapshot = manager.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.let(::readSnapshot)
        if (snapshot == null) return false
        state.replaceSelectionWithSnapshot(snapshot)
        return true
    }

    override fun hasRichContent(): Boolean =
        manager.primaryClipDescription?.hasMimeType(RICH_TEXT_MIME_TYPE) == true

    private fun readSnapshot(item: ClipData.Item): RichTextSnapshot? {
        val richSnapshot = item.htmlText
            ?.takeIf { hasRichContent() }
            ?.let(codec::decode)
        return richSnapshot
            ?: item.text?.toString()?.takeIf(String::isNotEmpty)?.let(::RichTextSnapshot)
    }
}

@Composable
fun rememberRichTextClipboard(scheme: RichTextStyleScheme): RichTextClipboard {
    val context = LocalContext.current
    return remember(context, scheme) { AndroidRichTextClipboard(context, scheme) }
}

private const val RICH_TEXT_MIME_TYPE = "application/vnd.onebox.richtext+json"
