package com.wanbaohe.bookkeeping.model

import com.shifenmiao.database.bookkeeping.entity.BookkeepingCategoryEntity

/**
 * 系统预置的记账分类。
 * 首次启动时通过 `BookkeepingRepository.ensureDefaults` 写入数据库，用户可在此基础上增减自定义分类。
 *
 * 支出 26 项 / 入账 11 项 / 不计入收支 5 项。
 *
 * 分类名经 [localizedDefaultCategoryName] 按当前 locale 解析: FeatureDatabase 按语言分库,
 * 每个语言的库在首次播种时写入对应语言的分类名。
 */
object DefaultCategories {

    private val E = BookkeepingRecordType.EXPENSE.code
    private val I = BookkeepingRecordType.INCOME.code
    private val X = BookkeepingRecordType.EXCLUDED.code

    /** 每次调用重新解析分类名(进程内切换语言后播种新库也能拿到正确语言) */
    fun all(): List<BookkeepingCategoryEntity> = listOf(

        // ── 支出 (26 项) ─────────────────────────────────────────────────────────
        category("c_food",          E, "food",          0),
        category("c_transport",     E, "transport",     1),
        category("c_clothing",      E, "clothing",      2),
        category("c_shopping",      E, "shopping",      3),
        category("c_housing",       E, "housing",       4),
        category("c_rent",          E, "rent",          5),
        category("c_digital",       E, "digital",       6),
        category("c_beauty",        E, "beauty",        7),
        category("c_service",       E, "service",       8),
        category("c_education",     E, "education",     9),
        category("c_entertainment", E, "entertainment", 10),
        category("c_game",          E, "game",          11),
        category("c_sports",        E, "sports",        12),
        category("c_life",          E, "life",          13),
        category("c_subscription",  E, "subscription",  14),
        category("c_travel",        E, "travel",        15),
        category("c_pet",           E, "pet",           16),
        category("c_medical",       E, "medical",       17),
        category("c_insurance",     E, "insurance",     18),
        category("c_charity",       E, "charity",       19),
        category("c_gift",          E, "gift",          20),
        category("c_social",        E, "social",        21),
        category("c_office",        E, "office",        22),
        category("c_redpacket",     E, "redpacket",     23),
        category("c_family_card",   E, "family",        24),
        category("c_other_exp",     E, "other",         25),

        // ── 入账 (11 项) ─────────────────────────────────────────────────────────
        category("c_salary",        I, "salary",        0),
        category("c_bonus",         I, "bonus",         1),
        category("c_business",      I, "business",      2),
        category("c_parttime",      I, "parttime",      3),
        category("c_invest_return", I, "invest_return", 4),
        category("c_rental_income", I, "rental_income", 5),
        category("c_dividend",      I, "dividend",      6),
        category("c_recv_red",      I, "recv_red",      7),
        category("c_recv_transfer", I, "transfer",      8),
        category("c_refund",        I, "refund",        9),
        category("c_income_other",  I, "income_other",  10),

        // ── 不计入收支 (5 项) ────────────────────────────────────────────────────
        category("c_investment",    X, "investment",    0),
        category("c_loan",          X, "loan",          1),
        category("c_creditcard",    X, "creditcard",    2),
        category("c_transfer_out",  X, "transfer_out",  3),
        category("c_excl_other",    X, "excl_other",    4),
    )

    private fun category(id: String, type: Int, iconKey: String, sortOrder: Int) =
        BookkeepingCategoryEntity(
            id,
            localizedDefaultCategoryName(id) ?: id,
            type,
            iconKey,
            sortOrder,
            true,
        )
}
