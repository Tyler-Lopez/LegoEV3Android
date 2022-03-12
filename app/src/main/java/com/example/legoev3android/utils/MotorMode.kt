package com.example.legoev3android.utils

enum class MotorMode(val command: Int) {
    DEGREE(Constants.EV3_MEDIUM_MOTOR_DEGREE),
    ROTATION(Constants.EV3_MEDIUM_MOTOR_ROTATION),
    POWER(Constants.EV3_MEDIUM_MOTOR_POWER),
}