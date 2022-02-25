package com.example.legoev3android.ui.fragments

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.legoev3android.R
import com.example.legoev3android.databinding.FragmentPermissionBinding
import com.example.legoev3android.ui.viewmodels.MainViewModel
import com.example.legoev3android.utils.BluetoothUtility

class PermissionFragment : Fragment(R.layout.fragment_permission) {

    private val viewModel: MainViewModel by viewModels()
    private var binding: FragmentPermissionBinding? = null
    private var permissionMsg: TextView? = null

    // Used to launch and receive results for permission requests
    private val requestPermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted ->
            if (isGranted.containsValue(false)) {
                permissionMsg?.text = "At least one permission was denied."
            } else {
                // Permissions were granted
                startBluetoothConnection()
            }
        }

    // Used to launch and receive searches for available bluetooth devices
    // Including those not yet paired
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            // If a device was found
            if (action == BluetoothDevice.ACTION_FOUND) {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.run {
                    println(this)
                    permissionMsg?.text = permissionMsg?.text.toString() + device.name
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register for broadcasts when a device is discovered
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        requireActivity().registerReceiver(receiver, filter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPermissionBinding.bind(view)
        permissionMsg = binding?.permissionMsg

        // If the user already has given bluetooth permissions
        if (BluetoothUtility.hasBluetoothPermissions(requireContext())) {
            startBluetoothConnection()
        } else {
            binding?.permissionMsg?.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestPermissionsLauncher.launch(
                        arrayOf(
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN
                        )
                    )
                }
            }
        }
    }

    // This is necessary to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    fun startBluetoothConnection() {
        val adapter = viewModel.getAdapter(requireContext())
        if (adapter == null)
        // Device doesn't support bluetooth
            permissionMsg?.text = "Device doesn't support Bluetooth."
        else {
            // Device supports bluetooth
            // Print out all discoverable devices
            permissionMsg?.text = "..."
            adapter.startDiscovery()
        }
    }
}