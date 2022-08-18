package com.dillon.supercam.utils.apk.constant

import android.Manifest

/**
 *  CreateDate: 2018/6/11
 *  Desc: 常量
 */
object Constant {


    // 执行下载任务时初始化ID
    const val DOWN_LOAD_INIT_ID: Long = 0
    // 当前系统下载管理器不可用，处于禁用状态
    const val DOWN_LOAD_MANAGER_UNABLE_USE: Long = -3
    //当执行下载apk 检测 apk 已经成功下载,直接安装，中断下载操作
    const val DOWN_LOAD_APK_HAS_EXIST: Long = -1
    // 判断存储apk 文件路径是否存在
    const val EXTERNAL_STORAGE_NOT_EXIST: Long = -2
}