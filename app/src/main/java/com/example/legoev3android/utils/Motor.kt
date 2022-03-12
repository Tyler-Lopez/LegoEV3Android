package com.example.legoev3android.utils

enum class Motor(val command: Int, val port: Int) {
    A(Constants.MOTOR_A, Constants.MOTOR_PORT_A),
    B(Constants.MOTOR_B, Constants.MOTOR_PORT_B),
    C(Constants.MOTOR_C, Constants.MOTOR_PORT_C),
    D(Constants.MOTOR_D, Constants.MOTOR_PORT_D)
}