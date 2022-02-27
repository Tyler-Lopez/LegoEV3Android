package com.example.legoev3android.ui.fragments

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
import com.example.legoev3android.ui.viewmodels.MainViewModel
import com.example.legoev3android.utils.SelectedDevice

class ControllerFragment : Fragment(R.layout.fragment_controller) {

    private val viewModel: MainViewModel by viewModels()
    private var binding: FragmentControllerBinding? = null
    private lateinit var bluetoothService: MyBluetoothService
    //

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // If a device was found
            when (intent.action) {
                BluetoothDevice.ACTION_UUID -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.run {
                        for (uuid in device.uuids)
                            binding?.centeredText?.text =
                                binding?.centeredText?.text.toString() + "\n" + uuid.toString()
                    }
                    if (device?.bondState == BluetoothDevice.BOND_NONE) {
                        println("BOND STATE FOUND TO BE NONE, CREATE BOND")
                        device.createBond()
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentControllerBinding.bind(view)
        bluetoothService = MyBluetoothService(requireContext()) {
            requireActivity().runOnUiThread {
                binding?.centeredText?.visibility = View.GONE
                binding?.constrainLayoutSuccessConnection?.visibility = View.VISIBLE
            }
        }

        binding?.buttonMotor?.setOnClickListener {
            bluetoothService.moveMotor()
        }
        binding?.centeredText?.text = "${SelectedDevice.BluetoothDevice?.bondState ?: "No bond"}"
        if (SelectedDevice.BluetoothDevice?.bondState != BluetoothDevice.BOND_BONDED)
            SelectedDevice.BluetoothDevice?.fetchUuidsWithSdp()
        else bluetoothService.connect(SelectedDevice.BluetoothDevice!!)
    }

    // This is necessary to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}