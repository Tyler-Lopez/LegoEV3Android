package com.example.legoev3android.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.legoev3android.R

class TopBorder(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs) {

    private val shadowPaint = Paint()
    private val strokePaint = Paint()
    private val shadowFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)

    // Invoked by Android and canvas provided
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        strokePaint.color = Color.argb(200, 219, 179, 0)
        shadowPaint.color = Color.argb(80, 20, 20, 20)
        shadowPaint.maskFilter = shadowFilter
        canvas.drawRect(
            0f,
            0f,
            width.toFloat(),
            15f,
            shadowPaint
        )

        canvas.drawRect(
            0f,
            0f,
            width.toFloat(),
            8f,
            strokePaint
        )
    }

}