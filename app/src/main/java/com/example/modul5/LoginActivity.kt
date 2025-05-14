package com.example.modul5

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.modul5.api.ApiClient
import com.example.modul5.request.LoginRequest
import com.example.modul5.api.SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inisialisasi view
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        sessionManager = SessionManager(this)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Proses login
            login(email, password)
        }
    }

    private fun login(email: String, password: String) {
        val request = LoginRequest(email, password)

        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiClient.authService.login(request)

            runOnUiThread {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val message = it.message
                        val accessToken = it.accessToken
                        val refreshToken = it.refreshToken

                        // Menyimpan message di SharedPreferences
                        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("Pesan", message)
                        editor.putString("akses", accessToken)
                        editor.putString("refresh", refreshToken)
                        editor.apply()

                        // Menyimpan token ke sessionManager
                        sessionManager.saveAuthToken(it.accessToken ?: "")
                        sessionManager.saveRefreshToken(it.refreshToken ?: "")

                        // Menampilkan Toast untuk konfirmasi login sukses
                        Toast.makeText(this@LoginActivity, "Login sukses", Toast.LENGTH_SHORT).show()

                        // Melanjutkan ke MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Menutup LoginActivity agar tidak bisa kembali
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Login gagal", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
