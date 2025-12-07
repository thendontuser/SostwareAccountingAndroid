package com.example.sostwareaccountingandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sostwareaccountingandroid.R
import com.example.sostwareaccountingandroid.UserWithDepartment
import com.google.android.material.button.MaterialButton

class UsersAdapter(
    private val onEditClick: (UserWithDepartment) -> Unit,
    private val onDeleteClick: (UserWithDepartment) -> Unit,
    private val onDepartmentClick: (Long?) -> Unit
) : ListAdapter<UserWithDepartment, UsersAdapter.UserViewHolder>(USER_DIFF_CALLBACK) {

    companion object {
        private val USER_DIFF_CALLBACK = object : DiffUtil.ItemCallback<UserWithDepartment>() {
            override fun areItemsTheSame(oldItem: UserWithDepartment, newItem: UserWithDepartment): Boolean {
                return oldItem.user.id == newItem.user.id
            }

            override fun areContentsTheSame(oldItem: UserWithDepartment, newItem: UserWithDepartment): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFullName: TextView = itemView.findViewById(R.id.tvFullName)
        private val tvLogin: TextView = itemView.findViewById(R.id.tvLogin)
        private val tvRole: TextView = itemView.findViewById(R.id.tvRole)
        private val tvDepartment: TextView = itemView.findViewById(R.id.tvDepartment)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)

        fun bind(userWithDepartment: UserWithDepartment) {
            tvFullName.text = userWithDepartment.user.getFullName()
            tvLogin.text = "Логин: ${userWithDepartment.user.login}"
            tvRole.text = "Роль: ${userWithDepartment.user.role}"
            tvDepartment.text = "Отдел: ${userWithDepartment.departmentName}"

            // Клик по отделу для фильтрации
            tvDepartment.setOnClickListener {
                onDepartmentClick(userWithDepartment.user.departmentId)
            }

            btnEdit.setOnClickListener {
                onEditClick(userWithDepartment)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(userWithDepartment)
            }
        }
    }
}