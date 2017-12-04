package com.hx.hongbao

data class ResultInfo(
        var name: String = "",
        var money: String = "",
        var 下注: Int = 0,
        var 梭哈: Boolean = false,
        var 倍率: Int = 1,
        var 输赢: Int = 0,
        var time: String = "") {

    fun getAllMoney(): Int {
        val m = money.toFloat() * 100
        return m.toInt()
    }
}
