package ru.relabs.kurjercontroller.utils.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.Button
import androidx.core.graphics.ColorUtils
import org.joda.time.DateTime
import org.joda.time.Seconds
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.TaskItem

/**
 * Created by ProOrange on 29.08.2018.
 */

fun View.setVisible(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun Button.setSelectButtonActive(active: Boolean, isAdditional: Boolean = false) {
    if (active) {
        this.setBackgroundResource(R.drawable.abc_btn_colored_material)
    } else {
        this.setBackgroundResource(R.drawable.abc_btn_default_mtrl_shape)
    }
    backgroundTintList = if (active && isAdditional) {
        ColorStateList.valueOf(resources.getColorCompat(R.color.colorFuchsiaBright))
    } else {
        null
    }
}


private fun getValueAnimator(from: Int, to: Int, view: View?, duration: Int = 250): ValueAnimator {
    return ValueAnimator.ofObject(ArgbEvaluator(), from, to).apply {
        setDuration(duration.toLong())
        addUpdateListener { animator -> view?.setBackgroundColor(animator.animatedValue as Int) }
    }
}

private fun startFlashValueAnimator(view: View, onAnimationEnd: (() -> Unit)? = null) {
    val targetColor = view.resources.getColor(R.color.colorAccent)
    val colorFrom = ColorUtils.setAlphaComponent(targetColor, 0)
    val colorTo = targetColor

    val colorAnimationTo =
        getValueAnimator(colorFrom, colorTo, view, 500)
    val colorAnimationFrom =
        getValueAnimator(colorTo, colorFrom, view, 500)
    onAnimationEnd?.let {
        colorAnimationFrom.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                it()
            }
        })
    }
    colorAnimationTo.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            colorAnimationFrom.start()
        }
    })

    colorAnimationTo.start()
}

fun View.performFlash() {
    startFlashValueAnimator(this)
}

fun Iterable<TaskItem>.placemarkColor(): Int {
    val minCloseTime = filter { it.closeTime != null && !it.isClosed }.maxBy {
        it.closeTime!!
    }
    //If nothing closed in set
    return if (minCloseTime == null) {
        if (any { !it.isClosed }) {
            Color.BLUE
        } else {
            Color.GRAY
        }
        //If something closed - choose color based on min close time
    } else {
        val diff = Seconds.secondsBetween(minCloseTime.closeTime, DateTime()).seconds
        when {
            diff < 1.5 * 60 * 60 -> Color.GREEN
            diff < 3 * 60 * 60 -> Color.YELLOW
            else -> Color.MAGENTA
        }
    }
}