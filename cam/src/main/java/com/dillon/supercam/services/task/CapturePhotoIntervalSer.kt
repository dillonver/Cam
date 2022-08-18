package com.dillon.supercam.services.task

import android.content.Intent
import com.blankj.utilcode.util.LogUtils
import com.dillon.supercam.base.App
import com.dillon.supercam.base.BSer
import com.dillon.supercam.key.ActionK
import com.dillon.supercam.key.CommonK
import com.dillon.supercam.utils.CoreUtils
import com.dillon.supercam.utils.LocalUtils
import java.util.*


class CapturePhotoIntervalSer : BSer() {

    private var mTimer: Timer? = null
    private var mTimerTask: TimerTask? = null

    override fun onCreate() {
        super.onCreate()
        val intent = Intent()
        intent.action = ActionK.ACTION_CAPTURE_HAD_START
        sendBroadcast(intent)

        CoreUtils.updateDynamicShortcuts(this, CommonK.shortCutPhoto, App.isCaptureContinuing)

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
                        if (App.isCaptureContinuing) {
                            autoClose++
                            CoreUtils.startCommonService(CapturePhotoSer::class.java)
                            LogUtils.i(autoClose)
                            val count=LocalUtils.getPhotoContinueCount()
                            val mCount= count ?: 3
                            if (autoClose > mCount) {
                                stopTimer()
                                stopSelf()
                            }
                        } else {
                            stopTimer()
                            stopSelf()
                        }

                    } catch (e: Exception) {
                    }
                }
            }

        }
        if (mTimer != null && mTimerTask != null) {
            mTimer?.schedule(mTimerTask, 0, LocalUtils.getPhotoContinueInterval()!! * 1000L)
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

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        CoreUtils.startCommonService(CaptureAllStopSer::class.java)
    }
}