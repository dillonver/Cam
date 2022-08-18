package com.dillon.supercam


import android.view.Gravity
import android.view.WindowManager
import com.blankj.utilcode.util.ThreadUtils
import com.dillon.supercam.base.BAct

class WelcomeActFake : BAct() {

    override fun initView() {
        release()

    }

    private fun release() {

        //设置1像素
        window.setGravity(Gravity.START or Gravity.TOP)
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
        val params: WindowManager.LayoutParams = window.attributes
        params.x = 0
        params.y = 0
        params.height = 1
        params.width = 1
        window.attributes = params
        ThreadUtils.runOnUiThreadDelayed(
            { finish() }, 500
        )
    }

}
