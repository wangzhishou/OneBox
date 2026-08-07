package com.wanbaohe.unitconverter.domain

enum class KinshipStep(
    val route: String,
    val label: String,
) {
    Father("father", "父亲"),
    Mother("mother", "母亲"),
    Spouse("spouse", "配偶"),
    Brother("brother", "兄弟"),
    Sister("sister", "姐妹"),
    Son("son", "儿子"),
    Daughter("daughter", "女儿");

    companion object {
        fun fromRoute(route: String): KinshipStep? {
            return values().firstOrNull { it.route == route }
        }
    }
}
