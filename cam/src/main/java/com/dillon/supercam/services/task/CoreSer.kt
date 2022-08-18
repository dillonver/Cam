package com.dillon.supercam.services.task

import com.dillon.supercam.base.App
import com.dillon.supercam.base.BSer


class CoreSer : BSer() {

    override fun onCreate() {
        super.onCreate()
        startTask()
        stopSelf()
    }

    override fun startCommand(): Int {
        return START_STICKY
    }

    private fun startTask() {
        App.pauseTaskSer = false
    }

}
