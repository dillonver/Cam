package com.dillon.supercam.base

import android.app.Activity
import android.os.Bundle
import android.view.MotionEvent
import android.view.animation.AccelerateInterpolator
import androidx.fragment.app.FragmentActivity
import com.dillon.supercam.R
import com.dillon.supercam.utils.CoreUtils
import com.dillon.supercam.utils.view.leonids.ParticleSystem

//BaseActivity
abstract class BAct : FragmentActivity() {
    private var ps: ParticleSystem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        beforeOnCreateView()
        super.onCreate(savedInstanceState)
        CoreUtils.appStyle(this)
        CoreUtils.simpleLog(this.javaClass.simpleName)
        initUi()
        initView()
        initData()
        baseInitData()
        initListener()
    }

    open fun beforeOnCreateView() {}

    open fun initUi() {}

    open fun initView() {}

    open fun initData() {}

    open fun initListener() {}

    private fun baseInitData() {}

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                ps = ParticleSystem(this, 100, R.drawable.star_white, 800)
                ps?.setScaleRange(0.7f, 1.3f)
                ps?.setSpeedRange(0.05f, 0.1f)
                ps?.setRotationSpeedRange(90f, 180f)
                ps?.setFadeOut(300, AccelerateInterpolator())
                ps?.emit(event.x.toInt(), event.y.toInt(), 40)
            }
            MotionEvent.ACTION_MOVE -> ps?.updateEmitPoint(event.x.toInt(), event.y.toInt())
            MotionEvent.ACTION_UP -> ps?.stopEmitting()
        }
        return true
    }

    override fun onPause() {
        super.onPause()
        ps?.stopEmitting()
    }

    override fun onDestroy() {
        super.onDestroy()
        ps?.stopEmitting()
    }

    override fun onBackPressed() {
        App.needRefresh = false
        super.onBackPressed()
    }

}
