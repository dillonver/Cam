package com.dillon.supercam.services.task

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.dillon.supercam.base.BSer
import java.util.*


class TestSer : BSer() {

    private var mTimer: Timer? = null
    private var mTimerTask: TimerTask? = null
    private var interValOld = 10L
    private var interValNew = 0L

    override fun onCreate() {
        super.onCreate()
        startTimer(interValOld)
    }


    override fun startCommand(): Int {
        return START_STICKY
    }

    private fun startTimer(interVal: Long) {
        if (mTimer == null) {
            mTimer = Timer()
        }
        if (mTimerTask == null) {
            mTimerTask = object : TimerTask() {
                override fun run() {
                    try {
                        //do something
                        interValNew = interValOld++
                        stopTimer()
                        ThreadUtils.runOnUiThreadDelayed({
                            startTimer(interValNew)
                            LogUtils.i("Change", interValNew)
                        }, interValNew*1000L)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        }
        if (mTimer != null && mTimerTask != null) {
            mTimer?.schedule(mTimerTask, 0, interVal * 1000L)
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
    }
}