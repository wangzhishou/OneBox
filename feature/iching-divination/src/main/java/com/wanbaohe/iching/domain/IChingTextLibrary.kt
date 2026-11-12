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

package com.wanbaohe.iching.domain

import android.content.Context
import com.wanbaohe.iching.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** 单卦的本地化文本:卦名、卦辞、六爻爻辞(自下而上,不含爻位前缀) */
data class HexagramText(
    val name: String,
    val judgment: String,
    val lines: List<String>,
)

@Serializable
private data class HexagramTextDto(
    val number: Int,
    val name: String,
    val judgment: String,
    val lines: List<String>,
)

@Serializable
private data class IChingTextsDto(
    val trigrams: Map<String, String>,
    val hexagrams: List<HexagramTextDto>,
)

/**
 * 易经文本库:64 卦卦名/卦辞/爻辞与八卦象名,从 raw 资源按当前 locale 加载并缓存
 * (res/raw 英文,res/raw-zh-rCN 中文,其余 locale 走资源回退)。加载失败降级为卦号占位,保证界面可用。
 */
@Singleton
class IChingTextLibrary @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var cache: Pair<String, IChingTextsDto>? = null

    /** 当前资源 locale 是否为中文(AI prompt 与占位文案据此选择语言) */
    val isChinese: Boolean
        get() = localeTag().startsWith("zh")

    fun hexagram(number: Int): HexagramText {
        val dto = load().hexagrams.firstOrNull { it.number == number } ?: return fallback(number)
        return HexagramText(dto.name, dto.judgment, dto.lines)
    }

    fun trigramName(code: Int): String =
        load().trigrams[code.toString()] ?: code.toString()

    private fun fallback(number: Int): HexagramText = HexagramText(
        name = if (isChinese) "第 $number 卦" else "Hexagram $number",
        judgment = "",
        lines = emptyList(),
    )

    private fun load(): IChingTextsDto {
        val tag = localeTag()
        cache?.let { (cachedTag, texts) -> if (cachedTag == tag) return texts }
        return synchronized(this) {
            val currentTag = localeTag()
            cache?.let { (cachedTag, texts) -> if (cachedTag == currentTag) return@synchronized texts }
            val texts = runCatching {
                context.resources.openRawResource(R.raw.iching_texts).bufferedReader().use { reader ->
                    json.decodeFromString<IChingTextsDto>(reader.readText())
                }
            }.getOrElse { IChingTextsDto(trigrams = emptyMap(), hexagrams = emptyList()) }
            cache = currentTag to texts
            texts
        }
    }

    private fun localeTag(): String =
        context.resources.configuration.locales.takeIf { !it.isEmpty }?.get(0)?.toLanguageTag()
            ?: Locale.getDefault().toLanguageTag()
}
