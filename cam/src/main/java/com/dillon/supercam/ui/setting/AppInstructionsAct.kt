package com.dillon.supercam.ui.setting


import android.app.Instrumentation
import android.os.Build
import android.os.Bundle
import android.transition.Transition
import android.transition.TransitionInflater
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.dillon.dialogs.MaterialDialog
import com.dillon.supercam.R
import com.dillon.supercam.base.App
import com.dillon.supercam.base.BAct
import com.dillon.supercam.databinding.ActivityInstructionsBinding


class AppInstructionsAct : BAct() {
    private lateinit var binding: ActivityInstructionsBinding
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
                binding.layWebView.animate()?.alpha(0f)
            }

            override fun onTransitionEnd(transition: Transition?) {
                binding.ivTitleLeft.animate()?.rotation(0F)
                binding.layWebView.animate()?.alpha(1f)
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
                binding.layWebView.animate()?.alpha(0f)

            }

            override fun onTransitionEnd(transition: Transition?) {
                binding.ivTitleLeft.animate()?.rotation(0F)
                binding.layWebView.animate()?.alpha(1f)


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
                binding.layWebView.animate()?.alpha(0f)

            }

            override fun onTransitionEnd(transition: Transition?) {
                binding.ivTitleLeft.animate()?.rotation(0F)
                binding.layWebView.animate()?.alpha(1f)

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
        binding = ActivityInstructionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        actTran()
    }


    override fun initData() {
        super.initData()
        val instruction =
            "https://supercam-1252281643.cos.ap-guangzhou.myqcloud.com/common/cam_instructions.html"
        initWebView(instruction)
    }

    private fun initWebView(url: String) {
        // 设置WebView的客户端
        binding.instructionWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return false // 返回false
            }
        }
        val webSettings: WebSettings = binding.instructionWebView.settings
        // 让JavaScript可以自动打开windows
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        // 设置缓存模式,一共有四种模式
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        // 支持缩放(适配到当前屏幕)
        webSettings.setSupportZoom(true)
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true;
        // 支持内容重新布局,一共有四种方式
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        webSettings.displayZoomControls = true
        webSettings.defaultFontSize = 35
        // binding.instructionWebView.isHorizontalScrollBarEnabled = false
        //  binding.instructionWebView.isVerticalScrollBarEnabled = false
        binding.instructionWebView.loadUrl(url)
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