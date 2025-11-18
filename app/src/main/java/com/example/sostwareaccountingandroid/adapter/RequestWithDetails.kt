package com.example.sostwareaccountingandroid.adapter

import com.example.sostwareaccountingandroid.entity.InstallationRequest

data class RequestWithDetails(
    val request: InstallationRequest,
    val softwareName: String,
    val deviceName: String
)