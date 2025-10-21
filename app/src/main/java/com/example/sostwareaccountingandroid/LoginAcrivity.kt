package com.example.sostwareaccountingandroid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.sostwareaccountingandroid.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        setupClickListeners()
        setupObservers()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            attemptLogin()
        }

        binding.tvRegister.setOnClickListener {
            navigateToRegistration()
        }
    }

    private fun attemptLogin() {
        val login = binding.etLogin.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (validateInput(login, password)) {
            binding.progressBar.visibility = View.VISIBLE
            userViewModel.loginUser(login, password)
        }
    }

    private fun validateInput(login: String, password: String): Boolean {
        var isValid = true

        if (login.isEmpty()) {
            binding.etLogin.error = "Введите логин"
            isValid = false
        } else {
            binding.etLogin.error = null
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Введите пароль"
            isValid = false
        } else {
            binding.etPassword.error = null
        }

        return isValid
    }

    private fun setupObservers() {
        userViewModel.loginResult.observe(this) { result ->
            binding.progressBar.visibility = View.GONE

            when (result) {
                is LoginResult.Success -> {
                    navigateToMainScreen(result.user)
                }
                is LoginResult.Error -> {
                    showError(result.message)
                }
            }
        }
    }

    private fun navigateToMainScreen(user: User) {
        val intent = if (user.role == "Администратор") {
            Intent(this, AdminMainActivity::class.java)
        } else {
            Intent(this, UserMainActivity::class.java)
        }
        intent.putExtra("USER", user)
        startActivity(intent)
        finish()
    }

    private fun navigateToRegistration() {
        startActivity(Intent(this, RegistrationActivity::class.java))
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}