package com.wanbaohe.unitconverter.domain

enum class UnitConverterTab(
    val route: String,
) {
    Calculator("calculator"),
    Converter("converter"),
    Relative("relative");

    companion object {
        fun fromRoute(route: String?) =
            values().firstOrNull { it.route.equals(route, ignoreCase = true) } ?: Converter
    }
}
