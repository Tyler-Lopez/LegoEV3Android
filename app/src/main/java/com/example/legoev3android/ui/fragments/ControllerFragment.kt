package com.example.legoev3android.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.legoev3android.R
import com.example.legoev3android.ui.viewmodels.MainViewModel

class ControllerFragment : Fragment(R.layout.fragment_controller) {
    private val viewModel: MainViewModel by viewModels()
}