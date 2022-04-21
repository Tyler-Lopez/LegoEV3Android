package com.example.legoev3android.domain.use_case

import androidx.lifecycle.LifecycleCoroutineScope
import com.example.legoev3android.services.MyBluetoothService
import com.example.legoev3android.ui.views.JoystickView
import com.example.legoev3android.utils.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking
import kotlin.math.abs

class JoystickSteerUseCase {

    @Volatile
    private var isRunning = false

    @Volatile
    private var power = 0f

    @Volatile
    private var degree = 0f

    suspend fun beginJoystickSteer(
        bluetoothService: MyBluetoothService,
        lifecycleCoroutineScope: LifecycleCoroutineScope,
        flows: Pair<StateFlow<Float>, StateFlow<Float>>
    ) {

        if (isRunning)
            return

        isRunning = true



        lifecycleCoroutineScope.launchWhenStarted {
            flows.first.collectLatest {
                println("here in first flow power is $it")
                power = it
            }
        }
        lifecycleCoroutineScope.launchWhenStarted {
            flows.second.collectLatest {
                println("here $it")
                degree = it
            }
        }
        coroutineScope {

            var stalledSide: Side = Side.NONE
            var leftMax: Float = findMaximum(bluetoothService, true)
            var rightMax: Float = findMaximum(bluetoothService, false)

            // E.g. for 10 & 25 = 15
            val centerDegreeDifference =
                maxOf(rightMax, leftMax).minus(minOf(rightMax, leftMax))

            // This loop is what actually controls driving, based on left and right max
            while (isRunning) {
                val side: Side =
                    if (power < 5f) Side.NONE
                    else
                        when (degree) {
                            in 120f..240f -> Side.LEFT
                            in 0f..60f, in 300f..360f -> Side.RIGHT
                            else -> Side.NONE
                        }

                // Normalize degree then denormalize to the centerDegreeDifference
                val targetDegrees: Float =
                    if (side == Side.NONE)
                    // If we want to generally go to the center, just ALWAYS go to the center
                    // E.g., if center is 5, never target 0 always go to 5
                        (leftMax - (centerDegreeDifference / 2f))
                    else
                        when (degree) {
                            in 0f..180f -> leftMax - (((180f - degree) / 180f) * centerDegreeDifference)
                            else -> (centerDegreeDifference * ((360f - degree) / 180f)) + rightMax
                        }

                // If the motor is stalled in this direction
                if (side == stalledSide && side != Side.NONE) {
                    // Check back in 15 ms
                    delay(15)
                } else {
                    // Read the current motor degree, and send appropriate response
                    //    runBlocking {
                    bluetoothService
                        .read(
                            MotorCommandFactory
                                .readMotor(
                                    Motor.A,
                                    MotorMode.DEGREE
                                )
                        )
                        {
                            // It would return null in event of invalid connection
                            // May be able to be removed later
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

                                // Are we already where we want to be?
                                // Replace this in the future with some sort of scaling unit, not 5 as a constant - that's bad
                                if (kotlin.math.abs(targetDegrees - it) > 5) {
                                    val steeringPower =
                                        (kotlin.math.abs(targetDegrees - it)
                                                / centerDegreeDifference) *
                                                when {
                                                    targetDegrees < it -> -40
                                                    else -> 40
                                                }

                                    //       println("Steering power is $steeringPower")


                                    if (stalledSide != side || side == Side.NONE) {
                                        // Attempt movement for 50 ms
                                        bluetoothService
                                            .driveMotor(
                                                MotorCommandFactory
                                                    .createSteerMovement(
                                                        motor = Motor.A,
                                                        speedPercent = steeringPower.toInt()
                                                    ),
                                                true
                                            )
                                    }
                                } // End block if not at destination
                                Thread.sleep(20)
                            }
                        }
                }
                //     }
            }
        }
    }

    private fun findMaximum(
        bluetoothService: MyBluetoothService,
        isLeft: Boolean
    ): Float {

        var lastSteerDegree = Float.MAX_VALUE
        var max = 0f
        var maxFound = false

        while (isRunning) {

            if (maxFound)
                break

            // Attempt a move left
            bluetoothService
                .driveMotor(
                    MotorCommandFactory
                        .createSteerMovement(
                            motor = Motor.A,
                            speedPercent = if (isLeft) 50 else -50
                        ),
                    true
                )

            // Wait for move to complete
            Thread.sleep(40)
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
                                (abs(
                                    lastSteerDegree - it
                                )).toInt()
                            lastSteerDegree = it
                            if (difference < 5) {
                                max = it
                                maxFound = true
                            }
                        }
                    }
            }
        }
        return max
    }

    fun stopJoystickSteer() {
        isRunning = false
    }
}