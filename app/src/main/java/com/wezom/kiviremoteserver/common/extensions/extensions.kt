package com.wezom.kiviremoteserver.common.extensions

import android.animation.TimeInterpolator
import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator

fun View.animateTranslationY(value: Float, duration: Long, interpolator: TimeInterpolator = DecelerateInterpolator(), alpha: Float? = null) {
    if (alpha != null) {
        this.animate()
                .alpha(alpha)
                .translationY(value)
                .setInterpolator(interpolator)
                .setDuration(duration)
                .start()

    } else {
        this.animate()
                .translationY(value)
                .setInterpolator(interpolator)
                .setDuration(duration)
                .start()
    }
}

fun View.animateTranslationX(value: Float, duration: Long, interpolator: TimeInterpolator = DecelerateInterpolator()) {
    this.animate()
            .translationX(value)
            .setInterpolator(interpolator)
            .setDuration(duration)
            .start()
}

fun View.animateAnimation(applicationContext: Context,
                          animationResource: Int,
                          duration: Long,
                          onAnimationStart: (() -> Unit?)? = null,
                          onAnimationEnd: (() -> Unit?)? = null,
                          interpolator: Interpolator = DecelerateInterpolator()) {

    val anim = AnimationUtils.loadAnimation(applicationContext, animationResource).apply {
        this.duration = duration
        this.interpolator = interpolator
        this.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) { onAnimationStart?.invoke() }
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) { onAnimationEnd?.invoke() }
        })
    }

    this.startAnimation(anim)
}