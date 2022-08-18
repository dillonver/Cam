@file:Suppress("DEPRECATION")

package com.dillon.supercam.utils.view.camera

import android.graphics.Bitmap
import android.hardware.Camera
import android.os.Build
import com.blankj.utilcode.util.*
import com.dillon.supercam.R
import com.dillon.supercam.base.App
import com.dillon.supercam.key.CommonK
import com.dillon.supercam.services.task.CapturePhotoSer
import com.dillon.supercam.services.task.CaptureVideoSer
import com.dillon.supercam.services.task.CaptureAudioSer
import com.dillon.supercam.services.task.CapturePhotoIntervalSer
import com.dillon.supercam.utils.CoreUtils
import com.dillon.supercam.utils.LocalUtils
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.Collections.sort

object CameraUtil {


    fun getCameraSize(
        list: List<Camera.Size>?,
        th: Int,
        defaultSize: Camera.Size
    ): Camera.Size {
        var tempTh = th
        if (null == list || list.isEmpty()) return defaultSize

       val imageSizeType= if (Build.VERSION.SDK_INT >= 30 && LocalUtils.getMediaType() == CommonK.Type_Media_Video) {
            CommonK.Type_size_1080
        }else{
           App.imageSizeType
       }

        tempTh = when (imageSizeType) {
            CommonK.Type_size_1080 -> {
                1900
            }
            CommonK.Type_size_2K -> {
                2500
            }
            CommonK.Type_size_4K -> {
                4000
            }
            CommonK.Type_size_Best -> {
                10000
            }
            else -> {
                2500
            }
        }

        sort(list) { lhs: Camera.Size, rhs: Camera.Size ->
            lhs.width.compareTo(rhs.width)
        }
        LogUtils.i(GsonUtils.toJson(list))
        var i = 0
        for (s in list) {
            if (s.width > tempTh) { //&& equalRate(s, rate)
                break
            }
            i++
        }
        return if (i == list.size) {
            if (list[i - 1].width == list[i - 2].width) {
                if (list[i - 1].height > list[i - 2].height) {
                    list[i - 1]
                } else {
                    list[i - 2]
                }
            } else {
                list[i - 1]
            }
        } else {
            if (i + 1 < list.size) {
                if (list[i].width == list[i + 1].width) {
                    if (list[i].height > list[i + 1].height) {
                        list[i]
                    } else {
                        list[i + 1]
                    }
                } else {
                    list[i]
                }
            } else {
                list[i]
            }

        }
    }

    fun getCameraSizeO(
        list: List<Camera.Size>?,
        th: Int,
        defaultSize: Camera.Size
    ): Camera.Size {
        if (null == list || list.isEmpty()) return defaultSize
        sort(list) { lhs: Camera.Size, rhs: Camera.Size ->
            lhs.width.compareTo(rhs.width)
        }
        var i = 0
        for (s in list) {
            if (s.width > th) { //&& equalRate(s, rate)
                break
            }
            i++
        }
        return if (i == list.size) {
            list[i - 1]
        } else {
            list[i]
        }
    }

    fun isSupportedFocusMode(
        focusList: MutableList<String>?,
        focusMode: String
    ): Boolean {
        if (focusList.isNullOrEmpty()) return false
        for (i in focusList.indices) {
            if (focusMode == focusList[i]) {
                return true
            }
        }
        return false
    }

    fun isSupportedFormats(
        supportedFormats: MutableList<Int>?,
        jpeg: Int
    ): Boolean {
        if (supportedFormats.isNullOrEmpty()) return false
        for (i in supportedFormats.indices) {
            if (jpeg == supportedFormats[i]) {
                return true
            }
        }
        return false
    }

    //保存照片
    fun saveTempPhoto(b: Bitmap, name: String): String {
        val photoPath = PathUtils.getExternalAppFilesPath() + "/temp/" + name
        try {
            val fileOutputStream = FileOutputStream(photoPath)
            val bos = BufferedOutputStream(fileOutputStream)
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            bos.flush()
            bos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return photoPath
    }

    fun savePhoto(b: Bitmap): String {
        LocalUtils.saveProExp(1)
        val photoPath = if (CommonK.Type_Camera_Back == App.cameraType) {
            App.savePhotoDir + ".b" + time + ".jpg"
        } else {
            App.savePhotoDir + ".f" + time + ".jpg"
        }
        try {
            val fileOutputStream = FileOutputStream(photoPath)
            val bos = BufferedOutputStream(fileOutputStream)
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            bos.flush()
            bos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return photoPath
    }

    //获取视频存储路径
    fun getVideoOutputPath(): String {
        LocalUtils.saveProExp(2)
        return if (CommonK.Type_Camera_Back == App.cameraType) {
            App.saveVideoDir + ".b" + time + ".mp4"
        } else {
            App.saveVideoDir + ".f" + time + ".mp4"
        }
    }

    private val time: String
        get() = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date(System.currentTimeMillis()))


    //开启CaptureSer
    fun startCaptureService() {
        if (CoreUtils.checkAllNecessaryPermissions()) {
            when (LocalUtils.getMediaType()) {
                CommonK.Type_Media_Photo -> {
                    startCapturePhotoService()
                }
                CommonK.Type_Media_Video -> {
                    startCaptureVideoService()
                }
                CommonK.Type_Media_Audio -> {
                    startCaptureAudioService()
                }
                else -> {
                }
            }
        } else {
            LogUtils.i(Utils.getApp().getString(R.string.permit_deny))
        }


    }

    fun startCaptureVideoService() {
        if (!App.capturingPhoto && !App.capturingVideo && !App.capturingAudio) {
            // ToastUtils.showShort("Start")
            CoreUtils.startCommonService(CaptureVideoSer::class.java)
        } else {
            LogUtils.e(" startCaptureVideoService fail -- camera is using")
        }

    }

    fun startCapturePhotoServiceWithoutContinuing() {
        if (!App.capturingPhoto && !App.capturingVideo && !App.capturingAudio) {
            // ToastUtils.showShort("Start")
            if (!App.isCaptureContinuing) {
                CoreUtils.startCommonService(CapturePhotoSer::class.java)
            }

        } else {
            LogUtils.e(" startCapturePhotoService fail -- camera is using")
        }
        //   CoreUtils.startCommonService(WidgetUpdateSer::class.java)

    }

    fun startCapturePhotoService() {
        if (!App.capturingPhoto && !App.capturingVideo && !App.capturingAudio) {
            // ToastUtils.showShort("Start")
            if (!App.isCaptureContinuing) {
                if (LocalUtils.getCaptureContinue()) {
                    App.isCaptureContinuing = true
                    CoreUtils.startCommonService(CapturePhotoIntervalSer::class.java)
                } else {
                    CoreUtils.startCommonService(CapturePhotoSer::class.java)
                }
            }

        } else {
            LogUtils.e(" startCapturePhotoService fail -- camera is using")
        }
        //   CoreUtils.startCommonService(WidgetUpdateSer::class.java)

    }

    fun startCaptureAudioService() {
        if (!App.capturingPhoto && !App.capturingVideo && !App.capturingAudio) {
            // ToastUtils.showShort("Start")
            CoreUtils.startCommonService(CaptureAudioSer::class.java)
        } else {
            LogUtils.e(" startCaptureVoiceService fail -- other is using")
        }
        //   CoreUtils.startCommonService(WidgetUpdateSer::class.java)

    }

    //关闭CaptureSer
    fun stopCaptureService() {
        CoreUtils.stopCommonService(CaptureVideoSer::class.java)
        CoreUtils.stopCommonService(CapturePhotoSer::class.java)
        CoreUtils.stopCommonService(CaptureAudioSer::class.java)
        CoreUtils.stopCommonService(CapturePhotoIntervalSer::class.java)
    }
}