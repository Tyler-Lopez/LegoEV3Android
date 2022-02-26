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
import com.example.legoev3android.databinding.FragmentSetupBinding
import com.example.legoev3android.ui.recyclerview.DeviceAdapter
import com.example.legoev3android.ui.viewmodels.MainViewModel
import com.example.legoev3android.utils.PermissionUtility

class SetupFragment : Fragment(R.layout.fragment_setup) {

    private val viewModel: MainViewModel by viewModels()
    private var binding: FragmentSetupBinding? = null

    /*

    UI ELEMENTS
    Each section must have visibility programmatically set

     */

    // UI: Centered single text
    private lateinit var centeredText: TextView

    // UI: Centered text and subtextWhen you want subtext and primary text
    private lateinit var centerConstraintLayout: ConstraintLayout
    private lateinit var textConstraintToSubtext: TextView
    private lateinit var textConstraintToText: TextView

    // UI: Show Recycler view
    private lateinit var rvDevices: RecyclerView
    private lateinit var rvConstraintLayout: ConstraintLayout
    private val deviceList = mutableListOf<BluetoothDevice>()

    /*

    RECEIVERS & BLUETOOTH
    Used to receive and handle permission requests and BluetoothAdapter discovery

     */


    // Used to launch and receive results for permission requests
    private val requestPermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted ->
            if (isGranted.containsValue(false))
                textConstraintToSubtext.text = getString(R.string.setup_permissions_denied)
            else // Permissions were granted
                findAvailableDevices()

        }

    // Used to launch and receive searches for available bluetooth devices
    // Including those not yet paired
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            // If a device was found
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.run {
                        this.fetchUuidsWithSdp()
                        //  deviceList.add(this)
                        //  rvDevices.adapter?.notifyItemInserted(deviceList.lastIndex)
                    }
                }
                BluetoothDevice.ACTION_UUID -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.run {
                        try {
                            println("FOUND DEVICE")
                            for (uuid in device.uuids) {
                                println(uuid)
                            }
                            deviceList.add(this)
                            rvDevices.adapter?.notifyItemInserted(deviceList.lastIndex)
                        } catch (e: Exception) {
                            println("Exception ")
                        }
                    }
                }
            }
        }
    }


    // Invoked after permissions have been confirmed
    // Return all available Bluetooth devices
    private fun findAvailableDevices() {
        val adapter =
            (requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
                .adapter

        centerConstraintLayout.visibility = View.GONE

        // The device is NOT supported for Bluetooth
        if (adapter == null) {
            // Hide the centerConstraintLayout, make centered text visible
            centeredText.visibility = View.VISIBLE
            centeredText.text = getString(R.string.setup_device_not_supported)
        }
        // Find all discoverable devices
        else {
            // Device supports bluetooth
            // Print out all discoverable devices
            rvDevices.adapter = DeviceAdapter(
                devices = deviceList
            ) { device ->
                rvConstraintLayout.visibility = View.GONE
                centeredText.visibility = View.VISIBLE
                adapter.cancelDiscovery()
                device.fetchUuidsWithSdp()
            }
            rvConstraintLayout.visibility = View.VISIBLE
            println(adapter.startDiscovery())
        }
    }

    /*

    FRAGMENT OVERRIDES

     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register for broadcasts when a device is discovered
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val filterUuid = IntentFilter(BluetoothDevice.ACTION_UUID)
        requireActivity().registerReceiver(receiver, filter)
        requireActivity().registerReceiver(receiver, filterUuid)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSetupBinding.bind(view)

        // Binding is asserted non null here - binding is only made null in
        // onDestroy to prevent memory leaks
        textConstraintToSubtext = binding!!.permissionText
        textConstraintToText = binding!!.permissionSubtext
        centeredText = binding!!.centeredText
        centerConstraintLayout = binding!!.constrainLayoutCenter
        rvDevices = binding!!.rvConnections
        rvConstraintLayout = binding!!.constrainLayoutDevicesSearch

        // Inform the user we are loading bluetooth (should be shown only very briefly)
        centeredText.text = getString(R.string.setup_loading_bluetooth)

        // If the user already has given bluetooth permissions
        if (PermissionUtility.hasPermissions(requireContext()))
            findAvailableDevices()

        // The user does not yet have permissions
        else {
            // Hide center text, prevent text/subtext/button
            centeredText.visibility = View.GONE
            centerConstraintLayout.visibility = View.VISIBLE
            textConstraintToSubtext.text =
                getString(R.string.setup_permissions_required_message)
            textConstraintToText.text = getString(R.string.setup_button_grant_permissions)
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

}