package com.example.legoev3android.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// https://www.raywenderlich.com/142-android-custom-view-tutorial
class JoystickView(
    context: Context,
    attrs: AttributeSet,
) : View(context, attrs) {

    private val joystick: Joystick = Joystick(context)
    private lateinit var canvas: Canvas

    // Set in onDraw
    private var centerX = 0f
    private var centerY = 0f

    private var _powerStateFlow = MutableStateFlow(0f)
    val powerStateFlow = _powerStateFlow.asStateFlow()

    private var _degreeStateFlow = MutableStateFlow(0f)
    val degreeStateFlow = _degreeStateFlow.asStateFlow()

    // Invoked by Android and canvas provided
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        centerX = width / 2f
        centerY = height / 2f
        this.canvas = canvas
        joystick.draw(canvas)
    }


    @SuppressLint("ClickableViewAccessibility")
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

    // Force view to redraw itself (not the best way to do this?)
    private fun update() {
        _powerStateFlow.value = joystick.getPower()
        _degreeStateFlow.value = joystick.getDegree()
        invalidate()
    }


}