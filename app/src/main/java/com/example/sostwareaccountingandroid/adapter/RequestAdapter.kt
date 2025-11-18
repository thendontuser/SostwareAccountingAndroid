package com.example.sostwareaccountingandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sostwareaccountingandroid.R
import java.text.SimpleDateFormat
import java.util.*

class RequestAdapter : ListAdapter<RequestWithDetails, RequestAdapter.RequestViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = getItem(position)
        holder.bind(request)
    }

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRequestId: TextView = itemView.findViewById(R.id.tvRequestId)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvSoftware: TextView = itemView.findViewById(R.id.tvSoftware)

        fun bind(request: RequestWithDetails) {
            tvRequestId.text = "Заявка #${request.request.id}"
            tvStatus.text = request.request.status
            tvDate.text = formatDate(request.request.requestDate)
            tvSoftware.text = "Программа: ${request.softwareName}"

            // Цвет статуса
            val statusColor = when (request.request.status) {
                "Установлено" -> R.color.success
                "На рассмотрении" -> R.color.warning
                "Отклонено" -> R.color.error
                else -> R.color.gray
            }
            tvStatus.setTextColor(ContextCompat.getColor(itemView.context, statusColor))
        }

        private fun formatDate(timestamp: Long): String {
            return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                .format(Date(timestamp))
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<RequestWithDetails>() {
        override fun areItemsTheSame(oldItem: RequestWithDetails, newItem: RequestWithDetails): Boolean {
            return oldItem.request.id == newItem.request.id
        }

        override fun areContentsTheSame(oldItem: RequestWithDetails, newItem: RequestWithDetails): Boolean {
            return oldItem == newItem
        }
    }
}