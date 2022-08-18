package com.dillon.supercam.bean

class AppInfo : BInfo() {
    var appPkg: String? = null
    var appName: String? = null
    var versionName: String? = null
    var versionCode: Int? = 0
    var packagePath: String? = null //安装包路径
    var isSystem: Boolean = false//是否是系统app
    var firstInstallTime: Long? = 0//第一次安装app的时间
    var lastUpdateTime: Long? = 0//最近更新app的时间

}