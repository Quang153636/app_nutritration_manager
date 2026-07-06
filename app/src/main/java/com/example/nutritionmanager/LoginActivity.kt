package com.example.nutritionmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.example.nutritionmanager.db.UserDatabaseHelper
import com.example.nutritionmanager.utils.SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var userDbHelper: UserDatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userDbHelper = UserDatabaseHelper(this)
        sessionManager = SessionManager(this)

        // Kiểm tra đã đăng nhập chưa
        if (sessionManager.isLoggedIn()) {
            goToMainActivity()
            return
        }

        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val btnGoToRegister = findViewById<MaterialButton>(R.id.btnGoToRegister)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = userDbHelper.loginUser(username, password)
            if (user != null) {
                // Lưu thông tin đăng nhập
                sessionManager.saveUser(user.id, user.username, user.fullName)
                Toast.makeText(this, "Chào mừng ${user.fullName.ifEmpty { user.username }}!", Toast.LENGTH_SHORT).show()
                goToMainActivity()
            } else {
                Toast.makeText(this, "Sai tên đăng nhập hoặc mật khẩu", Toast.LENGTH_SHORT).show()
            }
        }

        btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}