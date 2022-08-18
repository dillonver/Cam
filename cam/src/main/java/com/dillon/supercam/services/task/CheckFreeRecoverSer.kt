package com.dillon.supercam.services.task

import com.blankj.utilcode.util.LogUtils
import com.dillon.supercam.base.App
import com.dillon.supercam.base.BSer
import com.dillon.supercam.utils.CoreUtils
import java.util.*


class CheckFreeRecoverSer : BSer() {

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
                        if (App.capturingPhoto || App.capturingVideo || App.capturingAudio) {
                            stopWhenCapturing()
                        } else {
                            autoClose++
                            LogUtils.i(autoClose)
                            if (autoClose > 150) {//当一段时间没有捕获，恢复指定手机状态 5分钟
                                CoreUtils.recoverMode()
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
            mTimer?.schedule(mTimerTask, 0, 2000)
        }
    }

    private fun stopTimer() {
        if (mTimer != null) {
            mTimer?.cancel()
            mTimer = null
        }
        if (mTimerTask != null) {
            mTimerTask?.cancel()
            mTimerTask = null
        }
    }



    private fun stopWhenCapturing() {
        stopTimer()
        stopSelf()
    }
}