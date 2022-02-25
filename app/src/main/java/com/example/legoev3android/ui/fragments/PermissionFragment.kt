package com.example.legoev3android.ui.fragments

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.legoev3android.R
import com.example.legoev3android.databinding.FragmentPermissionBinding
import com.example.legoev3android.ui.recyclerview.DeviceAdapter
import com.example.legoev3android.ui.viewmodels.MainViewModel
import com.example.legoev3android.utils.PermissionUtility

class PermissionFragment : Fragment(R.layout.fragment_permission) {

    private val viewModel: MainViewModel by viewModels()
    private var binding: FragmentPermissionBinding? = null

    // When you only want to display a single message
    private lateinit var centeredText: TextView

    // When you want subtext and primary text
    private lateinit var centerConstraintLayout: ConstraintLayout
    private lateinit var textConstraintToSubtext: TextView
    private lateinit var textConstraintToText: TextView

    // Handle available device recycler view search
    private lateinit var rvDevices: RecyclerView
    private lateinit var deviceAdapter: DeviceAdapter
    private val deviceList = mutableListOf<BluetoothDevice>()

    // Used to launch and receive results for permission requests
    private val requestPermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted ->
            if (isGranted.containsValue(false)) {
                textConstraintToSubtext.text = "Permissions are required for this app to function."
            } else {
                // Permissions were granted
                findAvailableDevices()
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
                    deviceList.add(this)
                    rvDevices.adapter?.notifyItemInserted(deviceList.lastIndex)
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

        textConstraintToSubtext = binding!!.permissionText
        textConstraintToText = binding!!.permissionSubtext
        centeredText = binding!!.centeredText
        centerConstraintLayout = binding!!.constrainLayoutCenter
        rvDevices = binding!!.rvConnections

        centeredText.text = "LOADING BLUETOOTH"

        // If the user already has given bluetooth permissions
        if (PermissionUtility.hasPermissions(requireContext()))
            findAvailableDevices()
        else {
            // Else, make text and subtext visible
            //
            centeredText.visibility = View.GONE
            centerConstraintLayout.visibility = View.VISIBLE
            textConstraintToSubtext.text =
                "This application requires Location and Bluetooth permissions."
            textConstraintToText.text = "Click to grant permissions."
            centerConstraintLayout.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestPermissionsLauncher.launch(
                        arrayOf(
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                } else {
                    requestPermissionsLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
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

    private fun findAvailableDevices() {
        val adapter =
            (requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
                .adapter

        centerConstraintLayout.visibility = View.GONE


        if (adapter == null) {
            // Hide the centerConstraintLayout, make centered text visible
            centeredText.visibility = View.VISIBLE
            centeredText.text = "This device does not support Bluetooth connections."
        } else {
            // Device supports bluetooth
            // Print out all discoverable devices
            rvDevices.adapter = DeviceAdapter(deviceList)
            rvDevices.visibility = View.VISIBLE
            println(adapter.startDiscovery())
        }
    }
}