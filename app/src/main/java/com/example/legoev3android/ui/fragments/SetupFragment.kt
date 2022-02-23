package com.example.legoev3android.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.legoev3android.R
import com.example.legoev3android.databinding.FragmentSetupBinding
import com.example.legoev3android.ui.viewmodels.MainViewModel
import com.example.legoev3android.utils.BluetoothUtility

class SetupFragment : Fragment(R.layout.fragment_setup) {

    private val viewModel: MainViewModel by viewModels()
    private var binding: FragmentSetupBinding? = null

    private val requestPermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts
                .RequestMultiplePermissions()
        ) { isGranted ->
            if (isGranted.containsValue(false)) {
                // AT LEAST ONE PERMISSION WAS DENIED
                binding?.permissionMsg?.text = "You denied at least one permission."
            }
            else {
                binding?.permissionMsg?.text = "You granted both permissions"
            }
        }


    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts
                .RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                // AT LEAST ONE PERMISSION WAS DENIED
                binding?.permissionMsg?.text = "You denied at least one permission."
            }
            else {
                binding?.permissionMsg?.text = "You granted both permissions"
            }
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSetupBinding.bind(view)
        binding?.permissionMsg?.text = "Click to grant permission"
        binding?.permissionMsg?.setOnClickListener {
            when {
                BluetoothUtility.hasBluetoothPermissions(requireContext()) -> {
                    binding?.permissionMsg?.text = "Permission already granted."
                }
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        requestPermissionsLauncher.launch(
                            arrayOf(
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT
                            )
                        )
                    } else {
                        requestPermissionLauncher.launch(
                                Manifest.permission.BLUETOOTH
                        )
                    }
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