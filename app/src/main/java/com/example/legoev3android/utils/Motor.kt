package com.example.legoev3android.utils

enum class Motor(val command: Int, val side: Side) {
    A(Constants.MOTOR_A, Side.LEFT),
    B(Constants.MOTOR_B, Side.LEFT),
    C(Constants.MOTOR_C, Side.RIGHT),
    D(Constants.MOTOR_D, Side.RIGHT)
}