package com.example.legoev3android.utils


// Return a ByteArray representing a command to a given motor
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

        commandList.add(Constants.opOutput_Time_Speed.toByte()) // cc
        commandList.add(0x00) // cc

        commandList.add(motor.command.toByte()) // Select motors

        commandList.add((0x81).toByte()) // Percent represent
        val degreePowerAdjustment: Double =
            when (motor.side) {
                Side.LEFT -> {
                    // Turn left = Less power on left side
                    when (degree) {
                        // ROBOT SHOULD TURN LEFT TO SOME DEGREE
                        in 91..160 -> (160.0 - degree) / 70.0 // Decrease (+) power as approaches 140 degrees
                        in 161..180 -> -1.0 * (degree - 160.0) / (180.0 - 160.0) // Increase (-) power as approaches 180 degrees
                        in 181..200 -> -1.0 * ((200.0 - degree) / 70.0) // Decrease (-) power as approaches 225 degrees
                        in 201..269 -> (degree - 200.0) / (269.0 - 200.0) // Increase (+) power as approaches 270 degrees
                        else -> 1.0
                    }
                }
                Side.RIGHT -> {
                    when (degree) {
                        in 271..290 -> (290.0 - degree) / 70.0 // Decrease (+) power as approaches 140 degrees
                        in 291..360 -> -1.0 * ((degree - 290.0) / (360.0 - 290.0)) // Increase (-) power as approaches 360 degrees
                        in 0..70 -> -1.0 * ((70.0 - degree) / 70.0) // Decrease (-) power as approaches 45 degrees
                        in 71..89 -> (degree - 70.0) / (89.0 - 70.0) // Increase (+) power as approaches 270 degrees
                        else -> 1.0
                    }
                }
                Side.NONE -> 1.0
            }
        val forwardBackwardsAdjustment =
            if (degree in 181..360)
                -1
            else
                1
        commandList.add(
            (speedPercent * forwardBackwardsAdjustment * degreePowerAdjustment)
                .toInt()
                .toByte()
        )
        commandList.add(0x00) // No STEP 1 - full speed from start

        commandList.add(0x0A) // 10 ms

        commandList.add(0x00) // No STEP 3 - full speed at end

        commandList.add(0x00) // No breaking at end, instead float

        return commandList.toByteArray()
    }
}