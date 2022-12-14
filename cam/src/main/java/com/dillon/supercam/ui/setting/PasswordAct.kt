package com.dillon.supercam.ui.setting


import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.IBinder

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
import com.dillon.supercam.WelcomeAct
import com.dillon.supercam.WelcomeActFake
import com.dillon.supercam.WelcomeActShortCut
import com.dillon.supercam.base.App
import com.dillon.supercam.base.BAct
import com.dillon.supercam.key.CommonK
import com.dillon.supercam.ui.home.HomeAct
import com.dillon.supercam.utils.CoreUtils
import com.dillon.supercam.utils.LocalUtils
import com.dillon.supercam.utils.view.shimmer.Shimmer
import kotlinx.android.synthetic.main.activity_password.*
import kotlinx.android.synthetic.main.activity_password.act_bg


class PasswordAct : BAct(), View.OnClickListener {

    private var materialDialog: MaterialDialog? = null


    override fun initUi() {
        super.initUi()
        setContentView(R.layout.activity_password)
        actTran()
    }


    override fun initView() {
        if (LocalUtils.getAppHide()) {
            tv_show_title.text = getString(R.string.app_name_hide)
        } else {
            tv_show_title.text = getString(R.string.app_name)

        }
        val shimmer = Shimmer()
        shimmer.setDuration(6000)
        shimmer.start(tv_show_title)

        setAppBg()
    }

    private fun setAppBg() {
        if (!ActivityUtils.isActivityAlive(this)) return
        App.passBgPath = LocalUtils.getPageBg(CommonK.PAGE_PASS)
        if (App.passBgPath.isNullOrBlank()) return
        Glide.with(this).load(App.passBgPath)
            //.skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(act_bg)
    }

    override fun initListener() {
        super.initListener()
        tv_enter.setOnClickListener(this)
        tv_forget_password.setOnClickListener(this)
        et_pass.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) { //????????????
                // LogUtils.i("????????????")
                //    fireWorkView.bindEditText(et_pass)

            } else { //????????????
                //LogUtils.i("????????????")
                //    fireWorkView.removeBind()

                try {
                    val passStr = et_pass.text.toString().trim()
                    if (passStr == LocalUtils.getAppPassword() || CoreUtils.isDevPass(passStr)) {
                        startActivity(
                            Intent(this, HomeAct::class.java),
                            ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
                        )

                    } else {
                        ToastUtils.showShort(getString(R.string.incorrect_pass))
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_enter -> {
                val passStr = et_pass.text.toString().trim()
                if (passStr == LocalUtils.getAppPassword() || CoreUtils.isDevPass(passStr)) {
                    startActivity(
                        Intent(this, HomeAct::class.java),
                        ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
                    )
                } else {
                    ToastUtils.showShort(getString(R.string.incorrect_pass))
                }
            }
            tv_forget_password -> {

                val mTitle = getString(R.string.dialog_common_title)
                val mMessage = getString(R.string.contact_email_tip)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)
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
    }


    override fun onDestroy() {
        super.onDestroy()
        materialDialog?.cancel()
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
        et_pass.clearFocus()
        if (token != null) {
            //???????????????InputMethodManager???????????????????????????????????????
            //InputMethodManager?????????????????????????????????manager
            val im: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun actTran() {
        window.enterTransition =
            TransitionInflater.from(this).inflateTransition(R.transition.pass_tran_in)
        window.exitTransition =
            TransitionInflater.from(this).inflateTransition(R.transition.pass_tran_out)
    }

    override fun initData() {
        super.initData()
        ActivityUtils.finishActivity(WelcomeAct::class.java)
        ActivityUtils.finishActivity(WelcomeActFake::class.java)
        ActivityUtils.finishActivity(WelcomeActShortCut::class.java)
    }

}
