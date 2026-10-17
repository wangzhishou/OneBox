package com.wanbaohe.loancalculator.viewmodel

import java.util.Locale
import com.shifenmiao.model.money.MoneyFormat
import com.shifenmiao.model.money.AppCurrency
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.wanbaohe.loancalculator.R
import com.wanbaohe.loancalculator.domain.LoanCalculator
import com.wanbaohe.loancalculator.domain.LoanResult
import com.wanbaohe.loancalculator.domain.RepaymentMethod
import kotlin.math.roundToLong

/** 将元（字符串）转换为分（Long），无效时返回 0 */
fun String.yuanToFen(): Long {
    val d = toDoubleOrNull() ?: return 0L
    return (d * 100).roundToLong()
}

/** 将分（Long）格式化为当前币种的展示字符串，如 ¥1,234.50 / ₩12,300 */
fun Long.fenToMoneyString(currency: AppCurrency, locale: Locale = Locale.getDefault()): String =
    MoneyFormat.amount(this, currency, locale)

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

    /** 输入校验错误资源（各字段独立） */
    @get:StringRes
    var amountError by mutableStateOf<Int?>(null)
        private set

    @get:StringRes
    var termError by mutableStateOf<Int?>(null)
        private set

    @get:StringRes
    var rateError by mutableStateOf<Int?>(null)
        private set

    @get:StringRes
    var methodError by mutableStateOf<Int?>(null)
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
            amountError = R.string.loan_input_error_amount
            valid = false
        }
        if (termMonths <= 0) {
            termError = R.string.loan_input_error_term
            valid = false
        }
        val rate = annualRate.toDoubleOrNull()
        if (rate == null || rate <= 0.0) {
            rateError = R.string.loan_input_error_rate
            valid = false
        }
        if (repaymentMethod == null) {
            methodError = R.string.loan_input_error_method
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

