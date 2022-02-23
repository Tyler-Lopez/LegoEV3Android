package com.example.legoev3android.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.legoev3android.R
import com.example.legoev3android.ui.viewmodels.MainViewModel

class SetupFragment : Fragment(R.layout.fragment_setup) {
    private val viewModel: MainViewModel by viewModels()
}