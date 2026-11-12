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

package com.wanbaohe.app.ui

import androidx.compose.runtime.mutableStateMapOf
import com.shifenmiao.ai.agent.tool.AgentUserQuestionItem

class AIQuestionFormState {
    private val textAnswers = mutableStateMapOf<String, String>()
    private val selectedOptions = mutableStateMapOf<String, Set<String>>()

    fun updateTextAnswer(questionName: String, value: String) {
        textAnswers[questionName] = value
    }

    fun selectSingleOption(questionName: String, value: String) {
        selectedOptions[questionName] = setOf(value)
    }

    fun toggleMultipleOption(questionName: String, value: String) {
        val current = selectedOptions[questionName].orEmpty()
        selectedOptions[questionName] = if (value in current) {
            current - value
        } else {
            current + value
        }
    }

    fun getTextAnswer(questionName: String): String {
        return textAnswers[questionName].orEmpty()
    }

    fun isOptionSelected(questionName: String, value: String): Boolean {
        return value in selectedOptions[questionName].orEmpty()
    }

    fun isValid(questions: List<AgentUserQuestionItem>): Boolean {
        return questions.all { question ->
            !question.required || hasAnswer(question)
        }
    }

    fun buildAnswers(questions: List<AgentUserQuestionItem>): Map<String, Any> {
        return buildMap {
            questions.forEach { question ->
                if (!hasAnswer(question)) return@forEach
                if (question.isChoiceQuestion) {
                    val selected = selectedOptions[question.name].orEmpty().toList()
                    if (question.multiSelect) {
                        put(question.name, selected)
                    } else {
                        put(question.name, selected.first())
                    }
                } else {
                    put(question.name, getTextAnswer(question.name).trim())
                }
            }
        }
    }

    private fun hasAnswer(question: AgentUserQuestionItem): Boolean {
        return if (question.isChoiceQuestion) {
            selectedOptions[question.name].orEmpty().isNotEmpty()
        } else {
            getTextAnswer(question.name).trim().isNotEmpty()
        }
    }
}
