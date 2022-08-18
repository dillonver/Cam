package com.dillon.supercam.bean

data class UserInfo(
    var userId: String?=null,
    var born: Long?=0L,
    var icon: String?=null,
    var name: String?=null ,
    var isVip: Boolean=true,
    var overVip: Long?=0L,
    var locTime: Int?=8,
    var uID: String? = null,
    var pro: Boolean = true,
    var mark: String? = null,
    var ban: Boolean = false,
    var sID: String? = null,
    var chatBan: Boolean = false,
)