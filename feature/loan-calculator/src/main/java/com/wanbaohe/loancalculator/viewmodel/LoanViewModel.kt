package com.wanbaohe.loancalculator.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.wanbaohe.loancalculator.domain.LoanCalculator
import com.wanbaohe.loancalculator.domain.LoanResult
import com.wanbaohe.loancalculator.domain.RepaymentMethod
import kotlin.math.roundToLong

/** 将元（字符串）转换为分（Long），无效时返回 0 */
fun String.yuanToFen(): Long {
    val d = toDoubleOrNull() ?: return 0L
    return (d * 100).roundToLong()
}

/** 将分（Long）格式化为 ¥X,XXX.XX 展示字符串 */
fun Long.fenToYuanString(): String {
    val negative = this < 0
    val abs = if (negative) -this else this
    val major = abs / 100
    val minor = abs % 100
    val majorFormatted = major.toString().reversed()
        .chunked(3).joinToString(",").reversed()
    val sign = if (negative) "-" else ""
    return "${sign}¥${majorFormatted}.${minor.toString().padStart(2, '0')}"
}

/**
 * UI 状态，持有所有输入字段和计算结果。
 * 使用 Compose State 而非 StateFlow，避免不必要的协程开销（纯同步计算）。
 */
class LoanViewModel : ViewModel() {

    /** 贷款金额（元，字符串） */
    var amount by mutableStateOf("")
        private set

    /** 贷款期数（月），0 表示未选择 */
    var termMonths by mutableStateOf(0)
        private set

    /** 年利率（%，字符串） */
    var annualRate by mutableStateOf("")
        private set

    /** 已选还款方式，null 表示未选择 */
    var repaymentMethod by mutableStateOf<RepaymentMethod?>(null)
        private set

    /** 计算结果，null 表示尚未计算 */
    var result by mutableStateOf<LoanResult?>(null)
        private set

    /** 输入校验错误信息（各字段独立） */
    var amountError by mutableStateOf<String?>(null)
        private set
    var termError by mutableStateOf<String?>(null)
        private set
    var rateError by mutableStateOf<String?>(null)
        private set
    var methodError by mutableStateOf<String?>(null)
        private set

    // ── 输入更新 ──────────────────────────────────────────────────────────────

    fun updateAmount(value: String) {
        amount = value
        amountError = null
        result = null
    }

    fun updateTermMonths(months: Int) {
        termMonths = months
        termError = null
        result = null
    }

    fun updateAnnualRate(value: String) {
        annualRate = value
        rateError = null
        result = null
    }

    fun updateRepaymentMethod(method: RepaymentMethod) {
        repaymentMethod = method
        methodError = null
        result = null
    }

    // ── 计算触发 ──────────────────────────────────────────────────────────────

    /**
     * 校验所有输入后执行计算，校验失败时设置对应错误提示，返回 false；
     * 校验通过时同步完成计算，返回 true。
     */
    fun calculate(): Boolean {
        var valid = true

        val principalFen = amount.yuanToFen()
        if (principalFen <= 0) {
            amountError = "请输入有效的贷款金额"
            valid = false
        }
        if (termMonths <= 0) {
            termError = "请选择贷款期数"
            valid = false
        }
        val rate = annualRate.toDoubleOrNull()
        if (rate == null || rate <= 0.0) {
            rateError = "请输入有效的年利率"
            valid = false
        }
        if (repaymentMethod == null) {
            methodError = "请选择还款方式"
            valid = false
        }

        if (!valid) return false

        result = LoanCalculator.calculate(
            principalFen = principalFen,
            termMonths = termMonths,
            annualRatePct = rate!!,
            method = repaymentMethod!!,
        )
        return true
    }

    /** 重置所有状态 */
    fun reset() {
        amount = ""
        termMonths = 0
        annualRate = ""
        repaymentMethod = null
        result = null
        amountError = null
        termError = null
        rateError = null
        methodError = null
    }
}

