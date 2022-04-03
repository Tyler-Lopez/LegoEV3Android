package com.example.legoev3android.utils

import com.example.legoev3android.R

enum class ConnectionStatus(val stringId: Int, val imageId: Int) {
    DISCONNECTED(R.string.controller_disconnected, R.drawable.teal_error),
    ERROR(R.string.controller_error, R.drawable.teal_error),
    CONNECTING(R.string.controller_connecting, R.drawable.teal_loading),
    CONNECTED(R.string.controller_connected, R.drawable.teal_error)
}