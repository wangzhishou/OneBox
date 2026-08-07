package com.wanbaohe.bookkeeping.component

import com.wanbaohe.bookkeeping.model.BookkeepingRecordType
import com.wanbaohe.bookkeeping.model.BookkeepingRecordUi
import com.wanbaohe.bookkeeping.model.BookkeepingUiState
import com.wanbaohe.bookkeeping.service.BookkeepingService
import java.time.LocalDate

/**
 * 账目编辑表单状态机 — 接管 amount/note/date/category/type 的输入,
 * submit 时通过 [BookkeepingService] 落库。
 *
 * 内部不持有 StateFlow,纯函数式:外部传入旧 state,返回新 state。
 */
internal class RecordEditor(
    private val service: BookkeepingService,
) {
    companion object {
        private const val MAX_DECIMAL_PLACES = 2
    }

    fun startAdd(state: BookkeepingUiState, defaultCategoryId: String?): BookkeepingUiState {
        return state.copy(
            editingRecordId = null,
            amountInput = "",
            noteInput = "",
            selectedDate = LocalDate.now(),
            selectedCategoryId = defaultCategoryId,
        )
    }

    fun startEdit(state: BookkeepingUiState, record: BookkeepingRecordUi, fallbackCategoryId: String?): BookkeepingUiState {
        val amountStr = formatCentsToInput(record.amountCents)
        return state.copy(
            editingRecordId = record.id,
            amountInput = amountStr,
            noteInput = record.note,
            selectedDate = record.happenedDate,
            selectedType = record.type,
            selectedCategoryId = record.categoryId ?: fallbackCategoryId,
        )
    }

    fun hide(state: BookkeepingUiState): BookkeepingUiState =
        state.copy(editingRecordId = null)

    fun changeType(state: BookkeepingUiState, type: BookkeepingRecordType, categoryId: String?): BookkeepingUiState =
        state.copy(selectedType = type, selectedCategoryId = categoryId)

    fun changeDate(state: BookkeepingUiState, date: LocalDate): BookkeepingUiState =
        state.copy(selectedDate = date.coerceAtMost(LocalDate.now()))

    fun changeAmount(state: BookkeepingUiState, raw: String): BookkeepingUiState {
        val normalized = raw.filter { it.isDigit() || it == '.' }
        val dotCount = normalized.count { it == '.' }
        val safe = if (dotCount > 1) normalized.dropLast(1) else normalized
        val finalValue = if (safe == ".") "0." else safe
        return state.copy(amountInput = finalValue)
    }

    fun appendDigit(state: BookkeepingUiState, digit: Char): BookkeepingUiState {
        val current = state.amountInput
        if (digit == '.' && current.contains('.')) return state
        if (digit == '.' && current.isEmpty()) return state.copy(amountInput = "0.")
        if (digit == '0' && current == "0") return state
        if (digit != '.' && current == "0") return state.copy(amountInput = digit.toString())
        val dotIndex = current.indexOf('.')
        if (dotIndex >= 0 && current.length - dotIndex - 1 >= MAX_DECIMAL_PLACES) return state
        return state.copy(amountInput = current + digit)
    }

    fun deleteLast(state: BookkeepingUiState): BookkeepingUiState {
        if (state.amountInput.isEmpty()) return state
        return state.copy(amountInput = state.amountInput.dropLast(1))
    }

    fun changeNote(state: BookkeepingUiState, value: String): BookkeepingUiState =
        state.copy(noteInput = value)

    fun selectCategory(state: BookkeepingUiState, categoryId: String): BookkeepingUiState =
        state.copy(selectedCategoryId = categoryId)

    /** 如果输入合法返回提交所需 input,否则 null。 */
    fun buildInput(state: BookkeepingUiState): BookkeepingService.RecordInput? {
        val categoryId = state.selectedCategoryId ?: return null
        val amountCents = service.parseAmountToCents(state.amountInput)?.takeIf { it > 0 } ?: return null
        return BookkeepingService.RecordInput(
            categoryId = categoryId,
            type = state.selectedType,
            amountCents = amountCents,
            note = state.noteInput,
            happenedDate = state.selectedDate,
        )
    }

    private fun formatCentsToInput(cents: Long): String = if (cents % 100 == 0L) {
        (cents / 100).toString()
    } else {
        val major = cents / 100
        val minor = cents % 100
        "$major.${minor.toString().padStart(2, '0')}"
    }
}
