package com.example.modul5

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.modul5.api.ApiClient
import com.example.modul5.api.SessionManager
import com.example.modul5.request.RegisterRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)

        sessionManager = SessionManager(this)

        registerButton.setOnClickListener {
            val fullName = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
            } else {
                register(fullName, email, password)
            }
        }

        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun register(fullName: String, email: String, password: String) {
        val request = RegisterRequest(fullName, email, password)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.authService.register(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        val message = body.message ?: "Register berhasil"
                        val accessToken = body.accessToken ?: ""
                        val refreshToken = body.refreshToken ?: ""

                        sessionManager.saveAuthToken(accessToken)
                        sessionManager.saveRefreshToken(refreshToken)

                        Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMessage = getErrorMessage(response.errorBody())
                        Toast.makeText(
                            this@RegisterActivity,
                            "Register gagal: $errorMessage",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Terjadi kesalahan: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun getErrorMessage(errorBody: ResponseBody?): String {
        return try {
            val raw = errorBody?.string() ?: ""
            Log.e("RawErrorBody", raw)
            val jsonObject = JSONObject(raw)
            jsonObject.getString("message")
        } catch (e: Exception) {
            "Terjadi kesalahan saat memproses error"
        }
    }
}
