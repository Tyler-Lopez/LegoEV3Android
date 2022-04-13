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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
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
    private var joystickDriveThread: MainViewModel.JoystickDriveThread? = null
    private var joystickSteerThread: MainViewModel.JoystickSteerThread? = null

    private lateinit var bluetoothService: MyBluetoothService

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

                    // Failure to connect
                    adjustConnectionStatus(ConnectionStatus.ERROR)
                    // Device is either not reachable or is not an EV3
                    // TODO
                    textBoardBinding.textSubtext.text = getString(R.string.controller_status_error_connecting_to_device)
                }

                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    // TO-DO
                    // ADD SOMETHING FOR THE IS BONDING STATE TO NOTE YOU NEED TO ACCEPT ON DEVICE!
                    println("HERE BOND STATE CHANGED")
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device?.bondState == BluetoothDevice.BOND_BONDED) {
                        // Begin bluetooth service
                        startBluetoothServiceConnection(device)
                    }
                    // TODO remove logic from this removed textview into a information menu
                    // device?.let { binding?.centeredText?.text = device.bondState.toString() }
                }
                else -> println("HERE HERE $intent ${intent.action}")
            }
        }
    }

    // Invoked when we have bonded to a device which we have also confirmed is a LEGO EV3
    private fun startBluetoothServiceConnection(device: BluetoothDevice) {
        bluetoothService.connect(device)
        // ASYNC call after attempting connection
        viewModel.viewModelScope.launch {
            // If, in 10 seconds a connection has not been made - stop attempting connection
            delay(10000)
            if (viewModel.connectionStatus == ConnectionStatus.CONNECTING)
                disconnectSafely()
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

        // Set TextBoard Binding and change connection status for first time
        textBoardBinding = binding!!.textLargeBoardLayout
        rotateAnimation.duration = 3600
        rotateAnimation.repeatCount = Animation.INFINITE
        textBoardBinding.ivTopRightLoadingIcon.animation = rotateAnimation
        rotateAnimation.start()
        // Produce . . . animation next to Connected
        loopLoadingDots()
        // Set connection status to connecting
        adjustConnectionStatus(ConnectionStatus.CONNECTING)


        // Establish our bluetooth service and what we should do upon successful connection
        bluetoothService = MyBluetoothService(requireContext()) {

            // Inform view model we have bonded and are now starting service
            adjustConnectionStatus(ConnectionStatus.CONNECTED)
            textBoardBinding.textSubtext.text = getString(R.string.controller_status_connected)

            requireActivity().runOnUiThread {
                binding?.constrainLayoutSuccessConnection?.visibility = View.VISIBLE
            }
            // This should mean the go ahead on we are connected
            // clean this code up later
            joystickSteerThread =
                viewModel.JoystickSteerThread(bluetoothService, binding!!.joystickViewRight)
            joystickDriveThread =
                viewModel.JoystickDriveThread(bluetoothService, binding!!.joystickView)
            joystickDriveThread!!.start()
            joystickSteerThread!!.start()
        }

        // Establish what should happen if the CONNECT / DISCONNECT button is pushed
        textBoardBinding.buttonConnectButton.setOnClickListener {
            when (viewModel.connectionStatus) {
                // If we are not connected and should begin a connection
                ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR -> {
                    adjustConnectionStatus(ConnectionStatus.CONNECTING)
                    textBoardBinding.textSubtext.text = getString(R.string.controller_status_confirm_EV3)
                    // Attempt to make a connection
                    // Probably add something here in the future to not fetch uuids if we've already checked this robot to be EV3
                    SelectedDevice.BluetoothDevice?.fetchUuidsWithSdp()
                }
                // If we are currently connected and should stop connect
                else -> disconnectSafely()
            }

        }
        // Attempt to make a connection
        SelectedDevice.BluetoothDevice?.fetchUuidsWithSdp()


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
            bluetoothService.playSound(Note.A)
        }
        pianoBinding.noteASharp.setOnClickListener {
            bluetoothService.playSound(Note.ASharp)
        }
        pianoBinding.noteB.setOnClickListener {
            bluetoothService.playSound(Note.B)
        }
        pianoBinding.noteC.setOnClickListener {
            bluetoothService.playSound(Note.C)
        }
        pianoBinding.noteCSharp.setOnClickListener {
            bluetoothService.playSound(Note.CSharp)
        }
        pianoBinding.noteD.setOnClickListener {
            bluetoothService.playSound(Note.D)
        }
        pianoBinding.noteDSharp.setOnClickListener {
            bluetoothService.playSound(Note.DSharp)
        }
        pianoBinding.noteE.setOnClickListener {
            bluetoothService.playSound(Note.E)
        }
        pianoBinding.noteF.setOnClickListener {
            bluetoothService.playSound(Note.F)
        }
        pianoBinding.noteFSharp.setOnClickListener {
            bluetoothService.playSound(Note.FSharp)
        }
        pianoBinding.noteG.setOnClickListener {
            bluetoothService.playSound(Note.G)
        }
        pianoBinding.noteGSharp.setOnClickListener {
            bluetoothService.playSound(Note.GSharp)
        }
    }

    // This is necessary to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(receiver) // Unregister Intent receiver
        bluetoothService.destroy()
        binding = null
    }

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

    private fun adjustConnectionStatus(connectionStatus: ConnectionStatus) {
        // Inform view model of connection status change
        viewModel.connectionStatus = connectionStatus
        // Change HEADER text
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

    private fun disconnectSafely() {
        requireActivity().runOnUiThread {
            adjustConnectionStatus(ConnectionStatus.DISCONNECTED)
            textBoardBinding.textSubtext.text = getString(R.string.controller_status_disconnected)
        }
        // Disconnect cancels all active threads
        bluetoothService.disconnect()
        // FIX THIS IN FUTURE MAKE IT INTERRUPT BASED
        joystickDriveThread?.stopThreadSafely()
        joystickSteerThread?.stopThreadSafely()
        // https://stackoverflow.com/questions/8505707/android-best-and-safe-way-to-stop-thread
        joystickDriveThread = null
        joystickSteerThread = null
    }
}