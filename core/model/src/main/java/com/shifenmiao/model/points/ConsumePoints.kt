package com.shifenmiao.model.points

import java.util.Date

class ConsumePoints {
    var points: Int = 0
    var time: Long = Date().time
    var desc: String = ""
    var source: String = ""
}