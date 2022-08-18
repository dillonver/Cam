package com.dillon.supercam.services

import com.dillon.supercam.base.BSer
import com.dillon.supercam.utils.CoreUtils

//ASer的保护服务
class ASerP : BSer() {

    override fun onCreate() {
        super.onCreate()
        startTask()
        stopSelf()
    }

    override fun startCommand(): Int {
        return START_STICKY
    }

    private fun startTask() {
        CoreUtils.startCommonService(ASer::class.java)
    }
}
