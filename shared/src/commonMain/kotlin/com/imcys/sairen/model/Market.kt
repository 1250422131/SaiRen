package com.imcys.sairen.model

data class Market(
    val name: String,
    val params: String,
)

val marketList = listOf(
    Market("A股", "m:0+t:6,m:0+t:80,m:1+t:2,m:1+t:23"),
    Market("B股", "m:0+t:7,m:1+t:3"),
    Market("北交所", "m:0+t:81+s:2048"),
    Market("美股", "m:105,m:106,m:107"),
    Market("沪深", "m:1+s:2,m:0+s:2"),
    Market("港股", "m:128+s:3")
)
