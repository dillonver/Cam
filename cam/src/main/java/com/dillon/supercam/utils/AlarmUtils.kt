package com.dillon.supercam.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils

/**
 * AlarmManager工具类
 */

object AlarmUtils {

    // 获取AlarmManager实例
    private fun getAlarmManager(): AlarmManager {
        return Utils.getApp().getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    // 发送定时广播（执行广播中的定时任务）
    // context:上下文
    // requestCode:请求码，用于区分不同的任务
    // triggerAtTime:定时任务开启的时间，毫秒为单位
    // cls:广播接收器的class

    fun setAlarm(
        context: Context,
        requestCode: Int,
        cls: Class<*>,
        action: String,
        triggerAtTime: Long
    ) {
        val mgr = getAlarmManager()

        val intent = Intent(context, cls)
        intent.action = action
        val pi = PendingIntent.getBroadcast(
            context, requestCode,
            intent, 0
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 6.0及以上
            mgr.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtTime, pi
            )
        } else { // 4.4及以上
            mgr.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtTime, pi
            )
        }
    }

    // 取消指定requestCode的定时任务
    // 参数：
    // context:上下文
    // requestCode:请求码，用于区分不同的任务
    // cls:广播接收器的class
    fun cancelAlarm(
        context: Context, requestCode: Int,
        cls: Class<*>, action: String
    ) {
        val mgr = getAlarmManager()

        val intent = Intent(Utils.getApp(), cls)
        intent.action = action
        val pi = PendingIntent.getBroadcast(
            context, requestCode,
            intent, 0
        )

        mgr.cancel(pi)

        LogUtils.i("CancelAlarm:Success", "@requestCode:$requestCode")
    }


}
