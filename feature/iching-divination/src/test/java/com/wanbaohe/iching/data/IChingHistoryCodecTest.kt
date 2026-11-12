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

package com.wanbaohe.iching.data

import com.wanbaohe.iching.domain.HexagramGenerator
import com.wanbaohe.iching.model.HexagramLine
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

class IChingHistoryCodecTest {
	private val generator = HexagramGenerator()

	@Test
	fun `record preserves line order and rebuilds result`() {
		val result = generator.create(
			"事业发展",
			listOf(7, 7, 7, 8, 7, 7).map(::HexagramLine),
		)
		val record = IChingHistoryRecord.from(result).copy(
			id = "record-1",
			createdAt = 123L,
			aiContent = "解读内容",
		)

		val decoded = IChingHistoryCodec.decode(IChingHistoryCodec.encode(listOf(record))).single()
		assertEquals(listOf(7, 7, 7, 8, 7, 7), decoded.lineValues)
		assertEquals("解读内容", decoded.aiContent)
		assertEquals(result, decoded.toResult(generator))
	}

	@Test
	fun `malformed json safely returns empty history`() {
		assertTrue(IChingHistoryCodec.decode("not-json").isEmpty())
	}

	@Test
	fun `invalid line records are filtered`() {
		val valid = IChingHistoryRecord(
			id = "valid",
			question = "",
			lineValues = List(6) { 7 },
			primaryNumber = 1,
		)
		val invalid = valid.copy(id = "invalid", lineValues = listOf(7, 7, 10))

		val decoded = IChingHistoryCodec.decode(IChingHistoryCodec.encode(listOf(valid, invalid)))
		assertEquals(listOf("valid"), decoded.map(IChingHistoryRecord::id))
	}

	@Test
	fun `unknown fields remain forward compatible`() {
		val raw = """[{"id":"legacy","question":"","lineValues":[8,8,8,8,8,8],"primaryNumber":2,"primaryName":"坤为地","futureField":true}]"""
		val decoded = IChingHistoryCodec.decode(raw).single()
		assertEquals("legacy", decoded.id)
		assertEquals(2, decoded.toResult(generator).primary.number)
	}

	@Test
	fun `append keeps newest first replaces same id and enforces limit`() {
		val records = (1..100).map(::record)
		val limited = IChingHistoryOperations.append(records, record(101), maxSize = 100)
		assertEquals(100, limited.size)
		assertEquals("101", limited.first().id)
		assertEquals("99", limited.last().id)

		val replacement = record(50).copy(question = "updated")
		val updated = IChingHistoryOperations.append(limited, replacement, maxSize = 100)

		assertEquals(100, updated.size)
		assertEquals("50", updated.first().id)
		assertEquals("updated", updated.first().question)
		assertEquals(1, updated.count { it.id == "50" })
		assertEquals("99", updated.last().id)
	}

	@Test
	fun `AI update and removal only affect matching record`() {
		val records = listOf(record(1), record(2))
		val withAI = IChingHistoryOperations.updateAIContent(records, "2", "saved insight")
		assertEquals("", withAI[0].aiContent)
		assertEquals("saved insight", withAI[1].aiContent)
		assertEquals(listOf("2"), IChingHistoryOperations.remove(withAI, "1").map { it.id })
	}

	private fun record(index: Int) = IChingHistoryRecord(
		id = index.toString(),
		question = "question-$index",
		lineValues = List(6) { 7 },
		primaryNumber = 1,
		createdAt = index.toLong(),
	)
}


