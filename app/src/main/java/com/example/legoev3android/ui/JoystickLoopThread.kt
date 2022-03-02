package com.example.legoev3android.ui

import com.example.legoev3android.services.MyBluetoothService
import com.example.legoev3android.utils.Motor
import com.example.legoev3android.utils.MotorCommandFactory

class JoystickLoopThread(
    val bluetoothService: MyBluetoothService,
    private val joystickView: JoystickView
) : Thread() {
    override fun run() {
        super.run()
        while (true) {
            val power = joystickView.getPower()
            val degree = joystickView.getDegree()
        //    println("POWER IS $power")
            if (power > 0f)
                for (motor in Motor.values()) {
                    bluetoothService
                        .moveMotor(
                            MotorCommandFactory
                                .create(
                                    motor = motor,
                                    speedPercent = power.toInt(),
                                    degree = degree.toInt()
                                )
                        )
                }
            sleep(15)
        }
    }
}