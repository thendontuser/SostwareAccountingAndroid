package com.example.sostwareaccountingandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sostwareaccountingandroid.R
import com.example.sostwareaccountingandroid.RequestFullDetails
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class RequestsManagementAdapter(
    private val onApproveClick: (RequestFullDetails) -> Unit,
    private val onRejectClick: (RequestFullDetails) -> Unit,
    private val onDetailsClick: (RequestFullDetails) -> Unit,
    private val onUserClick: (Long) -> Unit
) : ListAdapter<RequestFullDetails, RequestsManagementAdapter.RequestViewHolder>(REQUEST_DIFF_CALLBACK) {

    companion object {
        private val REQUEST_DIFF_CALLBACK = object : DiffUtil.ItemCallback<RequestFullDetails>() {
            override fun areItemsTheSame(oldItem: RequestFullDetails, newItem: RequestFullDetails): Boolean {
                return oldItem.request.id == newItem.request.id
            }

            override fun areContentsTheSame(oldItem: RequestFullDetails, newItem: RequestFullDetails): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request_management, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvSoftware: TextView = itemView.findViewById(R.id.tvSoftware)
        private val tvDevice: TextView = itemView.findViewById(R.id.tvDevice)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val btnApprove: MaterialButton = itemView.findViewById(R.id.btnApprove)
        private val btnReject: MaterialButton = itemView.findViewById(R.id.btnReject)
        private val btnDetails: MaterialButton = itemView.findViewById(R.id.btnDetails)

        fun bind(requestFullDetails: RequestFullDetails) {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val requestDate = dateFormat.format(Date(requestFullDetails.request.requestDate))

            tvUserName.text = requestFullDetails.userName
            tvSoftware.text = "${requestFullDetails.softwareName} ${requestFullDetails.softwareVersion}"
            tvDevice.text = requestFullDetails.deviceName
            tvStatus.text = "Статус: ${requestFullDetails.request.status}"
            tvDate.text = "Дата: $requestDate"

            // Показываем/скрываем кнопки в зависимости от статуса
            when (requestFullDetails.request.status) {
                "На рассмотрении" -> {
                    btnApprove.visibility = View.VISIBLE
                    btnReject.visibility = View.VISIBLE
                    btnApprove.isEnabled = true
                    btnReject.isEnabled = true
                }
                "Установлено" -> {
                    btnApprove.visibility = View.VISIBLE
                    btnReject.visibility = View.VISIBLE
                    btnApprove.isEnabled = false
                    btnApprove.text = "Одобрено"
                    btnReject.isEnabled = false
                }
                "Отклонено" -> {
                    btnApprove.visibility = View.VISIBLE
                    btnReject.visibility = View.VISIBLE
                    btnApprove.isEnabled = false
                    btnReject.isEnabled = false
                    btnReject.text = "Отклонено"
                }
            }

            // Клик по имени пользователя
            tvUserName.setOnClickListener {
                onUserClick(requestFullDetails.request.userId)
            }

            btnApprove.setOnClickListener {
                if (btnApprove.isEnabled) {
                    onApproveClick(requestFullDetails)
                }
            }

            btnReject.setOnClickListener {
                if (btnReject.isEnabled) {
                    onRejectClick(requestFullDetails)
                }
            }

            btnDetails.setOnClickListener {
                onDetailsClick(requestFullDetails)
            }
        }
    }
}