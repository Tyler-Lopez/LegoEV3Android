package com.example.legoev3android.domain.use_case

data class ControllerUseCases(
    val monitorBattery: MonitorBatteryUseCase,
    val joystickDrive: JoystickDriveUseCase,
    val joystickSteer: JoystickSteerUseCase,
    val playNote: PlayNoteUseCase
)
