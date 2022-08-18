package xyz.dcln.bys.bean

data class Msg(
    var pusher: String? = null,
    var pusherIcon: String? = null,
    var receiver: String? = null,
    var status: String? = null,
    var time: Long? = null,
    var type: String? = null,
    var content: String? = null
)