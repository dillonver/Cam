package com.dillon.supercam.ui.setting


import android.app.Instrumentation
import android.os.Build
import android.os.Bundle
import android.transition.Transition
import android.transition.TransitionInflater
import android.view.View
import com.blankj.utilcode.util.AppUtils
import com.dillon.dialogs.MaterialDialog
import com.dillon.supercam.R
import com.dillon.supercam.base.App
import com.dillon.supercam.base.BAct
import com.dillon.supercam.databinding.ActivityAboutBinding


class AppAboutAct : BAct() {
    private lateinit var binding: ActivityAboutBinding
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
                binding.ivTitleLeft.animate()?.rotation(180F)
            }

            override fun onTransitionEnd(transition: Transition?) {
                binding.ivTitleLeft.animate()?.rotation(0F)

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
                binding.ivTitleLeft.animate()?.rotation(180F)
            }

            override fun onTransitionEnd(transition: Transition?) {
                binding.ivTitleLeft.animate()?.rotation(0F)

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
                binding.ivTitleLeft.animate()?.rotation(180F)
            }

            override fun onTransitionEnd(transition: Transition?) {
                binding.ivTitleLeft.animate()?.rotation(0F)

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
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        actTran()
    }


    override fun initData() {
        super.initData()
        val des = getString(R.string.app_name) + " " + AppUtils.getAppVersionName()
        binding.tvAppVersionDes.text = des
        binding.tvAppAbout.visibility = View.VISIBLE

        if (App.appConfig?.appDes.isNullOrBlank()) {
            binding.tvAppAbout.text = getString(R.string.app_des)
        } else {
            binding.tvAppAbout.text = App.appConfig?.appDes
        }

    }


    override fun initListener() {
        super.initListener()
        binding.ivTitleLeft.setOnClickListener { onBackPressed() }
    }


    override fun onStop() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && !isFinishing) {
            Instrumentation().callActivityOnSaveInstanceState(this, Bundle())
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        materialDialog?.cancel()
    }


}