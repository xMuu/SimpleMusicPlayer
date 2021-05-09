package dev.xmuu.smp.util

import dev.xmuu.smp.App

fun dp2px(dp: Float): Float = dp * App.context.resources.displayMetrics.density

fun Int.dp(): Int {
    return dp2px(this.toFloat()).toInt()
}