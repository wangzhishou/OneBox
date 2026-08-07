package com.wanbaohe.unitconverter.domain

object KinshipCalculator {

    fun resolve(
        gender: KinshipGender,
        steps: List<KinshipStep>,
    ): KinshipResult {
        if (steps.isEmpty()) {
            return KinshipResult(
                title = "自己",
                description = "请选择亲属链路后计算关系"
            )
        }
        val key = steps.joinToString(separator = ">") { it.route }
        val title = relationMap[key]?.invoke(gender) ?: buildDescriptiveTitle(gender, steps)
        return KinshipResult(
            title = title,
            description = steps.joinToString(separator = " · ") { it.label }
        )
    }

    private val relationMap = mapOf<String, (KinshipGender) -> String>(
        "father" to { "父亲" },
        "mother" to { "母亲" },
        "spouse" to { if (it == KinshipGender.Male) "妻子" else "丈夫" },
        "brother" to { "兄弟" },
        "sister" to { "姐妹" },
        "son" to { "儿子" },
        "daughter" to { "女儿" },
        "father>father" to { "爷爷" },
        "father>mother" to { "奶奶" },
        "mother>father" to { "外公" },
        "mother>mother" to { "外婆" },
        "father>brother" to { "伯叔" },
        "father>sister" to { "姑妈" },
        "mother>brother" to { "舅舅" },
        "mother>sister" to { "姨妈" },
        "brother>son" to { "侄子" },
        "brother>daughter" to { "侄女" },
        "sister>son" to { "外甥" },
        "sister>daughter" to { "外甥女" },
        "son>son" to { "孙子" },
        "son>daughter" to { "孙女" },
        "daughter>son" to { "外孙" },
        "daughter>daughter" to { "外孙女" },
        "spouse>father" to { if (it == KinshipGender.Male) "岳父" else "公公" },
        "spouse>mother" to { if (it == KinshipGender.Male) "岳母" else "婆婆" },
        "son>spouse" to { "儿媳" },
        "daughter>spouse" to { "女婿" },
        "brother>spouse" to { "兄弟配偶" },
        "sister>spouse" to { "姐妹配偶" },
        "father>brother>son" to { "堂兄弟" },
        "father>brother>daughter" to { "堂姐妹" },
        "father>sister>son" to { "表兄弟" },
        "father>sister>daughter" to { "表姐妹" },
        "mother>brother>son" to { "表兄弟" },
        "mother>brother>daughter" to { "表姐妹" },
        "mother>sister>son" to { "表兄弟" },
        "mother>sister>daughter" to { "表姐妹" },
        "father>father>father" to { "曾祖父" },
        "father>father>mother" to { "曾祖母" },
        "mother>mother>father" to { "外曾外公" },
        "mother>mother>mother" to { "外曾外婆" },
    )

    private fun buildDescriptiveTitle(
        gender: KinshipGender,
        steps: List<KinshipStep>,
    ): String {
        return steps.foldIndexed("我") { index, acc, step ->
            when (step) {
                KinshipStep.Spouse -> {
                    if (index == 0) {
                        if (gender == KinshipGender.Male) "${acc}的妻子" else "${acc}的丈夫"
                    } else {
                        "${acc}的配偶"
                    }
                }

                else -> "${acc}的${step.label}"
            }
        }
    }
}
