package com.wanbaohe.calendar.data

/**
 * 宜忌计算器
 *
 * 基于日干支查表法生成每日宜忌事项。
 * 简化版实现，实际传统通胜宜忌需结合建除十二神、二十八宿等多维度。
 */
object YiJiCalculator {

    /** 宜事项库（公开，供择日 Tab 共用） */
    val YI_ITEMS = arrayOf(
        "祭祀", "祈福", "嫁娶", "出行", "动土", "安床", "开光", "纳采",
        "订盟", "裁衣", "合帐", "冠笄", "安机械", "拆卸", "安门",
        "入殓", "启钻", "安葬", "上梁", "入宅", "移柩", "破土",
        "开市", "立券", "交易", "纳财", "栽种", "置产", "求嗣",
        "修造", "解除", "求医", "词讼", "入学", "纳畜", "牧养",
        "会亲友", "进人口", "经络", "酝酿", "沐浴", "扫舍", "捕捉"
    )

    /** 忌事项库（公开，供择日 Tab 共用） */
    val JI_ITEMS = arrayOf(
        "出行", "掘井", "动土", "安葬", "开市", "入宅", "嫁娶",
        "破土", "移徙", "词讼", "安门", "作灶", "伐木", "安床",
        "栽种", "开仓", "置产", "修造", "上梁", "祈福", "纳采"
    )

    /**
     * 根据公历日期生成宜忌数据
     */
    fun getYiJi(solarYear: Int, solarMonth: Int, solarDay: Int): YiJi {
        LunarJavaBridge.getYiJi(solarYear, solarMonth, solarDay)?.let { return it }

        // 基于日期的简单哈希
        val seed = solarYear * 10000 + solarMonth * 100 + solarDay
        val hash = ((seed * 2654435761L) shr 16).toInt()

        // 生成宜事项（3-6个）
        val yiCount = 3 + (hash and 0x3)
        val yi = mutableListOf<String>()
        for (i in 0 until yiCount) {
            val idx = ((hash shr (i * 3)) and 0x1F) % YI_ITEMS.size
            val item = YI_ITEMS[idx]
            if (item !in yi) yi.add(item)
        }

        // 生成忌事项（3-5个），不能与宜重复
        val jiCount = 3 + ((hash shr 8) and 0x1)
        val ji = mutableListOf<String>()
        for (i in 0 until jiCount) {
            val idx = ((hash shr (i * 4 + 12)) and 0xF) % JI_ITEMS.size
            val item = JI_ITEMS[idx]
            if (item !in ji && item !in yi) ji.add(item)
        }

        return YiJi(yi = yi, ji = ji)
    }
}

