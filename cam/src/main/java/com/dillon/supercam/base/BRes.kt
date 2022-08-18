package com.dillon.supercam.base

import java.io.Serializable


/**
 * Created by dillon on 2017/6/16.
 */
//BaseResponse
open class BRes<T> : Serializable {
    var code: Int = 0//状态码
    var message: String? = null//信息
    var msg: String? = null//信息
    var data: T? = null
}
