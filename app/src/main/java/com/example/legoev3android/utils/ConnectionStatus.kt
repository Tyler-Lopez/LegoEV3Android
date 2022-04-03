package com.example.legoev3android.utils

import android.view.View
import com.example.legoev3android.R
import com.example.legoev3android.databinding.TextLargeBoardBinding

enum class ConnectionStatus(
    val stringId: Int,
    val imageId: Int,
) {
    DISCONNECTED(R.string.controller_disconnected, R.drawable.teal_bluetooth_off),
    ERROR(R.string.controller_error, R.drawable.teal_error),
    CONNECTING(R.string.controller_connecting, R.drawable.teal_loading),
    CONNECTED(R.string.controller_connected, R.drawable.darkgrey_white_stroke_battery_100);
}