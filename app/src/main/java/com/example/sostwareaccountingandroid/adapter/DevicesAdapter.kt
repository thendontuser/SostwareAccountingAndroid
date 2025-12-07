package com.example.sostwareaccountingandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sostwareaccountingandroid.DeviceWithDepartment
import com.example.sostwareaccountingandroid.R
import com.google.android.material.button.MaterialButton

class DevicesAdapter(
    private val onEditClick: (DeviceWithDepartment) -> Unit,
    private val onDeleteClick: (DeviceWithDepartment) -> Unit,
    private val onDepartmentClick: (Long?) -> Unit
) : ListAdapter<DeviceWithDepartment, DevicesAdapter.DeviceViewHolder>(DEVICE_DIFF_CALLBACK) {

    companion object {
        private val DEVICE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<DeviceWithDepartment>() {
            override fun areItemsTheSame(oldItem: DeviceWithDepartment, newItem: DeviceWithDepartment): Boolean {
                return oldItem.device.id == newItem.device.id
            }

            override fun areContentsTheSame(oldItem: DeviceWithDepartment, newItem: DeviceWithDepartment): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvOS: TextView = itemView.findViewById(R.id.tvOS)
        private val tvIP: TextView = itemView.findViewById(R.id.tvIP)
        private val tvRAM: TextView = itemView.findViewById(R.id.tvRAM)
        private val tvDepartment: TextView = itemView.findViewById(R.id.tvDepartment)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)

        fun bind(deviceWithDepartment: DeviceWithDepartment) {
            val device = deviceWithDepartment.device

            tvName.text = device.name
            tvOS.text = "ОС: ${device.osName}"
            tvIP.text = "IP: ${device.ipAddress ?: "Не указан"}"
            tvRAM.text = "RAM: ${device.ramSize} ГБ"
            tvDepartment.text = "Отдел: ${deviceWithDepartment.departmentName}"

            // Клик по отделу для фильтрации
            tvDepartment.setOnClickListener {
                onDepartmentClick(device.departmentId)
            }

            btnEdit.setOnClickListener {
                onEditClick(deviceWithDepartment)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(deviceWithDepartment)
            }
        }
    }
}