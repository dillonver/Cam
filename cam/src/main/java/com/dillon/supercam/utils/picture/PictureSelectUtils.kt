package com.dillon.supercam.utils.picture

import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.blankj.utilcode.util.*
import com.dillon.supercam.base.App.Companion.saveTempDir
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * 选择图片工具类
 * 使用方法：
 * 1. 调用getByCamera()、getByAlbum()可通过拍照或相册获取图片
 * 2. 在onActivityResult中调用本工具类的onActivityResult方法处理通过相册或拍照获取的图片
 */
object PictureSelectUtils {

    const val CANCEL = 0 //取消
    const val CAMERA = 1 //拍照
    const val ALBUM = 2 //相册

    const val GET_BY_ALBUM = 11 //相册标记
    const val GET_BY_CAMERA = 12 //拍照标记
    const val GET_BY_CROP = 13  //裁剪标记

    private var cropFrom = 0 //1拍照后裁剪 2相册选择后裁剪
    private var tempFilePath: String? = null//装载拍照后的图片地址或相册选择的图片地址
    private var tempCropFilePath: String? = null//空文件地址，用于装载裁剪后的图片地址

    fun getCropFrom(): Int {
        return cropFrom
    }

    fun setCropFrom(from: Int) {
        cropFrom = from
    }

    fun getTempFilePath(): String? {
        return tempFilePath
    }

    fun getTempCropFilePath(): String? {
        return tempCropFilePath
    }

    fun setTempFilePathNull() {
        tempFilePath = null
    }

    fun setTempCropFilePathNull() {
        tempCropFilePath = null
    }

    // 通过相册获取图片
    fun getByAlbum(activity: Activity) {
        FileUtils.createOrExistsDir(saveTempDir)
        tempFilePath = saveTempDir + "." + System.currentTimeMillis() + ".jpg"
        FileUtils.createFileByDeleteOldFile(tempFilePath)
        val intent = Intent(Intent.ACTION_PICK)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        activity.startActivityForResult(intent, GET_BY_ALBUM)
    }

    // 通过拍照获取图片
    fun getByCamera(activity: Activity) {
        FileUtils.createOrExistsDir(saveTempDir)
        tempFilePath = saveTempDir + "." + System.currentTimeMillis() + ".jpg"
        FileUtils.createFileByDeleteOldFile(tempFilePath)
        val i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        i.putExtra(
            MediaStore.EXTRA_OUTPUT, UriUtils.file2Uri(
                FileUtils.getFileByPath(
                    tempFilePath
                )
            )
        ) //输出路径（拍照后的保存路径）
        activity.startActivityForResult(i, GET_BY_CAMERA)

    }


    /**
     * 处理拍照或相册获取的图片
     *
     * @param activity    上下文
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data        Intent
     * @param cropEnabled 是否裁剪
     * @param w           输出宽
     * @param h           输出高
     * @param aspectX     宽比例
     * @param aspectY     高比例
     * @return picturePath 图片路径
     */

    fun onActivityResult(
        activity: Activity, requestCode: Int, resultCode: Int, data: Intent?,
        cropEnabled: Boolean, w: Int, h: Int, aspectX: Int, aspectY: Int
    ): String? {
        var picturePath: String? = null
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GET_BY_ALBUM -> {
                    //data不会为空
                    val uri = data!!.data
                    //复制uri,生成副本图片
                    FileUtils.copy(UriUtils.uri2File(uri), FileUtils.getFileByPath(tempFilePath))
                    if (cropEnabled) {
                        setCropFrom(ALBUM)
                        val intent = cropPicture(
                            UriUtils.file2Uri(FileUtils.getFileByPath(tempFilePath)),
                            w,
                            h,
                            aspectX,
                            aspectY
                        )
                        try {
                            activity.startActivityForResult(intent, GET_BY_CROP)
                        } catch (e: Exception) {
                            //
                        }

                    } else {
                        setCropFrom(0)
                        picturePath = tempFilePath
                    }
                }
                GET_BY_CAMERA -> {
                    //拍照若已经指定Uri， data会为空，应该直接拿指定地址的Uri
                    if (cropEnabled) {
                        setCropFrom(CAMERA)
                        try {
                            activity.startActivityForResult(
                                cropPicture(
                                    UriUtils.file2Uri(FileUtils.getFileByPath(tempFilePath)),
                                    w,
                                    h,
                                    aspectX,
                                    aspectY
                                ),
                                GET_BY_CROP
                            )
                        } catch (e: Exception) {
                            //
                        }

                    } else {
                        setCropFrom(0)
                        picturePath = tempFilePath
                    }

                }
                GET_BY_CROP -> {
                    picturePath = tempCropFilePath
                }
            }
        }
        LogUtils.i("picturePath: $picturePath")
        return picturePath
    }


    /**
     * 裁剪，例如：输出100*100大小的图片，宽高比例是1:1
     *
     * @param uri     图片的uri
     * @param w       输出宽
     * @param h       输出高
     * @param aspectX 宽比例
     * @param aspectY 高比例
     */
    private fun cropPicture(uri: Uri?, w: Int, h: Int, aspectX: Int, aspectY: Int): Intent {
        tempCropFilePath = saveTempDir + "." + System.currentTimeMillis() + ".jpg"
        FileUtils.createFileByDeleteOldFile(tempCropFilePath)

        var mAspectX = aspectX
        var mAspectY = aspectY

        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")
        intent.putExtra("crop", "true")
        if (mAspectX != 0 && mAspectX == mAspectY) {
            /*宽高比例相同时，华为设备的系统默认裁剪框是圆形的，这里统一改成方形的*/
            if (Build.MANUFACTURER == "HUAWEI") {
                mAspectX = 9998
                mAspectY = 9999
            }
        }
        if (w != 0 && h != 0) {
            intent.putExtra("outputX", w)
            intent.putExtra("outputY", h)
        }
        if (mAspectX != 0 || mAspectY != 0) {
            intent.putExtra("aspectX", mAspectX)
            intent.putExtra("aspectY", mAspectY)
        }

        /*解决图片有黑边问题*/
        intent.putExtra("scale", true)
        intent.putExtra("scaleUpIfNeeded", true)

        /*解决跳转到裁剪提示“图片加载失败”问题*/
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION) //添加这一句表示对目标应用临时授权该Uri所代表的文件
        intent.putExtra(
            MediaStore.EXTRA_OUTPUT, Uri.fromFile(
                FileUtils.getFileByPath(
                    tempCropFilePath
                )
            )
        )
        // 输出格式
        intent.putExtra("outputFormat", "JPEG")
        // 不启用人脸识别
        intent.putExtra("noFaceDetection", true)
        //是否将数据保留在Bitmap中返回
        intent.putExtra("return-data", false)
        return intent
    }

}