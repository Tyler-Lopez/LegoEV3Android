package com.example.legoev3android.ui.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class BatteryView(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs) {

    private val battery = Battery(context)

    // Invoked by Android and canvas is provided
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }



}