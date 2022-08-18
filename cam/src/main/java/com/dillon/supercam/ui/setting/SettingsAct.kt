package com.dillon.supercam.ui.setting


import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.transition.Transition
import android.transition.TransitionInflater
import android.view.View
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dillon.dialogs.MaterialDialog
import com.dillon.supercam.R
import com.dillon.supercam.base.App
import com.dillon.supercam.base.BAct
import com.dillon.supercam.databinding.ActivitySettingsBinding
import com.dillon.supercam.key.CommonK
import com.dillon.supercam.services.task.CheckDrawOverlaySer
import com.dillon.supercam.utils.CoreUtils
import com.dillon.supercam.utils.LocalUtils
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions


class SettingsAct : BAct() {
    private lateinit var binding: ActivitySettingsBinding
    private var materialDialog: MaterialDialog? = null
    private var batteryIgnore = false
    private var canCamera = false
    private var canWhite = false
    private var canAudio = false
    private var canFloat = false

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
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        actTran()
    }

    override fun initData() {
        super.initData()
        setAppBg()
    }

    private fun setAppBg() {
        if (!ActivityUtils.isActivityAlive(this)) return
        App.settingBgPath = LocalUtils.getPageBg(CommonK.PAGE_SETTING)
        if (App.settingBgPath.isNullOrBlank()) return
        Glide.with(this).load(App.settingBgPath)
            //.skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.actBg)

    }

    override fun initListener() {
        super.initListener()
        binding.ivTitleLeft.setOnClickListener { onBackPressed() }
        binding.tvRepair.setOnClickListener { checkMyPermission() }
        binding.tvSuggestion.setOnClickListener { }
        binding.tvAppInstructions.setOnClickListener {
            startActivity(
                Intent(this, AppInstructionsAct::class.java),
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
            )

        }
        binding.tvAppAbout.setOnClickListener {
            startActivity(
                Intent(this, AppAboutAct::class.java),
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
            )

        }
        binding.tvAppNew.setOnClickListener {
        }
        binding.layActCode.setOnClickListener {
        }

        binding.layThemeSet.setOnClickListener {
            startActivity(
                Intent(this, ThemeAct::class.java),
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
            )
        }

        binding.tvCaptureSet.setOnClickListener {
            startActivity(
                Intent(this, CaptureSetAct::class.java),
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
            )
        }

        binding.tvSpecialSet.setOnClickListener {
            startActivity(
                Intent(this, SpecialSetAct::class.java),
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
            )
        }

    }


    private fun checkAppStatus() {

        if (App.updateInfo != null) {
            binding.tvAppNew.visibility = View.VISIBLE
        } else {
            binding.tvAppNew.visibility = View.INVISIBLE
        }


        //检测权限是否正常赋予
        batteryIgnore = CoreUtils.checkIgnoreBatteryOptimization()
        canCamera = XXPermissions.isGranted(this, Permission.CAMERA)
        canAudio = XXPermissions.isGranted(this, Permission.RECORD_AUDIO)
        canWhite = XXPermissions.isGranted(this, Permission.WRITE_EXTERNAL_STORAGE)

//            if (Build.VERSION.SDK_INT >= 30) {
//          XXPermissions.isGranted(this, Permission.MANAGE_EXTERNAL_STORAGE)
//        } else {
//            XXPermissions.isGranted(this, Permission.WRITE_EXTERNAL_STORAGE)
//        }
        canFloat = CoreUtils.checkDrawOverlaysPermission()

        if (batteryIgnore && canCamera && canAudio && canWhite && canFloat) {
            binding.tvRepair.visibility = View.INVISIBLE
            binding.ivAppStatusDot.setImageResource(R.drawable.shape_green_dot)
            binding.tvAppStatusDes.text = "正常"
            binding.tvAppStatusDes.setTextColor(ColorUtils.getColor(R.color.green))
        } else {
            binding.tvRepair.visibility = View.VISIBLE
            binding.ivAppStatusDot.setImageResource(R.drawable.shape_orange_dot)
            binding.tvAppStatusDes.text = "异常（权限丢失）"
            binding.tvAppStatusDes.setTextColor(ColorUtils.getColor(R.color.orange))
        }

    }

    @SuppressLint("BatteryLife")
    private fun checkMyPermission() {
        val permissionList = ArrayList<String>()
        permissionList.add(Manifest.permission.CAMERA)
        permissionList.add(Manifest.permission.RECORD_AUDIO)
//        if (Build.VERSION.SDK_INT >= 30) {
//            permissionList.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
//        } else {
        permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        //     }
        XXPermissions.with(this) // 申请单个权限
            .permission(permissionList)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: List<String>, all: Boolean) {
                    if (all) {
                        if (!batteryIgnore) {
                            try {
                                val intent =
                                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                intent.data = Uri.parse("package:$packageName")
                                startActivity(intent)
                            } catch (e: Exception) {
                                LogUtils.e(e)
                            }

                        } else {
                            checkAllNecessaryPermissions()
                        }
                    } else {
                        ToastUtils.showLong(getString(R.string.many_permission_gain_fail))
                    }
                }

                override fun onDenied(permissions: List<String>, never: Boolean) {
                    if (never) {
                        ToastUtils.showLong(getString(R.string.many_permissions_reject_never))
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(this@SettingsAct, permissions)
                    }
                }
            })
    }

    private fun checkAllNecessaryPermissions() {
        if (!CoreUtils.checkAllNecessaryPermissions()) {
            //理论上能进来设置页面，基本权限和电源优化管理权限已经开启了
            if (!CoreUtils.checkOverDraw()) {
                val mMessage = getString(R.string.tip_agree_overdraw)
                val title = getString(R.string.dialog_common_title)
                materialDialog?.cancel()
                materialDialog = MaterialDialog(this)

                    .title(text = title)
                    .message(text = mMessage)
                    .cornerRadius(res = R.dimen.dp_20)
                    .positiveButton(null, getString(R.string.go)) {
                        requestDrawOverlaysPermissions()

                    }
                materialDialog?.cancelOnTouchOutside(false)
                materialDialog?.setOnDismissListener {
                    App.needRefresh = false
                }
                if (materialDialog != null && !materialDialog!!.isShowing && ActivityUtils.isActivityAlive(
                        this
                    )
                ) {
                    materialDialog?.show()
                }
            } else {
                materialDialog?.cancel()
            }

        }

    }

    private fun requestDrawOverlaysPermissions() {
        CoreUtils.startCommonService(CheckDrawOverlaySer::class.java)
        XXPermissions.with(this)
            .permission(Permission.SYSTEM_ALERT_WINDOW)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: List<String>, all: Boolean) {
                    LogUtils.i("onGranted")

                }

                override fun onDenied(permissions: List<String>, never: Boolean) {
                    LogUtils.i("onDenied")
                }
            })

    }



    override fun onResume() {
        super.onResume()
        checkAppStatus()
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