package com.example.legoev3android.utils

import kotlin.math.abs
import kotlin.math.hypot

fun Float.Companion.getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val ac = abs(y2 - y1).toDouble()
    val cb = abs(x2 - x1).toDouble()
    return hypot(ac, cb).toFloat()
}