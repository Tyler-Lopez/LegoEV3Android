package com.example.legoev3android.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legoev3android.domain.use_case.ControllerUseCases
import com.example.legoev3android.services.MyBluetoothService
import com.example.legoev3android.ui.views.JoystickView
import com.example.legoev3android.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    controllerUseCases: ControllerUseCases
) : ViewModel() {

    var connectionStatus = ConnectionStatus.DISCONNECTED

    // EXECUTE BATTERY MONITOR USE CASE
    private val monitorBattery = controllerUseCases.monitorBattery
    fun beginMonitorBattery(
        bluetoothService: MyBluetoothService,
        batteryCallback: (Int) -> Unit
    ) {
        viewModelScope.launch {
            coroutineScope {
                monitorBattery.beginMonitoring(
                    bluetoothService
                ) {
                    println("Here, $it")
                    batteryCallback(it ?: -1)
                }
            }
        }
    }
    fun stopMonitorBattery() = monitorBattery.stopMonitoring()

    // EXECUTE JOYSTICK DRIVE USE CASE
    private val joystickDrive = controllerUseCases.joystickDrive
    fun beginJoystickDrive(
        bluetoothService: MyBluetoothService,
        joystickView: JoystickView
    ) {
        viewModelScope.launch {
            // https://stackoverflow.com/questions/58254985/is-it-better-to-use-a-thread-or-coroutine-in-kotlin
            // In Kotlin, when you need a coroutine to run on a different or special thread, you use a "dispatcher", which pretty much equivalent to a thread pool.
            withContext(Dispatchers.IO) {
                joystickDrive.beginJoystickDrive(
                    bluetoothService,
                    joystickView
                )
            }
        }
    }
    fun stopJoystickDrive() = joystickDrive.stopJoystickDrive()


    // EXECUTE JOYSTICK STEER USE CASE
    private val joystickSteer = controllerUseCases.joystickSteer
    fun beginJoystickSteer(
        bluetoothService: MyBluetoothService,
        joystickView: JoystickView
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                joystickSteer.beginJoystickSteer(
                    bluetoothService,
                    joystickView
                )
            }
        }
    }
    fun stopJoystickSteer() = joystickSteer.stopJoystickSteer()


}