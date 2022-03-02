package com.example.legoev3android.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.legoev3android.utils.getDistance
import kotlin.math.*

class Joystick {

    private val outerCircleRadius: Float = 150f
    private val innerCircleRadius: Float = 50f
    private var innerCirclePositionX = 0f
    private var innerCirclePositionY = 0f

    // Initialized in draw(canvas: Canvas)
    private var centerX = 0f
    private var centerY = 0f

    fun draw(canvas: Canvas) {
        centerX = canvas.width / 2f
        centerY = canvas.height / 2f
        drawOuterCircle(canvas)
        drawInnerCircle(canvas)
    }

    // Receive user touch input
    fun update(x: Float, y: Float) {
        // If the new value is within the joystick
        if (x in -outerCircleRadius..outerCircleRadius &&
            y in -outerCircleRadius..outerCircleRadius
        ) {
            innerCirclePositionX = x
            innerCirclePositionY = y
        }
        // If new value is within an acceptable range of the joystick
        else if (
            x in (-outerCircleRadius - 50f)..(outerCircleRadius + 50f) &&
            y in (-outerCircleRadius - 50f)..(outerCircleRadius + 50f)
        ) {
            // https://en.wikipedia.org/wiki/Atan2
            // Get edge value of outer circle in direction of touch
            var value = atan2(y, x).toDouble()
            innerCirclePositionX = outerCircleRadius * cos(value).toFloat()
            innerCirclePositionY = outerCircleRadius * sin(value).toFloat()
        }
    }

    // Get power as function of distance of inner circle to center relative to outer circle radius
    fun getPower() =
        (abs(
            Float.getDistance(
                centerX,
                centerY,
                innerCirclePositionX + centerX,
                innerCirclePositionY + centerY
            )
        ) / outerCircleRadius) * 100f

    // Get degree from 0 to 360 of innerCircle center vector relative to outerCircle center
    fun getDegree(): Double {
        val degrees =
            Math.toDegrees(atan2(-1f * innerCirclePositionY, innerCirclePositionX).toDouble())
        return (degrees + 360) % 360
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
        val innerPaint = Paint()
        innerPaint.color = Color.RED

        canvas.drawCircle(
            centerX + (innerCirclePositionX * 0.8f),
            centerY + (innerCirclePositionY * 0.8f),
            innerCircleRadius,
            innerPaint
        )
    }
}