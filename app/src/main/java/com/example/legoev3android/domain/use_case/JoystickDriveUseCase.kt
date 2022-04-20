package com.example.legoev3android.domain.use_case

import androidx.lifecycle.LifecycleCoroutineScope
import com.example.legoev3android.services.MyBluetoothService
import com.example.legoev3android.ui.views.JoystickView
import com.example.legoev3android.utils.Motor
import com.example.legoev3android.utils.MotorCommandFactory
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

class JoystickDriveUseCase {

    @Volatile
    private var isRunning = false

    @Volatile
    private var power = 0f

    @Volatile
    private var degree = 0f

    suspend fun beginJoystickDrive(
        bluetoothService: MyBluetoothService,
        lifecycleCoroutineScope: LifecycleCoroutineScope,
        flows: Pair<StateFlow<Float>, StateFlow<Float>>
    ) {

        if (isRunning)
            return

        isRunning = true

        lifecycleCoroutineScope.launchWhenStarted {
            flows.first.collectLatest {
                power = it
            }
        }
        lifecycleCoroutineScope.launchWhenStarted {
            flows.second.collectLatest {
                degree = it
            }
        }

        coroutineScope {
            while (isRunning) {
                if (power != 0f)
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
    }
}