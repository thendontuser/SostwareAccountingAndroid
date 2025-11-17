package com.example.sostwareaccountingandroid

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.sostwareaccountingandroid.databinding.ItemRequestBinding
import com.example.sostwareaccountingandroid.entity.InstallationRequest
import java.util.Date
import java.util.Locale

class InstallationRequestAdapter(
    private val onItemClick: (InstallationRequest) -> Unit
) : androidx.recyclerview.widget.ListAdapter<InstallationRequest, InstallationRequestAdapter.RequestViewHolder>(
    DiffCallback
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = getItem(position)
        holder.bind(request)
    }

    inner class RequestViewHolder(
        private val binding: ItemRequestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onItemClick(getItem(adapterPosition))
            }
        }

        fun bind(request: InstallationRequest) {
            binding.tvRequestId.text = "Заявка #${request.id}"
            binding.tvDate.text = formatDate(request.requestDate)
            binding.tvStatus.text = request.status

            // Установка цвета статуса
            val statusColor = when (request.status) {
                "Установлено" -> R.color.success
                "На рассмотрении" -> R.color.warning
                "Отклонено" -> R.color.error
                else -> R.color.gray
            }

            binding.tvStatus.setTextColor(
                ContextCompat.getColor(binding.root.context, statusColor)
            )
        }

        private fun formatDate(timestamp: Long): String {
            return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                .format(Date(timestamp))
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<InstallationRequest>() {
        override fun areItemsTheSame(
            oldItem: InstallationRequest,
            newItem: InstallationRequest
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: InstallationRequest,
            newItem: InstallationRequest
        ): Boolean {
            return oldItem == newItem
        }
    }
}