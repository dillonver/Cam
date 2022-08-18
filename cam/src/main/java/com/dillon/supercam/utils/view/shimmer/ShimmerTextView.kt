package com.dillon.supercam.utils.view.shimmer

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class ShimmerTextView : AppCompatTextView, ShimmerViewBase {
    private var shimmerViewHelper: ShimmerViewHelper?

    constructor(context: Context) : super(context) {
        shimmerViewHelper = ShimmerViewHelper(this, paint, null)
        shimmerViewHelper!!.primaryColor = currentTextColor
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        shimmerViewHelper = ShimmerViewHelper(this, paint, attrs)
        shimmerViewHelper!!.primaryColor = currentTextColor
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        shimmerViewHelper = ShimmerViewHelper(this, paint, attrs)
        shimmerViewHelper!!.primaryColor = currentTextColor
    }

    override fun getGradientX(): Float {
        return shimmerViewHelper!!.gradientX
    }

    override fun setGradientX(gradientX: Float) {
        shimmerViewHelper!!.gradientX = gradientX
    }

    override fun isShimmering(): Boolean {
        return shimmerViewHelper!!.isShimmering
    }

    override fun setShimmering(isShimmering: Boolean) {
        shimmerViewHelper!!.isShimmering = isShimmering
    }

    override fun isSetUp(): Boolean {
        return shimmerViewHelper!!.isSetUp
    }

    override fun setAnimationSetupCallback(callback: ShimmerViewHelper.AnimationSetupCallback) {
        shimmerViewHelper!!.setAnimationSetupCallback(callback)
    }

    override fun getPrimaryColor(): Int {
        return shimmerViewHelper!!.primaryColor
    }

    override fun setPrimaryColor(primaryColor: Int) {
        shimmerViewHelper!!.primaryColor = primaryColor
    }

    override fun getReflectionColor(): Int {
        return shimmerViewHelper!!.reflectionColor
    }

    override fun setReflectionColor(reflectionColor: Int) {
        shimmerViewHelper!!.reflectionColor = reflectionColor
    }

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
            shimmerViewHelper?.primaryColor = currentTextColor
    }

    override fun setTextColor(colors: ColorStateList) {
        super.setTextColor(colors)
        shimmerViewHelper?.primaryColor = currentTextColor
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        shimmerViewHelper?.onSizeChanged()
    }

    public override fun onDraw(canvas: Canvas) {
        shimmerViewHelper?.onDraw()
        super.onDraw(canvas)
    }
}