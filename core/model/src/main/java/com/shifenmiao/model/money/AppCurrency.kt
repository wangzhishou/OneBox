package com.shifenmiao.model.money

import com.shifenmiao.model.channel.FlavorType
import java.util.Locale

/**
 * 金额展示币种。
 *
 * 账目金额一律以「分」(主单位的 1/100)存储;展示时小数位由 [fractionDigits] 决定,
 * 零小数币种(KRW / IDR / JPY)不显示小数。
 *
 * 默认值按渠道 + 语言推导(见 [forFlavorAndLocale]):国内渠道人民币,海外渠道跟随语言,
 * 用户在「记账 → 设置 → 币种」里可以显式改,改了之后以存储值为准。
 */
enum class AppCurrency(
    val code: String,
    val symbol: String,
    val fractionDigits: Int,
) {
    CNY("CNY", "¥", 2),
    USD("USD", "$", 2),
    EUR("EUR", "€", 2),
    GBP("GBP", "£", 2),
    INR("INR", "₹", 2),
    JPY("JPY", "¥", 0),
    KRW("KRW", "₩", 0),
    IDR("IDR", "Rp", 0),
    PHP("PHP", "₱", 2),
    BRL("BRL", "R$", 2),
    TRY("TRY", "₺", 2),
    RUB("RUB", "₽", 2);

    companion object {

        /** 未显式设置时的默认币种 */
        val default: AppCurrency
            get() = forFlavorAndLocale(FlavorType.fromName(), Locale.getDefault())

        /**
         * 国内渠道(onebox/xiaomi/yyb/oppo/vivo/huawei)一律人民币;
         * 海外渠道(google/foss)按语言映射,识别不到时用美元。
         */
        fun forFlavorAndLocale(flavor: FlavorType, locale: Locale): AppCurrency {
            if (!flavor.isOverseas) return CNY
            return when (locale.language.lowercase(Locale.ROOT)) {
                "zh" -> CNY
                "hi" -> INR
                "de", "es" -> EUR
                "pt" -> BRL
                "ja" -> JPY
                "ko" -> KRW
                "in", "id" -> IDR
                "fil", "tl" -> PHP
                "ru" -> RUB
                "tr" -> TRY
                else -> USD
            }
        }

        fun fromCode(code: String?): AppCurrency? = code
            ?.takeIf { it.isNotBlank() }
            ?.let { stored -> entries.firstOrNull { it.code == stored } }

        /** 已存储的设置优先;未设置或已失效时回退 [default] */
        fun effective(storedCode: String?): AppCurrency = fromCode(storedCode) ?: default
    }
}

/**
 * 金额格式化唯一入口:符号、千分位、小数位都在这里,避免各页面各写一套。
 * 千分位与小数点跟随 [Locale.getDefault] 的语言习惯(如德语 1.234,50)。
 */
object MoneyFormat {

    /** 带货币符号的金额,负数为前置减号(如 `-₹1,234.50`、`₩12,300`) */
    fun amount(
        cents: Long,
        currency: AppCurrency,
        locale: Locale = Locale.getDefault(),
    ): String {
        val sign = if (cents < 0) "-" else ""
        return sign + currency.symbol + value(cents, currency, locale)
    }

    /** 不带符号的金额(如 `1,234.50`),用于自行拼接符号的场景 */
    fun value(
        cents: Long,
        currency: AppCurrency,
        locale: Locale = Locale.getDefault(),
    ): String {
        val major = kotlin.math.abs(cents) / 100.0
        return String.format(locale, "%,.${currency.fractionDigits}f", major)
    }

    /**
     * 编辑框回填用:固定 `.` 作小数点、不带千分位(与记账输入框的解析一致),
     * 零小数币种只给整数部分。
     */
    fun inputText(cents: Long, currency: AppCurrency): String {
        val sign = if (cents < 0) "-" else ""
        val absolute = kotlin.math.abs(cents)
        val major = absolute / 100
        if (currency.fractionDigits == 0) return "$sign$major"
        val minor = absolute % 100
        return "$sign$major.${minor.toString().padStart(2, '0')}"
    }
}
