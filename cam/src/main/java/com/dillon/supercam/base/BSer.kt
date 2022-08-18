package com.dillon.supercam.base

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.dillon.supercam.key.CommonK
import com.dillon.supercam.utils.CoreUtils
import com.blankj.utilcode.util.LogUtils

//BaseService
abstract class BSer : Service() {

    override fun onCreate() {
        super.onCreate()
        LogUtils.i(this.javaClass.simpleName + ": onCreate")
        CoreUtils.makeNotification(this, CommonK.CHANNEL_ID, CommonK.CHANNEL_NAME)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LogUtils.i(this.javaClass.simpleName + ": onStartCommand")
        return startCommand()
    }

    abstract fun startCommand(): Int

    override fun onDestroy() {
        super.onDestroy()
        LogUtils.i(this.javaClass.simpleName + ": onDestroy")

    }


}
