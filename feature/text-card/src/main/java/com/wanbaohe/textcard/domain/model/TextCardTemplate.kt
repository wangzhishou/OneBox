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

package com.wanbaohe.textcard.domain.model

import androidx.annotation.StringRes
import com.wanbaohe.textcard.R

data class TextCardTemplate(
    val id: String,
    @get:StringRes val titleRes: Int? = null,
    @get:StringRes val descRes: Int? = null,
    val customTitle: String? = null,
    val customDescription: String? = null,
    val applyStyle: TextCardDocument.() -> TextCardDocument,
) {
    fun applyTo(document: TextCardDocument): TextCardDocument =
        document.applyStyle().copy(templateId = id)

    companion object {
        val all: List<TextCardTemplate> = listOf(
            TextCardTemplate(
                id = "quote_gradient",
                titleRes = R.string.textcard_template_quote,
                descRes = R.string.textcard_template_quote_desc,
                applyStyle = {
                    copy(
                        canvas = CanvasSpec.Portrait,
                        background = GradientPresets.default,
                        backgroundOpacity = 1f,
                        title = title.copy(
                            baseSizeRatio = 0.078f,
                            alignment = CardTextAlignment.Left,
                            lineSpacingMultiplier = 1.15f
                        ),
                        body = body.copy(
                            baseSizeRatio = 0.034f,
                            alignment = CardTextAlignment.Left,
                            lineSpacingMultiplier = 1.4f
                        )
                    )
                }
            ),
            TextCardTemplate(
                id = "lined_notes",
                titleRes = R.string.textcard_template_notes,
                descRes = R.string.textcard_template_notes_desc,
                applyStyle = {
                    copy(
                        canvas = CanvasSpec.Portrait,
                        background = BackgroundSpec.Paper(PaperKind.Lined),
                        backgroundOpacity = 1f,
                        title = title.copy(
                            baseSizeRatio = 0.068f,
                            alignment = CardTextAlignment.Left
                        ),
                        body = body.copy(
                            baseSizeRatio = 0.034f,
                            alignment = CardTextAlignment.Left,
                            lineSpacingMultiplier = 1.55f
                        )
                    )
                }
            ),
            TextCardTemplate(
                id = "kraft_excerpt",
                titleRes = R.string.textcard_template_excerpt,
                descRes = R.string.textcard_template_excerpt_desc,
                applyStyle = {
                    copy(
                        canvas = CanvasSpec.Portrait,
                        background = BackgroundSpec.Paper(PaperKind.Kraft),
                        backgroundOpacity = 1f,
                        title = title.copy(
                            baseSizeRatio = 0.072f,
                            alignment = CardTextAlignment.Center
                        ),
                        body = body.copy(
                            baseSizeRatio = 0.036f,
                            alignment = CardTextAlignment.Justify,
                            lineSpacingMultiplier = 1.5f
                        ),
                        author = author.copy(alignment = CardTextAlignment.Right),
                        source = source.copy(alignment = CardTextAlignment.Right)
                    )
                }
            ),
            TextCardTemplate(
                id = "simple_notice",
                titleRes = R.string.textcard_template_notice,
                descRes = R.string.textcard_template_notice_desc,
                applyStyle = {
                    copy(
                        canvas = CanvasSpec.Square,
                        background = BackgroundSpec.None,
                        backgroundOpacity = 1f,
                        title = title.copy(
                            baseSizeRatio = 0.072f,
                            alignment = CardTextAlignment.Left
                        ),
                        body = body.copy(
                            baseSizeRatio = 0.036f,
                            alignment = CardTextAlignment.Left,
                            lineSpacingMultiplier = 1.4f
                        )
                    )
                }
            )
        )

        fun byId(id: String): TextCardTemplate = all.firstOrNull { it.id == id } ?: all.first()
    }
}
