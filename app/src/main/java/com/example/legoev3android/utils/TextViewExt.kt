package com.example.legoev3android.utils

import android.graphics.Typeface
import android.widget.TextView

fun TextView.makeLegoText() {
    this.typeface = Typeface.createFromAsset(
        context.assets,
        "legothick.ttf"
    )
}