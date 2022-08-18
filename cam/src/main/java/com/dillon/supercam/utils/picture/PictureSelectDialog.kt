package com.dillon.supercam.utils.picture

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.*

import com.dillon.supercam.R
import kotlinx.android.synthetic.main.dialog_select_photo.*

class PictureSelectDialog : Dialog, View.OnClickListener {
    private var mContext: Context? = null

    constructor(context: Context) : super(context) {
        mContext = context
    }

    constructor(context: Context, theme: Int) : super(context, theme) {
        mContext = context
        initDialog()
    }

    private fun initDialog() {
        val win = window
        win!!.decorView.setPadding(0, 0, 0, 0)
        win.setGravity(Gravity.RELATIVE_LAYOUT_DIRECTION or Gravity.BOTTOM)
        val lp = win.attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        win.attributes = lp
        setCanceledOnTouchOutside(false)
        show()
    }


    private fun hideDialog() {
        cancel()
        dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.dialog_select_photo)

        initListener()
    }

    private fun initListener() {
        lay_camera.setOnClickListener(this)
        lay_album.setOnClickListener(this)

        setOnKeyListener { _: DialogInterface?, keyCode: Int, event: KeyEvent ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {  //返回键监听
                hideDialog()
                mListener!!.onItemClick(PictureSelectUtils.CANCEL) //返回键关闭dialog
            }
            false
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isOutOfBounds(context, event)) {
            hideDialog()
            mListener!!.onItemClick(PictureSelectUtils.CANCEL) //触摸dialog外部关闭dialog
        }
        return super.onTouchEvent(event)
    }

    //判断当前用户触摸是否超出了Dialog的显示区域
    private fun isOutOfBounds(context: Context, event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        val slop = ViewConfiguration.get(context).scaledWindowTouchSlop
        val decorView = window!!.decorView
        return (x < -slop || y < -slop || x > decorView.width + slop
                || y > decorView.height + slop)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.lay_camera -> {
                hideDialog()
                mListener?.onItemClick(PictureSelectUtils.CAMERA)
            }
            R.id.lay_album -> {
                hideDialog()
                mListener?.onItemClick(PictureSelectUtils.ALBUM)
            }
        }
    }

    var mListener: OnItemClickListener? = null

    interface OnItemClickListener {
        //type 0取消，1拍照，2相册
        fun onItemClick(type: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        mListener = listener
    }
}