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

package com.shifenmiao.webview.resource

import com.shifenmiao.model.webview.WebResourceRuleDto

/**
 * 将远程下发的扁平 DTO 转换为强类型领域模型。
 *
 * 容错策略：单条规则不合法（缺字段、kind 未知）时返回 null，由调用方 filter 掉；
 * 这样一条坏规则不会污染整个列表。
 */
object WebResourceRuleMapper {

    fun toDomainOrNull(dto: WebResourceRuleDto): WebResourceRule? {
        val match = parseMatch(dto) ?: return null
        return when (dto.ruleKind) {
            WebResourceRuleDto.RULE_ASSET -> {
                val path = dto.assetPath?.trim().orEmpty()
                if (path.isEmpty()) null else WebResourceRule.AssetRule(match, path)
            }

            WebResourceRuleDto.RULE_REMOTE_URL -> {
                val real = dto.realUrl?.trim().orEmpty()
                if (real.isEmpty()) {
                    null
                } else {
                    val ttl = dto.cacheTtlSeconds
                        ?.takeIf { it > 0 }
                        ?.let { it * 1000L }
                    WebResourceRule.RemoteUrlRule(match, real, ttl)
                }
            }

            else -> null
        }
    }

    fun toDomainList(dtos: List<WebResourceRuleDto>?): List<WebResourceRule> =
        dtos.orEmpty().mapNotNull(::toDomainOrNull)

    private fun parseMatch(dto: WebResourceRuleDto): WebResourceMatch? {
        val value = dto.matchValue.trim()
        if (value.isEmpty()) return null
        return when (dto.matchKind) {
            WebResourceRuleDto.MATCH_HOST -> WebResourceMatch.Host(value)
            WebResourceRuleDto.MATCH_EXACT_URL -> WebResourceMatch.ExactUrl(value)
            WebResourceRuleDto.MATCH_URL_PREFIX -> WebResourceMatch.UrlPrefix(value)
            else -> null
        }
    }
}
