package com.dillon.supercam.utils.apk.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.math.BigInteger
import java.nio.channels.FileChannel
import java.security.MessageDigest

/**
 *  CreateDate: 2018/6/12
 *  Desc: 工具类
 */
class DownUtil {

    /**
     * 手机系统下载是否可用
     * @return true  可用 false 不可用
     */
    fun downLoadManagerIsEnabled(context: Context): Boolean {
        try {
            val state = context.applicationContext.packageManager
                .getApplicationEnabledSetting("com.android.providers.downloads")
            return !(state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                    state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED)
        } catch (e: Exception) {
        }
        return false
    }

    /**
     * Android N  及以上安装apk
     */
    fun androidNInstallApk(context: Context, file: File) {
        // 获取下载好的apk 路径
        val intentN = Intent(Intent.ACTION_VIEW)
        // 由于在没有activity 环境下启动activity, 添加 flag
        intentN.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileProvider", file
        )
        //添加这一句表示对目标应用临时授权该Uri所代表的文件
        intentN.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intentN.setDataAndType(apkUri, "application/vnd.android.package-archive")
        context.startActivity(intentN)
    }

    /**
     * Android N  以下系统 安装apk
     */
    fun androidMInstallApk(context: Context, date: Uri) {
        val promptInstall = Intent(Intent.ACTION_VIEW)
            .setDataAndType(date, "application/vnd.android.package-archive")
        // FLAG_ACTIVITY_NEW_TASK 可以保证安装成功时可以正常打开 app
        promptInstall.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(promptInstall)
    }
}