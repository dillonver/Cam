package com.dillon.supercam.ui.setting

import android.annotation.SuppressLint
import android.app.Instrumentation
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.transition.Transition
import android.transition.TransitionInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.blankj.utilcode.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dillon.dialogs.MaterialDialog
import com.dillon.supercam.R
import com.dillon.supercam.WelcomeAct
import com.dillon.supercam.WelcomeActFake
import com.dillon.supercam.base.App
import com.dillon.supercam.base.BAct
import com.dillon.supercam.key.CommonK
import com.dillon.supercam.utils.CoreUtils
import com.dillon.supercam.utils.LocalUtils
import kotlinx.android.synthetic.main.activity_special_set.*
import kotlinx.android.synthetic.main.layout_title_special_set.*


class SpecialSetAct : BAct() {

    private var materialDialog: MaterialDialog? = null

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

    override fun initUi() {
        super.initUi()
        setContentView(R.layout.activity_special_set)
        actTran()
    }

    private fun setAppBg() {
        if (!ActivityUtils.isActivityAlive(this)) return
        App.settingBgPath = LocalUtils.getPageBg(CommonK.PAGE_SETTING)
        if (App.settingBgPath.isNullOrBlank()) return
        Glide.with(this).load(App.settingBgPath)
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
            onBackPressed()
        }

        tv_title_center.setOnClickListener {
            val mTitle = AppUtils.getAppName() + "  v" + AppUtils.getAppVersionName()
            val mMessage = if (App.configInfo?.appDes.isNullOrBlank()) {
                resources.getString(R.string.app_des)
            } else {
                App.configInfo?.appDes
            }
            materialDialog?.cancel()
            materialDialog = MaterialDialog(this)
                .icon(R.drawable.ic_logo)
                .title(text = mTitle)
                .message(text = mMessage)
                .cornerRadius(res = R.dimen.dp_20)
            materialDialog?.show()
        }


        cb_camouflage_app.isChecked = LocalUtils.getAppHide()

        LocalUtils.getAppPassword()
        if (App.appPassWord.isNullOrBlank()) {
            cb_add_password.isChecked = false
            et_app_pass.visibility = View.INVISIBLE

        } else {
            cb_add_password.isChecked = true
            et_app_pass.visibility = View.VISIBLE
        }


        et_app_pass.setText(LocalUtils.getAppPassword().toString())
        et_app_pass.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) { //????????????
                // LogUtils.i("????????????")
                //        fireWorkView.bindEditText(et_app_pass)

            } else { //????????????
                //LogUtils.i("????????????")
                //       fireWorkView.removeBind()

                try {
                    val passStr = et_app_pass.text.toString().trim()
                    if (passStr.isBlank()) {
                        cb_add_password.isChecked = false
                    }
                    LocalUtils.saveAppPassword(passStr)
                    LogUtils.i(passStr)

                } catch (e: Exception) {
                }
            }
        }


    }

    override fun initData() {
        //-------------------APP-camouflage-------------------------

        cb_camouflage_app.setOnCheckedChangeListener { _, isChecked ->
            LocalUtils.saveAppHide(isChecked)
            CoreUtils.simpleLog("AppHide:$isChecked")
            if (isChecked) {
                CoreUtils.showHideApp(WelcomeAct::class.java.name, WelcomeActFake::class.java.name)
            } else {
                CoreUtils.showHideApp(WelcomeActFake::class.java.name, WelcomeAct::class.java.name)
            }
            CoreUtils.initShortcuts(this)
        }


        //-------------------APP-PASSWORD-------------------------
        cb_add_password.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                et_app_pass.visibility = View.VISIBLE
                et_app_pass.isFocusable = true
                et_app_pass.isFocusableInTouchMode = true
                et_app_pass.requestFocus()
            } else {
                et_app_pass.setText("")
                LocalUtils.saveAppPassword("")
                et_app_pass.visibility = View.INVISIBLE
            }

        }
    }

    //????????????????????????
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        //???????????????????????????????????????view?????????????????????????????????
        if (ev.action == MotionEvent.ACTION_DOWN) {
            //???????????????????????????view
            val v = currentFocus
            //????????????????????????????????????
            if (v != null && isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v.windowToken)
            }
        }
        //?????????activity???????????????????????????????????????????????????????????????????????????
        return super.dispatchTouchEvent(ev)
    }

    //???????????????????????????
    private fun isShouldHideKeyboard(
        v: View,
        event: MotionEvent
    ): Boolean {
        //?????????????????????????????????view???editText?????????????????????????????????
        if (v is EditText) {
            val l = intArrayOf(0, 0)
            v.getLocationInWindow(l)
            val left = l[0]
            val top = l[1]
            val bottom = top + v.getHeight()
            val right = left + v.getWidth()
            // ??????EditText????????????????????????
            return (event.x <= left || event.x >= right
                    || event.y <= top || event.y >= bottom)
        }
        // ??????????????????EditText?????????????????????????????????????????????????????????????????????EditText???
        return false
    }

    //?????????????????????editText????????????
    private fun hideKeyboard(token: IBinder?) {
        et_app_pass.clearFocus()
        if (token != null) {
            //???????????????InputMethodManager???????????????????????????????????????
            //InputMethodManager?????????????????????????????????manager
            val im: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }


    override fun onResume() {
        super.onResume()
        LogUtils.i("onResume")
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtils.i("onDestroy")
        materialDialog?.cancel()
    }


    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && !isFinishing) {
            Instrumentation().callActivityOnSaveInstanceState(this, Bundle())
        }
    }

}

