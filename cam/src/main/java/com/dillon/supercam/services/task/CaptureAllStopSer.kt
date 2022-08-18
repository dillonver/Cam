package com.dillon.supercam.services.task

import android.content.Intent
import com.blankj.utilcode.util.VibrateUtils
import com.dillon.supercam.base.App
import com.dillon.supercam.base.BSer
import com.dillon.supercam.key.ActionK
import com.dillon.supercam.key.CommonK
import com.dillon.supercam.utils.CoreUtils
import com.dillon.supercam.utils.LocalUtils


class CaptureAllStopSer : BSer() {
    override fun onCreate() {
        super.onCreate()
        startTask()
        stopSelf()
    }

    override fun startCommand(): Int {
        return START_STICKY
    }

    private fun startTask() {
        when {
            App.isCaptureContinuing -> {
                App.isCaptureContinuing = false
                CoreUtils.updateDynamicShortcuts(
                    this,
                    CommonK.shortCutPhoto,
                    App.isCaptureContinuing
                )
            }
            App.capturingAudio -> {
                App.capturingAudio = false
                LocalUtils.saveProExp(1)
                CoreUtils.updateDynamicShortcuts(this, CommonK.shortCutAudio, App.capturingAudio)

            }
            App.capturingVideo -> {
                App.capturingVideo = false
                CoreUtils.updateDynamicShortcuts(this, CommonK.shortCutVideo, App.capturingVideo)

            }
        }

        when (App.remindType) {
            CommonK.Type_Remind_Stop, CommonK.Type_Remind_Both -> {
                VibrateUtils.vibrate(300)
            }
        }
        val mIntent = Intent()
        mIntent.action = ActionK.ACTION_CAPTURE_HAD_STOP
        sendBroadcast(mIntent)
        CoreUtils.startCommonService(CheckFreeRecoverSer::class.java)
    }
}
