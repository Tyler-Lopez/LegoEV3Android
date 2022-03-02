package com.example.legoev3android.utils

import java.lang.Math.abs

object MotorCommandFactory {
    fun create(
        motor: Motor,
        speedPercent: Int,
        degree: Int // 90 degrees is forward, 180 is left, 360/0 is right
    ): ByteArray {
        val commandList = mutableListOf<Byte>()
        commandList.add(0x0E) // bb
        commandList.add(0x00) // bb
        commandList.add(0x00) // mm
        commandList.add(0x00) // mm
        commandList.add(Constants.DIRECT_COMMAND_NO_REPLY.toByte()) // tt
        commandList.add(0x00) // hh
        commandList.add(0x00) // hh
        // HANDLE MOVING LEFT
        commandList.add(Constants.opOutput_Time_Speed.toByte()) // cc
        commandList.add(0x00) // cc

        commandList.add(motor.command.toByte()) // Select motors

        commandList.add((0x81).toByte()) // Percent represent
        val degreePowerAdjustment: Double =
            when (motor.side) {
                Side.LEFT -> {
                    // Turn left = Less power on left side
                    if (degree in 91..270) {
                        kotlin.math.abs(180 - degree) / 90.0
                    // Turn right = Full power on left side
                    } else {
                        1.0
                    }
                }
                Side.RIGHT -> {
                    if (degree <= 90 || degree > 270) {
                        val tmp = (if (degree <= 91) 360 else 0) + degree
                        kotlin.math.abs(360 - tmp) / 90.0
                    }
                    else
                        1.0
                }
            }
        val forwardBackwardsAdjustment =
            if (degree in 181..360)
                -1
            else
                1

        commandList.add(
            (speedPercent * forwardBackwardsAdjustment * degreePowerAdjustment).toInt().toByte()
        )
        commandList.add(0x00) // No STEP 1 - full speed from start

        commandList.add(0x0A) // 10 ms

        commandList.add((0x00).toByte())

        commandList.add(0x00) // No break

        return commandList.toByteArray()
    }
}