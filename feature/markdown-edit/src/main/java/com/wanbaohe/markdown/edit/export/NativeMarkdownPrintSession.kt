package com.wanbaohe.markdown.edit.export

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.web.WebContent
import androidx.web.WebContentView
import androidx.web.WebFeature
import com.shifenmiao.core.R

internal class NativeMarkdownPrintSession(
    private val activity: ComponentActivity,
    private val fileName: String,
    private val onComplete: (Boolean, String?) -> Unit,
) {
    private var webView: WebView? = null
    private var webContent: WebContent? = null
    private var printStarted = false
    private var completed = false
    private var disposed = false
    private val lifecycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) dispose()
    }
    private val timeout = Runnable {
        if (!printStarted) complete(false, null)
    }

    fun print(html: String) {
        if (activity.isDestroyed) {
            dispose()
            onComplete(false, null)
            return
        }
        activity.lifecycle.addObserver(lifecycleObserver)
        val web = createWebView()
        activity.addContentView(web, ViewGroup.LayoutParams(1, 1))
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                if (completed || printStarted) return
                view.postDelayed({ startPrint() }, PRINT_DELAY_MS)
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError,
            ) {
                if (request.isForMainFrame) complete(false, error.description?.toString())
            }
        }
        web.loadDataWithBaseURL(
            "https://www.oneboxable.com/",
            html,
            "text/html",
            "UTF-8",
            null,
        )
        web.postDelayed(timeout, PRINT_TIMEOUT_MS)
    }

    private fun createWebView(): WebView {
        val content = runCatching {
            if (WebFeature.isFeatureSupported(WebFeature.WEB_CONTENT)) WebContent() else null
        }.getOrNull()
        webContent = content
        val web = if (content == null) {
            WebView(activity)
        } else {
            content.attach(activity, ::WebContentView)
        }
        webView = web
        return web
    }

    private fun startPrint() {
        if (completed || disposed || activity.isDestroyed) return
        val web = webView ?: return
        printStarted = true
        runCatching {
            val printManager = activity.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val adapter = web.createPrintDocumentAdapter(fileName)
            printManager.print(
                fileName,
                PrintCompletionAdapter(adapter, ::dispose),
                PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(
                        PrintAttributes.Resolution(
                            "pdf",
                            "pdf",
                            PRINT_RESOLUTION_DPI,
                            PRINT_RESOLUTION_DPI,
                        ),
                    )
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build(),
            )
        }.onSuccess {
            complete(true, activity.getString(R.string.pdf_export_prepare))
        }.onFailure { error ->
            complete(false, error.localizedMessage)
        }
    }

    private fun complete(success: Boolean, message: String?) {
        if (completed || disposed) return
        completed = true
        webView?.removeCallbacks(timeout)
        activity.lifecycle.removeObserver(lifecycleObserver)
        onComplete(success, message)
        if (!success) dispose()
    }

    private fun dispose() {
        if (disposed) return
        disposed = true
        webView?.removeCallbacks(timeout)
        activity.lifecycle.removeObserver(lifecycleObserver)
        val web = webView ?: return
        (web.parent as? ViewGroup)?.removeView(web)
        web.stopLoading()
        val content = webContent
        if (content == null) {
            web.destroy()
        } else {
            content.detach()
            content.close()
        }
        webContent = null
        webView = null
    }
}

private class PrintCompletionAdapter(
    private val delegate: PrintDocumentAdapter,
    private val onFinished: () -> Unit,
) : PrintDocumentAdapter() {
    override fun onStart() = delegate.onStart()

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?,
    ) {
        delegate.onLayout(oldAttributes, newAttributes, cancellationSignal, callback, extras)
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?,
    ) {
        delegate.onWrite(pages, destination, cancellationSignal, callback)
    }

    override fun onFinish() {
        delegate.onFinish()
        onFinished()
    }
}

private const val PRINT_DELAY_MS = 300L
private const val PRINT_TIMEOUT_MS = 30_000L
private const val PRINT_RESOLUTION_DPI = 600
