/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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

package com.t8rin.documentscanner.models

/**
 * This class contains the original document photo, and a cropper. The user can drag the corners
 * to make adjustments to the detected corners.
 *
 * @param originalPhotoFilePath the photo file path before cropping
 * @param originalPhotoWidth the original photo width
 * @param originalPhotoHeight the original photo height
 * @param corners the document's 4 corner points
 * @constructor creates a document
 */
class Document(
    val originalPhotoFilePath: String,
    val originalPhotoWidth: Int,
    val originalPhotoHeight: Int,
    var corners: Quad
)