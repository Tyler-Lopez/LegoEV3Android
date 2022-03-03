package com.example.legoev3android.ui.fragments

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.legoev3android.R
import com.example.legoev3android.databinding.FragmentControllerBinding
import com.example.legoev3android.services.MyBluetoothService
import com.example.legoev3android.ui.JoystickLoopThread
import com.example.legoev3android.ui.viewmodels.MainViewModel
import com.example.legoev3android.utils.*

class ControllerFragment : Fragment(R.layout.fragment_controller) {

    private val viewModel: MainViewModel by viewModels()
    private var binding: FragmentControllerBinding? = null
    private lateinit var bluetoothService: MyBluetoothService


    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            // If a device was found
            when (intent.action) {
                // This is always the first action called, retrieves device UUIDs
                BluetoothDevice.ACTION_UUID -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.run {
                        if (!device.uuids.isNullOrEmpty())
                            device.uuids.forEach {
                                println(it.uuid)
                                if ("${it.uuid}".uppercase() == Constants.ROBOT_UUID) {
                                    if (device.bondState == BluetoothDevice.BOND_NONE) {
                                        println("BOND STATE FOUND TO BE NONE, CREATE BOND")
                                        device.createBond()
                                    } else {
                                        bluetoothService.connect(SelectedDevice.BluetoothDevice!!)
                                    }
                                    return
                                }
                            }
                    }
                    binding?.centeredText?.text = "This is not a Lego EV3."
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    // TO-DO
                    // ADD SOMETHING FOR THE IS BONDING STATE TO NOTE YOU NEED TO ACCEPT ON DEVICE!
                    println("HERE BOND STATE CHANGED")
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device?.bondState == BluetoothDevice.BOND_BONDED) {
                        bluetoothService.connect(device)
                    }
                    device?.let { binding?.centeredText?.text = device.bondState.toString() }
                }
            }
        }
    }

    /*

    FRAGMENT OVERRIDES

     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register for broadcasts when a device is discovered
        val filter = IntentFilter(BluetoothDevice.ACTION_UUID)
        val filterBond = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        requireActivity().registerReceiver(receiver, filter)
        requireActivity().registerReceiver(receiver, filterBond)
    }

    lateinit var joystickThread: JoystickLoopThread
    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentControllerBinding.bind(view)


        bluetoothService = MyBluetoothService(requireContext()) {
            requireActivity().runOnUiThread {
                binding?.centeredText?.visibility = View.GONE
                binding?.constrainLayoutSuccessConnection?.visibility = View.VISIBLE
            }

        }
        JoystickLoopThread(bluetoothService, binding!!.joystickView).start()

        binding?.buttonSound?.setOnClickListener {
            bluetoothService.playSound()
        }

        // TEMPORARY TO CONTROL A MEDIUM MOTOR ON D
        binding?.buttonMediumClose?.setOnClickListener {
            bluetoothService.moveMotor(MotorCommandFactory.create(motor = Motor.D, 50, 20))
        }
        binding?.buttonMediumOpen?.setOnClickListener {
            bluetoothService.moveMotor(MotorCommandFactory.create(motor = Motor.D, 50, 340))
        }

        binding?.centeredText?.text = "${SelectedDevice.BluetoothDevice?.bondState ?: "No bond"}"
        SelectedDevice.BluetoothDevice?.fetchUuidsWithSdp()

    }

    // This is necessary to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(receiver) // Unregister Intent receiver
        bluetoothService.destroy()
        binding = null
    }
}