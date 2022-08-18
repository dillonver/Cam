package com.dillon.supercam.services.task


import android.content.Intent
import android.media.MediaRecorder
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.VibrateUtils
import com.dillon.supercam.base.App
import com.dillon.supercam.base.BSer
import com.dillon.supercam.key.ActionK
import com.dillon.supercam.key.CommonK
import com.dillon.supercam.utils.CoreUtils
import com.dillon.supercam.utils.LocalUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class CaptureAudioSer : BSer() {
    private var mMediaRecorder: MediaRecorder? = null
    private var mFile: File? = null

    override fun onCreate() {
        super.onCreate()
        if (CoreUtils.checkAllNecessaryPermissions()) {
            CoreUtils.simpleLog(App.currentMode+"--CAPTURE_AUDIO")
            CoreUtils.ringerModeSilent()
            startRecord()
        } else {
            LogUtils.i("Permission deny...")
            stopSelf()
        }
    }

    override fun startCommand(): Int {
        return START_STICKY
    }


    private fun startRecord() {
        App.isCaptureContinuing = false
        App.capturingAudio = true
        ThreadUtils.runOnUiThread {
            CoreUtils.updateDynamicShortcuts(this, CommonK.shortCutAudio, App.capturingAudio)
        }
        Thread {
            when (LocalUtils.getRemindType()) {
                CommonK.Type_Remind_Start, CommonK.Type_Remind_Both -> {
                    VibrateUtils.vibrate(300)
                }
            }
            val intent = Intent()
            intent.action = ActionK.ACTION_CAPTURE_HAD_START
            sendBroadcast(intent)
            val time = SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(Date(System.currentTimeMillis()))
            if (mMediaRecorder == null) mMediaRecorder = MediaRecorder()
            mFile = File(App.saveAudioDir, ".f$time.m4a")
            try {
                mMediaRecorder?.setOnInfoListener { _, p, _ ->
                    if (p == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                        stopSelf()
                    }
                }
                mMediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
                mMediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                mMediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                mMediaRecorder?.setOutputFile(mFile?.absolutePath)
                mMediaRecorder?.setMaxDuration(LocalUtils.getAudioMaxDur()!! * 1000)
                mMediaRecorder?.prepare()
                mMediaRecorder?.start()
                LogUtils.i("startRecord|$mFile")
            } catch (e: Exception) {
                //
            }
        }.start()


    }

    private fun stopRecord() {
        try {
            if (mMediaRecorder != null) {
                LogUtils.i("stopRecord", mFile)
                mMediaRecorder?.stop()
                mMediaRecorder?.reset()
                mMediaRecorder?.release()
                mMediaRecorder = null
            }
            CoreUtils.startCommonService(CaptureAllStopSer::class.java)
        } catch (e: Exception) {
            //  e.printStackTrace()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        stopRecord()
    }


}