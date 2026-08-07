package com.wanbaohe.bookkeeping.model

import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.bookkeeping.R

/**
 * 预置分类 id → 多语言字符串资源 的映射。
 *
 * 数据库里存的是种子时的名字(可能与应用当前语言不一致, 例如旧库),
 * UI 展示与种子生成都走这里按当前 locale 解析; 用户自建分类返回 null, 用其自命名。
 */
fun defaultCategoryNameResId(id: String): Int? = when (id) {
    "c_food" -> R.string.bookkeeping_cat_food
    "c_transport" -> R.string.bookkeeping_cat_transport
    "c_clothing" -> R.string.bookkeeping_cat_clothing
    "c_shopping" -> R.string.bookkeeping_cat_shopping
    "c_housing" -> R.string.bookkeeping_cat_housing
    "c_rent" -> R.string.bookkeeping_cat_rent
    "c_digital" -> R.string.bookkeeping_cat_digital
    "c_beauty" -> R.string.bookkeeping_cat_beauty
    "c_service" -> R.string.bookkeeping_cat_service
    "c_education" -> R.string.bookkeeping_cat_education
    "c_entertainment" -> R.string.bookkeeping_cat_entertainment
    "c_game" -> R.string.bookkeeping_cat_game
    "c_sports" -> R.string.bookkeeping_cat_sports
    "c_life" -> R.string.bookkeeping_cat_life
    "c_subscription" -> R.string.bookkeeping_cat_subscription
    "c_travel" -> R.string.bookkeeping_cat_travel
    "c_pet" -> R.string.bookkeeping_cat_pet
    "c_medical" -> R.string.bookkeeping_cat_medical
    "c_insurance" -> R.string.bookkeeping_cat_insurance
    "c_charity" -> R.string.bookkeeping_cat_charity
    "c_gift" -> R.string.bookkeeping_cat_gift
    "c_social" -> R.string.bookkeeping_cat_social
    "c_office" -> R.string.bookkeeping_cat_office
    "c_redpacket" -> R.string.bookkeeping_cat_redpacket
    "c_family_card" -> R.string.bookkeeping_cat_family_card
    "c_other_exp" -> R.string.bookkeeping_cat_other_exp
    "c_salary" -> R.string.bookkeeping_cat_salary
    "c_bonus" -> R.string.bookkeeping_cat_bonus
    "c_business" -> R.string.bookkeeping_cat_business
    "c_parttime" -> R.string.bookkeeping_cat_parttime
    "c_invest_return" -> R.string.bookkeeping_cat_invest_return
    "c_rental_income" -> R.string.bookkeeping_cat_rental_income
    "c_dividend" -> R.string.bookkeeping_cat_dividend
    "c_recv_red" -> R.string.bookkeeping_cat_recv_red
    "c_recv_transfer" -> R.string.bookkeeping_cat_recv_transfer
    "c_refund" -> R.string.bookkeeping_cat_refund
    "c_income_other" -> R.string.bookkeeping_cat_income_other
    "c_investment" -> R.string.bookkeeping_cat_investment
    "c_loan" -> R.string.bookkeeping_cat_loan
    "c_creditcard" -> R.string.bookkeeping_cat_creditcard
    "c_transfer_out" -> R.string.bookkeeping_cat_transfer_out
    "c_excl_other" -> R.string.bookkeeping_cat_excl_other
    else -> null
}

/** 预置分类的本地化显示名; 非预置分类返回 null */
fun localizedDefaultCategoryName(id: String): String? =
    defaultCategoryNameResId(id)?.let { AppContext.getString(it) }
