package com.example.legoev3android.ui.viewmodels

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legoev3android.services.MyBluetoothService
import com.example.legoev3android.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private var mBluetoothAdapter: BluetoothAdapter? = null

    fun registerForDeviceInformation(
        bluetoothService: MyBluetoothService,
        callback: (String) -> Unit
    ) {
        println("Invoked register for device information")
        // https://developer.android.com/topic/libraries/architecture/coroutines
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                    println("Reading information")
                    bluetoothService.readDeviceInformation {
                        println("here")
                        callback(it)
                }
            }
        }
    }


}