package com.example.legoev3android.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import kotlin.math.abs
import kotlin.math.hypot

// https://www.raywenderlich.com/142-android-custom-view-tutorial
class JoystickView(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs) {

    private val joystick: Joystick = Joystick()
    private lateinit var canvas: Canvas

    // Set in onDraw
    private var centerX = 0f
    private var centerY = 0f

    // Invoked by Android and canvas provided
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        centerX = width / 2f
        centerY = height / 2f
        this.canvas = canvas
        joystick.draw(canvas)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return when (event?.action) {
            ACTION_DOWN -> {
                joystick.update(event.x - centerX, event.y - centerY)
                update()
                true
            }
            ACTION_MOVE -> {
                    joystick.update(event.x - centerX, event.y - centerY)
                    update()
                true
            }
            ACTION_UP -> {
                joystick.update(0f, 0f)
                update()
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    fun update() {
        invalidate() // Force view to redraw itself (not the best way to do this?)
    }

    fun getPower() = joystick.getPower()
    fun getDegree() = joystick.getDegree()

}