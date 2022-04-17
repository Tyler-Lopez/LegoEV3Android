package com.example.legoev3android.domain.use_case

import com.example.legoev3android.services.MyBluetoothService
import com.example.legoev3android.ui.views.JoystickView
import com.example.legoev3android.utils.Motor
import com.example.legoev3android.utils.MotorCommandFactory
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

class JoystickDriveUseCase {

    @Volatile
    private var isRunning = false

    private var powerStateFlow: StateFlow<Float>? = null

    suspend fun beginJoystickDrive(
        bluetoothService: MyBluetoothService,
        powerStateFlow: StateFlow<Float>
    ) {
        if (isRunning)
            return

        this.powerStateFlow = powerStateFlow

        isRunning = true
        coroutineScope {
            while (isRunning) {
                val power = joystickView.getPower()
                val degree = joystickView.getDegree()
                if (power > 0f)
                /* DRIVE MOTORS */
                // Motor B and C control movement
                    for (motor in listOf(Motor.B, Motor.C)) {
                        bluetoothService
                            .driveMotor(
                                MotorCommandFactory
                                    .createDriveMovement(
                                        motor = motor,
                                        speedPercent = power.toInt(),
                                        degree = degree.toInt()
                                    )
                            )
                    }
                delay(10)
            }
        }
    }

    fun stopJoystickDrive() {
        isRunning = false
        powerStateFlow = null
    }
}