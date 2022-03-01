package com.example.legoev3android.utils

object MotorUtil {
    fun generateBytes():ByteArray {

        // A byte is 8 bits
        // Total is 20 - 2 = 18

        // 0x1234 => 16 bit, means 2 bytes or 2x 8-bit byte
        // Mindstorm uses little endian

        // 20 decimal size => 0x0012
        // because 20-2 = 18
        // 18 in base 10 is 18
        // 18 in base 16 (hexadecimal) is 12
        val mmBuffer = ByteArray(20) // 0x12 Command Length
        // [0] & [1] represent the length of 0x0012
        // 18 in [0] and 0 in [1] because little endian (twist)
        mmBuffer[0] = (20 - 2).toByte() // Tells program to expect 18 instructions?
        mmBuffer[1] = 0
        // [2] and [3] is message counter
        // Actually means nothing?
        mmBuffer[2] = 0
        mmBuffer[3] = 0
        // Command type
        // 0x00 = reply required
        // 0x80 = reply not required
        mmBuffer[4] = Constants.DIRECT_COMMAND_NO_REPLY.toByte()
        // [5] and [6] are reservation of global variables?
        // "at this time make zero"
        mmBuffer[5] = 0
        mmBuffer[6] = 0
        // [7] to the end is the program
        // 4.2.2 start motor b & c forward
        mmBuffer[7] = Constants.opOutput_Step_Speed.toByte()
        mmBuffer[8] = 0 // Layer_0
        mmBuffer[9] = (0x06).toByte() // 0110 for B and C motors
        mmBuffer[10] = (0x81).toByte() // Percentage indicated by 0x81
        mmBuffer[11] = (0x32).toByte() // Speed 50 in hexadecimal?
        mmBuffer[12] = 0
        mmBuffer[13] = (0x82).toByte() // 180 in hexadecimal
        mmBuffer[14] = (0x84).toByte()
        mmBuffer[15] = (0x03).toByte()
        mmBuffer[16] = (0x82).toByte()
        mmBuffer[17] = (0xB4).toByte()
        mmBuffer[18] = (0x00).toByte()
        mmBuffer[19] = 1
        return mmBuffer
    }
}