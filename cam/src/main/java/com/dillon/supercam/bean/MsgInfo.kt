package com.dillon.supercam.bean



class MsgInfo : BInfo() {
    val latestName: String? = null
    val list: MutableList<MsgInfoItem>? = null

    class MsgInfoItem : BInfo() {
        var duration: Int = 0
        var vip: Boolean = false
        var uID: String? = null
        var sID: String? = null
        var mMsg: String? = null
        var mTime: String? = null
        var mType: String? = null//text /image
    }
}