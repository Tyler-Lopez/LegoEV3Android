package com.example.legoev3android.services

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.example.legoev3android.utils.ConnectionState
import com.example.legoev3android.utils.Constants
import com.example.legoev3android.utils.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private var _connectionState: MutableStateFlow<ConnectionState>? = null

    fun connect(
        device: BluetoothDevice,
        _connectionState: MutableStateFlow<ConnectionState>
    ) {
        this._connectionState = _connectionState
        // If already connecting or connected, cancel threads
        if (mState == Constants.STATE_CONNECTING || mState == Constants.STATE_CONNECTED)
            mThreads.cancelThreads()
        // Start thread to connect with device
        println("here, before start connection")
        mThreads.startConnection(device)
        println("here, after start connection")
    }

    fun disconnect() {
        // Just cancels threads and updates state without nullifying adapter
        mThreads.cancelThreads()
        mState = Constants.STATE_NONE
    }

    fun destroy() {
        mThreads.cancelThreads()
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

    fun readBattery(
        bytes: ByteArray,
        listener: (Float?) -> Unit
    ) {
        if (mState == Constants.STATE_CONNECTED) {
            mThreads.readBattery(bytes) {
                listener(it)
            }
        } else listener(null)
    }


    fun driveMotor(bytes: ByteArray, asSteer: Boolean = false) {
        if (mState == Constants.STATE_CONNECTED)
            if (asSteer)
                mThreads.writeToSteer(bytes)
            else
                mThreads.writeToDrive(bytes)
        else println("ERROR: DRIVE COMMAND GIVEN WHEN NO LONGER CONNECTED") // Improve later
    }

    fun playSound(note: Note) {
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
            buffer[12] = note.byte1
            buffer[13] = note.byte2
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
        private var mReadBatteryThread: ConnectedThread? = null

        fun cancelThreads() {
            mConnectThread?.cancel().also { mConnectThread = null }
            mDriveThread?.cancel().also { mDriveThread = null }
            mSteerThread?.cancel().also { mSteerThread = null }
            mSoundThread?.cancel().also { mSoundThread = null }
            mReadDataThread?.cancel().also { mReadDataThread = null }
            mReadBatteryThread?.cancel().also { mReadBatteryThread = null }
        }

        fun startConnection(device: BluetoothDevice) {
            mConnectThread = ConnectThread(device)
            mConnectThread?.start()
        }

        fun writeToDrive(bytes: ByteArray) = mDriveThread?.writeToOutput(bytes)
        fun writeToSteer(bytes: ByteArray) = mSteerThread?.writeToOutput(bytes)
        fun writeToSound(bytes: ByteArray) = mSoundThread?.writeToOutput(bytes)
        fun read(bytes: ByteArray, listener: (Float) -> Unit) =
            mReadDataThread?.readInput(bytes) { listener(it) }

        fun readBattery(bytes: ByteArray, listener: (Float) -> Unit) {
            println("Here trying read")
            mReadBatteryThread?.readInput(bytes, true) { listener(it) }
            println("here after")
        }

        @SuppressLint("MissingPermission")
        private inner class ConnectThread(device: BluetoothDevice) : Thread() {

            private var mmSocket: BluetoothSocket? =
                device.createRfcommSocketToServiceRecord(UUID.fromString(Constants.ROBOT_UUID))


            override fun run() {
                if (mmSocket == null)
                    println("Socket is null")
                else println("SOCKET IS NOT NULL")
                println(mmSocket?.isConnected)
                println("Attempting to cancel discovery on adapter")
                mAdapter?.cancelDiscovery()
                println("Canceled adapter discovery")
                try {
                    mmSocket?.let { socket ->
                        println("Attempting to connect to socket")
                        socket.connect()
                        println("Connected to socket")
                        mDriveThread = ConnectedThread(socket)
                        mSteerThread = ConnectedThread(socket)
                        mReadDataThread = ConnectedThread(socket)
                        mReadBatteryThread = ConnectedThread(socket)
                        mSoundThread = ConnectedThread(socket)
                        mState = Constants.STATE_CONNECTED
                        println("Just about to inform of connection")
                        debug() // Really bad way of informing we made connection, fix this later
                    }
                } catch (e: IOException) {
                    println("EXCEPTION THROWN $e")
                    if (_connectionState?.value is ConnectionState.Connecting)
                        _connectionState?.value = ConnectionState.Disconnected
                    Timber.e(e) // Log error
                }
            }

            fun cancel() {

                try {
                    mmSocket?.inputStream?.close()
                    mmSocket?.outputStream?.close()
                    mmSocket?.close()
                    mmSocket = null
                } catch (e: IOException) {
                    println("Could not close the client socket $e")
                    // Could not close the client socket
                }
            }
        }

        private inner class ConnectedThread(private var mmSocket: BluetoothSocket?) : Thread() {
            private var mmInStream: InputStream? = mmSocket?.inputStream
            private var mmOutStream: OutputStream? = mmSocket?.outputStream


            fun readInput(
                mmBuffer: ByteArray,
                sentByBatteryDebug: Boolean = false,
                listener: (Float) -> Unit
            ) {
                try {
                    val reply = ByteArray(24)
                    if (sentByBatteryDebug)
                        println("here b4 write")
                    mmOutStream?.write(mmBuffer)
                    if (sentByBatteryDebug)
                        println("here after write")
                    mmInStream?.read(reply)
                    if (sentByBatteryDebug) {
                        println(reply.joinToString { "$it, " })
                        println(reply[5].toInt())
                    }
                    if (sentByBatteryDebug)
                        listener(
                            (ByteBuffer.wrap(
                                reply.copyOfRange(5, 9).reversedArray()
                            ).int).toFloat()
                        )
                    else
                        listener(
                            ByteBuffer.wrap(
                                reply.copyOfRange(5, 9).reversedArray()
                            ).float
                        )
                } catch (e: IOException) {
                    println("Socket closed while reading $e")
                }

            }

            fun writeToOutput(mmBuffer: ByteArray) {
                try {
                    mmOutStream?.write(mmBuffer)
                    mmOutStream?.flush()
                } catch (e: Exception) {
                    println(e.localizedMessage + " ERROR HAPPENED MOVING MOTOR")
                }
            }

            fun cancel() {
                try {
                    mmInStream?.close()
                    mmInStream = null
                    mmOutStream?.close()
                    mmOutStream = null
                    mmSocket?.close()
                    mmSocket = null
                } catch (e: IOException) {
                    // Could not close the connect socket
                }
            }
        }
    } // End MyThreads
}