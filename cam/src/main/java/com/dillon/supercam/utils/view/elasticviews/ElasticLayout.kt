/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 skydoves
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.dillon.supercam.utils.view.elasticviews

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View.OnClickListener
import android.widget.FrameLayout
import androidx.annotation.Px
import com.dillon.supercam.R
import com.dillon.supercam.utils.view.elasticviews.Definitions

@Suppress("unused")
class ElasticLayout @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), ElasticView {

  /** The target elastic scale size of the animation. */
  var scale = Definitions.DEFAULT_SCALE

  /** The default duration of the animation. */
  var duration = Definitions.DEFAULT_DURATION

  @Px
  var cornerRadius = 0f

  private var onClickListener: OnClickListener? = null
  private var onFinishListener: ElasticFinishListener? = null

  init {
    onCreate()
    when {
      attrs != null && defStyle != 0 -> getAttrs(attrs, defStyle)
      attrs != null -> getAttrs(attrs)
    }
  }

  private fun onCreate() {
    this.isClickable = true
    this.isFocusable = true
    super.setOnClickListener {
      elasticAnimation(this) {
        setDuration(this@ElasticLayout.duration)
        setScaleX(this@ElasticLayout.scale)
        setScaleY(this@ElasticLayout.scale)
        setOnFinishListener { invokeListeners() }
      }.doAction()
    }
  }

  private fun getAttrs(attrs: AttributeSet) {
    val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ElasticLayout)
    try {
      setTypeArray(typedArray)
    } finally {
      typedArray.recycle()
    }
  }

  private fun getAttrs(attrs: AttributeSet, defStyle: Int) {
    val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ElasticLayout, defStyle, 0)
    try {
      setTypeArray(typedArray)
    } finally {
      typedArray.recycle()
    }
  }

  private fun setTypeArray(typedArray: TypedArray) {
    this.scale = typedArray.getFloat(R.styleable.ElasticLayout_layout_scale, this.scale)
    this.duration = typedArray.getInt(R.styleable.ElasticLayout_layout_duration, this.duration)
    this.cornerRadius =
      typedArray.getDimension(R.styleable.ElasticLayout_layout_cornerRadius, this.cornerRadius)
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    initializeBackground()
  }

  private fun initializeBackground() {
    if (background is ColorDrawable) {
      background = GradientDrawable().apply {
        cornerRadius = this@ElasticLayout.cornerRadius
        setColor((background as ColorDrawable).color)
      }.mutate()
    }
  }

  override fun setOnClickListener(listener: OnClickListener?) {
    this.onClickListener = listener
  }

  override fun setOnFinishListener(listener: ElasticFinishListener?) {
    this.onFinishListener = listener
  }

  override fun setOnClickListener(block: () -> Unit) =
    setOnClickListener(OnClickListener { block() })

  override fun setOnFinishListener(block: () -> Unit) =
    setOnFinishListener(ElasticFinishListener { block() })

  private fun invokeListeners() {
    this.onClickListener?.onClick(this)
    this.onFinishListener?.onFinished()
  }

  /** Builder class for creating [ElasticLayout]. */
  class Builder(context: Context) {
    private val elasticLayout = ElasticLayout(context)

    fun setScale(value: Float) = apply { this.elasticLayout.scale = value }
    fun setDuration(value: Int) = apply { this.elasticLayout.duration = value }
    fun setCornerRadius(@Px value: Float) = apply { this.elasticLayout.cornerRadius = value }

    @JvmSynthetic
    inline fun setOnClickListener(crossinline block: () -> Unit) = apply {
      setOnClickListener(OnClickListener { block() })
    }

    fun setOnClickListener(value: OnClickListener) = apply {
      this.elasticLayout.setOnClickListener(value)
    }

    @JvmSynthetic
    inline fun setOnFinishListener(crossinline block: () -> Unit) = apply {
      setOnFinishListener(ElasticFinishListener { block() })
    }

    fun setOnFinishListener(value: ElasticFinishListener) = apply {
      this.elasticLayout.setOnFinishListener(value)
    }

    fun build() = this.elasticLayout
  }
}
