package com.dillon.supercam.services.task

import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.dillon.supercam.base.App
import com.dillon.supercam.base.BSer
import com.dillon.supercam.ui.home.HomeAct
import com.dillon.supercam.utils.apk.down.DownLoadApkManager
import java.util.*


class CheckCanInstallSer : BSer() {

    private var mTimer: Timer? = null
    private var mTimerTask: TimerTask? = null

    override fun onCreate() {
        super.onCreate()
        startTimer()
    }


    override fun startCommand(): Int {
        return START_STICKY
    }

    private fun startTimer() {
        if (mTimer == null) {
            mTimer = Timer()
        }
        var autoClose = 1
        if (mTimerTask == null) {
            mTimerTask = object : TimerTask() {
                override fun run() {
                    try {
                        if (packageManager.canRequestPackageInstalls()) {
                            permissionResultBack()
                        } else {
                            autoClose++
                            LogUtils.i(autoClose)
                            if (autoClose > 50) {
                                stopTimer()
                                stopSelf()
                            }
                        }
                    } catch (e: Exception) {
                    }
                }
            }

        }
        if (mTimer != null && mTimerTask != null) {
            mTimer?.schedule(mTimerTask, 0, 1500)
        }
    }

    private fun stopTimer() {
        if (App.updateInfo != null) {
            DownLoadApkManager.getInstance().startDownLoad(
                Utils.getApp(),
                App.updateInfo!!.downloadURL!!,
                AppUtils.getAppName(),
                ""
            )
            App.updateInfo = null
        }
        if (mTimer != null) {
            mTimer?.cancel()
            mTimer = null
        }
        if (mTimerTask != null) {
            mTimerTask?.cancel()
            mTimerTask = null
        }
    }


    //获取权限后返回
    private fun permissionResultBack() {
        stopTimer()
        ActivityUtils.startActivity(HomeAct::class.java)
        stopSelf()
    }
}