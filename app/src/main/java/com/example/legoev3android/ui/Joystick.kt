package com.example.legoev3android.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.legoev3android.utils.getDistance
import kotlin.math.*

class Joystick {

    private val outerCircleRadius: Float = 150f
    val innerCircleRadius: Float = 50f
    private var innerCirclePositionX = 0f
    private var innerCirclePositionY = 0f

    // Set in draw()
    var centerX = 0f
    var centerY = 0f

    fun draw(canvas: Canvas) {
        centerX = canvas.width / 2f
        centerY = canvas.height / 2f

        drawOuterCircle(canvas)
        drawInnerCircle(canvas)
    }

    fun update(x: Float, y: Float) {
        // If the new value is within the circle
        if (x in -outerCircleRadius..outerCircleRadius &&
            y in -outerCircleRadius..outerCircleRadius
        ) {
            innerCirclePositionX = x
            innerCirclePositionY = y
        } else if (
            x in (-outerCircleRadius - 50f)..(outerCircleRadius + 50f) &&
            y in (-outerCircleRadius - 50f)..(outerCircleRadius + 50f)
        ) {
            // https://en.wikipedia.org/wiki/Atan2
            var value = atan2(y, x).toDouble()
            innerCirclePositionX = outerCircleRadius * cos(value).toFloat()
            innerCirclePositionY = outerCircleRadius * sin(value).toFloat()
        }
    }

    fun getPower() =
        (maxOf(abs(innerCirclePositionX), abs(innerCirclePositionY)) / outerCircleRadius) * 100f

    fun getDegree(): Double {
        var value =
            Math.toDegrees(atan2(-1f * innerCirclePositionY, innerCirclePositionX).toDouble())
        return (value + 360) % 360
    }

    private fun drawOuterCircle(canvas: Canvas) {

        val outerPaint = Paint()
        outerPaint.color = Color.BLACK


        canvas.drawCircle(
            centerX,
            centerY,
            outerCircleRadius,
            outerPaint
        )
    }

    private fun drawInnerCircle(canvas: Canvas) {
        //    println("Drawing inner circle position x is $innerCirclePositionX")
        val innerPaint = Paint()
        innerPaint.color = Color.RED


        canvas.drawCircle(
            centerX + (innerCirclePositionX * 0.8f),
            centerY + (innerCirclePositionY * 0.8f),
            innerCircleRadius,
            innerPaint
        )
    }

    fun isPressed(pointX: Float, pointY: Float) =
        Float.getDistance(centerX, centerY, pointX, pointY) <= outerCircleRadius
}