package com.dillon.supercam.base


import android.app.Application
import android.os.Environment
import com.blankj.utilcode.util.*
import com.dillon.supercam.bean.*
import com.dillon.supercam.key.CacheK
import com.dillon.supercam.key.CommonK
import com.dillon.supercam.key.CommonK.Mode_silent
import com.dillon.supercam.services.ASer
import com.dillon.supercam.utils.CoreUtils
import com.dillon.supercam.utils.LocalUtils


/**
 * Created by dillon on 2017/6/11.
 */

class App : Application() {
    companion object {
        var appConfig: ConfigInfo? = null

        var authToken: String? = null

        var acOpen: Boolean = true
        var pauseTaskSer: Boolean = false

        var localUserInfo: UserInfo? = null

        var savePhotoDir = ""//图片文件保存目录
        var saveVideoDir = "" //视频文件保存目录
        var saveAudioDir = ""//录音文件保存目录
        var saveTempDir = ""//temp文件保存目录

        var captureTime: String? = null
        var imageSizeType: String? = CommonK.Type_size_2K

        var appPassWord: String? = null


        var cameraType: String? = null
        var mediaType: String? = null
        var remindType: String? = null


        var videoMaxDur: Int? = null //录像时长
        var audioMaxDur: Int? = null //录音时长

        var capturingVideo: Boolean = false//是否在录制
        var capturingPhoto: Boolean = false//是否在拍照
        var capturingAudio: Boolean = false//是否在录音

        var needRefresh = true


        var configInfo: ConfigInfo? = null

        var homeBgPath: String? = null
        var settingBgPath: String? = null
        var passBgPath: String? = null
        var faqBgPath: String? = null
        var captureBgPath: String? = null
        var chatBgPath: String? = null

        var appHide = false
        var currentNotice: String? = null

        var captureContinue = false//选择了连拍
        var captureContinueInterval: Int? = null //连拍间隔
        var captureContinueCount: Int? = null //连拍数量

        var isCaptureContinuing = false//正在计时连拍

        var taskCapture = ""
        var taskSingleHour = "00" //定时任务时
        var taskSingleMin = "00" //定时任务分
        var taskHourlyHour = "00" //定时任务时
        var taskHourlyMin = "00" //定时任务分
        var taskDailyHour = "00" //定时任务时
        var taskDailyMin = "00" //定时任务分

        var updateInfo: UpdateInfo? = null
        var recoverMode = Mode_silent

        var currentMode: String? = CommonK.Type_Home_Key //当前启动模式

        var roomCurrentMsgList = mutableListOf<MsgInfo.MsgInfoItem>()
        var msgInfoItemHisList = mutableListOf<MsgInfo.MsgInfoItem>()
        var tempMediaInfo: MediaInfo? = null

        var latestMsgName: String? = "1_1"//最新消息
        var showLatestMsgRed = false //标志最新消息的小红点
        var checkMsgLatestPause = false //标志是否请求最新消息
        var lastMsgInfoItem: MsgInfo.MsgInfoItem? = null//room最后一条消息

    }

    override fun onCreate() {
        super.onCreate()
        initAUC()
        initFileSave()
        iniLocalData()
        CoreUtils.initShortcuts(this)
    }


    private fun initAUC() {
        Utils.init(this)
        LogUtils.getConfig().setConsoleSwitch(AppUtils.isAppDebug())
    }

    private fun initFileSave() {
        savePhotoDir = PathUtils.getExternalAppFilesPath() + "/photo/"
        saveVideoDir = PathUtils.getExternalAppFilesPath() + "/video/"
        saveAudioDir = PathUtils.getExternalAppFilesPath() + "/audio/"
        saveTempDir = PathUtils.getExternalDocumentsPath() + "/SuperCam/"

        FileUtils.createOrExistsDir(saveTempDir)
        FileUtils.createOrExistsDir(saveAudioDir)
        FileUtils.createOrExistsDir(savePhotoDir)
        FileUtils.createOrExistsDir(saveVideoDir)

    }




    private fun iniLocalData() {
        if (null != LocalUtils.getLocalUser()) {
            CoreUtils.simpleLog("EXP:" + LocalUtils.getProExp() + " " + "COMMON_START")
        }
        acOpen = SPUtils.getInstance().getBoolean(CacheK.acOpen, true)
        CoreUtils.startCommonService(ASer::class.java)
    }
}