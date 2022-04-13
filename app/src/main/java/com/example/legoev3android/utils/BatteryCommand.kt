package com.example.legoev3android.utils

object BatteryCommand {

    fun createBatteryCommand(): ByteArray {
        val buffer = ByteArray(10)
        buffer[0] = (0x08).toByte()
        buffer[1] = 0
        buffer[2] = 0
        buffer[3] = 0
        buffer[4] = Constants.DIRECT_COMMAND_REPLY.toByte()
        buffer[5] = 0x01 // 4 bytes?
        buffer[6] = 0x00
        buffer[7] = 0x81.toByte() // opUI_Read
        buffer[8] = 0x12 // CMD: GET_LBATT
        buffer[9] = 0x60  // Necessary for return
        return buffer
    }
}