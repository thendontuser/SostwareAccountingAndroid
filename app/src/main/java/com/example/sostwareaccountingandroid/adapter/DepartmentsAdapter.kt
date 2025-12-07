package com.example.sostwareaccountingandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sostwareaccountingandroid.DepartmentWithStats
import com.example.sostwareaccountingandroid.R
import com.google.android.material.button.MaterialButton

class DepartmentsAdapter(
    private val onEditClick: (DepartmentWithStats) -> Unit,
    private val onDeleteClick: (DepartmentWithStats) -> Unit
) : ListAdapter<DepartmentWithStats, DepartmentsAdapter.DepartmentViewHolder>(DEPARTMENT_DIFF_CALLBACK) {

    companion object {
        private val DEPARTMENT_DIFF_CALLBACK = object : DiffUtil.ItemCallback<DepartmentWithStats>() {
            override fun areItemsTheSame(oldItem: DepartmentWithStats, newItem: DepartmentWithStats): Boolean {
                return oldItem.department.id == newItem.department.id
            }

            override fun areContentsTheSame(oldItem: DepartmentWithStats, newItem: DepartmentWithStats): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_department, parent, false)
        return DepartmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: DepartmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DepartmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvUsers: TextView = itemView.findViewById(R.id.tvUsers)
        private val tvDevices: TextView = itemView.findViewById(R.id.tvDevices)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)

        fun bind(departmentWithStats: DepartmentWithStats) {
            tvName.text = departmentWithStats.department.name
            tvUsers.text = "Сотрудников: ${departmentWithStats.userCount}"
            tvDevices.text = "Устройств: ${departmentWithStats.deviceCount}"

            btnEdit.setOnClickListener {
                onEditClick(departmentWithStats)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(departmentWithStats)
            }
        }
    }
}