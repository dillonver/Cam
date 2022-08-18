package com.dillon.supercam.utils.picture

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference

class PictureSelector private constructor(
    activity: Activity?,
    fragment: Fragment?,
    requestCode: Int
) {
    companion object {
        const val SELECT_REQUEST_CODE = 0x15 //选择图片请求码
        const val PICTURE_RESULT = "picture_result" //选择的图片结果

        /**
         * 创建 PictureSelector（用于 Activity）
         *
         * @param activity    Activity
         * @param requestCode 请求码，用于结果回调 onActivityResult() 中判断
         * @return PictureSelector
         */
        fun create(activity: Activity, requestCode: Int): PictureSelector {
            return PictureSelector(activity, requestCode)
        }

        /**
         * 创建 PictureSelector（用于 Fragment）
         *
         * @param fragment    Fragment
         * @param requestCode 请求码，用于结果回调 onActivityResult() 中判断
         * @return PictureSelector
         */
        fun create(fragment: Fragment, requestCode: Int): PictureSelector {
            return PictureSelector(fragment, requestCode)
        }
    }

    private val mRequestCode: Int = requestCode
    private val mActivity: WeakReference<Activity?> = WeakReference<Activity?>(activity)
    private val mFragment: WeakReference<Fragment?> = WeakReference<Fragment?>(fragment)

    private constructor(activity: Activity, requestCode: Int) : this(
        activity,
        null as Fragment?,
        requestCode
    )

    private constructor(fragment: Fragment, requestCode: Int) : this(
        fragment.activity,
        fragment,
        requestCode
    )
    /**
     * 选择图片（指定宽高及宽高比例裁剪）
     *
     *   是否裁剪
     *   裁剪宽
     *   裁剪高
     *   宽比例
     *   高比例
     */
    /**
     * 选择图片（默认不裁剪）
     */
    /**
     * 选择图片（根据参数决定是否裁剪）
     *
     * @param cropEnabled 是否裁剪
     */

    fun selectPicture(
        cropEnabled: Boolean = false,
        cropWidth: Int = 0,
        cropHeight: Int = 0,
        ratioWidth: Int = 0,
        ratioHeight: Int = 0
    ) {
        val activity = mActivity.get()
        val fragment = mFragment.get()
        val intent = Intent(activity, PictureSelectActivity::class.java)
        intent.putExtra(PictureSelectActivity.ENABLE_CROP, cropEnabled)
        intent.putExtra(PictureSelectActivity.CROP_WIDTH, cropWidth)
        intent.putExtra(PictureSelectActivity.CROP_HEIGHT, cropHeight)
        intent.putExtra(PictureSelectActivity.RATIO_WIDTH, ratioWidth)
        intent.putExtra(PictureSelectActivity.RATIO_HEIGHT, ratioHeight)
        if (fragment != null) {
            fragment.startActivityForResult(intent, mRequestCode)
        } else {
            activity?.startActivityForResult(intent, mRequestCode)
        }
    }


}