package com.example.legoev3android.ui.viewmodels

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.legoev3android.services.MyBluetoothService
import com.example.legoev3android.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private var mBluetoothService: MyBluetoothService? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null



    fun getAdapter(context: Context): BluetoothAdapter? {
        if (mBluetoothAdapter != null)
            return mBluetoothAdapter
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
            .adapter.also { mBluetoothAdapter = it }
        return mBluetoothAdapter
    }

    fun setup(context: Context) {
        mBluetoothService = MyBluetoothService(context)
    }
}