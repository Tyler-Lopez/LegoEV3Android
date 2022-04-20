package com.example.legoev3android.ui.fragments

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.legoev3android.R
import com.example.legoev3android.databinding.FragmentControllerBinding
import com.example.legoev3android.databinding.TextLargeBoardBinding
import com.example.legoev3android.ui.viewmodels.MainViewModel
import com.example.legoev3android.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ControllerFragment : Fragment(R.layout.fragment_controller) {

    private val viewModel: MainViewModel by activityViewModels()

    private var binding: FragmentControllerBinding? = null
    private var boardBinding: TextLargeBoardBinding? = null

    // Define animation which will be applied to the loading image view
    private var rotateAnimation = RotateAnimation(
        0f,
        360f,
        Animation.RELATIVE_TO_SELF,
        0.5f,
        Animation.RELATIVE_TO_SELF,
        0.5f
    )


    // This object is used to listen to all changes in bond state to device
    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            // If a device was found
            when (intent.action) {
                // First action to be received: we must ensure this is an EV3
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    viewModel.disconnectBluetoothService()
                }
                BluetoothDevice.ACTION_UUID -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.run {
                        if (!device.uuids.isNullOrEmpty())
                            device.uuids.forEach {
                                if ("${it.uuid}".uppercase() == Constants.ROBOT_UUID) {
                                    // This IS an EV3, and we have not yet bonded, bond
                                    if (device.bondState == BluetoothDevice.BOND_NONE) {
                                        device.createBond()
                                        return
                                        // This IS an EV3 we have bonded to before, start service
                                    } else if (device.bondState == BluetoothDevice.BOND_BONDED) {
                                        startBluetoothServiceConnection(device)
                                        return
                                    }
                                }
                            }
                    }
                    viewModel.updateConnection(ConnectionState.Error)
                }

                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device?.bondState == BluetoothDevice.BOND_BONDED) {
                        // Begin bluetoothService service
                        startBluetoothServiceConnection(device)
                    }
                    println("HERE ${device?.bondState}")
                }
            }
        }
    }

    // Invoked when we have bonded to a device which we have also confirmed is a LEGO EV3
    private fun startBluetoothServiceConnection(device: BluetoothDevice) {
        viewModel.connectBluetoothService(device)
    }

    // Register for UUID changes and Bond State changes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filter = IntentFilter(BluetoothDevice.ACTION_UUID)
        val filterDisconnected = IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        val filterBond = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        requireActivity().registerReceiver(receiver, filter)
        requireActivity().registerReceiver(receiver, filterDisconnected)
        requireActivity().registerReceiver(receiver, filterBond)
        monitorConnectionState()
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentControllerBinding.bind(view)
        // Set TextBoard Binding and change connection status for first time
        boardBinding = binding!!.textLargeBoardLayout
        rotateAnimation.duration = 3600
        rotateAnimation.repeatCount = Animation.INFINITE
        boardBinding!!.ivTopRightImage.animation = rotateAnimation
        rotateAnimation.start()

        // Produce . . . animation next to Connected
        loopLoadingDots()

        // Set connection status to connecting
        viewModel.updateConnection(ConnectionState.Connecting)

        // Establish our bluetoothService service and what we should do upon successful connection
        val leftJoystick = binding!!.joystickView
        val rightJoystick = binding!!.joystickViewRight
        viewModel.createBluetoothService(
            requireActivity(),
            lifecycleScope,
            Pair(leftJoystick.powerStateFlow, leftJoystick.degreeStateFlow),
            Pair(rightJoystick.powerStateFlow, rightJoystick.degreeStateFlow)
        )

        // Establish what should happen if the CONNECT / DISCONNECT button is pushed
        boardBinding?.buttonConnectButton?.setOnClickListener {
            viewModel.clickConnectionButton()
        }

        // Fetch UUIDS
        viewModel.selectedDevice?.fetchUuidsWithSdp()

        // Select either to see piano or joysticks
        binding!!.buttonShowKeyboard.setOnClickListener {
            binding!!.llJoysticks.visibility = View.GONE
            binding!!.layoutPianoHolder.visibility = View.VISIBLE
        }
        binding!!.buttonShowJoysticks.setOnClickListener {
            binding!!.llJoysticks.visibility = View.VISIBLE
            binding!!.layoutPianoHolder.visibility = View.GONE
        }
        //  Unbelievable, unpleasant piano we are forced to have
        val pianoBinding = binding!!.pianoWidget
        pianoBinding.noteA.setOnClickListener {
            viewModel.play(Note.A)
        }
        pianoBinding.noteASharp.setOnClickListener {
            viewModel.play(Note.ASharp)
        }
        pianoBinding.noteB.setOnClickListener {
            viewModel.play(Note.B)
        }
        pianoBinding.noteC.setOnClickListener {
            viewModel.play(Note.C)
        }
        pianoBinding.noteCSharp.setOnClickListener {
            viewModel.play(Note.CSharp)
        }
        pianoBinding.noteD.setOnClickListener {
            viewModel.play(Note.D)
        }
        pianoBinding.noteDSharp.setOnClickListener {
            viewModel.play(Note.DSharp)
        }
        pianoBinding.noteE.setOnClickListener {
            viewModel.play(Note.E)
        }
        pianoBinding.noteF.setOnClickListener {
            viewModel.play(Note.F)
        }
        pianoBinding.noteFSharp.setOnClickListener {
            viewModel.play(Note.FSharp)
        }
        pianoBinding.noteG.setOnClickListener {
            viewModel.play(Note.G)
        }
        pianoBinding.noteGSharp.setOnClickListener {
            viewModel.play(Note.GSharp)
        }
    }

    // This is necessary to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(receiver) // Unregister Intent receiver
        viewModel.disconnectBluetoothService()
        binding = null
        boardBinding = null
    }

    // Create ... Animation when Connecting
    private fun loopLoadingDots() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Prevent memory leak with binding != null check
            while (binding != null) {
                boardBinding?.textHeaderRight?.text =
                    when (boardBinding?.textHeaderRight?.text) {
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
                delay(500)
            }
        }
    }

    // Invoked on each ConnectionState change in the ViewModel
    private fun monitorConnectionState() {
        lifecycleScope.launchWhenStarted {
            viewModel.connectionState.collectLatest {
                println(it)
                boardBinding?.textHeader?.text = getString(it.stringId)
                // Set TextHeader to the appropriate size
                boardBinding?.textHeader?.layoutParams = it.getTextHeaderLayoutParams(
                    boardBinding?.textHeader!!.layoutParams
                )
                // Set Icon
                boardBinding?.ivTopRightImage?.setImageResource(it.imageId)
                // Set connection button visibility and text
                boardBinding?.buttonConnectButton?.text = it.connectionButtonText
                boardBinding?.rlConnectButton?.visibility = it.connectionButtonVisibility
                // Handle animation of icon
                boardBinding?.ivTopRightImage?.let { iv -> it.handleIconAnimation(iv, rotateAnimation) }
                // Set text appearances of header, subtext, device name
                boardBinding?.textHeader?.setTextAppearance(it.textHeaderAppearance)
                boardBinding?.textSubtext?.setTextAppearance(it.textSubtextAppearance)
                boardBinding?.textDeviceName?.setTextAppearance(it.textHeaderAppearance)
                // Handle ImageView background animation
                boardBinding?.techTextBgOff?.let { iv ->
                    it.handleBackgroundAnimation(iv, false)
                }
                boardBinding?.techTextBgOn?.let { iv ->
                    it.handleBackgroundAnimation(iv, true)
                }
            }
        }
    }
}