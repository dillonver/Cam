package com.dillon.supercam.utils.picture

import android.content.Intent
import com.blankj.utilcode.util.FileUtils
import com.dillon.supercam.R
import com.dillon.supercam.base.BAct
import com.dillon.supercam.utils.CoreUtils
import com.dillon.supercam.utils.picture.PictureSelectUtils.getByAlbum
import com.dillon.supercam.utils.picture.PictureSelectUtils.getByCamera
import com.dillon.supercam.utils.picture.PictureSelectUtils.onActivityResult

class PictureSelectActivity : BAct() {
    companion object {
        const val CROP_WIDTH = "crop_width"
        const val CROP_HEIGHT = "crop_Height"
        const val RATIO_WIDTH = "ratio_Width"
        const val RATIO_HEIGHT = "ratio_Height"
        const val ENABLE_CROP = "enable_crop"
    }

    private var mSelectPictureDialog: PictureSelectDialog? = null
    private var mCropWidth = 0
    private var mCropHeight = 0
    private var mRatioWidth = 0
    private var mRatioHeight = 0
    private var mCropEnabled = false
    override fun initUi() {
        super.initUi()
        setContentView(R.layout.activity_picture_select)
    }

    override fun initView() {
        super.initView()
        mCropEnabled = intent.getBooleanExtra(ENABLE_CROP, true)
        mCropWidth = intent.getIntExtra(CROP_WIDTH, 200)
        mCropHeight = intent.getIntExtra(CROP_HEIGHT, 200)
        mRatioWidth = intent.getIntExtra(RATIO_WIDTH, 1)
        mRatioHeight = intent.getIntExtra(RATIO_HEIGHT, 1)
        if (CoreUtils.checkAllNecessaryPermissions()) {
            selectPicture()
        }
    }

    //选择图片
    private fun selectPicture() {
        mSelectPictureDialog = PictureSelectDialog(this, R.style.ActionSheetDialogStyle)
        mSelectPictureDialog?.setOnItemClickListener(object :
            PictureSelectDialog.OnItemClickListener {
            override fun onItemClick(type: Int) {
                when (type) {
                    PictureSelectUtils.CAMERA -> {
                        getByCamera(this@PictureSelectActivity)
                    }
                    PictureSelectUtils.ALBUM -> {
                        getByAlbum(this@PictureSelectActivity)
                    }
                    PictureSelectUtils.CANCEL -> {
                        finish()
                        overridePendingTransition(
                            0,
                            R.anim.activity_out
                        ) //activity延迟150毫秒退出，为了执行完Dialog退出的动画
                    }
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /*选择图片中途按返回键*/
        if (resultCode == RESULT_CANCELED) {
            when (requestCode) {
                PictureSelectUtils.GET_BY_ALBUM, PictureSelectUtils.GET_BY_CAMERA -> {
                    PictureSelectUtils.setCropFrom(0)
                    FileUtils.delete(PictureSelectUtils.getTempFilePath()) //删除用来装载的空文件
                    PictureSelectUtils.setTempFilePathNull()
                    finish()
                }
                PictureSelectUtils.GET_BY_CROP -> {
                    FileUtils.delete(PictureSelectUtils.getTempFilePath())//删除用来装载的空文件
                    FileUtils.delete(PictureSelectUtils.getTempCropFilePath()) //删除用来装载截图的空文件
                    PictureSelectUtils.setTempCropFilePathNull()
                    PictureSelectUtils.setCropFrom(0)
                    finish()
                }
            }
        } else {
            val picturePath = onActivityResult(
                this,
                requestCode,
                resultCode,
                data,
                mCropEnabled,
                mCropWidth,
                mCropHeight,
                mRatioWidth,
                mRatioHeight
            )
            if (!picturePath.isNullOrBlank()) {
                val bean = PictureBean()
                when (requestCode) {
                    PictureSelectUtils.GET_BY_ALBUM, PictureSelectUtils.GET_BY_CAMERA -> {
                        bean.filePath = picturePath
                    }
                    PictureSelectUtils.GET_BY_CROP -> {
                        FileUtils.delete(PictureSelectUtils.getTempFilePath())
                        bean.filePath = picturePath
                        bean.cropFilePath = PictureSelectUtils.getTempCropFilePath()
                        bean.cropFileFrom = PictureSelectUtils.getCropFrom()
                    }
                }
                val intent = Intent()
                intent.putExtra(PictureSelector.PICTURE_RESULT, bean)
                setResult(RESULT_OK, intent)
                finish()
            }
        }

    }

}