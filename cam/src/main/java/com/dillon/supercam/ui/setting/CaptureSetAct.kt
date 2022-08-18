package com.dillon.supercam.ui.setting

import android.annotation.SuppressLint
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.transition.Transition
import android.transition.TransitionInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.dillon.dialogs.MaterialDialog
import com.blankj.utilcode.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dillon.supercam.R
import com.dillon.supercam.base.App
import com.dillon.supercam.base.BAct
import com.dillon.supercam.key.ActionK
import com.dillon.supercam.key.CacheK
import com.dillon.supercam.key.CommonK
import com.dillon.supercam.key.CommonK.requestCaptureDaily
import com.dillon.supercam.key.CommonK.requestCaptureHourly
import com.dillon.supercam.key.CommonK.requestCaptureSingle
import com.dillon.supercam.receivers.ARec
import com.dillon.supercam.services.task.CheckDisturbSer
import com.dillon.supercam.services.task.CheckNotificationsSer
import com.dillon.supercam.utils.AlarmUtils
import com.dillon.supercam.utils.CoreUtils
import com.dillon.supercam.utils.LocalUtils
import com.dillon.supercam.utils.view.camera.CameraUtil
import com.dillon.supercam.utils.view.leonids.ParticleSystem
import com.dillon.supercam.utils.view.leonids.modifiers.ScaleModifier
import kotlinx.android.synthetic.main.activity_capture_set.*
import kotlinx.android.synthetic.main.layout_title_capture_set.*
import kotlinx.android.synthetic.main.layout_title_capture_set.iv_title_left
import kotlinx.android.synthetic.main.layout_title_capture_set.lay_title_left
import kotlinx.android.synthetic.main.layout_title_capture_set.sw_ac
import kotlinx.android.synthetic.main.layout_title_home.*
import java.util.*


class CaptureSetAct : BAct(), View.OnClickListener {

    private var materialDialog: MaterialDialog? = null
    private var permissionsDialog: MaterialDialog? = null

    private var maxAudioDurCommon = 60
    private var maxVideoDurCommon = 30

    private var maxPhotoInterval = 100
    private var maxPhotoCount = 99


    override fun initUi() {
        super.initUi()
        setContentView(R.layout.activity_capture_set)
        actTran()
    }

    private fun setAppBg() {
        if (!ActivityUtils.isActivityAlive(this)) return
        App.captureBgPath = LocalUtils.getPageBg(CommonK.PAGE_CAPTURE)
        if (App.captureBgPath.isNullOrBlank()) return
        Glide.with(this).load(App.captureBgPath)
            //.skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(act_bg)

    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        super.initView()
        setAppBg()
        lay_title_left.setOnClickListener {
            //finish()
            onBackPressed()
        }

        tv_title_center.setOnClickListener {
            val mTitle = AppUtils.getAppName() + "  v" + AppUtils.getAppVersionName()
            val mMessage: String
            if (App.configInfo != null) {
                mMessage = if (CoreUtils.checkZh()) {
                    if (App.configInfo!!.appDes.isNullOrBlank()) {
                        resources.getString(R.string.app_des)
                    } else {
                        App.configInfo!!.appDes!!
                    }

                } else {
                    if (App.configInfo!!.appDesEn.isNullOrBlank()) {
                        resources.getString(R.string.app_des)
                    } else {
                        App.configInfo!!.appDesEn!!
                    }
                }
            } else {
                mMessage = resources.getString(R.string.app_des)
            }
            materialDialog?.cancel()
            materialDialog = MaterialDialog(this)
                .icon(R.drawable.ic_logo)
                .title(text = mTitle)
                .message(text = mMessage)
                .cornerRadius(res = R.dimen.dp_20)
            if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                    this
                )
            ) {
                materialDialog?.show()
            }
        }


        if (App.localUserInfo != null) {
            iv_media_photo_continue_float.visibility = View.INVISIBLE
            LocalUtils.getMediaType()
            LocalUtils.getCaptureContinue()
            when (App.mediaType) {
                CommonK.Type_Media_Photo -> {
                    cb_media_photo_continue.visibility = View.VISIBLE
                    if (App.captureContinue) {
                        cb_media_photo_continue.isChecked = true
                        lay_photo_continue_max.visibility = View.VISIBLE
                    } else {
                        cb_media_photo_continue.isChecked = false
                        lay_photo_continue_max.visibility = View.INVISIBLE
                    }
                }
                else -> {
                    lay_photo_continue.visibility = View.INVISIBLE
                }
            }
        }


        sw_ac.isChecked = App.acOpen
        lay_smart_capture.visibility = if (App.acOpen) View.VISIBLE else View.GONE
        lay_timer_capture.visibility = if (App.acOpen) View.VISIBLE else View.GONE

        sw_ac.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                App.acOpen = true
                //ToastUtils.showShort(resources.getString(R.string.ac_open))
                SPUtils.getInstance().put(CacheK.acOpen, true)
                CoreUtils.simpleLog("SMART_OPEN")
            } else {
                App.acOpen = false
                SPUtils.getInstance().put(CacheK.acOpen, false)
                CameraUtil.stopCaptureService()
                CoreUtils.recoverMode()
                CoreUtils.simpleLog("SMART_CLOSE")

            }
            lay_smart_capture.visibility = if (App.acOpen) View.VISIBLE else View.GONE
            lay_timer_capture.visibility = if (App.acOpen) View.VISIBLE else View.GONE
        }

        //----------
        LocalUtils.getMediaType()

        when (App.mediaType) {
            CommonK.Type_Media_Photo -> {
                cb_media_photo.isChecked = true
                cb_media_video.isChecked = false
                cb_media_audio.isChecked = false
                lay_camera.visibility = View.VISIBLE
            }
            CommonK.Type_Media_Video -> {
                cb_media_video.isChecked = true
                cb_media_photo.isChecked = false
                cb_media_audio.isChecked = false
                lay_camera.visibility = View.VISIBLE
                lay_photo_continue.visibility = View.INVISIBLE
            }
            CommonK.Type_Media_Audio -> {
                cb_media_audio.isChecked = true
                cb_media_photo.isChecked = false
                cb_media_video.isChecked = false
                lay_camera.visibility = View.GONE
                lay_photo_continue.visibility = View.INVISIBLE
            }
            else -> {
                cb_media_photo.isChecked = true
                cb_media_video.isChecked = false
                cb_media_audio.isChecked = false
                lay_camera.visibility = View.VISIBLE

            }
        }

        //------
        LocalUtils.getCameraType()
        when (App.cameraType) {
            CommonK.Type_Camera_Back -> {
                cb_back_camera.isChecked = true
                cb_front_camera.isChecked = false
            }
            else -> {
                cb_back_camera.isChecked = false
                cb_front_camera.isChecked = true
            }
        }

        //-------------
        LocalUtils.getCaptureTime()
        when (App.captureTime) {
            CommonK.Type_Screen_On -> {
                cb_screen_on.isChecked = true
                cb_screen_off.isChecked = false
                cb_home_key.isChecked = false
                cb_recent_key.isChecked = false
                cb_volume_key.isChecked = false

            }
            CommonK.Type_Screen_Off -> {
                cb_screen_on.isChecked = false
                cb_screen_off.isChecked = true
                cb_home_key.isChecked = false
                cb_recent_key.isChecked = false
                cb_volume_key.isChecked = false

            }

            CommonK.Type_Home_Key -> {
                cb_screen_on.isChecked = false
                cb_screen_off.isChecked = false
                cb_recent_key.isChecked = false
                cb_home_key.isChecked = true
                cb_volume_key.isChecked = false

            }
            CommonK.Type_Recent_Key -> {
                cb_screen_on.isChecked = false
                cb_screen_off.isChecked = false
                cb_home_key.isChecked = false
                cb_recent_key.isChecked = true
                cb_volume_key.isChecked = false

            }
            CommonK.Type_Volume_Key -> {
                cb_screen_on.isChecked = false
                cb_screen_off.isChecked = false
                cb_home_key.isChecked = false
                cb_recent_key.isChecked = false
                cb_volume_key.isChecked = true

            }
            else -> {
                cb_screen_on.isChecked = false
                cb_screen_off.isChecked = false
                cb_recent_key.isChecked = false
                cb_volume_key.isChecked = false
                cb_home_key.isChecked = true
            }
        }

        //---------------
        LocalUtils.getRemindType()
        when (App.remindType) {
            CommonK.Type_Remind_Both -> {
                cb_remind_both.isChecked = true
                cb_remind_start.isChecked = false
                cb_remind_stop.isChecked = false
                cb_remind_none.isChecked = false

            }
            CommonK.Type_Remind_None -> {
                cb_remind_both.isChecked = false
                cb_remind_start.isChecked = false
                cb_remind_stop.isChecked = false
                cb_remind_none.isChecked = true

            }
            CommonK.Type_Remind_Start -> {
                cb_remind_both.isChecked = false
                cb_remind_start.isChecked = true
                cb_remind_stop.isChecked = false
                cb_remind_none.isChecked = false

            }
            CommonK.Type_Remind_Stop -> {
                cb_remind_both.isChecked = false
                cb_remind_start.isChecked = false
                cb_remind_stop.isChecked = true
                cb_remind_none.isChecked = false

            }
            else -> {
                cb_remind_both.isChecked = false
                cb_remind_start.isChecked = false
                cb_remind_stop.isChecked = false
                cb_remind_none.isChecked = true
            }
        }

        //--------------
        LocalUtils.getImageSizeType()
        when (App.imageSizeType) {
            CommonK.Type_size_2K -> {
                cb_size_2k.isChecked = true
                cb_size_4k.isChecked = false
                cb_size_1080p.isChecked = false
                cb_size_best.isChecked = false

            }

            CommonK.Type_size_4K -> {
                cb_size_2k.isChecked = false
                cb_size_4k.isChecked = true
                cb_size_1080p.isChecked = false
                cb_size_best.isChecked = false

            }
            CommonK.Type_size_1080 -> {
                cb_size_2k.isChecked = false
                cb_size_4k.isChecked = false
                cb_size_1080p.isChecked = true
                cb_size_best.isChecked = false

            }
            CommonK.Type_size_Best -> {
                cb_size_2k.isChecked = false
                cb_size_4k.isChecked = false
                cb_size_1080p.isChecked = false
                cb_size_best.isChecked = true

            }
            else -> {
                cb_size_2k.isChecked = true
                cb_size_4k.isChecked = false
                cb_size_1080p.isChecked = false
                cb_size_best.isChecked = false
            }
        }


        et_photo_interval.setText(LocalUtils.getPhotoContinueInterval().toString())
        et_photo_interval.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) { //获得焦点
                // LogUtils.i("获得焦点")
                //  fireWorkView.bindEditText(et_video_max_dur)

            } else { //失去焦点
                //LogUtils.i("失去焦点")
                //   fireWorkView.removeBind()
                try {
                    var numDurStr = et_photo_interval.text.toString().trim()
                    if (numDurStr.isBlank() || 3 > numDurStr.toInt()) {
                        ToastUtils.showShort(getString(R.string.min_photo_interval))
                        numDurStr = "3"
                        et_photo_interval.setText(numDurStr)
                    }
                    if (App.localUserInfo!!.pro && numDurStr.toInt() > maxPhotoInterval) {
                        ToastUtils.showLong(getString(R.string.over_photo_interval))
                        numDurStr = "" + maxPhotoInterval
                        et_photo_interval.setText(numDurStr)
                    }

                    LocalUtils.savePhotoContinueInterval(numDurStr.toInt())
                    LogUtils.i(numDurStr.toInt())
                } catch (e: Exception) {
                }
            }
        }

        et_photo_count.setText(LocalUtils.getPhotoContinueCount().toString())
        et_photo_count.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) { //获得焦点
                // LogUtils.i("获得焦点")
                //  fireWorkView.bindEditText(et_video_max_dur)

            } else { //失去焦点
                //LogUtils.i("失去焦点")
                //   fireWorkView.removeBind()
                try {
                    var numDurStr = et_photo_count.text.toString().trim()
                    if (numDurStr.isBlank() || 2 > numDurStr.toInt()) {
                        ToastUtils.showShort(getString(R.string.min_photo_count))
                        numDurStr = "2"
                        et_photo_count.setText(numDurStr)
                    }
                    if (App.localUserInfo!!.pro && numDurStr.toInt() > maxPhotoCount) {
                        ToastUtils.showLong(getString(R.string.over_photo_count))
                        numDurStr = "" + maxPhotoCount
                        et_photo_count.setText(numDurStr)
                    }

                    LocalUtils.savePhotoContinueCount(numDurStr.toInt())
                    LogUtils.i(numDurStr.toInt())
                } catch (e: Exception) {
                }
            }
        }

        et_video_max_dur.setText(LocalUtils.getVideoMaxDur().toString())
        et_video_max_dur.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) { //获得焦点
                // LogUtils.i("获得焦点")
                //  fireWorkView.bindEditText(et_video_max_dur)

            } else { //失去焦点
                //LogUtils.i("失去焦点")
                //   fireWorkView.removeBind()
                try {
                    var numDurStr = et_video_max_dur.text.toString().trim()
                    if (numDurStr.isBlank() || 5 > numDurStr.toInt()) {
                        ToastUtils.showShort(getString(R.string.video_min_dur_tip))
                        numDurStr = "5"
                        et_video_max_dur.setText(numDurStr)
                    }
                    if (!App.localUserInfo!!.pro && numDurStr.toInt() > maxVideoDurCommon) {
                        ToastUtils.showLong(getString(R.string.over_30s))
                        numDurStr = "" + maxVideoDurCommon
                        et_video_max_dur.setText(numDurStr)
                    }

                    LocalUtils.saveVideoMaxDur(numDurStr.toInt())
                    LogUtils.i(numDurStr.toInt())
                } catch (e: Exception) {
                }
            }
        }

        et_audio_max_dur.setText(LocalUtils.getAudioMaxDur().toString())
        et_audio_max_dur.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) { //获得焦点
                // LogUtils.i("获得焦点")
                //    fireWorkView.bindEditText(et_audio_max_dur)

            } else { //失去焦点
                //LogUtils.i("失去焦点")
                //     fireWorkView.removeBind()
                try {
                    var numDurStr = et_audio_max_dur.text.toString().trim()
                    if (numDurStr.isBlank() || 5 > numDurStr.toInt()) {
                        ToastUtils.showShort(getString(R.string.audio_min_dur_tip))
                        numDurStr = "5"
                        et_audio_max_dur.setText(numDurStr)
                    }
                    if (!App.localUserInfo!!.pro && numDurStr.toInt() > maxAudioDurCommon) {
                        ToastUtils.showLong(getString(R.string.over_60s))
                        numDurStr = "" + maxAudioDurCommon
                        et_audio_max_dur.setText(numDurStr)
                    }

                    LocalUtils.saveAudioMaxDur(numDurStr.toInt())
                    LogUtils.i(numDurStr.toInt())
                } catch (e: Exception) {
                }
            }
        }

        et_task_hour_capture_single.setText(LocalUtils.getTaskSingleHour())
        et_task_hour_capture_single.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) { //获得焦点
                    // LogUtils.i("获得焦点")
                    //  fireWorkView.bindEditText(et_video_max_dur)

                } else { //失去焦点
                    //LogUtils.i("失去焦点")
                    //   fireWorkView.removeBind()
                    try {
                        var numDurStr = et_task_hour_capture_single.text.toString().trim()
                        if (numDurStr.isBlank()) {
                            numDurStr = "00"
                            et_task_hour_capture_single.setText(numDurStr)
                        }

                        if (numDurStr.toInt() == 0 && et_task_min_capture_single.text.toString()
                                .trim().toInt() == 0
                        ) {
                            et_task_hour_capture_single.setText("00")
                            cb_task_capture_single.isChecked = false
                            LocalUtils.saveTaskCapture("")
                            return@OnFocusChangeListener
                        }

                        if (numDurStr.toInt() > 23) {
                            ToastUtils.showShort(getString(R.string.task_hour_max_tip))
                            numDurStr = "23"
                            et_task_hour_capture_single.setText(numDurStr)
                        }
                        if (numDurStr.toInt() in 0..9) {
                            numDurStr = "0" + numDurStr.toInt()
                            et_task_hour_capture_single.setText(numDurStr)
                        }
                        LocalUtils.saveTaskSingleHour(numDurStr)

                        LogUtils.i(numDurStr.toInt())
                        alarmSingle()

                    } catch (e: Exception) {
                    }
                }
            }

        et_task_min_capture_single.setText(LocalUtils.getTaskSingleMin())
        et_task_min_capture_single.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) { //获得焦点
                    // LogUtils.i("获得焦点")
                    //  fireWorkView.bindEditText(et_video_max_dur)

                } else { //失去焦点
                    //LogUtils.i("失去焦点")
                    //   fireWorkView.removeBind()
                    try {
                        var numDurStr = et_task_min_capture_single.text.toString().trim()
                        if (numDurStr.isBlank()) {
                            numDurStr = "00"
                            et_task_min_capture_single.setText(numDurStr)
                        }

                        if (numDurStr.toInt() == 0 && et_task_hour_capture_single.text.toString()
                                .trim().toInt() == 0
                        ) {
                            et_task_min_capture_single.setText("00")
                            cb_task_capture_single.isChecked = false
                            LocalUtils.saveTaskCapture("")
                            return@OnFocusChangeListener
                        }

                        if (numDurStr.toInt() > 59) {
                            ToastUtils.showShort(getString(R.string.task_min_max_tip))
                            numDurStr = "59"
                            et_task_min_capture_single.setText(numDurStr)
                        }
                        if (numDurStr.toInt() in 0..9) {
                            numDurStr = "0" + numDurStr.toInt()
                            et_task_min_capture_single.setText(numDurStr)
                        }
                        LocalUtils.saveTaskSingleMin(numDurStr)
                        LogUtils.i(numDurStr.toInt())
                        alarmSingle()

                    } catch (e: Exception) {
                    }
                }
            }

        et_task_hour_capture_hourly.setText(LocalUtils.getTaskHourlyHour())
        et_task_hour_capture_hourly.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) { //获得焦点
                    // LogUtils.i("获得焦点")
                    //  fireWorkView.bindEditText(et_video_max_dur)

                } else { //失去焦点
                    //LogUtils.i("失去焦点")
                    //   fireWorkView.removeBind()
                    try {
                        var numDurStr = et_task_hour_capture_hourly.text.toString().trim()
                        if (numDurStr.isBlank()) {
                            numDurStr = "00"
                            et_task_hour_capture_hourly.setText(numDurStr)
                        }

                        if (numDurStr.toInt() == 0 && et_task_min_capture_hourly.text.toString()
                                .trim().toInt() == 0
                        ) {
                            et_task_hour_capture_hourly.setText("00")

                            cb_task_capture_hourly.isChecked = false
                            LocalUtils.saveTaskCapture("")
                            return@OnFocusChangeListener
                        }
                        if (numDurStr.toInt() > 23) {
                            ToastUtils.showShort(getString(R.string.task_hour_max_tip))
                            numDurStr = "23"
                            et_task_hour_capture_hourly.setText(numDurStr)
                        }
                        if (numDurStr.toInt() in 0..9) {
                            numDurStr = "0" + numDurStr.toInt()
                            et_task_hour_capture_hourly.setText(numDurStr)
                        }
                        LocalUtils.saveTaskHourlyHour(numDurStr)

                        LogUtils.i(numDurStr.toInt())
                        alarmHourly()

                    } catch (e: Exception) {
                    }
                }
            }

        et_task_min_capture_hourly.setText(LocalUtils.getTaskHourlyMin())
        et_task_min_capture_hourly.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) { //获得焦点
                    // LogUtils.i("获得焦点")
                    //  fireWorkView.bindEditText(et_video_max_dur)

                } else { //失去焦点
                    //LogUtils.i("失去焦点")
                    //   fireWorkView.removeBind()
                    try {
                        var numDurStr = et_task_min_capture_hourly.text.toString().trim()
                        if (numDurStr.isBlank()) {
                            numDurStr = "00"
                            et_task_min_capture_hourly.setText(numDurStr)
                        }
                        if (numDurStr.toInt() == 0 && et_task_hour_capture_hourly.text.toString()
                                .trim().toInt() == 0
                        ) {
                            et_task_min_capture_hourly.setText("00")

                            cb_task_capture_hourly.isChecked = false
                            LocalUtils.saveTaskCapture("")
                            return@OnFocusChangeListener
                        }

                        if (numDurStr.toInt() > 59) {
                            ToastUtils.showShort(getString(R.string.task_min_max_tip))
                            numDurStr = "59"
                            et_task_min_capture_hourly.setText(numDurStr)
                        }
                        if (numDurStr.toInt() in 0..9) {
                            numDurStr = "0" + numDurStr.toInt()
                            et_task_min_capture_hourly.setText(numDurStr)
                        }
                        LocalUtils.saveTaskHourlyMin(numDurStr)
                        LogUtils.i(numDurStr.toInt())

                        alarmHourly()

                    } catch (e: Exception) {
                    }
                }
            }

        et_task_hour_capture_daily.setText(LocalUtils.getTaskDailyHour())
        et_task_hour_capture_daily.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) { //获得焦点
                    // LogUtils.i("获得焦点")
                    //  fireWorkView.bindEditText(et_video_max_dur)

                } else { //失去焦点
                    //LogUtils.i("失去焦点")
                    //   fireWorkView.removeBind()
                    try {
                        var numDurStr = et_task_hour_capture_daily.text.toString().trim()
                        if (numDurStr.isBlank()) {
                            numDurStr = "00"
                            et_task_hour_capture_daily.setText(numDurStr)
                        }
                        if (numDurStr.toInt() == 0 && et_task_min_capture_daily.text.toString()
                                .trim().toInt() == 0
                        ) {
                            et_task_hour_capture_daily.setText("00")

                            cb_task_capture_daily.isChecked = false
                            LocalUtils.saveTaskCapture("")
                            return@OnFocusChangeListener
                        }
                        if (numDurStr.toInt() > 23) {
                            ToastUtils.showShort(getString(R.string.task_hour_max_tip))
                            numDurStr = "23"
                            et_task_hour_capture_daily.setText(numDurStr)
                        }
                        if (numDurStr.toInt() in 0..9) {
                            numDurStr = "0" + numDurStr.toInt()
                            et_task_hour_capture_daily.setText(numDurStr)
                        }
                        LocalUtils.saveTaskDailyHour(numDurStr)

                        LogUtils.i(numDurStr.toInt())
                        alarmDaily()

                    } catch (e: Exception) {
                    }
                }
            }

        et_task_min_capture_daily.setText(LocalUtils.getTaskDailyMin())
        et_task_min_capture_daily.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) { //获得焦点
                    // LogUtils.i("获得焦点")
                    //  fireWorkView.bindEditText(et_video_max_dur)

                } else { //失去焦点
                    //LogUtils.i("失去焦点")
                    //   fireWorkView.removeBind()
                    try {
                        var numDurStr = et_task_min_capture_daily.text.toString().trim()
                        if (numDurStr.isBlank()) {
                            numDurStr = "00"
                            et_task_min_capture_daily.setText(numDurStr)
                        }
                        if (numDurStr.toInt() == 0 && et_task_hour_capture_daily.text.toString()
                                .trim().toInt() == 0
                        ) {
                            et_task_min_capture_daily.setText("00")

                            cb_task_capture_daily.isChecked = false
                            LocalUtils.saveTaskCapture("")
                            return@OnFocusChangeListener
                        }

                        if (numDurStr.toInt() > 59) {
                            ToastUtils.showShort(getString(R.string.task_min_max_tip))
                            numDurStr = "59"
                            et_task_min_capture_daily.setText(numDurStr)
                        }
                        if (numDurStr.toInt() in 0..9) {
                            numDurStr = "0" + numDurStr.toInt()
                            et_task_min_capture_daily.setText(numDurStr)
                        }
                        LocalUtils.saveTaskDailyMin(numDurStr)
                        LogUtils.i(numDurStr.toInt())

                        alarmDaily()

                    } catch (e: Exception) {
                    }
                }
            }

    }

    override fun initData() {


        //--------------CameraType---------
        cb_front_camera.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveCameraType(
                    CommonK.Type_Camera_Front
                )
                cb_back_camera.isChecked = false

            } else {
                if (!cb_back_camera.isChecked) {
                    cb_front_camera.isChecked = true
                }
            }
        }
        cb_back_camera.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveCameraType(
                    CommonK.Type_Camera_Back
                )
                cb_front_camera.isChecked = false

            } else {
                if (!cb_front_camera.isChecked) {
                    cb_back_camera.isChecked = true
                }
            }
        }

        //----------------MediaType----------

        cb_media_photo_continue.setOnCheckedChangeListener { _, isChecked ->
            LocalUtils.saveCaptureContinue(isChecked)
            if (isChecked) {
                lay_photo_continue_max.visibility = View.VISIBLE
            } else {
                lay_photo_continue_max.visibility = View.INVISIBLE
            }
        }
        cb_media_photo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveMediaType(
                    CommonK.Type_Media_Photo
                )
                cb_media_video.isChecked = false
                cb_media_audio.isChecked = false
                lay_camera.visibility = View.VISIBLE
                lay_photo_continue.visibility = View.VISIBLE

                if (App.localUserInfo!!.pro || LocalUtils.getProExp() > 100) {
                    cb_media_photo_continue.visibility = View.VISIBLE
                    iv_media_photo_continue_float.visibility = View.INVISIBLE
                    if (LocalUtils.getCaptureContinue()) {
                        cb_media_photo_continue.isChecked = true
                        lay_photo_continue_max.visibility = View.VISIBLE
                    } else {
                        cb_media_photo_continue.isChecked = false
                        lay_photo_continue_max.visibility = View.INVISIBLE
                    }
                } else {
                    cb_media_photo_continue.visibility = View.INVISIBLE
                    iv_media_photo_continue_float.visibility = View.VISIBLE
                    lay_photo_continue_max.visibility = View.INVISIBLE
                }

            } else {
                if (!cb_media_video.isChecked && !cb_media_audio.isChecked) {
                    cb_media_photo.isChecked = true
                    lay_camera.visibility = View.VISIBLE
                    lay_photo_continue.visibility = View.VISIBLE
                    if (App.localUserInfo!!.pro || LocalUtils.getProExp() > 100) {
                        cb_media_photo_continue.visibility = View.VISIBLE
                        iv_media_photo_continue_float.visibility = View.INVISIBLE
                        if (LocalUtils.getCaptureContinue()) {
                            cb_media_photo_continue.isChecked = true
                            lay_photo_continue_max.visibility = View.VISIBLE
                        } else {
                            cb_media_photo_continue.isChecked = false
                            lay_photo_continue_max.visibility = View.INVISIBLE
                        }
                    } else {
                        cb_media_photo_continue.visibility = View.INVISIBLE
                        iv_media_photo_continue_float.visibility = View.VISIBLE
                        lay_photo_continue_max.visibility = View.INVISIBLE
                    }

                }
            }
        }
        cb_media_video.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveMediaType(
                    CommonK.Type_Media_Video
                )
                cb_media_photo.isChecked = false
                cb_media_audio.isChecked = false
                lay_camera.visibility = View.VISIBLE
                lay_photo_continue.visibility = View.INVISIBLE
            } else {
                if (!cb_media_photo.isChecked && !cb_media_audio.isChecked) {
                    cb_media_video.isChecked = true
                    lay_camera.visibility = View.VISIBLE
                    lay_photo_continue.visibility = View.INVISIBLE
                }
            }
        }
        cb_media_audio.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveMediaType(
                    CommonK.Type_Media_Audio
                )
                cb_media_video.isChecked = false
                cb_media_photo.isChecked = false
                lay_camera.visibility = View.GONE
                lay_photo_continue.visibility = View.INVISIBLE

            } else {
                if (!cb_media_video.isChecked && !cb_media_photo.isChecked) {
                    cb_media_audio.isChecked = true
                    lay_camera.visibility = View.GONE
                    lay_photo_continue.visibility = View.INVISIBLE
                }
            }
        }

        //------------StatusType-----------
        cb_screen_on.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveCaptureTime(
                    CommonK.Type_Screen_On
                )
                cb_screen_off.isChecked = false
                cb_home_key.isChecked = false
                cb_recent_key.isChecked = false
                cb_volume_key.isChecked = false

            } else {
                if (!cb_screen_off.isChecked && !cb_home_key.isChecked && !cb_recent_key.isChecked && !cb_volume_key.isChecked) {
                    cb_screen_on.isChecked = true
                }
            }

        }
        cb_screen_off.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveCaptureTime(
                    CommonK.Type_Screen_Off
                )
                cb_screen_on.isChecked = false
                cb_home_key.isChecked = false
                cb_recent_key.isChecked = false
                cb_volume_key.isChecked = false


            } else {
                if (!cb_screen_on.isChecked && !cb_home_key.isChecked && !cb_recent_key.isChecked && !cb_volume_key.isChecked) {
                    cb_screen_off.isChecked = true
                }
            }
        }
        cb_home_key.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveCaptureTime(
                    CommonK.Type_Home_Key
                )
                cb_screen_off.isChecked = false
                cb_screen_on.isChecked = false
                cb_recent_key.isChecked = false
                cb_volume_key.isChecked = false


            } else {
                if (!cb_screen_off.isChecked && !cb_screen_on.isChecked && !cb_recent_key.isChecked && !cb_volume_key.isChecked) {
                    cb_home_key.isChecked = true
                }
            }
        }
        cb_recent_key.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveCaptureTime(
                    CommonK.Type_Recent_Key
                )
                cb_screen_off.isChecked = false
                cb_screen_on.isChecked = false
                cb_home_key.isChecked = false
                cb_volume_key.isChecked = false


            } else {
                if (!cb_screen_off.isChecked && !cb_screen_on.isChecked && !cb_home_key.isChecked && !cb_volume_key.isChecked) {
                    cb_recent_key.isChecked = true
                }
            }
        }
        cb_volume_key.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveCaptureTime(
                    CommonK.Type_Volume_Key
                )
                cb_screen_off.isChecked = false
                cb_screen_on.isChecked = false
                cb_home_key.isChecked = false
                cb_recent_key.isChecked = false

            } else {
                if (!cb_screen_off.isChecked && !cb_screen_on.isChecked && !cb_home_key.isChecked && !cb_recent_key.isChecked) {
                    cb_volume_key.isChecked = true
                }
            }
        }

        //------------RemindType-----------
        cb_remind_none.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveRemindType(
                    CommonK.Type_Remind_None
                )
                cb_remind_both.isChecked = false
                cb_remind_start.isChecked = false
                cb_remind_stop.isChecked = false

            } else {
                if (!cb_remind_both.isChecked && !cb_remind_start.isChecked && !cb_remind_stop.isChecked) {
                    cb_remind_none.isChecked = true
                }
            }
        }
        cb_remind_start.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveRemindType(
                    CommonK.Type_Remind_Start
                )
                cb_remind_both.isChecked = false
                cb_remind_none.isChecked = false
                cb_remind_stop.isChecked = false

            } else {
                if (!cb_remind_both.isChecked && !cb_remind_none.isChecked && !cb_remind_stop.isChecked) {
                    cb_remind_start.isChecked = true
                }
            }
        }
        cb_remind_stop.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveRemindType(
                    CommonK.Type_Remind_Stop
                )
                cb_remind_both.isChecked = false
                cb_remind_none.isChecked = false
                cb_remind_start.isChecked = false

            } else {
                if (!cb_remind_both.isChecked && !cb_remind_none.isChecked && !cb_remind_start.isChecked) {
                    cb_remind_stop.isChecked = true
                }
            }
        }
        cb_remind_both.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveRemindType(
                    CommonK.Type_Remind_Both
                )
                cb_remind_stop.isChecked = false
                cb_remind_none.isChecked = false
                cb_remind_start.isChecked = false

            } else {
                if (!cb_remind_stop.isChecked && !cb_remind_none.isChecked && !cb_remind_start.isChecked) {
                    cb_remind_both.isChecked = true
                }
            }
        }

        //------------ImageSizeType-----------
        cb_size_1080p.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveImageSizeType(
                    CommonK.Type_size_1080
                )
                cb_size_2k.isChecked = false
                cb_size_4k.isChecked = false
                cb_size_best.isChecked = false
            } else {
                if (!cb_size_best.isChecked && !cb_size_2k.isChecked && !cb_size_4k.isChecked) {
                    cb_size_1080p.isChecked = true
                }
            }
        }
        cb_size_2k.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveImageSizeType(
                    CommonK.Type_size_2K
                )
                cb_size_4k.isChecked = false
                cb_size_1080p.isChecked = false
                cb_size_best.isChecked = false
            } else {
                if (!cb_size_best.isChecked && !cb_size_1080p.isChecked && !cb_size_4k.isChecked) {
                    cb_size_2k.isChecked = true
                }
            }
        }
        cb_size_4k.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveImageSizeType(
                    CommonK.Type_size_4K
                )
                cb_size_2k.isChecked = false
                cb_size_1080p.isChecked = false
                cb_size_best.isChecked = false
            } else {
                if (!cb_size_best.isChecked && !cb_size_1080p.isChecked && !cb_size_2k.isChecked) {
                    cb_size_4k.isChecked = true
                }
            }
        }
        cb_size_best.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveImageSizeType(
                    CommonK.Type_size_Best
                )
                cb_size_2k.isChecked = false
                cb_size_4k.isChecked = false
                cb_size_1080p.isChecked = false
            } else {
                if (!cb_size_1080p.isChecked && !cb_size_2k.isChecked && !cb_size_4k.isChecked) {
                    cb_size_best.isChecked = true
                }
            }
        }


        //-------------------Task Capture-------------------------
        cb_task_capture_single.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveTaskCapture(CommonK.Task_single)
                cb_task_capture_hourly.isChecked = false
                cb_task_capture_daily.isChecked = false
                lay_task_detail_capture_single.visibility = View.VISIBLE
                lay_task_detail_capture_daily.visibility = View.GONE
                lay_task_detail_capture_hourly.visibility = View.GONE
            } else {
                lay_task_detail_capture_single.visibility = View.GONE
                if (!cb_task_capture_hourly.isChecked && !cb_task_capture_daily.isChecked) {
                    LocalUtils.saveTaskCapture("")
                }
            }
        }

        cb_task_capture_hourly.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveTaskCapture(CommonK.Task_hourly)
                cb_task_capture_single.isChecked = false
                cb_task_capture_daily.isChecked = false
                lay_task_detail_capture_single.visibility = View.GONE
                lay_task_detail_capture_daily.visibility = View.GONE
                lay_task_detail_capture_hourly.visibility = View.VISIBLE
            } else {
                lay_task_detail_capture_hourly.visibility = View.GONE
                if (!cb_task_capture_single.isChecked && !cb_task_capture_daily.isChecked) {
                    LocalUtils.saveTaskCapture("")
                }
            }
        }

        cb_task_capture_daily.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveTaskCapture(CommonK.Task_daily)
                cb_task_capture_hourly.isChecked = false
                cb_task_capture_single.isChecked = false
                lay_task_detail_capture_single.visibility = View.GONE
                lay_task_detail_capture_daily.visibility = View.VISIBLE
                lay_task_detail_capture_hourly.visibility = View.GONE
            } else {
                lay_task_detail_capture_daily.visibility = View.GONE
                if (!cb_task_capture_hourly.isChecked && !cb_task_capture_single.isChecked) {
                    LocalUtils.saveTaskCapture("")
                }
            }
        }
        App.taskCapture = LocalUtils.getTaskCapture()
        when (App.taskCapture) {
            CommonK.Task_single -> {
                cb_task_capture_single.isChecked = true
            }
            CommonK.Task_hourly -> {
                cb_task_capture_hourly.isChecked = true
            }
            CommonK.Task_daily -> {
                cb_task_capture_daily.isChecked = true
            }
            else -> {
                cb_task_capture_single.isChecked = false
                cb_task_capture_hourly.isChecked = false
                cb_task_capture_daily.isChecked = false
            }
        }

        //------------RecoverMode-----------
        cb_recover_silent.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalUtils.saveRecoverMode(CommonK.Mode_silent)
                cb_recover_sound.isChecked = false
                cb_recover_vibrate.isChecked = false
            } else {
                if (!cb_recover_sound.isChecked && !cb_recover_vibrate.isChecked) {
                    cb_recover_silent.isChecked = true
                }
            }

        }
        cb_recover_sound.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                CoreUtils.simpleLog("NORMAL")
                LocalUtils.saveRecoverMode(CommonK.Mode_sound)
                cb_recover_silent.isChecked = false
                cb_recover_vibrate.isChecked = false
            } else {
                if (!cb_recover_silent.isChecked && !cb_recover_vibrate.isChecked) {
                    cb_recover_sound.isChecked = true
                }
            }

        }
        cb_recover_vibrate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                CoreUtils.simpleLog("VIBRATE")
                LocalUtils.saveRecoverMode(CommonK.Mode_vibrate)
                cb_recover_sound.isChecked = false
                cb_recover_silent.isChecked = false

            } else {
                if (!cb_recover_sound.isChecked && !cb_recover_silent.isChecked) {
                    cb_recover_vibrate.isChecked = true
                }
            }
        }
        App.recoverMode = LocalUtils.getRecoverMode()
        when (App.recoverMode) {
            CommonK.Mode_silent -> {
                cb_recover_silent.isChecked = true
            }
            CommonK.Mode_vibrate -> {
                cb_recover_vibrate.isChecked = true
            }
            CommonK.Mode_sound -> {
                cb_recover_sound.isChecked = true
            }
            else -> {
                cb_recover_silent.isChecked = true
            }
        }
    }

    override fun initListener() {
        super.initListener()
        cb_notification_permissions.setOnClickListener(this)
        cb_do_not_disturb_permissions.setOnClickListener(this)
        iv_media_photo_continue_float.setOnClickListener(this)

        tv_explain_do_not_disturb.setOnClickListener(this)
        tv_explain_close_notification.setOnClickListener(this)

        tv_explain_media_audio.setOnClickListener(this)
        tv_explain_media_photo.setOnClickListener(this)
        tv_explain_photo_continue.setOnClickListener(this)
        tv_explain_media_video.setOnClickListener(this)

        tv_explain_remind_none.setOnClickListener(this)
        tv_explain_remind_both.setOnClickListener(this)
        tv_explain_remind_start.setOnClickListener(this)
        tv_explain_remind_stop.setOnClickListener(this)

        tv_explain_screen_off.setOnClickListener(this)
        tv_explain_screen_on.setOnClickListener(this)
        tv_explain_home_key.setOnClickListener(this)
        tv_explain_volume_key.setOnClickListener(this)
        tv_explain_recent_key.setOnClickListener(this)

        tv_explain_task_capture_single.setOnClickListener(this)
        tv_explain_task_capture_hourly.setOnClickListener(this)
        tv_explain_task_capture_daily.setOnClickListener(this)


        tv_explain_recover_silent.setOnClickListener(this)
        tv_explain_recover_vibrate.setOnClickListener(this)
        tv_explain_recover_sound.setOnClickListener(this)

        tv_secret_param.setOnClickListener(this)
        tv_capture_way.setOnClickListener(this)
        tv_capture_type.setOnClickListener(this)
        tv_camera_param.setOnClickListener(this)
        tv_remind_type.setOnClickListener(this)
        tv_timer_type.setOnClickListener(this)
        tv_idle_type.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_secret_param -> {
                ParticleSystem(this@CaptureSetAct, 10, R.drawable.star_white, 3000)
                    .setSpeedByComponentsRange(-0.1f, 0.1f, -0.1f, 0.02f)
                    .setAcceleration(0.000003f, 90)
                    .setInitialRotationRange(0, 360)
                    .setRotationSpeed(120f)
                    .setFadeOut(2000)
                    .addModifier(ScaleModifier(0f, 1.5f, 0, 1500))
                    .oneShot(lay_secret_param, 20)
            }

            tv_capture_way -> {
                ParticleSystem(this@CaptureSetAct, 10, R.drawable.star_white, 3000)
                    .setSpeedByComponentsRange(-0.1f, 0.1f, -0.1f, 0.02f)
                    .setAcceleration(0.000003f, 90)
                    .setInitialRotationRange(0, 360)
                    .setRotationSpeed(120f)
                    .setFadeOut(2000)
                    .addModifier(ScaleModifier(0f, 1.5f, 0, 1500))
                    .oneShot(lay_capture_way, 20)
            }

            tv_capture_type -> {
                ParticleSystem(this@CaptureSetAct, 10, R.drawable.star_white, 3000)
                    .setSpeedByComponentsRange(-0.1f, 0.1f, -0.1f, 0.02f)
                    .setAcceleration(0.000003f, 90)
                    .setInitialRotationRange(0, 360)
                    .setRotationSpeed(120f)
                    .setFadeOut(2000)
                    .addModifier(ScaleModifier(0f, 1.5f, 0, 1500))
                    .oneShot(lay_capture_type, 20)
            }

            tv_camera_param -> {
                ParticleSystem(this@CaptureSetAct, 10, R.drawable.star_white, 3000)
                    .setSpeedByComponentsRange(-0.1f, 0.1f, -0.1f, 0.02f)
                    .setAcceleration(0.000003f, 90)
                    .setInitialRotationRange(0, 360)
                    .setRotationSpeed(120f)
                    .setFadeOut(2000)
                    .addModifier(ScaleModifier(0f, 1.5f, 0, 1500))
                    .oneShot(lay_camera_param, 20)
            }

            tv_remind_type -> {
                ParticleSystem(this@CaptureSetAct, 10, R.drawable.star_white, 3000)
                    .setSpeedByComponentsRange(-0.1f, 0.1f, -0.1f, 0.02f)
                    .setAcceleration(0.000003f, 90)
                    .setInitialRotationRange(0, 360)
                    .setRotationSpeed(120f)
                    .setFadeOut(2000)
                    .addModifier(ScaleModifier(0f, 1.5f, 0, 1500))
                    .oneShot(lay_remind_type, 20)
            }

            tv_timer_type -> {
                ParticleSystem(this@CaptureSetAct, 10, R.drawable.star_white, 3000)
                    .setSpeedByComponentsRange(-0.1f, 0.1f, -0.1f, 0.02f)
                    .setAcceleration(0.000003f, 90)
                    .setInitialRotationRange(0, 360)
                    .setRotationSpeed(120f)
                    .setFadeOut(2000)
                    .addModifier(ScaleModifier(0f, 1.5f, 0, 1500))
                    .oneShot(lay_timer_type, 20)
            }

            tv_idle_type -> {
                ParticleSystem(this@CaptureSetAct, 10, R.drawable.star_white, 3000)
                    .setSpeedByComponentsRange(-0.1f, 0.1f, -0.1f, 0.02f)
                    .setAcceleration(0.000003f, 90)
                    .setInitialRotationRange(0, 360)
                    .setRotationSpeed(120f)
                    .setFadeOut(2000)
                    .addModifier(ScaleModifier(0f, 1.5f, 0, 1500))
                    .oneShot(lay_idle_type, 20)
            }

            cb_notification_permissions -> {
                closeAppNotificationPermissions()
            }
            cb_do_not_disturb_permissions -> {
                enableDoNotDisturbPermissions()
            }

            tv_explain_do_not_disturb -> {
                val mMessage = getString(R.string.explain_silent_shooting)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }

            tv_explain_close_notification -> {
                val mMessage = getString(R.string.explain_close_notification)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }

            tv_explain_media_audio -> {
                val mMessage = getString(R.string.explain_media_audio)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }

            tv_explain_media_photo -> {
                val mMessage = getString(R.string.explain_media_photo)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }

            tv_explain_photo_continue -> {
                val mMessage = getString(R.string.explain_photo_continue)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }

            tv_explain_media_video -> {
                val mMessage = getString(R.string.explain_media_video)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }

            tv_explain_remind_both -> {
                val mMessage = getString(R.string.explain_remind_both)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }

            tv_explain_remind_none -> {
                val mMessage = getString(R.string.explain_remind_none)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }

            tv_explain_remind_start -> {
                val mMessage = getString(R.string.explain_remind_start)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }

            tv_explain_remind_stop -> {
                val mMessage = getString(R.string.explain_remind_stop)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }

            tv_explain_screen_on -> {
                val mMessage = getString(R.string.explain_screen_on)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }

            tv_explain_screen_off -> {
                val mMessage = getString(R.string.explain_screen_off)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }

            tv_explain_home_key -> {
                val mMessage = getString(R.string.explain_home_key)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }

            tv_explain_recent_key -> {
                val mMessage = getString(R.string.explain_recent_key)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }

            tv_explain_volume_key -> {
                val mMessage = getString(R.string.explain_volume_key)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }

            tv_explain_task_capture_single -> {
                val mMessage = getString(R.string.explain_capture_task_capture_single)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }

            tv_explain_task_capture_hourly -> {
                val mMessage = getString(R.string.explain_capture_task_capture_hourly)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }

            tv_explain_task_capture_daily -> {
                val mMessage = getString(R.string.explain_capture_task_capture_daily)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }

            tv_explain_recover_silent -> {
                val mMessage = getString(R.string.explain_recover_silent)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }

            tv_explain_recover_vibrate -> {
                val mMessage = getString(R.string.explain_recover_vibrate)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }

            tv_explain_recover_sound -> {
                val mMessage = getString(R.string.explain_recover_sound)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            }
        }
    }

    private fun checkRecommendPermission() {
        if (!NotificationUtils.areNotificationsEnabled() && CoreUtils.checkDoNotDisturb()) {
            lay_recommend.visibility = View.GONE
        } else {
            lay_recommend.visibility = View.VISIBLE
        }
        checkNotificationPermissions()
        checkDoNotDisturbPermissions()
    }

    private fun checkNotificationPermissions() {
        if (!NotificationUtils.areNotificationsEnabled()) {
            lay_notification_permissions.visibility = View.GONE
            cb_notification_permissions.isChecked = true
            cb_notification_permissions.isEnabled = false
        } else {
            lay_notification_permissions.visibility = View.VISIBLE
            cb_notification_permissions.isChecked = false
            cb_notification_permissions.isEnabled = true
        }

    }

    private fun checkDoNotDisturbPermissions() {
        if (CoreUtils.checkDoNotDisturb()) {
            lay_do_not_disturb_permissions.visibility = View.GONE
            cb_do_not_disturb_permissions.isChecked = true
            cb_do_not_disturb_permissions.isEnabled = false
            lay_remind.visibility = View.VISIBLE
            lay_ring_recover.visibility = View.VISIBLE
        } else {
            lay_do_not_disturb_permissions.visibility = View.VISIBLE
            cb_do_not_disturb_permissions.isChecked = false
            cb_do_not_disturb_permissions.isEnabled = true
            lay_remind.visibility = View.GONE
            lay_ring_recover.visibility = View.GONE

        }

    }


    private fun closeAppNotificationPermissions() {
        CoreUtils.startCommonService(CheckNotificationsSer::class.java)
        if (NotificationUtils.areNotificationsEnabled()) {
            CoreUtils.goAppNotificationSetting(this)
        }

    }


    private fun enableDoNotDisturbPermissions() {
        if (!CoreUtils.checkDoNotDisturb()) {
            requestDoNotDisturbPermission()
        }

    }


    //时间分发方法重写
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        //如果是点击事件，获取点击的view，并判断是否要收起键盘
        if (ev.action == MotionEvent.ACTION_DOWN) {
            //获取目前得到焦点的view
            val v = currentFocus
            //判断是否要收起并进行处理
            if (v != null && isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v.windowToken)
            }
        }
        //这个是activity的事件分发，一定要有，不然就不会有任何的点击事件了
        return super.dispatchTouchEvent(ev)
    }

    //判断是否要收起键盘
    private fun isShouldHideKeyboard(
        v: View,
        event: MotionEvent
    ): Boolean {
        //如果目前得到焦点的这个view是editText的话进行判断点击的位置
        if (v is EditText) {
            val l = intArrayOf(0, 0)
            v.getLocationInWindow(l)
            val left = l[0]
            val top = l[1]
            val bottom = top + v.getHeight()
            val right = left + v.getWidth()
            // 点击EditText的事件，忽略它。
            return (event.x <= left || event.x >= right
                    || event.y <= top || event.y >= bottom)
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上
        return false
    }

    //隐藏软键盘并让editText失去焦点
    private fun hideKeyboard(token: IBinder?) {
        et_photo_count.clearFocus()
        et_photo_interval.clearFocus()
        et_video_max_dur.clearFocus()
        et_audio_max_dur.clearFocus()
        et_task_hour_capture_single.clearFocus()
        et_task_min_capture_single.clearFocus()

        et_task_hour_capture_hourly.clearFocus()
        et_task_min_capture_hourly.clearFocus()

        et_task_hour_capture_daily.clearFocus()
        et_task_min_capture_daily.clearFocus()
        if (token != null) {
            //这里先获取InputMethodManager再调用他的方法来关闭软键盘
            //InputMethodManager就是一个管理窗口输入的manager
            val im: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    private fun requestDoNotDisturbPermission() {
        try {
            CoreUtils.startCommonService(CheckDisturbSer::class.java)
            val intent = Intent(
                Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
            )
            startActivity(intent)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    private fun alarmSingle() {
        val calendar = Calendar.getInstance()
        val nowHour = calendar.get(Calendar.HOUR_OF_DAY)
        val nowMin = calendar.get(Calendar.MINUTE)
        val mHour = LocalUtils.getTaskSingleHour().toInt()
        val mMin = LocalUtils.getTaskSingleMin().toInt()

        val tempCalendar = Calendar.getInstance()
        tempCalendar.add(Calendar.HOUR_OF_DAY, mHour - nowHour)
        tempCalendar.add(Calendar.MINUTE, mMin - nowMin)

        if (LocalUtils.getTaskCapture() == CommonK.Task_single) {
            if (tempCalendar.timeInMillis > calendar.timeInMillis) {
                AlarmUtils.setAlarm(
                    this,
                    requestCaptureSingle,
                    ARec::class.java,
                    ActionK.ACTION_ALARM_SINGLE,
                    tempCalendar.timeInMillis,
                )
            } else {
                AlarmUtils.setAlarm(
                    this,
                    requestCaptureSingle,
                    ARec::class.java,
                    ActionK.ACTION_ALARM_SINGLE,
                    tempCalendar.timeInMillis + 24 * 60 * 60 * 1000L,
                )
            }
        } else {
            AlarmUtils.cancelAlarm(
                this@CaptureSetAct,
                requestCaptureSingle,
                ARec::class.java,
                ActionK.ACTION_ALARM_SINGLE_CANCEL
            )
        }
    }

    private fun alarmHourly() {
        val calendar = Calendar.getInstance()
        // val nowHour = calendar.get(Calendar.HOUR_OF_DAY)
        val nowMin = calendar.get(Calendar.MINUTE)
        // val mHour = LocalUtils.getTaskHourlyHour().toInt()
        val mMin = LocalUtils.getTaskHourlyMin().toInt()

        val tempCalendar = Calendar.getInstance()
        // tempCalendar.add(Calendar.HOUR_OF_DAY, mHour - nowHour)
        tempCalendar.add(Calendar.MINUTE, mMin - nowMin)

        if (LocalUtils.getTaskCapture() == CommonK.Task_hourly) {
            if (tempCalendar.timeInMillis > calendar.timeInMillis) {
                AlarmUtils.setAlarm(
                    this,
                    requestCaptureHourly,
                    ARec::class.java,
                    ActionK.ACTION_ALARM_HOURLY,
                    tempCalendar.timeInMillis,
                )
            } else {
                AlarmUtils.setAlarm(
                    this,
                    requestCaptureHourly,
                    ARec::class.java,
                    ActionK.ACTION_ALARM_HOURLY,
                    tempCalendar.timeInMillis + 1 * 60 * 60 * 1000L,
                )
            }

        } else {
            AlarmUtils.cancelAlarm(
                this@CaptureSetAct,
                requestCaptureHourly,
                ARec::class.java,
                ActionK.ACTION_ALARM_HOURLY_CANCEL
            )
        }
    }

    private fun alarmDaily() {
        val calendar = Calendar.getInstance()
        val nowHour = calendar.get(Calendar.HOUR_OF_DAY)
        val nowMin = calendar.get(Calendar.MINUTE)
        val mHour = LocalUtils.getTaskDailyHour().toInt()
        val mMin = LocalUtils.getTaskDailyMin().toInt()

        val tempCalendar = Calendar.getInstance()
        tempCalendar.add(Calendar.HOUR_OF_DAY, mHour - nowHour)
        tempCalendar.add(Calendar.MINUTE, mMin - nowMin)

        if (LocalUtils.getTaskCapture() == CommonK.Task_daily) {
            if (tempCalendar.timeInMillis > calendar.timeInMillis) {
                AlarmUtils.setAlarm(
                    this,
                    requestCaptureDaily,
                    ARec::class.java,
                    ActionK.ACTION_ALARM_DAILY,
                    tempCalendar.timeInMillis
                )
            } else {
                AlarmUtils.setAlarm(
                    this,
                    requestCaptureDaily,
                    ARec::class.java,
                    ActionK.ACTION_ALARM_DAILY,
                    tempCalendar.timeInMillis + 24 * 60 * 60 * 1000L,
                )
            }

        } else {
            AlarmUtils.cancelAlarm(
                this@CaptureSetAct,
                requestCaptureDaily,
                ARec::class.java,
                ActionK.ACTION_ALARM_DAILY_CANCEL
            )
        }
    }

    private fun actTran() {
        window.enterTransition =
            TransitionInflater.from(this).inflateTransition(R.transition.common_tran_in)
        window.exitTransition =
            TransitionInflater.from(this).inflateTransition(R.transition.common_tran_in)
        window.returnTransition =
            TransitionInflater.from(this).inflateTransition(R.transition.common_tran_out)
        window.reenterTransition =
            TransitionInflater.from(this).inflateTransition(R.transition.common_tran_in)

        window.enterTransition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition?) {
                iv_title_left?.animate()?.rotation(180F)
            }

            override fun onTransitionEnd(transition: Transition?) {
                iv_title_left?.animate()?.rotation(0F)

            }

            override fun onTransitionCancel(transition: Transition?) {

            }

            override fun onTransitionPause(transition: Transition?) {
            }

            override fun onTransitionResume(transition: Transition?) {
            }

        })

        window.reenterTransition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition?) {
                iv_title_left?.animate()?.rotation(180F)
            }

            override fun onTransitionEnd(transition: Transition?) {
                iv_title_left?.animate()?.rotation(0F)

            }

            override fun onTransitionCancel(transition: Transition?) {

            }

            override fun onTransitionPause(transition: Transition?) {
            }

            override fun onTransitionResume(transition: Transition?) {
            }

        })

        window.returnTransition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition?) {
                iv_title_left?.animate()?.rotation(180F)
            }

            override fun onTransitionEnd(transition: Transition?) {
                // iv_title_left?.animate()?.rotation(0F)

            }

            override fun onTransitionCancel(transition: Transition?) {

            }

            override fun onTransitionPause(transition: Transition?) {
            }

            override fun onTransitionResume(transition: Transition?) {
            }

        })
    }

    override fun onResume() {
        super.onResume()
        LogUtils.i("onResume")
        checkRecommendPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtils.i("onDestroy")
        materialDialog?.cancel()
        permissionsDialog?.cancel()
        CoreUtils.stopCommonService(CheckNotificationsSer::class.java)
        CoreUtils.stopCommonService(CheckDisturbSer::class.java)
    }


    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && !isFinishing) {
            Instrumentation().callActivityOnSaveInstanceState(this, Bundle())
        }
    }

}

