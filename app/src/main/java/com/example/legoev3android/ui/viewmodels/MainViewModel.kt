package com.example.legoev3android.ui.viewmodels

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legoev3android.domain.use_case.ControllerUseCases
import com.example.legoev3android.services.MyBluetoothService
import com.example.legoev3android.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    controllerUseCases: ControllerUseCases
) : ViewModel() {

    private var _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState = _connectionState.asStateFlow()
    var activelyConnecting = false

    private var bluetoothService: MyBluetoothService? = null
    var selectedDevice: BluetoothDevice? = null


    fun updateConnection(connectionState: ConnectionState) {
        _connectionState.value = connectionState
    }


    fun clickConnectionButton() {
        when (_connectionState.value) {
            // If we are not connected and should begin a connection
            ConnectionState.Disconnected, ConnectionState.Error-> {
                _connectionState.value = ConnectionState.Connecting
                // Fetch UUIDS
                selectedDevice?.fetchUuidsWithSdp()
            }
            // If we are currently connected and should stop connect
            else -> {
                println("Here we were already connected so should stop")
                disconnectBluetoothService()
            }
        }
    }

    fun createBluetoothService(
        context: Context,
        lifecycleCoroutineScope: LifecycleCoroutineScope,
        leftJoystickFlows: Pair<StateFlow<Float>, StateFlow<Float>>,
        rightJoystickFlows: Pair<StateFlow<Float>, StateFlow<Float>>
    ) {

        if (bluetoothService != null)
            return

        bluetoothService = MyBluetoothService(context) {

            _connectionState.value = ConnectionState.Connected

            if (bluetoothService != null) {
                beginMonitorBattery {
                    println("Battery received as $it")
                }
                beginJoystickSteer(
                    lifecycleCoroutineScope = lifecycleCoroutineScope,
                    flows = rightJoystickFlows
                )
                beginJoystickDrive(
                    lifecycleCoroutineScope = lifecycleCoroutineScope,
                    flows = leftJoystickFlows
                )
            }
        }
    }

    fun connectBluetoothService(
        device: BluetoothDevice
    ) {
        bluetoothService?.connect(device)
    }


    fun disconnectBluetoothService() {
        println("Here, disconnect invoked")
        _connectionState.value = ConnectionState.Disconnected
        bluetoothService?.disconnect()
        stopMonitorBattery()
        stopJoystickDrive()
        stopJoystickSteer()
    }

    // EXECUTE PLAY NOTE USE CASE
    private val playNote = controllerUseCases.playNote
    fun play(note: Note) {
        if (bluetoothService != null)
            playNote(bluetoothService!!, note)
    }

    // EXECUTE BATTERY MONITOR USE CASE
    private val monitorBattery = controllerUseCases.monitorBattery
    private fun beginMonitorBattery(
        batteryCallback: (Int) -> Unit
    ) {
        viewModelScope.launch {
            coroutineScope {
                monitorBattery.beginMonitoring(
                    bluetoothService!!
                ) {
                    println("Here, $it")
                    batteryCallback(it ?: -1)
                }
            }
        }
    }

    private fun stopMonitorBattery() = monitorBattery.stopMonitoring()

    // EXECUTE JOYSTICK DRIVE USE CASE
    private val joystickDrive = controllerUseCases.joystickDrive
    private fun beginJoystickDrive(
        lifecycleCoroutineScope: LifecycleCoroutineScope,
        flows: Pair<StateFlow<Float>, StateFlow<Float>>
    ) {
        viewModelScope.launch {
            // https://stackoverflow.com/questions/58254985/is-it-better-to-use-a-thread-or-coroutine-in-kotlin
            // In Kotlin, when you need a coroutine to run on a different or special thread, you use a "dispatcher", which pretty much equivalent to a thread pool.
            withContext(Dispatchers.IO) {
                joystickDrive.beginJoystickDrive(
                    bluetoothService!!,
                    lifecycleCoroutineScope,
                    flows
                )
            }
        }
    }

    private fun stopJoystickDrive() = joystickDrive.stopJoystickDrive()


    // EXECUTE JOYSTICK STEER USE CASE
    private val joystickSteer = controllerUseCases.joystickSteer
    private fun beginJoystickSteer(
        lifecycleCoroutineScope: LifecycleCoroutineScope,
        flows: Pair<StateFlow<Float>, StateFlow<Float>>
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                joystickSteer.beginJoystickSteer(
                    bluetoothService!!,
                    lifecycleCoroutineScope,
                    flows
                )
            }
        }
    }

    private fun stopJoystickSteer() = joystickSteer.stopJoystickSteer()
}