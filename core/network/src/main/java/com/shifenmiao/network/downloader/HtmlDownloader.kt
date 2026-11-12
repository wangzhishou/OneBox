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
 * Interface for downloading HTML content from URLs
 */
interface HtmlDownloader {
    /**
     * Download HTML content from the specified URL
     *
     * @param url The URL to download from
     * @param config Optional configuration for the download operation
     * @return Result containing either the HTML content or an error
     */
    suspend fun downloadHtml(
        url: String,
        config: HtmlDownloadConfig = HtmlDownloadConfig.DEFAULT
    ): HtmlDownloadResult

    /**
     * Cancel any ongoing downloads
     */
    fun cancelDownload()
}

