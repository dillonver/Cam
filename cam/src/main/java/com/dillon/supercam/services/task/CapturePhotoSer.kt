package com.dillon.supercam.services.task

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.WindowManager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.VibrateUtils
import com.dillon.supercam.base.App
import com.dillon.supercam.base.BSer
import com.dillon.supercam.key.ActionK
import com.dillon.supercam.key.CommonK
import com.dillon.supercam.utils.CoreUtils
import com.dillon.supercam.utils.LocalUtils
import com.dillon.supercam.utils.view.camera.CameraView
import java.lang.Exception

class CapturePhotoSer : BSer(), SurfaceHolder.Callback, CameraView.CallServiceListener {
    private var windowManager: WindowManager? = null
    private var cameraView: CameraView? = null
    private var isCameraBack = false  //true为后摄像头 false为前

    override fun onCreate() {
        super.onCreate()
        val cameraType = LocalUtils.getCameraType()
        isCameraBack = CommonK.Type_Camera_Back == cameraType
        if (CoreUtils.checkAllNecessaryPermissions()) {
            CoreUtils.simpleLog(App.currentMode + "--CAPTURE_PHOTO")
            CoreUtils.ringerModeSilent()
            try {
                windowManager =
                    this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val layoutParams =
                    WindowManager.LayoutParams(
                        1, 1,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                    )
                layoutParams.gravity = Gravity.START or Gravity.TOP
                cameraView = CameraView(this, this)
                cameraView!!.setDefaultCamera(isCameraBack)
                windowManager!!.addView(cameraView!!, layoutParams)
                cameraView!!.holder?.addCallback(this)
            } catch (e: Exception) {
                e.printStackTrace()
                stopSelf()
            }

        } else {
            LogUtils.i("Permission deny...")
            stopSelf()
        }


    }

    override fun startCommand(): Int {
        return START_STICKY
    }

    // Method called right after Surface created (initializing and starting MediaRecorder)
    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        App.capturingPhoto = true

        when (LocalUtils.getRemindType()) {
            CommonK.Type_Remind_Start, CommonK.Type_Remind_Stop, CommonK.Type_Remind_Both -> {
                VibrateUtils.vibrate(300)
            }
        }
        val intent = Intent()
        intent.action = ActionK.ACTION_CAPTURE_PHOTO_HAD_START
        sendBroadcast(intent)
        cameraView?.capture()

    }

    // Stop recording and remove SurfaceView
    override fun onDestroy() {
        LogUtils.i(this.javaClass.simpleName + ".onDestroy")
        try {
            // CameraUtil.ringerModeRecover()
            cameraView?.closeCamera()
            windowManager?.removeView(cameraView)
            App.capturingPhoto = false
            if (!App.isCaptureContinuing) {
                CoreUtils.startCommonService(CaptureAllStopSer::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    override fun surfaceChanged(
        surfaceHolder: SurfaceHolder,
        format: Int,
        width: Int,
        height: Int
    ) {
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {}

    override fun stopService() {
        // LogUtils.i(this.javaClass.simpleName + ".stopService")
        stopSelf()
    }

}