package com.dillon.supercam.services

import android.content.Intent.*
import android.content.IntentFilter
import com.dillon.supercam.base.BSer
import com.dillon.supercam.key.ActionK
import com.dillon.supercam.receivers.ARec
import com.dillon.supercam.services.task.CoreSer
import com.dillon.supercam.utils.CoreUtils


class ASer : BSer() {

    private var aRec: ARec? = null


    private fun registerCoreReceiver() {
        aRec = ARec()
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_SCREEN_ON)
        intentFilter.addAction(ACTION_SCREEN_OFF)
        intentFilter.addAction(ACTION_USER_PRESENT)
        intentFilter.addAction(ACTION_CLOSE_SYSTEM_DIALOGS)


        intentFilter.addAction(ActionK.ACTION_VOLUME_CHANGED)
        intentFilter.addAction(ActionK.ACTION_START_CAPTURE_VIDEO)
        intentFilter.addAction(ActionK.ACTION_START_CAPTURE_PHOTO)
        intentFilter.addAction(ActionK.ACTION_STOP_CAPTURE_VIDEO)
        intentFilter.addAction(ActionK.ACTION_START_CAPTURE_AUDIO)
        intentFilter.addAction(ActionK.ACTION_STOP_CAPTURE_AUDIO)

        intentFilter.addAction(ActionK.ACTION_DOWNLOAD_COMPLETE)


        intentFilter.addAction(ActionK.ACTION_ALARM_SINGLE_CANCEL)
        intentFilter.addAction(ActionK.ACTION_ALARM_HOURLY_CANCEL)
        intentFilter.addAction(ActionK.ACTION_ALARM_DAILY_CANCEL)
        intentFilter.addAction(ActionK.ACTION_ALARM_SINGLE)
        intentFilter.addAction(ActionK.ACTION_ALARM_HOURLY)
        intentFilter.addAction(ActionK.ACTION_ALARM_DAILY)

        registerReceiver(aRec, intentFilter)
    }

    override fun onCreate() {
        super.onCreate()

        registerCoreReceiver()

    }


    override fun startCommand(): Int {
        CoreUtils.startCommonService(CoreSer::class.java)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (aRec != null) {
            unregisterReceiver(aRec)

            CoreUtils.startCommonService(ASerP::class.java)

        }


    }
}