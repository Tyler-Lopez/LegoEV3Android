package com.example.legoev3android.utils

object MotorCommandFactory {
    fun create(
        motor: Motor,
        speedPercent: Int,
        degree: Int
    ): ByteArray {
        val commandList = mutableListOf<Byte>()
        commandList.add(0x12) // bb
        commandList.add(0x00) // bb
        commandList.add(0x00) // mm
        commandList.add(0x00) // mm
        commandList.add(Constants.DIRECT_COMMAND_NO_REPLY.toByte()) // tt
        commandList.add(0x00) // hh
        commandList.add(0x00) // hh
        // HANDLE MOVING LEFT
        commandList.add(Constants.opOutput_Step_Speed.toByte()) // cc
        commandList.add(0x00) // cc

        commandList.add(motor.command.toByte()) // Select motors

        commandList.add((0x81).toByte()) // Percent represent
        commandList.add(
            ((if (degree == 270) -1 else 1) * speedPercent)
                .toByte()
        ) // Speed
        commandList.add(0x00) // No STEP 1 - full speed from start

        commandList.add((0x82).toByte())
        commandList.add((0x84).toByte())
        commandList.add((0x03).toByte())

        commandList.add((0x82).toByte())
        commandList.add((0xB4).toByte())
        commandList.add((0x00).toByte())

        commandList.add(1.toByte())

        return commandList.toByteArray()
    }
}