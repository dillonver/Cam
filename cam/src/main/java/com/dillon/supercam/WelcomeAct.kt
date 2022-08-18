package com.dillon.supercam

import android.app.ActivityOptions
import android.content.Intent
import com.dillon.supercam.base.BAct
import com.dillon.supercam.test.TestAct

import com.dillon.supercam.ui.setting.PasswordAct
import com.dillon.supercam.ui.home.HomeAct
import com.dillon.supercam.utils.LocalUtils


class WelcomeAct : BAct() {

    override fun initView() {
         release()
       // test()


    }

    private fun release() {
        if (LocalUtils.getAppPassword().isNullOrBlank()) {
            startActivity(
                Intent(this, HomeAct::class.java),
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
            )
        } else {
            startActivity(
                Intent(this, PasswordAct::class.java),
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
            )
        }
        finishAfterTransition()

    }


    private fun test() {
        startActivity(
            Intent(
                this,
                TestAct::class.java

            )
        )
        finish()

    }

}
