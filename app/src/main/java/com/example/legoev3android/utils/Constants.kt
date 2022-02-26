package com.example.legoev3android.utils

object Constants {
    const val REQUEST_CODE_BLUETOOTH_PERMISSION = 0
    const val ROBOT_NAME = "EV3A"
    // UUID are case-insensitive
    const val ROBOT_UUID = "00001101-0000-1000-8000-00805F9B34FB"
    const val VERSA_UUID = "0000110a-0000-1000-8000-00805f9b34fb"
    const val STATE_NONE = 0 // Nothing
    const val STATE_LISTEN = 1 // Listening for incoming connection
    const val STATE_CONNECTING = 2 // Initiating ongoing connection
    const val STATE_CONNECTED = 3 // Connected to remote device

}