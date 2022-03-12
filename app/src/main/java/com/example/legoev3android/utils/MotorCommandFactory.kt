package com.example.legoev3android.utils


// Return a ByteArray representing a command to a given motor
object MotorCommandFactory {


    // Returns an input relative to degree that is entirely positive or entirely negative of
    // the speed percent
    fun createDriveMovement(
        motor: Motor,
        speedPercent: Int,
        degree: Int
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
        val forwardBackwardsAdjustment =
            if (degree in 181..360)
                -1
            else
                1
        commandList.add((speedPercent * forwardBackwardsAdjustment).toByte())
        commandList.add(0x00) // No STEP 1 - full speed from start
        commandList.add(0x0A) // 10 ms
        commandList.add(0x00) // No STEP 3 - full speed at end
        commandList.add(0x00) // No breaking at end, instead float
        return commandList.toByteArray()
    }

    // Returns an input relative to degree that is entirely positive or entirely negative of
    // the speed percent
    fun createSteerMovement(
        motor: Motor,
        speedPercent: Int,
        side: Side
    ): ByteArray {
        val commandList = mutableListOf<Byte>()
        commandList.add(0x10) // bb
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
        commandList.add((((speedPercent * 0.5f).toInt()).times(if (side == Side.LEFT) 1 else -1)).toByte())
        commandList.add(0x00) // No STEP 1 - full speed from start
        commandList.add(0x82.toByte()) // "Encoded as 2 bytes to follow"?
        commandList.add(0x32.toByte()) // 100 ms in Little Endian
        commandList.add(0x00.toByte())
        commandList.add(0x00) // No STEP 3 - full speed at end
        commandList.add(0x00) // No breaking at end, instead float
        return commandList.toByteArray()
    }


    fun readMotor(
        motor: Motor,
        mode: MotorMode
    ): ByteArray {
        val commandList = mutableListOf<Byte>()
        commandList.add(0x0D)
        commandList.add(0x00)
        commandList.add(0x00)
        commandList.add(0x00)
        commandList.add(Constants.DIRECT_COMMAND_REPLY.toByte())
        commandList.add(0x04) // Reserve 4 bytes
        commandList.add(0x00)
        commandList.add(Constants.opInput_Device.toByte())
        commandList.add(0x1D) // READY_SI
        commandList.add(0x00) // Chain
        commandList.add(motor.port.toByte()) // Access motor port
        commandList.add(mode.command.toByte()) // Speed, degree, rotation
        commandList.add(0x00)
        commandList.add(0x01)  // 1 VALUE RETURNED
        commandList.add(0x60)  // Necessary for return
        return commandList.toByteArray()
    }

    // Returns an input relative to degree suited to maneuver a vehicle with
    // left and right side independent motors, like a tank
    fun createTankMovement(
        motor: Motor,
        isLeft: Boolean,
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
            when (isLeft) {
                true -> {
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
                false -> {
                    when (degree) {
                        in 271..290 -> (290.0 - degree) / 70.0 // Decrease (+) power as approaches 140 degrees
                        in 291..360 -> -1.0 * ((degree - 290.0) / (360.0 - 290.0)) // Increase (-) power as approaches 360 degrees
                        in 0..70 -> -1.0 * ((70.0 - degree) / 70.0) // Decrease (-) power as approaches 45 degrees
                        in 71..89 -> (degree - 70.0) / (89.0 - 70.0) // Increase (+) power as approaches 270 degrees
                        else -> 1.0
                    }
                }
            }
        val forwardBackwardsAdjustment =
            if (degree in 181..360)
                1
            else
                -1
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