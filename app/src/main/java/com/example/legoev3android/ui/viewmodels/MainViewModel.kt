package com.example.legoev3android.ui.viewmodels

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legoev3android.services.MyBluetoothService
import com.example.legoev3android.ui.JoystickView
import com.example.legoev3android.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Float.min
import java.lang.Math.abs
import java.util.*
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private var mBluetoothAdapter: BluetoothAdapter? = null

    // Come back to this for reading other stuff, like ports
    /*
    fun registerForDeviceInformation(
        bluetoothService: MyBluetoothService,
        callback: (String) -> Unit
    ) {
        println("Invoked register for device information")
        // https://developer.android.com/topic/libraries/architecture/coroutines
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                while (true) {
                    println("Reading information")

                    delay(1000)
                }
            }
        }
    }

     */

    inner class JoystickDriveThread(
        private val bluetoothService: MyBluetoothService,
        private val joystickView: JoystickView
    ) : Thread() {
        override fun run() {
            super.run()
            while (true) {
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

                sleep(15)
            }
        }
    }

    inner class JoystickSteerThread(
        private val bluetoothService: MyBluetoothService,
        private val joystickView: JoystickView
    ) : Thread() {

        private var stalledSide: Side = Side.NONE
        private var lastSide: Side = Side.NONE
        private var leftMax: Float = 0f
        private var rightMax: Float = 0f

        /*

        STEERING

        Find left and right maximum values automatically

        Then, these are used to prevent motor stalling

         */
        override fun run() {
            super.run()
            var lastSteerDegree = Float.MAX_VALUE

            // FIND LEFT MAXIMUM
            var leftFound = false
            while (true) {
                if (leftFound) {
                    lastSteerDegree = Float.MAX_VALUE
                    break
                }
                // Attempt a move left
                bluetoothService
                    .driveMotor(
                        MotorCommandFactory
                            .createSteerMovement(
                                motor = Motor.A,
                                speedPercent = 50
                            )
                    )
                // Wait for move to complete
                sleep(40)
                // Run a blocking call to receive input on current degrees
                runBlocking {
                    bluetoothService
                        .read(
                            MotorCommandFactory
                                .readMotor(
                                    Motor.A,
                                    MotorMode.DEGREE
                                )
                        )
                        {
                            if (it != null) {
                                val difference: Int =
                                    (kotlin.math.abs(
                                        lastSteerDegree - it
                                    )).toInt()
                                lastSteerDegree = it
                                if (difference < 5) {
                                    leftMax = it
                                    leftFound = true
                                }
                            }
                        }
                }
            }
            println("Left maximum found to be $leftMax")
            // FIND RIGHT MAXIMUM
            var rightFound = false
            while (true) {
                if (rightFound) {
                    lastSteerDegree = Float.MAX_VALUE
                    break
                }
                // Attempt a move left
                bluetoothService
                    .driveMotor(
                        MotorCommandFactory
                            .createSteerMovement(
                                motor = Motor.A,
                                speedPercent = -50
                            )
                    )
                // Wait for move to complete
                sleep(40)
                // Run a blocking call to receive input on current degrees
                runBlocking {
                    bluetoothService
                        .read(
                            MotorCommandFactory
                                .readMotor(
                                    Motor.A,
                                    MotorMode.DEGREE
                                )
                        )
                        {
                            if (it != null) {
                                val difference: Int =
                                    (kotlin.math.abs(
                                        lastSteerDegree - it
                                    )).toInt()
                                lastSteerDegree = it
                                if (difference < 5) {
                                    rightMax = it
                                    rightFound = true
                                }
                            }
                        }
                }
            }
            println("Left max found as $leftMax Right max found as $rightMax")

            // This represents exact center
            // E.g. for 10 & 25 = 17.5
            val centerDegree = leftMax + rightMax / 2f
            // E.g. for 10 & 25 = 15
            val centerDegreeDifference = maxOf(rightMax, leftMax).minus(minOf(rightMax, leftMax))

            // This loop is what actually controls driving, based on left and right max
            while (true) {
                val degree = joystickView.getDegree().toInt()
                val side: Side? =
                    when (degree) {
                        in 120..240 -> Side.LEFT
                        in 0..60, in 300..360 -> Side.RIGHT
                        else -> null
                    }

                // Normalize degree then denormalize to the centerDegreeDifference
                val targetDegrees: Float =
                    if (side == Side.NONE || joystickView.getPower() == 0f)
                    // If we want to generally go to the center, just ALWAYS go to the center
                    // E.g., if center is 5, never target 0 always go to 5
                        centerDegree
                    else
                        when (degree) {
                            in 0..180 -> leftMax - ((180f - degree) / 180f) * (centerDegreeDifference)
                            else -> (centerDegreeDifference * ((360f - degree) / 180f)) + rightMax
                        }

                println("Left max is $leftMax, Right max is $rightMax, user attempting to move towards $targetDegrees")


                // If the motor is stalled in this direction
                if (side == stalledSide) {
                    // Check back in 15 ms
                    println("here")
                    sleep(15)
                } else {
                    // Read the current motor degree
                    runBlocking {
                        bluetoothService
                            .read(
                                MotorCommandFactory
                                    .readMotor(
                                        Motor.A,
                                        MotorMode.DEGREE
                                    )
                            )
                            {
                                if (it != null) {
                                    // Reading for a stall...
                                    stalledSide = when {
                                        kotlin.math.abs(it - leftMax) < 5 -> {
                                            Side.LEFT
                                        }
                                        kotlin.math.abs(it - rightMax) < 5 -> {
                                            Side.RIGHT
                                        }
                                        else -> Side.NONE
                                    }

                                    println("We are currently at $it, target is $targetDegrees")
                                    // Are we already where we want to be?
                                    if (kotlin.math.abs(targetDegrees - it) == 0f) {
                                        sleep(15)
                                    } else {
                                        val steeringPower =
                                            (kotlin.math.abs(targetDegrees - it)
                                                    / centerDegreeDifference) *
                                                    when {
                                                        targetDegrees < it -> -40
                                                        else -> 40
                                                    }

                                        println("Steering power is $steeringPower")



                                        if (stalledSide != side || side == Side.NONE) {
                                            // Attempt movement for 50 ms
                                            bluetoothService
                                                .driveMotor(
                                                    MotorCommandFactory
                                                        .createSteerMovement(
                                                            motor = Motor.A,
                                                            speedPercent = steeringPower.toInt()
                                                        )
                                                )
                                            sleep(40)
                                        }
                                    }
                                }
                            }
                    }
                }
            }
        }
    }
}