package com.dillon.supercam.bean


class ConfigInfo : BInfo() {
    var urlH: String? = null//Home背景图
    var urlS: String? = null//Setting背景
    var urlP: String? = null//Pass背景
    var urlF: String? = null//Faq背景
    var urlC: String? = null//CHAT背景

    var notice: String? = null//应用内通知

    var appDes: String? = null//APP描述
    var appDesEn: String? = null//
    var appUrlDes: String? = "1、https://play.google.com/store/apps/details?id=com.dillon.supercam \n2、https://www.pgyer.com/supercamera"//官网地址描述

    var banOld = false//是否禁用旧版本



    var chatShow = true

    var roomVoiceMax=30

    var appAbout: String? = null
    var appInstructions: String? = null

    var latestVersion: Int = 0
    var latestVersionDes: String? = null
    var latestVersionName: String? = null
    var latestVersionUrl: String? = null

}