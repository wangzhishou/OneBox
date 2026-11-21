/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wanbaohe.core.ui.widget.web

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.UiThread
import androidx.lifecycle.ViewModel
import androidx.web.WebContent
import androidx.web.WebContentView
import androidx.web.WebFeature
import java.util.function.Function

class PersistentWebContentViewModel : ViewModel() {

    private enum class Backend {
        UNINITIALIZED,
        WEB_CONTENT,
        LEGACY,
    }

    private var backend = Backend.UNINITIALIZED
    private var webContent: WebContent? = null
    private var attachedView: WebContentView? = null

    @Volatile
    private var isClosed = false

    @UiThread
    internal fun <T : WebContentView> attach(
        context: Context,
        viewFactory: (Context) -> T,
    ): T {
        check(!isClosed) { "Cannot attach a closed PersistentWebContentViewModel." }
        val content = getOrCreateWebContent()
        val view = if (content == null) {
            viewFactory(context)
        } else {
            content.attach(context, Function { viewFactory(it) })
        }
        attachedView = view
        return view
    }

    @UiThread
    internal fun detach(view: WebContentView): Boolean {
        if (backend != Backend.WEB_CONTENT) {
            attachedView = null
            return false
        }
        if (attachedView !== view) return true
        webContent?.detach()
        attachedView = null
        return true
    }

    @UiThread
    private fun getOrCreateWebContent(): WebContent? {
        when (backend) {
            Backend.WEB_CONTENT -> return webContent
            Backend.LEGACY -> return null
            Backend.UNINITIALIZED -> Unit
        }

        val content = runCatching {
            if (WebFeature.isFeatureSupported(WebFeature.WEB_CONTENT)) WebContent() else null
        }.onFailure { error ->
            Log.w(TAG, "WEB_CONTENT unavailable; using legacy WebView", error)
        }.getOrNull()

        return if (content == null) {
            backend = Backend.LEGACY
            null
        } else {
            backend = Backend.WEB_CONTENT
            webContent = content
            content
        }
    }

    override fun onCleared() {
        isClosed = true
        if (Looper.myLooper() == Looper.getMainLooper()) {
            closeWebContent()
        } else {
            mainHandler.post(::closeWebContent)
        }
    }

    @UiThread
    private fun closeWebContent() {
        webContent?.close()
        webContent = null
        attachedView = null
    }

    private companion object {
        const val TAG = "PersistentWebContent"
        val mainHandler = Handler(Looper.getMainLooper())
    }
}
