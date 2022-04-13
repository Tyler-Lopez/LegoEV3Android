package com.example.legoev3android.domain.use_case

import com.example.legoev3android.services.MyBluetoothService
import com.example.legoev3android.utils.BatteryCommand
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

class MonitorBatteryUseCase {

    @Volatile
    private var isRunning = false

    suspend fun beginMonitoring(
        bluetoothService: MyBluetoothService,
        batteryListener: (Int?) -> Unit
    ) {
        isRunning = true
        coroutineScope {
            while (isRunning) {
                bluetoothService.readBattery(
                    BatteryCommand.createBatteryCommand()
                ) {
                    batteryListener(it?.toInt())
                }
                delay(4000)
            }
        }
    }

    fun stopMonitoring() {
        isRunning = false
    }

}