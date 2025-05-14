package com.example.modul5

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.modul5.api.ApiClient
import com.example.modul5.request.RefreshTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var selamatDatang: TextView
    private lateinit var akses: TextView
    private lateinit var refresh: TextView
    private lateinit var logoutButton: Button
    private val handler = Handler(Looper.getMainLooper())

    private val checkTokenInterval: Long = TimeUnit.SECONDS.toMillis(5)

    private fun isTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size == 3) {
                val payload = String(Base64.decode(parts[1], Base64.DEFAULT))
                val jsonObject = JSONObject(payload)
                val expTime = jsonObject.getLong("exp") * 1000
                System.currentTimeMillis() > expTime
            } else true
        } catch (e: Exception) {
            true
        }
    }

    private fun startTokenChecker() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                checkAndRefreshToken()
                handler.postDelayed(this, checkTokenInterval)
            }
        }, checkTokenInterval)
    }

    private fun checkAndRefreshToken() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("akses", "")
        val refreshToken = sharedPreferences.getString("refresh", "")

        if (accessToken != null && isTokenExpired(accessToken)) {
            if (refreshToken != null && !isTokenExpired(refreshToken)) {
                refreshAccessToken(refreshToken)
            } else {
                logoutAndRedirectToLogin()
            }
        }
    }

    private fun refreshAccessToken(refreshToken: String) {
        val request = RefreshTokenRequest(refreshToken)

        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiClient.authService.refreshToken(request)
            runOnUiThread {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val message = it.message
                        val newAccessToken = it.accessToken

                        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        with(sharedPreferences.edit()) {
                            putString("Pesan", message)
                            putString("akses", newAccessToken)
                            apply()
                        }
                    }
                } else {
                    logoutAndRedirectToLogin()
                    Toast.makeText(this@MainActivity, "Refresh token gagal", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun logoutAndRedirectToLogin() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("akses", null)
        val message = "Logout Berhasil"

        CoroutineScope(Dispatchers.IO).launch {
            var logoutMessage = "Logout gagal"

            if (accessToken != null) {
                try {
                    val response = ApiClient.authService.logout("Bearer $accessToken")
                    logoutMessage = if (response.isSuccessful) message else "Logout gagal di server"
                } catch (e: Exception) {
                    logoutMessage = "Terjadi kesalahan jaringan saat logout"
                }
            } else {
                logoutMessage = "Token tidak ditemukan, langsung logout"
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, logoutMessage, Toast.LENGTH_SHORT).show()
                sharedPreferences.edit().clear().apply()

                handler.removeCallbacksAndMessages(null)
                startActivity(Intent(this@MainActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val message = sharedPreferences.getString("Pesan", "")

        selamatDatang = findViewById(R.id.selamat)
        akses = findViewById(R.id.aksesToken)
        refresh = findViewById(R.id.refreshToken)
        logoutButton = findViewById(R.id.logout_button)

        selamatDatang.text = "Selamat datang, $message!"

        // Sembunyikan akses token dan refresh token dari layar
        akses.visibility = View.GONE
        refresh.visibility = View.GONE

        logoutButton.setOnClickListener {
            logoutAndRedirectToLogin()
        }

        startTokenChecker()
    }
}
