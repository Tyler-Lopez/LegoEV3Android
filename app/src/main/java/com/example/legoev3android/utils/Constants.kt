package com.example.legoev3android.utils

object Constants {
    const val REQUEST_CODE_BLUETOOTH_PERMISSION = 0
    const val ROBOT_NAME = "EV3A"
    // UUID are case-insensitive
    const val ROBOT_UUID = "00001101-0000-1000-8000-00805F9B34FB"
    const val VERSA_UUID = "0000110a-0000-1000-8000-00805f9b34fb"
    const val STATE_NONE = 0 // Nothing
    const val STATE_LISTEN = 1 // Listening for incoming connection
    const val STATE_CONNECTING = 2 // Initiating ongoing connection
    const val STATE_CONNECTED = 3 // Connected to remote device

    // COMMUNICATION TRANSCRIPTION
    // Motors
    const val MOTOR_A = 0x01
    const val MOTOR_B = 0x02
    const val MOTOR_C = 0x04
    const val MOTOR_D = 0x08

    // 4.0 Direct Commands
    const val DIRECT_COMMAND_REPLY = 0x00
    const val DIRECT_COMMAND_NO_REPLY = 0x80

    // 4.1 Direct Replies
    const val DIRECT_REPLY = 0x02
    const val DIRECT_REPLY_ERROR = 0x04


    // FIRMWARE TRANSCRIPTION



    // 4.7 Program select operations (pg. 45 / 109)
    const val opSelect8 = 0x5C
    const val opSelect16 = 0x5D
    const val opSelect32 = 0x5E
    const val opSelectF = 0x5F

    // 4.8 Input port operations (pg. 46 / 109)
    const val opInput_Device_List = 0x98
    const val opInput_Device = 0x99

    object obInput_Device_CMD {
        const val GET_FORMAT = 0x02
        const val CAL_MINMAX = 0x03
        const val CAL_DEFAULT = 0x04
        const val GET_TYPEMODE = 0x05
        const val GET_SYMBOL = 0x06
        const val CAL_MIN = 0x07
        const val CAL_MAX = 0x08
        const val SETUP = 0x09
        const val CLR_ALL = 0x0A
        const val GET_RAW = 0x0B
        const val GET_CONNECTION = 0x0C

        object CONNECTION_TYPE {
            const val CONN_UNKNOW = 0x6F // Fake
            const val CONN_DAISYCHAIN = 0x75 // Daisy Chained
            const val CONN_NXT_COLOR = 0x76 // NXT Color Sensor
            const val CONN_NXT_DUMB = 0x77 // NXT Analog Sensor
            const val CONN_NXT_IIC = 0x78 // NXT IIC Sensor
            const val CONN_INPUT_DUMB = 0x79 // EV3 Input Device w/ ID Resistor
            const val CONN_INPUT_UART = 0x7A // EV3 Input UART Sensor
            const val CONN_OUTPUT_DUMB = 0x7B // EV3 Output Device with ID resistor
            const val CONN_OUTPUT_INTELLIGENT = 0x7C // EV3 Output Device with Communication
            const val CONN_OUTPUT_TACHO = 0x7D // EV3 Tacho motor with ID resistor
            const val CONN_NONE = 0x7E // Port empty or not available
            const val CONN_ERROR = 0x7F // Port not empty and type is invalid
        }

        const val STOP_ALL = 0x0D // Stop all devices
        const val GET_NAME = 0x15
        const val GET_MODENAME = 0x16
        const val GET_FIGURES = 0x18
        const val GET_CHANGES = 0x19
        const val CLR_CHANGES = 0x1A
        const val READY_PCT = 0x1B
        const val READY_RAW = 0x1C
        const val READY_SI = 0x1D
        const val GET_MINMAX = 0x1E
        const val GET_BUMPS = 0x1F
    }

    const val opInput_Read = 0x9A
    const val opInput_Test = 0x9B
    const val opInput_Ready = 0x9C
    const val opInput_ReadSI = 0x9D
    const val opInput_ReadExt = 0x9E
    const val opInput_Write = 0x9F

    // 4.9 Output port operations (pg. 53 / 109)
    const val opOutput_Set_Type = 0xA1
    const val opOutput_Reset = 0xA2
    const val opOutput_Stop = 0xA3
    const val opOutput_Power = 0xA4
    const val opOutput_Speed = 0xA5
    const val opOutput_Start = 0xA6
    const val opOutput_Polarity = 0xA7
    const val opOutput_Read = 0xA8
    const val opOutput_Test = 0xA9
    const val opOutput_Ready = 0xAA
    const val opOutput_Step_Power = 0xAC
    const val opOutput_Time_Power = 0xAD
    const val opOutput_Step_Speed = 0xAE
    const val opOutput_Time_Speed = 0xAF
    const val opOutput_Step_Sync = 0xB0
    const val opOutput_Time_Sync = 0xB1
    const val opOutput_Clr_Count = 0xB2
    const val opOutput_Get_Count = 0xB3
    const val opOutput_Prg_Stop = 0xB4


    // 4.10 Sound operations (pg. 59 / 109)
    const val opSound = 0x94

    object opSound_CMD {
        const val BREAK = 0x01
        const val PLAY = 0x02
        const val REPEAT = 0x03
    }

    const val opSound_Test = 0x95
    const val opSound_Ready = 0x96

}