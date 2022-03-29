package com.example.legoev3android.ui.fragments

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.legoev3android.R
import com.example.legoev3android.databinding.FragmentControllerBinding
import com.example.legoev3android.services.MyBluetoothService
import com.example.legoev3android.ui.viewmodels.MainViewModel
import com.example.legoev3android.utils.*

class ControllerFragment : Fragment(R.layout.fragment_controller) {

    private val viewModel: MainViewModel by viewModels()
    private var binding: FragmentControllerBinding? = null
    private lateinit var bluetoothService: MyBluetoothService

    // This object is used to listen to all changes in bond state to device
    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            // If a device was found
            when (intent.action) {
                // First action to be received: we must ensure this is an EV3
                BluetoothDevice.ACTION_UUID -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.run {
                        if (!device.uuids.isNullOrEmpty())
                            device.uuids.forEach {
                                if ("${it.uuid}".uppercase() == Constants.ROBOT_UUID) {
                                    // This IS an EV3, begin connection with device
                                    if (device.bondState == BluetoothDevice.BOND_NONE) {
                                        device.createBond()
                                        return
                                    }
                                }
                            }
                    }
                    // If we have reached here: no UUID matched the EV3 constant
                    // TODO use a different way to inform user of failure
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

        // Register for UUID received, used to ensure this is an EV3
        val filter = IntentFilter(BluetoothDevice.ACTION_UUID)
        // Register for all bond state changes (connect / disconnect / failed)
        val filterBond = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        requireActivity().registerReceiver(receiver, filter)
        requireActivity().registerReceiver(receiver, filterBond)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentControllerBinding.bind(view)

        val transition = Slide()
        transition.slideEdge = Gravity.END
        transition.addTarget(binding?.cardView)
        transition.duration = 600


        bluetoothService = MyBluetoothService(requireContext()) {
            requireActivity().runOnUiThread {
                TransitionManager.beginDelayedTransition(
                    binding?.root as ViewGroup?,
                    transition
                )
                binding?.cardView?.visibility = View.GONE
                binding?.constrainLayoutSuccessConnection?.visibility = View.VISIBLE
            }
            // This should mean the go ahead on we are connected
            viewModel.JoystickSteerThread(bluetoothService, binding!!.joystickView).start()
            viewModel.JoystickDriveThread(bluetoothService, binding!!.joystickView).start()

        }

        //  viewModel.JoystickDriveThread(bluetoothService, binding!!.joystickView).start()

        binding?.buttonSound?.setOnClickListener {
            bluetoothService.playSound()
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