package com.example.legoev3android.services

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.example.legoev3android.utils.Constants
import kotlinx.coroutines.delay
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import android.os.Handler
import java.nio.ByteBuffer

class MyBluetoothService(
    val context: Context,
    private val handler: Handler,
    val debug: () -> Unit
) {

    // Member fields
    private var mConnectThread: ConnectThread? = null
    private var mMotorThread: ConnectedThread? = null
    private var mSoundThread: ConnectedThread? = null
    private var mReadDataThread: ConnectedThread? = null


    private var mAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
            .adapter
    var mState = Constants.STATE_NONE
        private set

    fun connect(device: BluetoothDevice) {
        // Cancel thread attempting to make a connection
        if (mState == Constants.STATE_CONNECTING)
            mConnectThread?.cancel().also { mConnectThread = null }
        // Cancel threads currently running a connection
        if (mState == Constants.STATE_CONNECTING) {
            mMotorThread?.cancel().also { mMotorThread = null }
            mSoundThread?.cancel().also { mSoundThread = null }
            mReadDataThread?.cancel().also { mReadDataThread = null }
        }
        // Start thread to connect with device
        mConnectThread = ConnectThread(device)
        mConnectThread?.start()
    }

    fun destroy() {
        mMotorThread?.cancel()
        mConnectThread?.cancel()
        mReadDataThread?.cancel()
        mAdapter = null
        mState = Constants.STATE_NONE
    }

    fun readDeviceInformation(
        callback: (String) -> Unit
    ) {
        if (mState == Constants.STATE_CONNECTED) {
            val buffer = ByteArray(15)
            buffer[0] = (0x0D).toByte()
            buffer[1] = 0
            buffer[2] = 0
            buffer[3] = 0
            buffer[4] = Constants.DIRECT_COMMAND_REPLY.toByte()
            buffer[5] = 0x04 // 4 bytes?
            buffer[6] = 0x00
            buffer[7] = Constants.opInput_Device.toByte()
            buffer[8] = 0x1D // READY_SI
            buffer[9] = 0x00
            buffer[10] = 0x00 // Port 1?
            buffer[11] = 0x00 // Don't change type?
            buffer[12] = 0x00 // Don't change mode?
            buffer[13] = 0X01 // Single datapoint
            buffer[14] = 0x60
            mSoundThread?.readInput(buffer, callback)
        } else println("ERROR NO LONGER CONNECTED?")
    }

    fun moveMotor(bytes: ByteArray) {
        if (mState == Constants.STATE_CONNECTED) {
            mMotorThread?.writeToOutput(bytes)
        } else println("ERROR NO LONGER CONNECTED?")
    }

    fun playSound() {
        if (mState == Constants.STATE_CONNECTED) {
            val buffer = ByteArray(17)
            buffer[0] = (0x0F).toByte()
            buffer[1] = 0
            buffer[2] = 0
            buffer[3] = 0
            buffer[4] = (0x80).toByte()
            buffer[5] = 0
            buffer[6] = 0
            buffer[7] = (0x94).toByte()
            buffer[8] = (0x01).toByte()
            buffer[9] = (0x81).toByte()
            buffer[10] = (0x02).toByte()
            buffer[11] = (0x82).toByte()
            buffer[12] = (0xE8).toByte()
            buffer[13] = (0x03).toByte()
            buffer[14] = (0x82).toByte()
            buffer[15] = (0xE8).toByte()
            buffer[16] = (0x03).toByte()
            mSoundThread?.writeToOutput(buffer)
        } else println("ERROR NO LONGER CONNECTED?")
    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(UUID.fromString(Constants.ROBOT_UUID))
        }

        override fun run() {
            mAdapter?.cancelDiscovery()
            try {
                mmSocket?.let { socket ->
                    socket.connect()
                    mMotorThread = ConnectedThread(socket)
                    mReadDataThread = ConnectedThread(socket)
                    mSoundThread = ConnectedThread(socket)
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
        val MESSAGE_READ: Int = 0


        fun readInput(
            mmBuffer: ByteArray,
            callback: (String) -> Unit
        ) {
            var reply = ByteArray(24)
            mmOutStream.write(mmBuffer)
            println(mmInStream.read(reply))
            println("REPLY IS " + ByteBuffer.wrap(reply.copyOfRange(5, 9).reversedArray()).float)
            println(reply.joinToString { it.toString() + " " })
        }

        fun writeToOutput(mmBuffer: ByteArray) {
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