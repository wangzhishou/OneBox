package com.shifenmiao.model.points

import java.util.Date

class RewardPoints {
    var points: Int = 0
    var time: Long = Date().time
    var desc: String = ""
    var source: String = ""
    var bizId: String = ""
}

