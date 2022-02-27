package com.example.legoev3android.services

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.example.legoev3android.utils.Constants
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class MyBluetoothService(
    val context: Context,
    val debug: () -> Unit
) {

    // Member fields
    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null
    private val mAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
            .adapter
    var mState = Constants.STATE_NONE
        private set
    private var mNewState = Constants.STATE_NONE

    fun connect(device: BluetoothDevice) {
        // Cancel thread attempting to make a connection
        if (mState == Constants.STATE_CONNECTING)
            mConnectThread?.cancel().also { mConnectThread = null }
        // Cancel threads currently running a connection
        if (mState == Constants.STATE_CONNECTING)
            mConnectedThread?.cancel().also { mConnectedThread = null }
        // Start thread to connect with device
        mConnectThread = ConnectThread(device)
        mConnectThread?.start()
    }

    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(UUID.fromString(Constants.ROBOT_UUID))
        }

        override fun run() {
            mAdapter?.cancelDiscovery()

            mmSocket?.let { socket ->
                socket.connect()
                debug()
                // Manage socket by passing into another thread
            }
        }

        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                // Could not close the client socket
            }
        }
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream

        //   private val mmBuffer: ByteArray = ByteArray(20)
        // TODO Check documentation to fill this in later
        fun moveMotor() {
            val mmBuffer = ByteArray(0x12) // 0x12 Command Length
            // Each byte has 8 bits

        }

        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                // Could not close the connect socket
            }
        }
    }

}