package com.example.sostwareaccountingandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sostwareaccountingandroid.DeveloperWithStats
import com.example.sostwareaccountingandroid.R
import com.google.android.material.button.MaterialButton

class DevelopersAdapter(
    private val onEditClick: (DeveloperWithStats) -> Unit,
    private val onDeleteClick: (DeveloperWithStats) -> Unit
) : ListAdapter<DeveloperWithStats, DevelopersAdapter.DeveloperViewHolder>(DEVELOPER_DIFF_CALLBACK) {

    companion object {
        private val DEVELOPER_DIFF_CALLBACK = object : DiffUtil.ItemCallback<DeveloperWithStats>() {
            override fun areItemsTheSame(oldItem: DeveloperWithStats, newItem: DeveloperWithStats): Boolean {
                return oldItem.developer.id == newItem.developer.id
            }

            override fun areContentsTheSame(oldItem: DeveloperWithStats, newItem: DeveloperWithStats): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeveloperViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_developer, parent, false)
        return DeveloperViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeveloperViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DeveloperViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvCompanyType: TextView = itemView.findViewById(R.id.tvCompanyType)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvSoftwareCount: TextView = itemView.findViewById(R.id.tvSoftwareCount)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)

        fun bind(developerWithStats: DeveloperWithStats) {
            val developer = developerWithStats.developer

            tvName.text = developer.name
            tvCompanyType.text = "Тип: ${developer.companyType}"
            tvLocation.text = "Местоположение: ${developer.location ?: "Не указано"}"
            tvSoftwareCount.text = "Программ: ${developerWithStats.softwareCount}"

            btnEdit.setOnClickListener {
                onEditClick(developerWithStats)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(developerWithStats)
            }
        }
    }
}