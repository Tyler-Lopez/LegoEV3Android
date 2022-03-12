package com.example.legoev3android.services

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.example.legoev3android.utils.Constants
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.*

class MyBluetoothService(
    val context: Context,
    val debug: () -> Unit
) {

    // Used to access current state of connection to device
    private var mState = Constants.STATE_NONE

    // Used to handle thread management
    private var mThreads: MyThreads = MyThreads()
    private var mAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
            .adapter

    fun connect(device: BluetoothDevice) {
        // If already connecting or connected, cancel threads
        if (mState == Constants.STATE_CONNECTING || mState == Constants.STATE_CONNECTED)
            mThreads.cancelThreads()
        // Start thread to connect with device
        mThreads.startConnection(device)
    }


    fun destroy() {
        MyThreads().cancelThreads()
        mAdapter = null
        mState = Constants.STATE_NONE
    }


    fun read(
        bytes: ByteArray,
        listener: (Float?) -> Unit
    ) {
        if (mState == Constants.STATE_CONNECTED) {
            mThreads.read(bytes) {
                listener(it)
            }
        } else listener(null)
    }

    fun driveMotor(bytes: ByteArray) {
        if (mState == Constants.STATE_CONNECTED)
            mThreads.writeToDrive(bytes)
        else println("ERROR: DRIVE COMMAND GIVEN WHEN NO LONGER CONNECTED") // Improve later
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
            mThreads.writeToSound(buffer)
        } else println("ERROR NO LONGER CONNECTED?")
    }


    /*

    THREADS
    Each manage a certain commands to the robot

     */
    private inner class MyThreads {
        private var mConnectThread: ConnectThread? = null

        // Handle rear wheel power
        private var mDriveThread: ConnectedThread? = null

        // Handle front wheel power steering
        private var mSteerThread: ConnectedThread? = null

        // Handle playing sound
        private var mSoundThread: ConnectedThread? = null

        // Handle write / reply from roboto
        private var mReadDataThread: ConnectedThread? = null

        fun cancelThreads() {
            mDriveThread?.cancel().also { mDriveThread = null }
            mSteerThread?.cancel().also { mSteerThread = null }
            mSoundThread?.cancel().also { mSoundThread = null }
            mReadDataThread?.cancel().also { mReadDataThread = null }
        }

        fun startConnection(device: BluetoothDevice) {
            mConnectThread = ConnectThread(device)
            mConnectThread?.start()
        }

        fun writeToDrive(bytes: ByteArray) = mDriveThread?.writeToOutput(bytes)
        fun writeToSteer(bytes: ByteArray) = mSteerThread?.writeToOutput(bytes)
        fun writeToSound(bytes: ByteArray) = mSoundThread?.writeToOutput(bytes)
        fun read(bytes: ByteArray, listener: (Float) -> Unit) = mReadDataThread?.readInput(bytes) { listener(it) }

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
                        mDriveThread = ConnectedThread(socket)
                        mSteerThread = ConnectedThread(socket)
                        mReadDataThread = ConnectedThread(socket)
                        mSoundThread = ConnectedThread(socket)
                        mState = Constants.STATE_CONNECTED
                        debug() // Really bad way of informing we made connection, fix this later
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


            fun readInput(
                mmBuffer: ByteArray,
                listener: (Float) -> Unit
            ) {
                val reply = ByteArray(24)
                mmOutStream.write(mmBuffer)
                mmInStream.read(reply)
                listener(
                    ByteBuffer.wrap(
                        reply.copyOfRange(5, 9).reversedArray()
                    ).float
                )
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
    } // End MyThreads
}