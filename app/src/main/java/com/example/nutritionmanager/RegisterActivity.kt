package com.example.nutritionmanager

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.example.nutritionmanager.db.UserDatabaseHelper
import com.example.nutritionmanager.model.User

class RegisterActivity : AppCompatActivity() {

    private lateinit var userDbHelper: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        userDbHelper = UserDatabaseHelper(this)

        val etFullName = findViewById<TextInputEditText>(R.id.etFullName)
        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val btnGoToLogin = findViewById<MaterialButton>(R.id.btnGoToLogin)

        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (username.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên đăng nhập", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userDbHelper.isUsernameExists(username)) {
                Toast.makeText(this, "Tên đăng nhập đã tồn tại", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userDbHelper.isEmailExists(email)) {
                Toast.makeText(this, "Email đã được đăng ký", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = User(
                username = username,
                email = email,
                password = password,
                fullName = fullName
            )

            val success = userDbHelper.registerUser(user)
            if (success) {
                Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Đăng ký thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show()
            }
        }

        btnGoToLogin.setOnClickListener {
            finish()
        }
    }
}