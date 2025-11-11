package com.example.sostwareaccountingandroid

import android.app.Application
import com.example.sostwareaccountingandroid.di.ServiceLocator

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Инициализируем наши сервисы
        ServiceLocator.initialize(this)
    }
}