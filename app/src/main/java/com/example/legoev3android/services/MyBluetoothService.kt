package com.example.legoev3android.services

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import com.example.legoev3android.utils.Constants
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class MyBluetoothService(private val handler: Handler) : Service() {
    val bluetoothAdapter: BluetoothAdapter? = (
            applicationContext.getSystemService(Context.BLUETOOTH_SERVICE)
                    as BluetoothManager).adapter

    private inner class ConnectThread(device: BluetoothDevice) : Thread() {
        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(UUID.fromString(Constants.ROBOT_UUID))
        }

        override fun run() {
            // Cancel discovery as otherwise slows connection
            // Always cancel before connection attempt
            bluetoothAdapter?.cancelDiscovery()

            mmSocket?.let { socket ->
                socket.connect()
                // Initiate a thread for transferring data
                //  manageMyConnectedSocket(socket)
            }

        }

        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                println("Could not close the client socket.")
            }
        }
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for stream

        // run seems to be used to READ from bluetooth device... we won't use here yet
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                println("Error occurred sending data")
                // Some other things can be done here, check docs if necessary
                return
            }
            // Something about written message and handler here avoiding for now
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}