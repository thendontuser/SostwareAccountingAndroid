package com.example.sostwareaccountingandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sostwareaccountingandroid.R
import com.example.sostwareaccountingandroid.SoftwareWithDeveloper
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Locale

class SoftwareAdapter(
    private val onEditClick: (SoftwareWithDeveloper) -> Unit,
    private val onDeleteClick: (SoftwareWithDeveloper) -> Unit,
    private val onDeveloperClick: (Long?) -> Unit
) : ListAdapter<SoftwareWithDeveloper, SoftwareAdapter.SoftwareViewHolder>(SOFTWARE_DIFF_CALLBACK) {

    companion object {
        private val SOFTWARE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<SoftwareWithDeveloper>() {
            override fun areItemsTheSame(oldItem: SoftwareWithDeveloper, newItem: SoftwareWithDeveloper): Boolean {
                return oldItem.software.id == newItem.software.id
            }

            override fun areContentsTheSame(oldItem: SoftwareWithDeveloper, newItem: SoftwareWithDeveloper): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoftwareViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_software, parent, false)
        return SoftwareViewHolder(view)
    }

    override fun onBindViewHolder(holder: SoftwareViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SoftwareViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvVersion: TextView = itemView.findViewById(R.id.tvVersion)
        private val tvLicense: TextView = itemView.findViewById(R.id.tvLicense)
        private val tvDeveloper: TextView = itemView.findViewById(R.id.tvDeveloper)
        private val tvLicenseDates: TextView = itemView.findViewById(R.id.tvLicenseDates)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)

        fun bind(softwareWithDeveloper: SoftwareWithDeveloper) {
            val software = softwareWithDeveloper.software

            tvName.text = software.name
            tvVersion.text = "Версия: ${software.version}"
            tvLicense.text = "Лицензия: ${software.licenseType}"
            tvDeveloper.text = "Производитель: ${softwareWithDeveloper.developerName}"

            // Клик по производителю для фильтрации
            tvDeveloper.setOnClickListener {
                onDeveloperClick(software.developerId)
            }

            // Отображение дат лицензии
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val datesText = buildString {
                if (software.licenseStartDate != null) {
                    append("Начало: ${dateFormat.format(software.licenseStartDate!!)}")
                }
                if (software.licenseEndDate != null) {
                    if (isNotEmpty()) append(" | ")
                    append("Окончание: ${dateFormat.format(software.licenseEndDate!!)}")
                }
                if (isEmpty()) {
                    append("Срок лицензии не указан")
                }
            }
            tvLicenseDates.text = datesText

            btnEdit.setOnClickListener {
                onEditClick(softwareWithDeveloper)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(softwareWithDeveloper)
            }
        }
    }
}