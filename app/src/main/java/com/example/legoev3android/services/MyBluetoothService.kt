package com.example.legoev3android.services

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import com.example.legoev3android.utils.Constants
import com.example.legoev3android.utils.MotorCommandFactory
import com.example.legoev3android.utils.MotorUtil
import com.example.legoev3android.utils.Motors
import timber.log.Timber
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
    private var mAdapter: BluetoothAdapter? =
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

    fun destroy() {
        mConnectedThread?.cancel()
        mConnectThread?.cancel()
        mAdapter = null
        mState = Constants.STATE_NONE
    }

    fun moveMotor() {
        if (mState == Constants.STATE_CONNECTED)
            mConnectedThread?.moveMotor()
        else println("ERROR NO LONGER CONNECTED?")
    }

    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(UUID.fromString(Constants.ROBOT_UUID))
        }

        override fun run() {
            mAdapter?.cancelDiscovery()
            try {
                mmSocket?.let { socket ->
                    socket.connect()
                    mConnectedThread = ConnectedThread(socket)
                    mState = Constants.STATE_CONNECTED
                    debug()
                }
            } catch (e: IOException) {
                Timber.e(e) // Log error
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
            val mmBuffer = MotorUtil.generateBytes()
            try {
                mmOutStream.write(mmBuffer)
                mmOutStream.flush()
            } catch (e: Exception) {
                println(e.localizedMessage + " ERROR HAPPENED MOVING MOTOR")
            }
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