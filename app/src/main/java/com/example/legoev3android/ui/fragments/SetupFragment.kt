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
import android.transition.Slide
import android.transition.TransitionManager
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.legoev3android.R
import com.example.legoev3android.databinding.DevicesSelectLayoutBinding
import com.example.legoev3android.databinding.FragmentSetupBinding
import com.example.legoev3android.databinding.TextFeatureHeaderSubtextBinding
import com.example.legoev3android.ui.recyclerview.DeviceAdapter
import com.example.legoev3android.ui.viewmodels.MainViewModel
import com.example.legoev3android.utils.PermissionUtil
import com.example.legoev3android.utils.SelectedDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SetupFragment : Fragment(R.layout.fragment_setup) {

    private val viewModel: MainViewModel by viewModels()
    private var binding: FragmentSetupBinding? = null


    /*

    UI ELEMENTS
    Each section must have visibility programmatically set

     */

    // UI: Centered single text
    private lateinit var centeredText: TextView

    // UI: Permission Layout
    private lateinit var permissionLayout: TextFeatureHeaderSubtextBinding

    // UI: Bluetooth Header
    //  private lateinit var blueToothHeader: TextElectronicHeaderBinding

    // UI: Show Recycler view
    private lateinit var rvDevices: RecyclerView
    private lateinit var devicesSelectLayout: DevicesSelectLayoutBinding
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
            // Set permissions layout to the non-active state
            togglePermissionLayout(isOn = false)
            // Permissions were granted
            if (!isGranted.containsValue(false))
                findAvailableDevices()
        }

    // Used to launch and receive searches for available bluetooth devices
    // Including those not yet paired
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // If a device was found
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.run {
                        deviceList.add(this)
                        rvDevices.adapter?.notifyItemInserted(deviceList.lastIndex)
                    }
                }
            }
        }
    }


    // Invoked after permissions have been confirmed
    // Return all available Bluetooth devices
    private fun findAvailableDevices() {
        // Animate OUT the "Request Permissions" button if necessary
        if (permissionLayout.mainLayout.visibility != View.GONE) {
            // Animate OUT permission layout
            val transition = Slide()
            transition.slideEdge = Gravity.START
            transition.addTarget(permissionLayout.mainLayout)
            transition.duration = 1200
            TransitionManager.beginDelayedTransition(
                binding?.root as ViewGroup?,
                transition
            )
            permissionLayout.mainLayout.visibility = View.GONE
        }


        val adapter =
            (requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
                .adapter

        // The device is NOT supported for Bluetooth
        if (adapter == null) {
            // Hide the centerConstraintLayout, make centered text visible
            centeredText.visibility = View.VISIBLE
            centeredText.text = getString(R.string.setup_device_not_supported)
        } else {

            rvDevices.adapter = DeviceAdapter(
                devices = deviceList
            ) {
                SelectedDevice.BluetoothDevice = it
                adapter.cancelDiscovery()
                findNavController().navigate(R.id.action_setupFragment_to_controllerFragment)
            }


            val pairedDevices = adapter.bondedDevices
            pairedDevices.forEach { device ->

                deviceList.add(device)
                rvDevices.adapter?.notifyItemInserted(deviceList.lastIndex)
            }

            // https://stackoverflow.com/questions/63276134/getter-for-defaultdisplay-display-is-deprecated-deprecated-in-java
            // Get height appropriately for all devices
            val height: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requireActivity().windowManager.maximumWindowMetrics.bounds.height()
            } else {
                val displayMetrics = DisplayMetrics()
                @Suppress("DEPRECATION") // Suppress as we have the modern approach covered
                requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
                displayMetrics.heightPixels
            }

            // Animate in devices Recycler View
            // It will start up and animate down into position
            binding!!.devicesSelectLayout.constrainLayoutDevicesSearch.visibility = View.VISIBLE
            binding!!.devicesSelectLayout.constrainLayoutDevicesSearch.translationY =
                -1f * height

            val animation = binding!!.devicesSelectLayout.constrainLayoutDevicesSearch
                .animate()
            // When the animation concludes, THEN start discovery
            // This prevents the size of the animating view changing during the brief animation
            animation.withEndAction {
                adapter.startDiscovery()
            }
            animation.duration = 2500
            animation.translationY(0f).start()
        }
    }

    /*

    FRAGMENT OVERRIDES

     */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSetupBinding.bind(view)

        // Register for broadcasts when a device is discovered
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        requireActivity().registerReceiver(receiver, filter)
        // Binding is asserted non null here - binding is only made null in
        // onDestroy to prevent memory leaks
        centeredText = binding!!.centeredText
        permissionLayout = binding!!.permissionsLayout
        devicesSelectLayout = binding!!.devicesSelectLayout
        rvDevices = devicesSelectLayout.recyclerViewDevicesLayout.rvConnections


        // Begin rotation of blue geared circle infinitely
        val rotateAnimation = RotateAnimation(
            0f,
            360f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotateAnimation.duration = 3600
        rotateAnimation.repeatCount = Animation.INFINITE
        permissionLayout.loadingDots.startAnimation(rotateAnimation)

        // Implement on-click listener for the permission layout button
        permissionLayout.techButtonBg.setOnClickListener {
            togglePermissionLayout(isOn = true)

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

        // If the user already has given bluetooth permissions
        if (PermissionUtil.hasPermissions(requireContext()))
            findAvailableDevices()
        // The user does not yet have permissions
        else {
            // https://medium.com/l-r-engineering/launching-kotlin-coroutines-in-android-coroutine-scope-context-800d280ebd80
            viewLifecycleOwner.lifecycleScope.launch {
                delay(500)
                // Permissions layout animations
                val transition = Slide()
                transition.slideEdge = Gravity.TOP
                transition.addTarget(permissionLayout.mainLayout)
                transition.duration = 1200
                TransitionManager.beginDelayedTransition(
                    binding?.root as ViewGroup?,
                    transition
                )
                permissionLayout.mainLayout.visibility = View.VISIBLE
            }
        }
    }

    // Turn permission layout ON / OFF
    private fun togglePermissionLayout(isOn: Boolean) {
        // Animate between backgrounds with a fade, diff time to prevent low alpha
        permissionLayout
            .techButtonBg
            .animate()
            .alpha(if (isOn) 0f else 1f)
            .duration = if (isOn) 2000 else 500
        permissionLayout
            .techButtonBgOn
            .animate()
            .alpha(if (isOn) 1f else 0f)
            .duration = if (isOn) 500 else 2000
        // Text Appearance defined in themes.xml
        permissionLayout.textHeader.setTextAppearance(
            if (isOn)
                R.style.TextLightShadow
            else
                R.style.TextTealShadow
        )
    }

    // This is necessary to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(receiver) // Unregister Intent receiver
        binding = null
    }

}