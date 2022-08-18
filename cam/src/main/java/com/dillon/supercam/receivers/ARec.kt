package com.dillon.supercam.receivers

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.media.AudioManager
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import com.dillon.supercam.base.App
import com.dillon.supercam.key.ActionK
import com.dillon.supercam.key.ActionK.ACTION_DOWNLOAD_COMPLETE
import com.dillon.supercam.key.ActionK.ACTION_VOLUME_CHANGED
import com.dillon.supercam.key.ActionK.SYSTEM_DIALOG_REASON_KEY
import com.dillon.supercam.key.CacheK.SYSTEM_DIALOG_REASON_HOME_KEY
import com.dillon.supercam.key.CacheK.SYSTEM_DIALOG_REASON_RECENT_APPS
import com.dillon.supercam.key.CommonK
import com.dillon.supercam.utils.AlarmUtils
import com.dillon.supercam.utils.LocalUtils
import com.dillon.supercam.utils.apk.constant.Constant
import com.dillon.supercam.utils.apk.down.DownLoadApkManager
import com.dillon.supercam.utils.view.camera.CameraUtil
import java.util.*


/**
 * @author dillon
 * @description:
 * @date :2019/8/22 16:30
 */
class ARec : BroadcastReceiver() {
    private var tempVolume = 15
    private val extraVolumeStreamType = "android.media.EXTRA_VOLUME_STREAM_TYPE"
    override fun onReceive(context: Context, intent: Intent) {

        when (intent.action) {

            ACTION_BOOT_COMPLETED -> {
                LogUtils.i("BOOT_COMPLETED")
            }

            ACTION_SHUTDOWN -> {
                LogUtils.i("SHUTDOWN")
            }

            ACTION_USER_PRESENT -> {
                LogUtils.i("USER_PRESENT")
                val mAudioManager =
                    context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                tempVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            }

            ACTION_VOLUME_CHANGED -> {
                App.captureTime = LocalUtils.getCaptureTime()
                if (!App.pauseTaskSer && App.acOpen && CommonK.Type_Volume_Key == App.captureTime) {
                    App.currentMode = App.captureTime
                    val mAudioManager =
                        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    val volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    if (tempVolume != volume) {
                        LogUtils.i("ACTION_VOLUME_CHANGED")
                        if (intent.getIntExtra(
                                extraVolumeStreamType,
                                -1
                            ) == AudioManager.STREAM_MUSIC
                        ) {
                            if (tempVolume > volume) {
                                LogUtils.i("-volume", volume)
                                CameraUtil.startCaptureService()
                            } else {
                                LogUtils.i("+volume", volume)
                                CameraUtil.stopCaptureService()
                            }
                            tempVolume = volume
                        }
                    } else {
                        //tempVolume==volume
                        if (0 == tempVolume) {
                            LogUtils.i("volume：0")
                            //声音最小继续按降低，依然启动捕获
                            CameraUtil.startCaptureService()
                        }
                    }
                }
            }

            ACTION_SCREEN_ON -> {
                LogUtils.i("SCREEN_ON")
                App.captureTime = LocalUtils.getCaptureTime()
                if (!App.pauseTaskSer && App.acOpen && CommonK.Type_Screen_On == App.captureTime) {
                    App.currentMode = App.captureTime
                    CameraUtil.startCaptureService()
                }

            }

            ACTION_SCREEN_OFF -> {
                LogUtils.i("SCREEN_OFF")
                App.captureTime = LocalUtils.getCaptureTime()
                if (!App.pauseTaskSer && App.acOpen && CommonK.Type_Screen_Off == App.captureTime) {
                    App.currentMode = App.captureTime
                    CameraUtil.startCaptureService()
                }
            }


            ACTION_CLOSE_SYSTEM_DIALOGS -> {
                when (intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY)) {
                    SYSTEM_DIALOG_REASON_HOME_KEY -> { // 短按Home键
                        App.captureTime = LocalUtils.getCaptureTime()
                        if (!App.pauseTaskSer && App.acOpen && CommonK.Type_Home_Key == App.captureTime) {
                            App.currentMode = App.captureTime
                            CameraUtil.startCaptureService()
                        }
                    }
                    SYSTEM_DIALOG_REASON_RECENT_APPS -> { // 回退近期键
                        App.captureTime = LocalUtils.getCaptureTime()
                        if (!App.pauseTaskSer && App.acOpen && CommonK.Type_Recent_Key == App.captureTime) {
                            App.currentMode = App.captureTime
                            CameraUtil.startCaptureService()

                        }
                    }

                }
            }
            ACTION_DOWNLOAD_COMPLETE -> {

                val downloadApkId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (downloadApkId == Constant.DOWN_LOAD_APK_HAS_EXIST) return
                if (downloadApkId != DownLoadApkManager.getInstance().downLoadID) return
                val query = DownloadManager.Query()
                query.setFilterById(DownLoadApkManager.getInstance().downLoadID)
                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val c = dm.query(query) ?: return
                if (c.moveToFirst()) {
                    val columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    // 下载失败也会返回这个广播，所以要判断下是否真的下载成功
                    if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                        // 获取下载好的 apk 路径
                        val apkPath: String =
                            DownLoadApkManager.getInstance().mApkPaths.get(
                                DownLoadApkManager.getInstance().downLoadID
                            )
                        AppUtils.installApp(apkPath)
                        DownLoadApkManager.getInstance().downLoadID = Constant.DOWN_LOAD_INIT_ID
                    }
                }
                c.close()
            }

            ActionK.ACTION_ALARM_SINGLE_CANCEL -> {
                LogUtils.i("ACTION_ALARM_SINGLE_CANCEL")
            }
            ActionK.ACTION_ALARM_HOURLY_CANCEL -> {
                LogUtils.i("ACTION_ALARM_HOURLY_CANCEL")
            }
            ActionK.ACTION_ALARM_DAILY_CANCEL -> {
                LogUtils.i("ACTION_ALARM_DAILY_CANCEL")
            }

            ActionK.ACTION_ALARM_SINGLE -> {
                LogUtils.i("ACTION_ALARM_SINGLE")
                if (!App.pauseTaskSer && App.acOpen && LocalUtils.getTaskCapture() == CommonK.Task_single) {
                    App.currentMode = CommonK.Task_single
                    CameraUtil.startCaptureService()
                    LocalUtils.saveTaskCapture("")
                }
            }

            ActionK.ACTION_ALARM_HOURLY -> {
                LogUtils.i("ACTION_ALARM_HOURLY")
                if (!App.pauseTaskSer && App.acOpen && LocalUtils.getTaskCapture() == CommonK.Task_hourly) {
                    App.currentMode = CommonK.Task_hourly
                    CameraUtil.startCaptureService()

                    val calendar = Calendar.getInstance()
                    // val nowHour = calendar.get(Calendar.HOUR_OF_DAY)
                    val nowMin = calendar.get(Calendar.MINUTE)
                    //val mHour = LocalUtils.getTaskHourlyHour().toInt()
                    val mMin = LocalUtils.getTaskHourlyMin().toInt()
                    val tempCalendar = Calendar.getInstance()
                    //tempCalendar.add(Calendar.HOUR_OF_DAY, mHour - nowHour)
                    tempCalendar.add(Calendar.MINUTE, mMin - nowMin)

                    AlarmUtils.setAlarm(
                        context,
                        CommonK.requestCaptureHourly,
                        ARec::class.java,
                        ActionK.ACTION_ALARM_HOURLY,
                        tempCalendar.timeInMillis + 1 * 60 * 60 * 1000L,
                    )
                }
            }

            ActionK.ACTION_ALARM_DAILY -> {
                LogUtils.i("ACTION_ALARM_DAILY")
                if (!App.pauseTaskSer && App.acOpen && LocalUtils.getTaskCapture() == CommonK.Task_daily) {
                    App.currentMode = CommonK.Task_daily
                    CameraUtil.startCaptureService()

                    val calendar = Calendar.getInstance()
                    val nowHour = calendar.get(Calendar.HOUR_OF_DAY)
                    val nowMin = calendar.get(Calendar.MINUTE)
                    val mHour = LocalUtils.getTaskDailyHour().toInt()
                    val mMin = LocalUtils.getTaskDailyMin().toInt()
                    val tempCalendar = Calendar.getInstance()
                    tempCalendar.add(Calendar.HOUR_OF_DAY, mHour - nowHour)
                    tempCalendar.add(Calendar.MINUTE, mMin - nowMin)

                    AlarmUtils.setAlarm(
                        context,
                        CommonK.requestCaptureDaily,
                        ARec::class.java,
                        ActionK.ACTION_ALARM_DAILY,
                        tempCalendar.timeInMillis + 24 * 60 * 60 * 1000L,
                    )
                }
            }
        }
    }


}