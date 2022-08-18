package com.dillon.supercam.ui.shortcut


import android.view.Gravity
import android.view.WindowManager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.dillon.supercam.WelcomeActShortCut
import com.dillon.supercam.base.App
import com.dillon.supercam.base.BAct
import com.dillon.supercam.key.CommonK
import com.dillon.supercam.services.task.CaptureAudioSer
import com.dillon.supercam.services.task.CapturePhotoIntervalSer
import com.dillon.supercam.services.task.CaptureVideoSer
import com.dillon.supercam.utils.CoreUtils
import com.dillon.supercam.utils.view.camera.CameraUtil


class OnePxAct : BAct() {
    override fun initView() {
        super.initView()
        //设置1像素
        window.setGravity(Gravity.START or Gravity.TOP)
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
        val params: WindowManager.LayoutParams = window.attributes
        params.x = 0
        params.y = 0
        params.height = 1
        params.width = 1
        window.attributes = params
        val shortCutType = intent.extras?.getString(CommonK.shortCutType)
        LogUtils.i(shortCutType)

        when (shortCutType) {
            CommonK.shortCutPhoto -> {
                if (App.isCaptureContinuing) {
                    CoreUtils.stopCommonService(CapturePhotoIntervalSer::class.java)
                } else {
                    LogUtils.i("SHORTCUT_CAPTURE_PHOTO")
                    App.currentMode = CommonK.shortCutPhoto
                    CameraUtil.startCapturePhotoService()
                }
            }

            CommonK.shortCutVideo -> {
                if (App.capturingVideo) {
                    CoreUtils.stopCommonService(CaptureVideoSer::class.java)
                } else {
                    LogUtils.i("SHORTCUT_CAPTURE_VIDEO")
                    App.currentMode = CommonK.shortCutVideo
                    CameraUtil.startCaptureVideoService()

                }
            }

            CommonK.shortCutAudio -> {
                if (App.capturingAudio) {
                    CoreUtils.stopCommonService(CaptureAudioSer::class.java)
                } else {
                    LogUtils.i("SHORTCUT_CAPTURE_AUDIO")
                    App.currentMode = CommonK.shortCutAudio
                    CameraUtil.startCaptureAudioService()
                }
            }
            CommonK.shortCutEnter -> {
                LogUtils.i("SHORTCUT_ENTER_APP")
                ActivityUtils.startActivity(WelcomeActShortCut::class.java)

            }
        }

        ThreadUtils.runOnUiThreadDelayed(
            { finish() }, 500
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        LogUtils.i(this.javaClass.name + "onDestroy")
    }

}
