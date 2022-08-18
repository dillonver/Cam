package com.dillon.supercam.ui.home


import android.app.Instrumentation
import android.os.Build
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.View
import android.widget.ImageView
import com.blankj.utilcode.util.*
import com.dillon.video.Jzvd
import com.dillon.dialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.dillon.supercam.R
import com.dillon.supercam.base.BAct
import com.dillon.supercam.bean.MediaInfo
import com.dillon.supercam.key.CommonK
import com.dillon.supercam.key.ParamsK
import kotlinx.android.synthetic.main.activity_media_show.*
import com.bumptech.glide.request.RequestOptions
import com.dillon.supercam.utils.glide.BlurTransformation


class MediaShowAct : BAct(), View.OnClickListener {
    private var materialDialog: MaterialDialog? = null

    override fun initUi() {
        super.initUi()
        setContentView(R.layout.activity_media_show)
        actTran()

    }

    override fun initData() {
        super.initData()
        try {
            val mMediaInfo = intent.getSerializableExtra(ParamsK.intentTemp) as MediaInfo
            when {
                CommonK.Type_Media_Video == mMediaInfo.type -> {
                    lay_video.visibility = View.VISIBLE
                    lay_photo.visibility = View.INVISIBLE

                    jz_video.clarity.visibility = View.GONE
                    //jz_video.fullscreenButton.visibility = View.INVISIBLE
                    jz_video.replayTextView.visibility = View.INVISIBLE
                    jz_video.posterImageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    Glide.with(Utils.getApp().baseContext).load(mMediaInfo.file)
                        .into(jz_video.posterImageView)
                    jz_video.setUp(mMediaInfo.path, mMediaInfo.title)
                    jz_video.startVideoAfterPreloading()

                }
                CommonK.Type_Media_Photo == mMediaInfo.type -> {
                    lay_video.visibility = View.INVISIBLE
                    lay_photo.visibility = View.VISIBLE
                    if (mMediaInfo.file != null) {
                        Glide.with(this).load(mMediaInfo.file).into(photo_view)
                        Glide.with(this).load(mMediaInfo.file)
                            .apply(RequestOptions.bitmapTransform(BlurTransformation(this, 25, 3)))
                            .into(ivPhoto)
                    } else {
                        Glide.with(this).load(mMediaInfo.path).into(photo_view)
                        Glide.with(this).load(mMediaInfo.path)
                            .apply(RequestOptions.bitmapTransform(BlurTransformation(this, 25, 3)))
                            .into(ivPhoto)
                    }

                }
                else -> {
                    //录音文件
                    lay_video.visibility = View.VISIBLE
                    lay_photo.visibility = View.INVISIBLE

                    jz_video.clarity.visibility = View.GONE
                    // jz_video.fullscreenButton.visibility = View.INVISIBLE
                    jz_video.replayTextView.visibility = View.INVISIBLE
                    jz_video.setUp(mMediaInfo.path, mMediaInfo.title)
                    jz_video.startVideoAfterPreloading()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun initListener() {
        super.initListener()
        v_photo_top.setOnClickListener(this)
        v_photo_bottom.setOnClickListener(this)
        v_video_top.setOnClickListener(this)
        v_video_bottom.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v) {
            v_photo_top, v_photo_bottom, v_video_top, v_video_bottom -> {
                onBackPressed()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Jzvd.releaseAllVideos()
    }


    override fun onBackPressed() {
        if (Jzvd.backPress()) {
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        materialDialog?.cancel()
        Jzvd.releaseAllVideos()
    }

    private fun actTran() {
        window.enterTransition =
            TransitionInflater.from(this).inflateTransition(R.transition.media_tran_in)
        window.exitTransition =
            TransitionInflater.from(this).inflateTransition(R.transition.media_tran_in)
        window.returnTransition =
            TransitionInflater.from(this).inflateTransition(R.transition.media_tran_out)
        window.reenterTransition =
            TransitionInflater.from(this).inflateTransition(R.transition.media_tran_in)

    }

    override fun onStop() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && !isFinishing) {
            Instrumentation().callActivityOnSaveInstanceState(this, Bundle())
        }
        super.onStop()
    }

}
