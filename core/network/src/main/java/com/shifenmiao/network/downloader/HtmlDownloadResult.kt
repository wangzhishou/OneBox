/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.network.downloader

/**
 * Result wrapper for HTML download operations
 */
sealed class HtmlDownloadResult {
    /**
     * Successful download
     * @param htmlContent The downloaded HTML content
     * @param url The actual URL (may differ from requested if redirected)
     * @param contentType The content type from response headers
     * @param contentLength The content length in bytes
     */
    data class Success(
        val htmlContent: String,
        val url: String,
        val contentType: String?,
        val contentLength: Long
    ) : HtmlDownloadResult()

    /**
     * Download failed
     * @param error The exception that caused the failure
     * @param message User-friendly error message
     * @param url The URL that was attempted
     */
    data class Failure(
        val error: Throwable,
        val message: String,
        val url: String
    ) : HtmlDownloadResult()
}

