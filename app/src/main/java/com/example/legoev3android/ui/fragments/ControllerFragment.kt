package com.example.legoev3android.ui.fragments

import android.annotation.SuppressLint
import android.app.ActionBar
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
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.legoev3android.R
import com.example.legoev3android.databinding.FragmentControllerBinding
import com.example.legoev3android.databinding.TextLargeBoardBinding
import com.example.legoev3android.services.MyBluetoothService
import com.example.legoev3android.ui.viewmodels.MainViewModel
import com.example.legoev3android.utils.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ControllerFragment : Fragment(R.layout.fragment_controller) {

    private val viewModel: MainViewModel by viewModels()

    private var binding: FragmentControllerBinding? = null
    private lateinit var textBoardBinding: TextLargeBoardBinding

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
                                    viewModel.connectionMessage =
                                        R.string.controller_status_confirmed_EV3
                                    // This IS an EV3, begin connection with device
                                    if (device.bondState == BluetoothDevice.BOND_NONE) {
                                        device.createBond()
                                        return
                                    }
                                }
                            }
                    }

                    // Failure to connect
                    viewModel.connectionStatus = ConnectionStatus.ERROR
                    // Device is either not reachable or is not an EV3
                    viewModel.connectionMessage =
                        R.string.controller_status_error_connecting_to_device
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
                    // TODO remove logic from this removed textview into a information menu
                    // device?.let { binding?.centeredText?.text = device.bondState.toString() }
                }
                else -> println("HERE HERE $intent ${intent.action}")
            }
        }
    }

    /*

    FRAGMENT OVERRIDES

     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Assume that when we create this view we are ATTEMPTING TO CONNECT
        viewModel.connectionStatus = ConnectionStatus.CONNECTING
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

        // Set TextBoard Binding and begin the update text loop
        textBoardBinding = binding!!.textLargeBoardLayout
        updateTextLoop()


        // Establish our bluetooth service and what we should do upon successful connection
        bluetoothService = MyBluetoothService(requireContext()) {
            requireActivity().runOnUiThread {
                binding?.constrainLayoutSuccessConnection?.visibility = View.VISIBLE
            }
            // This should mean the go ahead on we are connected
            viewModel.JoystickSteerThread(bluetoothService, binding!!.joystickView).start()
            viewModel.JoystickDriveThread(bluetoothService, binding!!.joystickView).start()
        }

        // Establish what should happen if the CONNECT / DISCONNECT button is pushed
        textBoardBinding.buttonConnectButton.setOnClickListener {
            when (viewModel.connectionStatus) {
                // If we are not connected and should begin a connection
                ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR -> {
                    viewModel.connectionStatus = ConnectionStatus.CONNECTING
                    viewModel.connectionMessage = R.string.controller_status_confirm_EV3
                    // Attempt to make a connection
                    SelectedDevice.BluetoothDevice?.fetchUuidsWithSdp()
                }
                else -> {

                }
            }

        }

        // Attempt to make a connection
        SelectedDevice.BluetoothDevice?.fetchUuidsWithSdp()
    }

    // This is necessary to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(receiver) // Unregister Intent receiver
        bluetoothService.destroy()
        binding = null
    }

    // Will constantly run in this fragment to update the text section of the UI including images
    @SuppressLint("MissingPermission")
    private fun updateTextLoop() {
        // Define animation which will be applied to the loading image view
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

        textBoardBinding.textDeviceName.text =
            "${SelectedDevice.BluetoothDevice?.name}  ${SelectedDevice.BluetoothDevice?.address}"

        // Used and updated to see if we have changed connection status
        var lastConnectionStatus = ConnectionStatus.DISCONNECTED

        // We will continue to loop within ViewModel scope, delaying every second.
        // As soon as an update is made to the MainViewModel's connection status, we will reflect
        // that here
        viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                // Change subtext if necessary
                if (textBoardBinding.textSubtext.text != getString(viewModel.connectionMessage)) {
                    requireActivity().runOnUiThread {
                        textBoardBinding.textHeader.text =
                            getString(viewModel.connectionStatus.stringId)
                        textBoardBinding.textSubtext.text = getString(viewModel.connectionMessage)
                    }
                }

                when (viewModel.connectionStatus) {
                    ConnectionStatus.DISCONNECTED -> {

                    }
                    ConnectionStatus.ERROR -> {
                        // If we were previously not at an error state
                        if (lastConnectionStatus != viewModel.connectionStatus) {
                            // Cease connecting animation
                            rotateAnimation.cancel()
                            // Change connection animation to a error
                            textBoardBinding.loadingDots.setImageResource(viewModel.connectionStatus.imageId)
                            // Remove the ... which was present in CONNECTING on loop
                            val params = textBoardBinding.textHeader.layoutParams
                            params.width = LinearLayout.LayoutParams.MATCH_PARENT
                            textBoardBinding.textHeader.layoutParams = params
                            // Make button to connect appear visible
                            textBoardBinding.rlConnectButton.visibility = View.VISIBLE
                        }
                    }
                    ConnectionStatus.CONNECTING -> {
                        // If needed, start animation and set image to loading dots
                        if (lastConnectionStatus != viewModel.connectionStatus) {
                            // Make connection button go away
                            textBoardBinding.rlConnectButton.visibility = View.GONE
                            textBoardBinding.loadingDots.setImageResource(viewModel.connectionStatus.imageId)
                            textBoardBinding.loadingDots.startAnimation(rotateAnimation)
                            // Add back the ...
                            val params = textBoardBinding.textHeader.layoutParams
                            params.width = 0 // 0 is because it is set by the weight sum
                            params.height = 60
                            textBoardBinding.textHeader.layoutParams = params
                        }
                        // Creating Connecting ... effect
                        textBoardBinding.textHeaderRight.text =
                            when (textBoardBinding.textHeaderRight.text) {
                                getString(R.string.controller_connecting_three) -> getString(R.string.controller_connecting_zero)
                                getString(R.string.controller_connecting_two) + " " -> getString(
                                    R.string.controller_connecting_three
                                )
                                getString(R.string.controller_connecting_one) + "  " -> getString(
                                    R.string.controller_connecting_two
                                ) + " "
                                getString(R.string.controller_connecting_zero) -> getString(R.string.controller_connecting_one) + "  "
                                else -> getString(R.string.controller_connecting_zero)
                            }
                    }
                    ConnectionStatus.CONNECTED -> {

                    }
                }
                lastConnectionStatus = viewModel.connectionStatus
                delay(500)
            }
        }
    }
}