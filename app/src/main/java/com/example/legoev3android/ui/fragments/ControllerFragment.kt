package com.example.legoev3android.ui.fragments

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.legoev3android.R
import com.example.legoev3android.databinding.FragmentControllerBinding
import com.example.legoev3android.databinding.FragmentSetupBinding
import com.example.legoev3android.services.MyBluetoothService
import com.example.legoev3android.ui.viewmodels.MainViewModel
import com.example.legoev3android.utils.PermissionUtility
import com.example.legoev3android.utils.SelectedDevice

class ControllerFragment : Fragment(R.layout.fragment_controller) {

    private val viewModel: MainViewModel by viewModels()
    private var binding: FragmentControllerBinding? = null
  //

    /*

    FRAGMENT OVERRIDES

     */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentControllerBinding.bind(view)
         val myBlueToothService = MyBluetoothService(requireContext())
        SelectedDevice.BluetoothDevice?.createBond()
        SelectedDevice.BluetoothDevice?.let {
            myBlueToothService.connect(it)
        }
    }

    // This is necessary to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}