package com.example.legoev3android.ui.views

import android.content.Context
import android.graphics.*
import com.example.legoev3android.R
import com.example.legoev3android.utils.getDistance
import kotlin.math.*

class Joystick(
    val context: Context
) {

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
    fun getDegree(): Float {
        val degrees =
            Math.toDegrees(atan2(-1f * innerCirclePositionY, innerCirclePositionX).toDouble())
        return ((degrees + 360) % 360).toFloat()
    }


    private fun drawOuterCircle(canvas: Canvas) {

        val shadowPaint = Paint()
        shadowPaint.color = Color.argb(80, 20, 20, 20)
        shadowPaint.maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
        canvas.drawCircle(
            centerX,
            centerY,
            outerCircleRadius + 10f,
            shadowPaint
        )
        val mMarker = BitmapFactory.decodeResource(context.resources, R.drawable.outer_joystick_on)
        canvas.drawBitmap(
            Bitmap.createScaledBitmap(mMarker, (outerCircleRadius * 2).toInt(), (outerCircleRadius * 2).toInt(), false),
            centerX  - outerCircleRadius,
            centerY  - outerCircleRadius,
            Paint()
        )
    }

    private fun drawInnerCircle(canvas: Canvas) {
        val shadowPaint = Paint()
        shadowPaint.color = Color.argb(75, 20, 20, 20)
        shadowPaint.maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
        canvas.drawCircle(
            centerX + (innerCirclePositionX * 0.4f) + 5f,
            centerY + (innerCirclePositionY * 0.4f) + 5f,
            outerCircleRadius - 10f,
            shadowPaint
        )

        val mMarker = BitmapFactory.decodeResource(context.resources, R.drawable.joystick_image)
        canvas.drawBitmap(
            mMarker,
            centerX + (innerCirclePositionX * 0.3f) - (mMarker.width / 2f),
            centerY + (innerCirclePositionY * 0.3f) - (mMarker.height / 2f),
            Paint()
        )
    }
}