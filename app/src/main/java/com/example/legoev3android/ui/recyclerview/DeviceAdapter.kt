package com.example.legoev3android.ui.recyclerview

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.legoev3android.R
import com.example.legoev3android.databinding.ItemDeviceBinding

class DeviceAdapter(
    private var devices: List<BluetoothDevice>
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    inner class DeviceViewHolder(
        val binding: ItemDeviceBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DeviceViewHolder {
        val layoutInflater = LayoutInflater
            .from(parent.context)
        val binding = ItemDeviceBinding.inflate(layoutInflater, parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.binding.apply {
            tvMacAddress.text = devices[position].address
        }
    }

    override fun getItemCount(): Int = devices.size
}