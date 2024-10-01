package com.draw.ultis

import android.view.View
import android.view.ViewGroup
import androidx.transition.TransitionManager

object ViewControl {
    fun View.visible() {
        visibility = View.VISIBLE
    }

    fun View.gone() {
        visibility = View.GONE
    }

    fun View.invisible() {
        visibility = View.INVISIBLE
    }

    fun ViewGroup.actionAnimation() {
        TransitionManager.beginDelayedTransition(this)
    }

    fun View.onSingleClick(
        throttleDelay: Long = 800,
        onClick: (View) -> Unit
    ) {
        setOnClickListener {
            onClick(this)
            isClickable = false
            postDelayed({ isClickable = true }, throttleDelay)
        }
    }
}