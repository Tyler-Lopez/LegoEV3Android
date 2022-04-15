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
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.legoev3android.R
import com.example.legoev3android.databinding.FragmentControllerBinding
import com.example.legoev3android.databinding.TextLargeBoardBinding
import com.example.legoev3android.services.MyBluetoothService
import com.example.legoev3android.ui.viewmodels.MainViewModel
import com.example.legoev3android.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ControllerFragment : Fragment(R.layout.fragment_controller) {

    private val viewModel: MainViewModel by activityViewModels()

    private var binding: FragmentControllerBinding? = null
    private lateinit var textBoardBinding: TextLargeBoardBinding

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
                    viewModel.updateConnection(ConnectionStatus.ERROR)
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
        // TODO Add back in time-out feature
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
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentControllerBinding.bind(view)

        // Set TextBoard Binding and change connection status for first time
        textBoardBinding = binding!!.textLargeBoardLayout
        rotateAnimation.duration = 3600
        rotateAnimation.repeatCount = Animation.INFINITE
        textBoardBinding.ivTopRightLoadingIcon.animation = rotateAnimation
        rotateAnimation.start()

        // Produce . . . animation next to Connected
        loopLoadingDots()

        // Set connection status to connecting
        viewModel.updateConnection(ConnectionStatus.CONNECTING)

        // Establish our bluetoothService service and what we should do upon successful connection
        viewModel.createBluetoothService(
            requireActivity(),
            binding!!.joystickView,
            binding!!.joystickViewRight
        )

        // Establish what should happen if the CONNECT / DISCONNECT button is pushed
        textBoardBinding.buttonConnectButton.setOnClickListener {
            when (viewModel.connectionStatus) {
                // If we are not connected and should begin a connection
                ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR -> {
                    viewModel.updateConnection(ConnectionStatus.CONNECTING)
                    // Fetch UUIDS
                    viewModel.selectedDevice?.fetchUuidsWithSdp()
                }
                // If we are currently connected and should stop connect
                else -> viewModel.disconnectBluetoothService()
            }
        }

        // Fetch UUIDS
        viewModel.selectedDevice?.fetchUuidsWithSdp()

        // Listen to connection changes in the view model
        viewModel.connectionChangeListener = (connectionStatusHandler)

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
    }

    // Create ... Animation when Connecting
    private fun loopLoadingDots() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Prevent memory leak with binding != null check
            while (binding != null) {
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
                delay(500)
            }
        }
    }

    private val connectionStatusHandler: (ConnectionStatus) -> Unit = { connectionStatus ->
        requireActivity().runOnUiThread {
            textBoardBinding.textHeader.text = getString(viewModel.connectionStatus.stringId)
            // Change image in the top-right
            textBoardBinding.ivTopRightLoadingIcon.visibility =
                if (connectionStatus == ConnectionStatus.CONNECTING)
                    View.VISIBLE else View.GONE

            textBoardBinding.ivTopRightImage.visibility =
                if (connectionStatus == ConnectionStatus.CONNECTING)
                    View.GONE else View.VISIBLE

            textBoardBinding.ivTopRightLoadingIcon.clearAnimation()

            textBoardBinding.ivTopRightImage.setImageResource(viewModel.connectionStatus.imageId)

            when (connectionStatus) {
                ConnectionStatus.CONNECTED -> {
                    changeTextBackground(true)
                    // Remove the ... which was present in CONNECTING on loop
                    val params = textBoardBinding.textHeader.layoutParams
                    params.width = LinearLayout.LayoutParams.MATCH_PARENT
                    textBoardBinding.textHeader.layoutParams = params
                    // Update button to attempt a new connection to make it visible
                    textBoardBinding.rlConnectButton.visibility = View.VISIBLE
                    textBoardBinding.tvConnectButton.text = "DISCONNECT"
                }
                ConnectionStatus.CONNECTING -> {
                    textBoardBinding.ivTopRightLoadingIcon.animation = rotateAnimation
                    rotateAnimation.start()
                    changeTextBackground(false)
                    // Add back the ...
                    val params = textBoardBinding.textHeader.layoutParams
                    params.width = 0 // 0 is because it is set by the weight sum
                    params.height = 60
                    textBoardBinding.textHeader.layoutParams = params
                    // Make connection button go away
                    textBoardBinding.rlConnectButton.visibility = View.GONE
                }
                ConnectionStatus.ERROR, ConnectionStatus.DISCONNECTED -> {
                    changeTextBackground(false)
                    // Remove the ... which was present in CONNECTING on loop
                    val params = textBoardBinding.textHeader.layoutParams
                    params.width = LinearLayout.LayoutParams.MATCH_PARENT
                    textBoardBinding.textHeader.layoutParams = params
                    // Update button to attempt a new connection to make it visible
                    textBoardBinding.rlConnectButton.visibility = View.VISIBLE
                    textBoardBinding.tvConnectButton.text = "CONNECT"
                }
            }
        }
    }

    private fun changeTextBackground(isConnected: Boolean) {
        // Animate between backgrounds with a fade, diff time to prevent low alpha
        textBoardBinding
            .techTextBgOff
            .animate()
            .alpha(if (isConnected) 0f else 1f)
            .duration = if (isConnected) 2000 else 500
        textBoardBinding
            .techTextBgOn
            .animate()
            .alpha(if (isConnected) 1f else 0f)
            .duration = if (isConnected) 500 else 2000
        // Set header text appearance
        textBoardBinding.textHeader.setTextAppearance(
            if (isConnected)
                R.style.TextDarkShadow
            else
                R.style.TextTealShadow
        )
        // Set subtext text appearance
        textBoardBinding.textSubtext.setTextAppearance(
            if (isConnected)
                R.style.TextDarkShadow
            else
                R.style.TextLightShadow
        )
        // Set device text appearance
        textBoardBinding.textDeviceName.setTextAppearance(
            if (isConnected)
                R.style.TextDarkShadow
            else
                R.style.TextTealShadow
        )
    }
}