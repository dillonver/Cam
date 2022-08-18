package com.dillon.supercam.ui.setting

import android.app.Instrumentation
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.Transition
import android.transition.TransitionInflater
import com.dillon.dialogs.MaterialDialog
import com.blankj.utilcode.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dillon.supercam.R
import com.dillon.supercam.base.App
import com.dillon.supercam.base.BAct
import com.dillon.supercam.key.CommonK
import com.dillon.supercam.utils.CoreUtils
import com.dillon.supercam.utils.LocalUtils
import com.dillon.supercam.utils.picture.PictureBean
import com.dillon.supercam.utils.picture.PictureSelector
import kotlinx.android.synthetic.main.activity_theme.*

import kotlinx.android.synthetic.main.layout_title_common.*
import kotlinx.android.synthetic.main.layout_title_common.iv_title_left
import kotlinx.android.synthetic.main.layout_title_common.lay_title_center
import kotlinx.android.synthetic.main.layout_title_common.lay_title_left
import kotlinx.android.synthetic.main.layout_title_common.tv_title_center


class ThemeAct : BAct() {
    private var selectHome = 10
    private var selectSet = 11
    private var selectFaq = 12
    private var selectPass = 14
    private var selectChat = 15
    private var materialDialog: MaterialDialog? = null

    override fun initUi() {
        super.initUi()
        setContentView(R.layout.activity_theme)
        actTran()
    }


    override fun initView() {
        super.initView()

        lay_title_left.setOnClickListener {
            //finish()
            onBackPressed()
        }
        tv_title_center.text = getString(R.string.theme_title)
        lay_title_center.setOnClickListener {
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


    }

    override fun initData() {
        super.initData()

        App.homeBgPath = LocalUtils.getPageBg(CommonK.PAGE_HOME)
        if (App.homeBgPath.isNullOrBlank()) {
            if (App.configInfo != null && !App.configInfo!!.urlH.isNullOrBlank()) {
                Glide.with(this).load(App.configInfo!!.urlH).error(R.drawable.ic_round_add_24)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(iv_home_bg)
            } else {
                Glide.with(this).load(R.drawable.ic_round_add_24)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(iv_home_bg)
            }
        } else {
            Glide.with(this).load(App.homeBgPath)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(iv_home_bg)
        }

        App.settingBgPath = LocalUtils.getPageBg(CommonK.PAGE_SETTING)
        if (!App.settingBgPath.isNullOrBlank()) {
            Glide.with(this).load(App.settingBgPath)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(iv_setting_bg)
        }

        App.captureBgPath = LocalUtils.getPageBg(CommonK.PAGE_CAPTURE)
        if (!App.captureBgPath.isNullOrBlank()) {
            Glide.with(this).load(App.captureBgPath)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(iv_faq_bg)
        }

        App.passBgPath = LocalUtils.getPageBg(CommonK.PAGE_PASS)
        if (!App.passBgPath.isNullOrBlank()) {
            Glide.with(this).load(App.passBgPath)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(iv_pass_bg)
        }

        App.chatBgPath = LocalUtils.getPageBg(CommonK.PAGE_CHAT)
        if (!App.chatBgPath.isNullOrBlank()) {
            Glide.with(this).load(App.chatBgPath)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(iv_chat_bg)
        }

    }


    override fun initListener() {
        super.initListener()
        iv_home_bg.setOnClickListener {
            PictureSelector
                .create(this, selectHome)
                .selectPicture(false)
        }
        iv_home_bg.setOnLongClickListener {
            Glide.with(this@ThemeAct).load(R.drawable.ic_round_add_24).into(iv_home_bg)
            LocalUtils.removePageBg(CommonK.PAGE_HOME)
            true
        }

        iv_setting_bg.setOnClickListener {
            PictureSelector
                .create(this, selectSet)
                .selectPicture(false)
        }
        iv_setting_bg.setOnLongClickListener {
            Glide.with(this@ThemeAct).load(R.drawable.ic_round_add_24).into(iv_setting_bg)
            LocalUtils.removePageBg(CommonK.PAGE_SETTING)
            true
        }

        iv_pass_bg.setOnClickListener {
            PictureSelector
                .create(this, selectPass)
                .selectPicture(false)
        }
        iv_pass_bg.setOnLongClickListener {
            Glide.with(this@ThemeAct).load(R.drawable.ic_round_add_24).into(iv_pass_bg)
            LocalUtils.removePageBg(CommonK.PAGE_PASS)
            true
        }

        iv_faq_bg.setOnClickListener {
            PictureSelector
                .create(this, selectFaq)
                .selectPicture(false)
        }
        iv_faq_bg.setOnLongClickListener {
            Glide.with(this@ThemeAct).load(R.drawable.ic_round_add_24).into(iv_faq_bg)
            LocalUtils.removePageBg(CommonK.PAGE_CAPTURE)
            true
        }

        iv_chat_bg.setOnClickListener {
            PictureSelector
                .create(this, selectChat)
                .selectPicture(false)
        }
        iv_chat_bg.setOnLongClickListener {
            Glide.with(this@ThemeAct).load(R.drawable.ic_round_add_24).into(iv_chat_bg)
            LocalUtils.removePageBg(CommonK.PAGE_CHAT)
            true
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        materialDialog?.cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /*结果回调*/
        try {
            if (data != null) {
                val pictureBean =
                    data.getSerializableExtra(PictureSelector.PICTURE_RESULT) as PictureBean?
                if (pictureBean != null) {
                    val imageFile = pictureBean.filePath
                    when (requestCode) {
                        selectHome -> {
                            Glide.with(this).load(imageFile)
                                .error(R.drawable.app_bg)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(iv_home_bg)
                            LocalUtils.savePageBg(CommonK.PAGE_HOME, imageFile)
                        }
                        selectSet -> {
                            Glide.with(this).load(imageFile)
                                .error(R.drawable.app_bg)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(iv_setting_bg)
                            LocalUtils.savePageBg(CommonK.PAGE_SETTING, imageFile)
                        }
                        selectFaq -> {
                            Glide.with(this).load(imageFile)
                                .error(R.drawable.app_bg)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(iv_faq_bg)
                            LocalUtils.savePageBg(CommonK.PAGE_CAPTURE, imageFile)
                        }
                        selectPass -> {
                            Glide.with(this).load(imageFile)
                                .error(R.drawable.app_bg)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(iv_pass_bg)
                            LocalUtils.savePageBg(CommonK.PAGE_PASS, imageFile)
                        }
                        selectChat -> {
                            Glide.with(this).load(imageFile)
                                .error(R.drawable.app_bg)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(iv_chat_bg)
                            LocalUtils.savePageBg(CommonK.PAGE_CHAT, imageFile)
                        }

                    }

                }
            } else {
                LogUtils.i("null==pictureBean")
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    override fun onStop() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && !isFinishing) {
            Instrumentation().callActivityOnSaveInstanceState(this, Bundle())
        }
        super.onStop()
    }

}

