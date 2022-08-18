package com.dillon.supercam.utils

import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils
import com.dillon.supercam.base.App
import com.dillon.supercam.bean.UserInfo
import com.dillon.supercam.key.CacheK
import com.dillon.supercam.key.CommonK


object LocalUtils {

    fun saveCameraType(cameraType: String) {
        App.cameraType = cameraType
        SPUtils.getInstance().put(CacheK.useCameraType, cameraType)
    }

    fun getCameraType(): String? {
        if (App.cameraType != null) {
            return App.cameraType
        }
        App.cameraType =
            SPUtils.getInstance().getString(CacheK.useCameraType, CommonK.Type_Camera_Back)
        return App.cameraType
    }


    fun saveMediaType(mediaType: String) {
        App.mediaType = mediaType
        SPUtils.getInstance().put(CacheK.useMediaType, mediaType)
    }

    fun getMediaType(): String? {
        if (App.mediaType != null) {
            return App.mediaType
        }
        App.mediaType =
            SPUtils.getInstance().getString(CacheK.useMediaType, CommonK.Type_Media_Photo)
        return App.mediaType
    }


    fun saveRemindType(remindType: String) {
        App.remindType = remindType
        SPUtils.getInstance().put(CacheK.useRemindType, remindType)
    }

    fun getRemindType(): String? {
        if (App.remindType != null) {
            return App.remindType
        }
        App.remindType =
            SPUtils.getInstance().getString(CacheK.useRemindType, CommonK.Type_Remind_None)
        return App.remindType
    }


    fun saveAppPassword(password: String) {
        App.appPassWord = password
        SPUtils.getInstance().put(CacheK.appPassword, password)
    }

    fun getAppPassword(): String? {
        if (App.appPassWord != null) {
            return App.appPassWord
        }
        App.appPassWord = SPUtils.getInstance().getString(CacheK.appPassword, "")
        return App.appPassWord
    }


    fun saveVideoMaxDur(videoMaxDur: Int) {
        App.videoMaxDur = videoMaxDur
        SPUtils.getInstance().put(CacheK.videoMaxDur, videoMaxDur)
    }

    fun getVideoMaxDur(): Int? {
        if (App.videoMaxDur != null) {
            return App.videoMaxDur
        }
        App.videoMaxDur = SPUtils.getInstance().getInt(CacheK.videoMaxDur, 15)
        return App.videoMaxDur
    }


    fun saveAudioMaxDur(audioMaxDur: Int) {
        App.audioMaxDur = audioMaxDur
        SPUtils.getInstance().put(CacheK.audioMaxDur, audioMaxDur)
    }

    fun getAudioMaxDur(): Int? {
        if (App.audioMaxDur != null) {
            return App.audioMaxDur
        }
        App.audioMaxDur = SPUtils.getInstance().getInt(CacheK.audioMaxDur, 30)
        return App.audioMaxDur
    }


    fun saveCaptureTime(takeType: String) {
        App.captureTime = takeType
        SPUtils.getInstance().put(CacheK.takeType, takeType)
    }


    fun getCaptureTime(): String? {
        if (App.captureTime != null) {
            return App.captureTime
        }
        App.captureTime = SPUtils.getInstance().getString(CacheK.takeType, CommonK.Type_Home_Key)
        return App.captureTime
    }

    fun saveImageSizeType(imageSizeType: String) {
        App.imageSizeType = imageSizeType
        SPUtils.getInstance().put(CacheK.imageSizeType, imageSizeType)
    }

    fun getImageSizeType(): String? {
        if (App.imageSizeType != null) {
            return App.imageSizeType
        }
        App.imageSizeType =
            SPUtils.getInstance().getString(CacheK.imageSizeType, CommonK.Type_size_2K)
        return App.imageSizeType
    }


    fun saveAppHide(appHide: Boolean) {
        App.appHide = appHide
        SPUtils.getInstance().put(CacheK.showHideApp, appHide)
    }

    fun getAppHide(): Boolean {
        App.appHide = SPUtils.getInstance().getBoolean(CacheK.showHideApp, false)
        return App.appHide
    }


    fun saveProExp(exp: Int) {

        val oldExp = getProExp()
        val newExp = oldExp + exp
        SPUtils.getInstance().put(CacheK.proExp, newExp)

    }

    fun getProExp(): Int {
        return SPUtils.getInstance().getInt(CacheK.proExp, 0)
    }


    fun savePageBg(pageType: String, srcFilePath: String?) {
        if (null == srcFilePath) return
        FileUtils.delete(getPageBg(pageType))//删除原来的
        SPUtils.getInstance().put(pageType, srcFilePath)

    }

    fun getPageBg(pageType: String): String? {
        return SPUtils.getInstance().getString(pageType, "")
    }

    fun removePageBg(pageType: String) {
        FileUtils.delete(getPageBg(pageType))//删除原来的
        SPUtils.getInstance().remove(pageType)

    }

    fun saveCaptureContinue(captureContinue: Boolean) {
        App.captureContinue = captureContinue
        SPUtils.getInstance().put(CacheK.captureContinue, captureContinue)
    }

    fun getCaptureContinue(): Boolean {
        App.captureContinue = SPUtils.getInstance().getBoolean(CacheK.captureContinue, false)
        return App.captureContinue
    }


    fun savePhotoContinueInterval(interval: Int) {
        App.captureContinueInterval = interval
        SPUtils.getInstance().put(CacheK.captureContinueInterval, interval)
    }

    fun getPhotoContinueInterval(): Int? {
        if (App.captureContinueInterval != null) {
            return App.captureContinueInterval
        }
        App.captureContinueInterval =
            SPUtils.getInstance().getInt(CacheK.captureContinueInterval, 3)
        return App.captureContinueInterval
    }


    fun savePhotoContinueCount(count: Int) {
        App.captureContinueCount = count
        SPUtils.getInstance().put(CacheK.captureContinueCount, count)
    }

    fun getPhotoContinueCount(): Int? {
        if (App.captureContinueCount != null) {
            return App.captureContinueCount
        }
        App.captureContinueCount = SPUtils.getInstance().getInt(CacheK.captureContinueCount, 5)
        return App.captureContinueCount
    }

    //-------------------
    fun saveTaskCapture(taskCapture: String) {
        App.taskCapture = taskCapture
        SPUtils.getInstance().put(CacheK.taskCapture, taskCapture)
    }

    fun getTaskCapture(): String {
        App.taskCapture = SPUtils.getInstance().getString(CacheK.taskCapture, "")
        return App.taskCapture
    }

    fun saveTaskSingleHour(taskSingleHour: String) {
        App.taskSingleHour = taskSingleHour
        SPUtils.getInstance().put(CacheK.taskSingleHour, taskSingleHour)
    }

    fun getTaskSingleHour(): String {
        App.taskSingleHour = SPUtils.getInstance().getString(CacheK.taskSingleHour, "00")
        return App.taskSingleHour
    }

    fun saveTaskSingleMin(taskSingleMin: String) {
        App.taskSingleMin = taskSingleMin
        SPUtils.getInstance().put(CacheK.taskSingleMin, taskSingleMin)
    }

    fun getTaskSingleMin(): String {
        App.taskSingleMin = SPUtils.getInstance().getString(CacheK.taskSingleMin, "00")
        return App.taskSingleMin
    }


    fun saveTaskHourlyHour(taskHourlyHour: String) {
        App.taskHourlyHour = taskHourlyHour
        SPUtils.getInstance().put(CacheK.taskHourlyHour, taskHourlyHour)
    }

    fun getTaskHourlyHour(): String {
        App.taskHourlyHour = SPUtils.getInstance().getString(CacheK.taskHourlyHour, "00")
        return App.taskHourlyHour
    }

    fun saveTaskHourlyMin(taskHourlyMin: String) {
        App.taskHourlyMin = taskHourlyMin
        SPUtils.getInstance().put(CacheK.taskHourlyMin, taskHourlyMin)
    }

    fun getTaskHourlyMin(): String {
        App.taskHourlyMin = SPUtils.getInstance().getString(CacheK.taskHourlyMin, "00")
        return App.taskHourlyMin
    }


    fun saveTaskDailyHour(taskDailyHour: String) {
        App.taskDailyHour = taskDailyHour
        SPUtils.getInstance().put(CacheK.taskDailyHour, taskDailyHour)
    }

    fun getTaskDailyHour(): String {
        App.taskDailyHour = SPUtils.getInstance().getString(CacheK.taskDailyHour, "00")
        return App.taskDailyHour
    }

    fun saveTaskDailyMin(taskDailyMin: String) {
        App.taskDailyMin = taskDailyMin
        SPUtils.getInstance().put(CacheK.taskDailyMin, taskDailyMin)
    }

    fun getTaskDailyMin(): String {
        App.taskDailyMin = SPUtils.getInstance().getString(CacheK.taskDailyMin, "00")
        return App.taskDailyMin
    }

    //-------------------

    fun saveRecoverMode(recoverMode: String) {
        App.recoverMode = recoverMode
        SPUtils.getInstance().put(CacheK.recoverMode, recoverMode)
    }

    fun getRecoverMode(): String {
        App.recoverMode = SPUtils.getInstance().getString(CacheK.recoverMode, CommonK.Mode_silent)
        return App.recoverMode
    }

    fun saveLocalUser(user: UserInfo?): Boolean {
        App.localUserInfo = user
        if (null == user) {
            //清除localUser
            saveAuthToken(null)
            SPUtils.getInstance().remove(CacheK.localUserCache)
            return true
        }
        val userJson = GsonUtils.toJson(user)
        if (!userJson.isNullOrBlank()) {
            SPUtils.getInstance().put(
                CacheK.localUserCache,
                userJson
            )
            return true
        }
        return false
    }

    fun getLocalUser(): UserInfo? {
        var localUser: UserInfo? = App.localUserInfo
        if (localUser != null) {
            return App.localUserInfo
        }
        val localUserJson = SPUtils.getInstance().getString(CacheK.localUserCache)
        if (!localUserJson.isNullOrBlank()) {
            localUser = GsonUtils.fromJson(
                localUserJson,
                UserInfo::class.java
            )
        }
        App.localUserInfo = localUser
        return App.localUserInfo
    }

    fun saveAuthToken(token: String?) {
        App.authToken = token
        if (null == token) {
            SPUtils.getInstance().remove(CacheK.authToken)
            return
        }
        SPUtils.getInstance().put(CacheK.authToken, App.authToken)
    }

    fun getAuthToken(): String? {
        if (null != App.authToken) {
            return App.authToken
        }
        App.authToken = SPUtils.getInstance().getString(CacheK.authToken, "")
        return App.authToken
    }
}