package com.dillon.supercam.utils.view.shimmer

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Build
import android.view.View

class Shimmer {
    var repeatCount: Int
        private set
    var duration: Long
        private set
    var startDelay: Long
        private set
    var direction: Int
        private set
    var animatorListener: AnimatorListener? = null
        private set
    private var animator: ObjectAnimator? = null

    fun setRepeatCount(repeatCount: Int): Shimmer {
        this.repeatCount = repeatCount
        return this
    }

    fun setDuration(duration: Long): Shimmer {
        this.duration = duration
        return this
    }

    fun setStartDelay(startDelay: Long): Shimmer {
        this.startDelay = startDelay
        return this
    }

    fun setDirection(direction: Int): Shimmer {
        require(!(direction != ANIMATION_DIRECTION_LTR && direction != ANIMATION_DIRECTION_RTL)) { "The animation direction must be either ANIMATION_DIRECTION_LTR or ANIMATION_DIRECTION_RTL" }
        this.direction = direction
        return this
    }

    fun setAnimatorListener(animatorListener: AnimatorListener?): Shimmer {
        this.animatorListener = animatorListener
        return this
    }

    fun <V> start(shimmerView: V) where V : View?, V : ShimmerViewBase? {
        if (isAnimating) {
            return
        }
        val animate = Runnable {
            shimmerView!!.isShimmering = true
            var fromX = 0f
            var toX = shimmerView.width.toFloat()
            if (direction == ANIMATION_DIRECTION_RTL) {
                fromX = shimmerView.width.toFloat()
                toX = 0f
            }
            animator = ObjectAnimator.ofFloat(shimmerView, "gradientX", fromX, toX)
            animator?.repeatCount = repeatCount
            animator?.duration = duration
            animator?.startDelay = startDelay
            animator?.addListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    shimmerView.isShimmering = false
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        shimmerView.postInvalidate()
                    } else {
                        shimmerView.postInvalidateOnAnimation()
                    }
                    animator = null
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            if (animatorListener != null) {
                animator?.addListener(animatorListener)
            }
            animator?.start()
        }
        if (!shimmerView!!.isSetUp) {
            shimmerView.setAnimationSetupCallback { animate.run() }
        } else {
            animate.run()
        }
    }

    fun pause() {
        if (animator != null) {
            animator!!.pause()
        }
    }
    fun cancel() {
        if (animator != null) {
            animator!!.cancel()
        }
    }

    val isAnimating: Boolean
        get() = animator != null && animator!!.isRunning

    companion object {
        const val ANIMATION_DIRECTION_LTR = 0
        const val ANIMATION_DIRECTION_RTL = 1
        private const val DEFAULT_REPEAT_COUNT = ValueAnimator.INFINITE
        private const val DEFAULT_DURATION: Long = 1000
        private const val DEFAULT_START_DELAY: Long = 0
        private const val DEFAULT_DIRECTION = ANIMATION_DIRECTION_LTR
    }

    init {
        repeatCount = DEFAULT_REPEAT_COUNT
        duration = DEFAULT_DURATION
        startDelay = DEFAULT_START_DELAY
        direction = DEFAULT_DIRECTION
    }
}