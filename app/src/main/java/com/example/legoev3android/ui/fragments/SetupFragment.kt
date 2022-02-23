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
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted ->
            if (isGranted.containsValue(false)) {
                // Denied at least one permission
                // Show button
            } else {
                // Permission granted
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSetupBinding.bind(view)

        // TO-DO navigate to bluetooth
        if (BluetoothUtility.hasBluetoothPermissions(requireContext())) {
            binding?.permissionMsg?.text = "Permission already granted."
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
}