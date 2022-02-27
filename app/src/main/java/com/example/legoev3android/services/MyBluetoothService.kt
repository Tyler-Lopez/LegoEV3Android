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

            mmSocket?.let { socket ->
                socket.connect()
                mConnectedThread = ConnectedThread(socket)
                mState = Constants.STATE_CONNECTED
                debug()
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
            val mmBuffer = ByteArray(20) // 0x12 Command Length
            mmBuffer[0] = (20 - 2).toByte()
            mmBuffer[1] = 0
            mmBuffer[2] = 34
            mmBuffer[3] = 12
            mmBuffer[4] = (0x80).toByte()
            mmBuffer[5] = 0
            mmBuffer[6] = 0
            mmBuffer[7] = (0xae).toByte()
            mmBuffer[8] = 0
            mmBuffer[9] = (0x06).toByte()
            mmBuffer[10] = (0x81).toByte()
            mmBuffer[11] = (0x32).toByte()
            mmBuffer[12] = 0
            mmBuffer[13] = (0x82).toByte()
            mmBuffer[14] = (0x84).toByte()
            mmBuffer[15] = (0x03).toByte()
            mmBuffer[16] = (0x82).toByte()
            mmBuffer[17] = (0xB4).toByte()
            mmBuffer[18] = (0x00).toByte()
            mmBuffer[19] = 1
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