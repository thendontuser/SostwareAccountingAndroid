package com.example.sostwareaccountingandroid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sostwareaccountingandroid.databinding.ActivityLoginBinding
import com.example.sostwareaccountingandroid.di.ServiceLocator
import com.example.sostwareaccountingandroid.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels { ServiceLocator.getAuthViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Новый способ обработки нажатия назад
        setupBackPressedHandler()

        setupObservers()
        setupClickListeners()
    }

    private fun setupBackPressedHandler() {
        // Создаем callback для обработки нажатия назад
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Закрываем приложение при нажатии назад на экране логина
                finishAffinity()
            }
        }

        // Регистрируем callback
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun setupObservers() {
        // Наблюдаем за состоянием логина с помощью StateFlow
        lifecycleScope.launch {
            authViewModel.loginState.collect { state ->
                when (state) {
                    is AuthViewModel.LoginState.Loading -> {
                        showLoading(true)
                    }
                    is AuthViewModel.LoginState.Success -> {
                        showLoading(false)
                        navigateToMainScreen(state.user)
                    }
                    is AuthViewModel.LoginState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                    is AuthViewModel.LoginState.Idle -> {
                        showLoading(false)
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val login = binding.etLogin.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            authViewModel.login(login, password)
        }

        binding.tvRegister.setOnClickListener {
            navigateToRegistration()
        }
    }

    private fun navigateToMainScreen(user: com.example.sostwareaccountingandroid.entity.User) {
        val intent = if (user.role == "Администратор") {
            Intent(this, AdminMainActivity::class.java)
        } else {
            Intent(this, UserMainActivity::class.java)
        }
        intent.putExtra("USER_ID", user.id)
        startActivity(intent)
        finish()
    }

    private fun navigateToRegistration() {
        startActivity(Intent(this, RegistrationActivity::class.java))
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnLogin.isEnabled = !show
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}