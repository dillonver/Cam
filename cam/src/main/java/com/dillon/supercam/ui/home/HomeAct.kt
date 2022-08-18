package com.dillon.supercam.ui.home


import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.Instrumentation
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.blankj.utilcode.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dillon.dialogs.MaterialDialog
import com.dillon.dialogs.checkbox.checkBoxPrompt
import com.dillon.recyclerview.OnItemClickListener
import com.dillon.recyclerview.SwipeRecyclerView
import com.dillon.supercam.R
import com.dillon.supercam.WelcomeAct
import com.dillon.supercam.WelcomeActFake
import com.dillon.supercam.WelcomeActShortCut
import com.dillon.supercam.base.App
import com.dillon.supercam.base.App.Companion.configInfo
import com.dillon.supercam.base.App.Companion.needRefresh
import com.dillon.supercam.base.BAct
import com.dillon.supercam.bean.*
import com.dillon.supercam.key.ActionK
import com.dillon.supercam.key.CacheK
import com.dillon.supercam.key.CommonK
import com.dillon.supercam.key.ParamsK
import com.dillon.supercam.services.task.CaptureAudioSer
import com.dillon.supercam.services.task.CapturePhotoIntervalSer
import com.dillon.supercam.services.task.CaptureVideoSer
import com.dillon.supercam.ui.setting.AppInstructionsAct
import com.dillon.supercam.ui.setting.CaptureSetAct
import com.dillon.supercam.ui.setting.PasswordAct
import com.dillon.supercam.ui.setting.SettingsAct
import com.dillon.supercam.utils.CoreUtils
import com.dillon.supercam.utils.LocalUtils
import com.dillon.supercam.utils.view.camera.CameraUtil
import com.dillon.supercam.utils.view.leonids.ParticleSystem
import com.dillon.supercam.utils.view.leonids.modifiers.ScaleModifier
import com.dillon.supercam.utils.view.shimmer.Shimmer
import com.dillon.video.Jzvd
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.layout_audio_playing_home.*
import kotlinx.android.synthetic.main.layout_title_home.*


@SuppressLint("SetTextI18n")
class HomeAct : BAct(), OnItemClickListener, HomeAdapter.ItemManagerListener,
    SwipeRecyclerView.LoadMoreListener, SwipeRefreshLayout.OnRefreshListener {
    private var materialDialog: MaterialDialog? = null

    private var shimmerPhoto: Shimmer? = null
    private var shimmerVideo: Shimmer? = null
    private var shimmerVoice: Shimmer? = null
    private var selectType: String = CommonK.Type_Media_Photo
    private var mAdapter: HomeAdapter? = null
    private var mediaInfoListByPage: MutableList<MediaInfo>? = null
    private var mediaInfoListBySelectType: MutableList<MediaInfo>? = null

    private var pageNum = 1//当前页数
    private var pageSize = 10//分页数量如何


    override fun initUi() {
        super.initUi()
        setContentView(R.layout.activity_home)
        actTran()

    }


    override fun initView() {
        super.initView()
        checkFirstIn()
        val intentFilter = IntentFilter()
        intentFilter.addAction(ActionK.ACTION_CAPTURE_HAD_START)
        intentFilter.addAction(ActionK.ACTION_CAPTURE_HAD_STOP)
        registerReceiver(myBroadcastReceive, intentFilter)
        LocalUtils.getLocalUser()
        if (null == App.localUserInfo) {
            val localUser = UserInfo()
            val deviceId = DeviceUtils.getUniqueDeviceId()
            val uID = if (deviceId.length > 6) {
                deviceId.substring(deviceId.length - 6, deviceId.length).trim()
            } else {
                deviceId
            }
            localUser.uID = uID
            LocalUtils.saveLocalUser(localUser)
            tv_title_left.text = localUser.uID
            CoreUtils.simpleLog("FIRST_START")
        } else {
            //
            if (App.localUserInfo!!.uID == App.localUserInfo!!.sID) {
                val deviceId = DeviceUtils.getUniqueDeviceId()
                val uID = if (deviceId.length > 6) {
                    deviceId.substring(deviceId.length - 6, deviceId.length).trim()
                } else {
                    deviceId
                }
                App.localUserInfo!!.uID = uID
                LocalUtils.saveLocalUser(App.localUserInfo!!)
            }

            tv_title_left.text = if (App.localUserInfo?.sID.isNullOrBlank()) {
                App.localUserInfo!!.uID
            } else {
                App.localUserInfo!!.sID
            }
        }


        sw_ac.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                App.acOpen = true
                ToastUtils.showShort(resources.getString(R.string.ac_open))
                SPUtils.getInstance().put(CacheK.acOpen, true)
                CoreUtils.simpleLog("SMART_OPEN")
            } else {
                App.acOpen = false
                SPUtils.getInstance().put(CacheK.acOpen, false)
                CameraUtil.stopCaptureService()
                CoreUtils.recoverMode()
                CoreUtils.simpleLog("SMART_CLOSE")

            }
        }
        tv_select_photo.textSize = 15f
        tv_select_video.textSize = 14f
        tv_select_audio.textSize = 14f
        tv_select_photo.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        tv_select_video.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        tv_select_audio.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)

        shimmerPhoto = Shimmer()
        shimmerPhoto?.setDuration(3000)
        shimmerPhoto?.start(tv_select_photo)
        shimmerVideo = Shimmer()
        shimmerVideo?.setDuration(3000)
        shimmerVoice = Shimmer()
        shimmerVoice?.setDuration(3000)


        val pool = RecyclerView.RecycledViewPool()
        pool.setMaxRecycledViews(0, 10)
        recyclerView.setRecycledViewPool(pool)
        recyclerView.itemAnimator?.changeDuration = 0
        (recyclerView.itemAnimator as SimpleItemAnimator?)!!.supportsChangeAnimations = false

        recyclerView.setLoadMoreListener(this)
        refreshLayout.setOnRefreshListener(this)
        recyclerView.setOnItemClickListener(this)
        recyclerView?.layoutManager = GridLayoutManager(this, 1)
        mAdapter = HomeAdapter(this, this)
        recyclerView?.adapter = mAdapter

        setAppBg()
    }

    private fun checkFirstIn() {
        val firstIn = SPUtils.getInstance().getBoolean(CacheK.firstIn, false)
        if (!firstIn) {
            SPUtils.getInstance().put(CacheK.firstIn, true)
            val title = getString(R.string.how_to_use)
            val mMessage =
                if (configInfo?.appDes.isNullOrBlank()) {
                    resources.getString(R.string.app_des)
                } else {
                    configInfo?.appDes
                }
            materialDialog?.cancel()
            materialDialog = MaterialDialog(this)
                .title(text = title)
                .icon(R.drawable.ic_logo)
                .message(text = mMessage)
                .cornerRadius(res = R.dimen.dp_20)
                .positiveButton(null, getString(R.string.check)) {
                    goInstructionAct()
                }
            materialDialog?.setOnDismissListener { needRefresh = false }
            if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                    this
                )
            ) {
                materialDialog?.show()
            }
        }
    }

    override fun initData() {
        super.initData()
        ActivityUtils.finishActivity(WelcomeAct::class.java)
        ActivityUtils.finishActivity(WelcomeActFake::class.java)
        ActivityUtils.finishActivity(WelcomeActShortCut::class.java)
        ActivityUtils.finishActivity(PasswordAct::class.java)
    }


    @SuppressLint("QueryPermissionsNeeded")
    override fun initListener() {
        super.initListener()
        lay_iv_title_right.setOnClickListener {
            goSettingAct()
        }
        lay_title_left.setOnClickListener {
            goCaptureSetAct()
        }
        lay_title_left.setOnLongClickListener {
            val mTitle = AppUtils.getAppName() + "  v" + AppUtils.getAppVersionName()
            val mMessage = getString(R.string.your_id) + LocalUtils.getLocalUser()?.uID +
                    "     " + getString(R.string.your_exp) + LocalUtils.getProExp()

            materialDialog?.cancel()
            materialDialog = MaterialDialog(this)
                .icon(R.drawable.ic_logo)
                .title(text = mTitle)
                .message(text = mMessage)
                .cornerRadius(res = R.dimen.dp_20)
            materialDialog?.setOnDismissListener { needRefresh = false }
            if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                    this
                )
            ) {
                materialDialog?.show()
            }
            true
        }

        lay_iv_recording.setOnClickListener {
            when {
                App.isCaptureContinuing -> {
                    CoreUtils.stopCommonService(CapturePhotoIntervalSer::class.java)
                }
                App.capturingVideo -> {
                    CoreUtils.stopCommonService(CaptureVideoSer::class.java)
                }
                App.capturingAudio -> {
                    CoreUtils.stopCommonService(CaptureAudioSer::class.java)
                }
                else -> {
                    LogUtils.i("Not recording")
                }
            }
        }
        tv_select_photo.setOnClickListener(object : ClickUtils.OnMultiClickListener(2) {
            override fun onTriggerClick(v: View) {
                recyclerView.smoothScrollToPosition(0)
            }

            override fun onBeforeTriggerClick(v: View, count: Int) {
                ParticleSystem(this@HomeAct, 10, R.drawable.star_white, 3000)
                    .setSpeedByComponentsRange(-0.1f, 0.1f, -0.1f, 0.02f)
                    .setAcceleration(0.000003f, 90)
                    .setInitialRotationRange(0, 360)
                    .setRotationSpeed(120f)
                    .setFadeOut(2000)
                    .addModifier(ScaleModifier(0f, 1.5f, 0, 1500))
                    .oneShot(tv_select_photo, 20)
                if (CommonK.Type_Media_Video == selectType || CommonK.Type_Media_Audio == selectType) {
                    tv_select_photo.textSize = 15f
                    tv_select_video.textSize = 14f
                    tv_select_audio.textSize = 14f

                    tv_select_photo.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    tv_select_video.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    tv_select_audio.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    selectType = CommonK.Type_Media_Photo
                    shimmerPhoto?.start(tv_select_photo)
                    shimmerVideo?.cancel()
                    shimmerVoice?.cancel()
                    refreshData(CommonK.Type_Media_Photo)
                }

            }
        })
        tv_select_photo.setOnLongClickListener {
            val photoCount = FileUtils.listFilesInDir(App.savePhotoDir).size
            val title = getString(R.string.delete_tip)
            val mMessage =
                getString(R.string.current_photo_p1) + " " + photoCount + "  " + getString(R.string.size) + FileUtils.getSize(
                    App.savePhotoDir
                ) + getString(
                    R.string.current_photo_p2
                )
            val checkBoxStr = getString(R.string.only_un_tap)
            var withoutLove = false
            materialDialog?.cancel()
            materialDialog = MaterialDialog(this)
                .title(text = title)
                .message(text = mMessage)
                .cornerRadius(res = R.dimen.dp_20)
                .checkBoxPrompt(text = checkBoxStr) { checked ->
                    run {
                        withoutLove = checked
                    }
                }
                .negativeButton(null, getString(R.string.delete)) {
                    if (photoCount > 0) {
                        if (withoutLove) {
                            deleteMediaFileData(CommonK.Type_Media_Photo, true)
                        } else {
                            deleteMediaFileData(CommonK.Type_Media_Photo, false)
                        }

                    } else {
                        ToastUtils.showShort(getString(R.string.unnecessary_delete))
                    }

                }
                .positiveButton(null, getString(R.string.cancel)) {}
            materialDialog?.setOnDismissListener { needRefresh = false }
            if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                    this
                )
            ) {
                materialDialog?.show()
            }
            true
        }

        tv_select_video.setOnClickListener(object : ClickUtils.OnMultiClickListener(2) {
            override fun onTriggerClick(v: View) {
                recyclerView.smoothScrollToPosition(0)
            }

            override fun onBeforeTriggerClick(v: View, count: Int) {
                ParticleSystem(this@HomeAct, 10, R.drawable.star_white, 3000)
                    .setSpeedByComponentsRange(-0.1f, 0.1f, -0.1f, 0.02f)
                    .setAcceleration(0.000003f, 90)
                    .setInitialRotationRange(0, 360)
                    .setRotationSpeed(120f)
                    .setFadeOut(2000)
                    .addModifier(ScaleModifier(0f, 1.5f, 0, 1500))
                    .oneShot(tv_select_video, 20)
                if (CommonK.Type_Media_Photo == selectType || CommonK.Type_Media_Audio == selectType) {
                    tv_select_photo.textSize = 14f
                    tv_select_video.textSize = 15f
                    tv_select_audio.textSize = 14f

                    tv_select_photo.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    tv_select_audio.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    tv_select_video.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    selectType = CommonK.Type_Media_Video
                    shimmerVideo?.start(tv_select_video)
                    shimmerPhoto?.cancel()
                    shimmerVoice?.cancel()
                    refreshData(CommonK.Type_Media_Video)
                }
            }
        })

        tv_select_video.setOnLongClickListener {

            val videoCount = FileUtils.listFilesInDir(App.saveVideoDir).size

            val title = getString(R.string.delete_tip)
            val mMessage =
                getString(R.string.current_video_p1) + " " + videoCount + "  " + getString(R.string.size) + FileUtils.getSize(
                    App.saveVideoDir
                ) + getString(
                    R.string.current_video_p2
                )
            val checkBoxStr = getString(R.string.only_un_tap)
            var withoutLove = false
            materialDialog?.cancel()
            materialDialog = MaterialDialog(this)
                .title(text = title)
                .message(text = mMessage)
                .cornerRadius(res = R.dimen.dp_20)
                .checkBoxPrompt(text = checkBoxStr) { checked ->
                    run {
                        withoutLove = checked
                    }
                }
                .negativeButton(null, getString(R.string.delete)) {
                    if (videoCount > 0) {
                        if (withoutLove) {
                            deleteMediaFileData(CommonK.Type_Media_Video, true)
                        } else {
                            deleteMediaFileData(CommonK.Type_Media_Video, false)
                        }


                    } else {
                        ToastUtils.showShort(getString(R.string.unnecessary_delete))
                    }
                }
                .positiveButton(null, getString(R.string.cancel)) {}
            materialDialog?.setOnDismissListener { needRefresh = false }
            if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                    this
                )
            ) {
                materialDialog?.show()
            }


            true
        }

        tv_select_audio.setOnClickListener(object : ClickUtils.OnMultiClickListener(2) {
            override fun onTriggerClick(v: View) {
                recyclerView.smoothScrollToPosition(0)
            }

            override fun onBeforeTriggerClick(v: View, count: Int) {
                ParticleSystem(this@HomeAct, 10, R.drawable.star_white, 3000)
                    .setSpeedByComponentsRange(-0.1f, 0.1f, -0.1f, 0.02f)
                    .setAcceleration(0.000003f, 90)
                    .setInitialRotationRange(0, 360)
                    .setRotationSpeed(120f)
                    .setFadeOut(2000)
                    .addModifier(ScaleModifier(0f, 1.5f, 0, 1500))
                    .oneShot(tv_select_audio, 20)
                if (CommonK.Type_Media_Video == selectType || CommonK.Type_Media_Photo == selectType) {
                    tv_select_photo.textSize = 14f
                    tv_select_video.textSize = 14f
                    tv_select_audio.textSize = 15f
                    tv_select_photo.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    tv_select_video.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    tv_select_audio.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    selectType = CommonK.Type_Media_Audio
                    shimmerVoice?.start(tv_select_audio)
                    shimmerPhoto?.cancel()
                    shimmerVideo?.cancel()
                    refreshData(CommonK.Type_Media_Audio)
                }
            }
        })

        tv_select_audio.setOnLongClickListener {
            val audioCount = FileUtils.listFilesInDir(App.saveAudioDir).size
            val title = getString(R.string.delete_tip)
            val mMessage =
                getString(R.string.current_audio_p1) + " " + audioCount + "  " + getString(R.string.size) + FileUtils.getSize(
                    App.saveAudioDir
                ) + getString(
                    R.string.current_audio_p2
                )
            val checkBoxStr = getString(R.string.only_un_tap)
            var withoutLove = false
            materialDialog?.cancel()
            materialDialog = MaterialDialog(this)
                .title(text = title)
                .message(text = mMessage)
                .cornerRadius(res = R.dimen.dp_20)
                .checkBoxPrompt(text = checkBoxStr) { checked ->
                    run {
                        withoutLove = checked
                    }
                }
                .negativeButton(null, getString(R.string.delete)) {
                    if (audioCount > 0) {
                        if (withoutLove) {
                            deleteMediaFileData(CommonK.Type_Media_Audio, true)
                        } else {
                            deleteMediaFileData(CommonK.Type_Media_Audio, false)
                        }

                    } else {
                        ToastUtils.showShort(getString(R.string.unnecessary_delete))

                    }
                }
                .positiveButton(null, getString(R.string.cancel)) {}
            materialDialog?.setOnDismissListener { needRefresh = false }
            if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                    this
                )
            ) {
                materialDialog?.show()
            }

            true
        }

    }


    private fun refreshData(selectType: String) {
        pageNum = 1
        refreshLayout.isRefreshing = false
        mediaInfoListBySelectType = CoreUtils.getMediaInfoListByType(selectType)
        //第一页数据
        mediaInfoListByPage =
            CoreUtils.getMediaInfoListByPage(mediaInfoListBySelectType, pageSize, pageNum)
        if (mediaInfoListByPage.isNullOrEmpty()) {
            tv_empty.visibility = View.VISIBLE
            recyclerView.loadMoreFinish(true, false)
        } else {
            tv_empty.visibility = View.INVISIBLE
            recyclerView.loadMoreFinish(false, true)
        }
        recyclerView.postDelayed({
            mAdapter?.notifyDataSetChanged(mediaInfoListByPage)
        }, 50)

    }


    override fun onPause() {
        super.onPause()
        LogUtils.i("onPause")
        App.checkMsgLatestPause = true
        if (materialDialog != null && materialDialog!!.isShowing) {
            materialDialog?.cancel()
        }

    }

    override fun onResume() {
        super.onResume()
        LogUtils.i("onResume")
        sw_ac.isChecked = App.acOpen
        App.checkMsgLatestPause = false
        getNetData()
        if (needRefresh) {
            refreshData(selectType)
        } else {
            needRefresh = true
        }
        if (App.capturingVideo || App.capturingAudio || App.isCaptureContinuing) {
            lay_iv_recording.visibility = View.VISIBLE
        } else {
            lay_iv_recording.visibility = View.INVISIBLE
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        materialDialog?.cancel()

        unregisterReceiver(myBroadcastReceive)
    }


    override fun onLoadMore() {
        pageNum++
        val tempMediaInfoList =
            CoreUtils.getMediaInfoListByPage(mediaInfoListBySelectType, pageSize, pageNum)
        if (tempMediaInfoList.isNullOrEmpty()) {
            recyclerView!!.loadMoreFinish(true, false)
            LogUtils.i("noMore", "page$pageNum")
        } else {
            recyclerView.postDelayed({
                mediaInfoListByPage?.addAll(tempMediaInfoList)
                mAdapter?.notifyItemRangeInserted(
                    mediaInfoListByPage!!.size - tempMediaInfoList.size,
                    tempMediaInfoList.size
                )
            }, 500) //  延时运行

            val listSize = tempMediaInfoList.size
            if (listSize < pageSize) {
                //一页加载完
                recyclerView.loadMoreFinish(true, false)
                LogUtils.i("In one page", "Page$pageNum", "Size$listSize")
            } else {
                //可能有下一页
                recyclerView.loadMoreFinish(false, true)
                LogUtils.i("More than one page", "Page$pageNum", "Size$listSize")
            }
            ToastUtils.showShort(getString(R.string.loading_more))
        }
    }

    override fun onRefresh() {
        refreshData(selectType)
    }


    private var myBroadcastReceive: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                ActionK.ACTION_CAPTURE_HAD_START -> {
                    lay_iv_recording.visibility = View.VISIBLE
                }
                ActionK.ACTION_CAPTURE_HAD_STOP -> {
                    lay_iv_recording.visibility = View.INVISIBLE
                    refreshData(selectType)
                }
            }
        }
    }

    override fun show(mediaInfo: MediaInfo, position: Int) {
        App.tempMediaInfo = mediaInfo
        if (mediaInfo.type == CommonK.Type_Media_Audio) {
            if (ActivityUtils.isActivityAlive(this)) {
                val playingDialog = MaterialDialog(this)
                playingDialog.setContentView(R.layout.layout_audio_playing_home)
                playingDialog.cornerRadius(res = R.dimen.dp_20)
                playingDialog.mVideo?.setUp(mediaInfo.path, mediaInfo.title)
                playingDialog.mVideo?.startVideoAfterPreloading()
                playingDialog.setOnDismissListener {
                    Jzvd.releaseAllVideos()
                }
                if (!playingDialog.isShowing) {
                    playingDialog.show()
                }
            }
        } else {
            startActivity(
                Intent(this, MediaShowAct::class.java).putExtra(ParamsK.intentTemp, mediaInfo),
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
            )
        }

    }

    override fun love(mediaInfo: MediaInfo, position: Int) {
        val fileName = mediaInfo.file!!.name
        val loved = SPUtils.getInstance().getBoolean(fileName, false)
        if (loved) {
            SPUtils.getInstance().put(fileName, false)
        } else {
            SPUtils.getInstance().put(fileName, true)
        }
        mAdapter?.notifyItemChanged(position)

    }

    override fun share(mediaInfo: MediaInfo, position: Int) {
        CoreUtils.shareMediaFile(this@HomeAct, mediaInfo)

    }

    override fun save(mediaInfo: MediaInfo, position: Int) {
        if (LocalUtils.getLocalUser() != null && LocalUtils.getLocalUser()!!.pro) {
            CoreUtils.saveMediaExternal(this@HomeAct, mediaInfo)
        } else {
            ToastUtils.showShort(getString(R.string.vip_feature_tip))
        }
    }

    override fun delete(mediaInfo: MediaInfo, position: Int) {

        val mTitle = getString(R.string.dialog_common_title)
        val mMessage = getString(R.string.delete) + " " + mediaInfo.title
        materialDialog?.cancel()
        materialDialog = MaterialDialog(this)
            //.icon(R.mipmap.ic_launcher)
            .title(text = mTitle)
            .message(text = mMessage)
            .cornerRadius(res = R.dimen.dp_20)
            .negativeButton(null, getString(R.string.delete)) {
                val flag = FileUtils.delete(mediaInfo.file)
                if (flag) {
                    SPUtils.getInstance().remove(mediaInfo.file!!.name)
                    recyclerView.postDelayed({
                        try {
                            LogUtils.i("position:$position")
                            mediaInfoListByPage?.removeAt(position)
                            mediaInfoListBySelectType?.removeAt(position)
                            mAdapter?.notifyItemRemoved(position)
                            mAdapter?.notifyItemRangeChanged(position, mediaInfoListByPage!!.size)
                            if (mediaInfoListByPage.isNullOrEmpty()) {
                                refreshData(selectType)
                            }
                        } catch (e: Exception) {
                            //防止删除未及时，又切换列表导致的崩溃
                            e.printStackTrace()
                        }

                    }, 500) //  延时运行

                } else {
                    ToastUtils.showShort(getString(R.string.failed))
                }

                if (mediaInfoListBySelectType != null && mediaInfoListBySelectType!!.isEmpty()) {
                    tv_empty.visibility = View.VISIBLE
                }


            }
            .positiveButton(null, getString(R.string.cancel)) {
            }
        materialDialog?.setOnDismissListener { needRefresh = false }
        if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                this
            )
        ) {
            materialDialog?.show()
        }

    }

    private fun goCaptureSetAct() {
        startActivity(
            Intent(this, CaptureSetAct::class.java),
            ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        )
    }

    private fun goSettingAct() {
        startActivity(
            Intent(this, SettingsAct::class.java),
            ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        )
    }

    private fun goInstructionAct() {
        startActivity(
            Intent(this, AppInstructionsAct::class.java),
            ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        )

    }

    private fun getNetData() {

    }

    private fun setAppBg() {
        if (!ActivityUtils.isActivityAlive(this)) return
        App.homeBgPath = LocalUtils.getPageBg(CommonK.PAGE_HOME)
        if (App.homeBgPath.isNullOrBlank()) {
            if (configInfo?.urlH.isNullOrBlank()) return
            Glide.with(this).load(configInfo!!.urlH).error(R.drawable.app_bg)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(act_bg)
        } else {
            Glide.with(this).load(App.homeBgPath)
                //.skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(act_bg)

        }
    }

    override fun onItemClick(view: View?, adapterPosition: Int) {
        ParticleSystem(this, 10, R.drawable.star_white, 3000)
            .setSpeedByComponentsRange(-0.1f, 0.1f, -0.1f, 0.02f)
            .setAcceleration(0.000003f, 90)
            .setInitialRotationRange(0, 360)
            .setRotationSpeed(120f)
            .setFadeOut(2000)
            .addModifier(ScaleModifier(0f, 1.5f, 0, 1500))
            .oneShot(view, 20)
    }


    private fun deleteMediaFileData(type: String, withoutLove: Boolean) {
        when (type) {
            CommonK.Type_Media_Photo -> {
                Thread {
                    val success = if (withoutLove) {
                        CoreUtils.deleteFilesInDirWithoutLove(App.savePhotoDir)
                    } else {
                        CoreUtils.deleteFilesInDir(App.savePhotoDir)
                    }
                    if (success) {
                        ToastUtils.showShort(Utils.getApp().getString(R.string.done))
                    } else {
                        ToastUtils.showShort(Utils.getApp().getString(R.string.failed))
                    }

                    ViewUtils.runOnUiThread {
                        if (CommonK.Type_Media_Video == selectType || CommonK.Type_Media_Audio == selectType) {
                            tv_select_photo.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                            tv_select_video.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                            tv_select_audio.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                            selectType = CommonK.Type_Media_Photo
                            shimmerPhoto?.start(tv_select_photo)
                            shimmerVideo?.cancel()
                            shimmerVoice?.cancel()
                        }
                        refreshData(CommonK.Type_Media_Photo)
                    }
                }.start()
            }
            CommonK.Type_Media_Video -> {
                Thread {
                    val success = if (withoutLove) {
                        CoreUtils.deleteFilesInDirWithoutLove(App.saveVideoDir)
                    } else {
                        CoreUtils.deleteFilesInDir(App.saveVideoDir)
                    }
                    if (success) {
                        ToastUtils.showShort(Utils.getApp().getString(R.string.done))
                    } else {
                        ToastUtils.showShort(Utils.getApp().getString(R.string.failed))
                    }
                    ViewUtils.runOnUiThread {
                        if (CommonK.Type_Media_Photo == selectType || CommonK.Type_Media_Audio == selectType) {
                            tv_select_photo.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                            tv_select_audio.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                            tv_select_video.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                            selectType = CommonK.Type_Media_Video
                            shimmerVideo?.start(tv_select_video)
                            shimmerPhoto?.cancel()
                            shimmerVoice?.cancel()
                        }
                        refreshData(CommonK.Type_Media_Video)
                    }
                }.start()
            }
            CommonK.Type_Media_Audio -> {
                Thread {
                    val success = if (withoutLove) {
                        CoreUtils.deleteFilesInDirWithoutLove(App.saveAudioDir)
                    } else {
                        CoreUtils.deleteFilesInDir(App.saveAudioDir)
                    }
                    if (success) {
                        ToastUtils.showShort(Utils.getApp().getString(R.string.done))
                    } else {
                        ToastUtils.showShort(Utils.getApp().getString(R.string.failed))
                    }
                    ViewUtils.runOnUiThread {
                        if (CommonK.Type_Media_Video == selectType || CommonK.Type_Media_Photo == selectType) {
                            tv_select_photo.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                            tv_select_video.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                            tv_select_audio.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                            selectType = CommonK.Type_Media_Audio
                            shimmerVoice?.start(tv_select_audio)
                            shimmerPhoto?.cancel()
                            shimmerVideo?.cancel()
                        }
                        refreshData(CommonK.Type_Media_Audio)
                    }
                }.start()
            }
        }

    }

    override fun onBackPressed() {
        finish()
    }

    override fun onStop() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && !isFinishing) {
            Instrumentation().callActivityOnSaveInstanceState(this, Bundle())
        }
        super.onStop()
    }

    private fun actTran() {
        window.enterTransition =
            TransitionInflater.from(this).inflateTransition(R.transition.home_tran_in)
//        window.exitTransition =
//            TransitionInflater.from(this).inflateTransition(R.transition.home_tran_out)

    }
}