package com.wanbaohe.loancalculator.domain

import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * 贷款还款方式枚举
 */
enum class RepaymentMethod(val label: String, val description: String) {
    /** 等本等息：每月还固定本金 + 固定利息（按总利息平摊） */
    EQUAL_INSTALLMENT("等本等息", "月供、本金、利息每月相同"),

    /** 等额本息：每月还款额相同，本金逐月递增，利息逐月递减 */
    EQUAL_PAYMENT("等额本息", "月供每月相同，本金逐月递增，利息逐月递减"),

    /** 等额本金：每月还固定本金，利息逐月递减，月供逐月递减 */
    EQUAL_PRINCIPAL("等额本金", "月供每月递减，本金每月相同，利息每月递减"),

    /** 先息后本：每月只还利息，到期一次性还清全部本金 */
    INTEREST_FIRST("先息后本", "每月还固定利息，到期还全部本金");
}

/**
 * 单期还款数据（金额精度：分）
 * @param month        期数（从1开始）
 * @param payment      本期月还款总额（分）
 * @param principal    本期还本金（分）
 * @param interest     本期还利息（分）
 * @param remaining    还款后剩余本金（分）
 */
data class InstallmentItem(
    val month: Int,
    val payment: Long,
    val principal: Long,
    val interest: Long,
    val remaining: Long,
)

/**
 * 贷款计算结果（金额精度：分）
 * @param method           还款方式
 * @param totalLoan        贷款总额（分）
 * @param totalRepayment   还款总额（分）
 * @param totalInterest    总利息（分）
 * @param firstPayment     首月还款（分）
 * @param lastPayment      末月还款（分，等额本息/先息后本与首月相同）
 * @param schedule         逐期还款明细
 */
data class LoanResult(
    val method: RepaymentMethod,
    val totalLoan: Long,
    val totalRepayment: Long,
    val totalInterest: Long,
    val firstPayment: Long,
    val lastPayment: Long,
    val schedule: List<InstallmentItem>,
)

/**
 * 贷款核心计算器（纯函数，无副作用）
 *
 * 所有金额参数以"分"为单位传入，内部也以分计算后四舍五入，避免浮点误差。
 */
object LoanCalculator {

    /**
     * 执行贷款计算
     * @param principalFen  贷款金额（分）
     * @param termMonths    还款期数（月）
     * @param annualRatePct 年利率（百分比，如 4.9 表示 4.9%）
     * @param method        还款方式
     */
    fun calculate(
        principalFen: Long,
        termMonths: Int,
        annualRatePct: Double,
        method: RepaymentMethod,
    ): LoanResult = when (method) {
        RepaymentMethod.EQUAL_INSTALLMENT -> calcEqualInstallment(principalFen, termMonths, annualRatePct)
        RepaymentMethod.EQUAL_PAYMENT     -> calcEqualPayment(principalFen, termMonths, annualRatePct)
        RepaymentMethod.EQUAL_PRINCIPAL   -> calcEqualPrincipal(principalFen, termMonths, annualRatePct)
        RepaymentMethod.INTEREST_FIRST    -> calcInterestFirst(principalFen, termMonths, annualRatePct)
    }

    // ── 等本等息 ─────────────────────────────────────────────────────────────────
    private fun calcEqualInstallment(
        principalFen: Long,
        termMonths: Int,
        annualRatePct: Double,
    ): LoanResult {
        val monthlyRate = annualRatePct / 100.0 / 12.0
        // 每月固定本金
        val monthlyPrincipal = (principalFen.toDouble() / termMonths).roundToLong()
        // 每月固定利息（按本金 × 月利率）
        val monthlyInterest = (principalFen.toDouble() * monthlyRate).roundToLong()
        val monthlyPayment = monthlyPrincipal + monthlyInterest

        val schedule = (1..termMonths).map { month ->
            val remaining = principalFen - monthlyPrincipal * month
            InstallmentItem(
                month = month,
                payment = monthlyPayment,
                principal = monthlyPrincipal,
                interest = monthlyInterest,
                remaining = remaining.coerceAtLeast(0L),
            )
        }

        val totalRepayment = monthlyPayment * termMonths
        return LoanResult(
            method = RepaymentMethod.EQUAL_INSTALLMENT,
            totalLoan = principalFen,
            totalRepayment = totalRepayment,
            totalInterest = totalRepayment - principalFen,
            firstPayment = monthlyPayment,
            lastPayment = monthlyPayment,
            schedule = schedule,
        )
    }

    // ── 等额本息 ─────────────────────────────────────────────────────────────────
    private fun calcEqualPayment(
        principalFen: Long,
        termMonths: Int,
        annualRatePct: Double,
    ): LoanResult {
        val monthlyRate = annualRatePct / 100.0 / 12.0
        val p = principalFen.toDouble()
        val r = monthlyRate
        val n = termMonths

        // 月供公式：P × r × (1+r)^n / ((1+r)^n - 1)
        val factor = (1 + r).pow(n)
        val monthlyPayment = if (r == 0.0) {
            p / n
        } else {
            p * r * factor / (factor - 1)
        }
        val monthlyPaymentFen = monthlyPayment.roundToLong()

        var remaining = principalFen
        val schedule = mutableListOf<InstallmentItem>()
        for (month in 1..termMonths) {
            val interestFen = (remaining.toDouble() * r).roundToLong()
            val principalFenItem = if (month == termMonths) {
                // 最后一期：还清剩余本金（修正浮点累积误差）
                remaining
            } else {
                (monthlyPayment - interestFen.toDouble()).roundToLong()
            }
            val actualPayment = principalFenItem + interestFen
            remaining -= principalFenItem
            schedule.add(
                InstallmentItem(
                    month = month,
                    payment = actualPayment,
                    principal = principalFenItem,
                    interest = interestFen,
                    remaining = remaining.coerceAtLeast(0L),
                )
            )
        }

        val totalRepayment = schedule.sumOf { it.payment }
        return LoanResult(
            method = RepaymentMethod.EQUAL_PAYMENT,
            totalLoan = principalFen,
            totalRepayment = totalRepayment,
            totalInterest = totalRepayment - principalFen,
            firstPayment = schedule.first().payment,
            lastPayment = schedule.last().payment,
            schedule = schedule,
        )
    }

    // ── 等额本金 ─────────────────────────────────────────────────────────────────
    private fun calcEqualPrincipal(
        principalFen: Long,
        termMonths: Int,
        annualRatePct: Double,
    ): LoanResult {
        val monthlyRate = annualRatePct / 100.0 / 12.0
        // 每月固定还本金
        val monthlyPrincipal = (principalFen.toDouble() / termMonths).roundToLong()

        var remaining = principalFen
        val schedule = mutableListOf<InstallmentItem>()
        for (month in 1..termMonths) {
            val interestFen = (remaining.toDouble() * monthlyRate).roundToLong()
            val actualPrincipal = if (month == termMonths) remaining else monthlyPrincipal
            val payment = actualPrincipal + interestFen
            remaining -= actualPrincipal
            schedule.add(
                InstallmentItem(
                    month = month,
                    payment = payment,
                    principal = actualPrincipal,
                    interest = interestFen,
                    remaining = remaining.coerceAtLeast(0L),
                )
            )
        }

        val totalRepayment = schedule.sumOf { it.payment }
        return LoanResult(
            method = RepaymentMethod.EQUAL_PRINCIPAL,
            totalLoan = principalFen,
            totalRepayment = totalRepayment,
            totalInterest = totalRepayment - principalFen,
            firstPayment = schedule.first().payment,
            lastPayment = schedule.last().payment,
            schedule = schedule,
        )
    }

    // ── 先息后本 ─────────────────────────────────────────────────────────────────
    private fun calcInterestFirst(
        principalFen: Long,
        termMonths: Int,
        annualRatePct: Double,
    ): LoanResult {
        val monthlyRate = annualRatePct / 100.0 / 12.0
        val monthlyInterest = (principalFen.toDouble() * monthlyRate).roundToLong()

        val schedule = (1..termMonths).map { month ->
            val isLastMonth = month == termMonths
            // 最后一期归还全部本金 + 利息；其余期只还利息
            val principal = if (isLastMonth) principalFen else 0L
            val payment = principal + monthlyInterest
            val remaining = if (isLastMonth) 0L else principalFen
            InstallmentItem(
                month = month,
                payment = payment,
                principal = principal,
                interest = monthlyInterest,
                remaining = remaining,
            )
        }

        val totalRepayment = schedule.sumOf { it.payment }
        return LoanResult(
            method = RepaymentMethod.INTEREST_FIRST,
            totalLoan = principalFen,
            totalRepayment = totalRepayment,
            totalInterest = totalRepayment - principalFen,
            firstPayment = schedule.first().payment,
            lastPayment = schedule.last().payment,
            schedule = schedule,
        )
    }
}

